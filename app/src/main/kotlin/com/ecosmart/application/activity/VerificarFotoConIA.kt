package com.ecosmart.application.activity

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.di.EcoGptApiKey
import com.ecosmart.infrastructure.network.EcoGptClient
import com.ecosmart.infrastructure.network.EcoGptResponseMapper
import com.ecosmart.infrastructure.security.CalculadorHuellaPerceptual
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import javax.inject.Inject

private const val ESPERA_ANTES_DE_REINTENTAR_MS = 3_000L

data class DatosVerificacionFoto(
    val usuarioId: UsuarioId,
    val actividadId: ActividadId,
    val categoria: CategoriaActividad,
    val resultadoEsperado: String,
    val descripcionUsuario: String,
    val archivoFoto: File,
)

sealed class ResultadoVerificarFotoConIA {
    data class Completado(val registro: RegistroVerificacion) : ResultadoVerificarFotoConIA()
    data object TopeDiarioAlcanzado : ResultadoVerificarFotoConIA()
    data object DuplicadaLocalmente : ResultadoVerificarFotoConIA()
    data object Timeout : ResultadoVerificarFotoConIA()
    data object SinConexion : ResultadoVerificarFotoConIA()
    data class ErrorEcoGpt(val mensaje: String) : ResultadoVerificarFotoConIA()
}

/**
 * US9 — verificación de Reciclar/Reutilizar vía EcoGPT (RF-025 a RF-030,
 * RF-050, RF-051, RF-058, RF-059, RNF-008).
 *
 * El timeout de 30s NO se controla acá con un `withTimeout` propio: el
 * `OkHttpClient` de EcoGPT (`NetworkModule`, T018) ya fija `callTimeout` en
 * exactamente 30s. Ese timeout se manifiesta como [SocketTimeoutException]
 * (una subclase de [IOException]), por eso se atrapa primero y por
 * separado de un [IOException] genérico ("sin conexión") — research.md §2.
 *
 * Corrección post-QA (2026-09-23): el backend real de EcoGPT corre en un
 * hosting free-tier que "duerme" tras inactividad (research.md §2.1); su
 * arranque en frío podía manifestarse como un falso Timeout/SinConexion en
 * el primer intento aunque el dispositivo tuviera conexión real. Se agrega
 * un único reintento automático transparente (sin que el usuario vea
 * ningún error intermedio) antes de reportar una falla real, más
 * [precalentarBackend] para que la pantalla de verificación empiece a
 * despertar el backend apenas se abre, no recién al enviar.
 */
class VerificarFotoConIA @Inject constructor(
    private val ecoGptClient: EcoGptClient,
    private val aplicarTopeDiario: AplicarTopeDiario,
    private val calculadorHuellaPerceptual: CalculadorHuellaPerceptual,
    private val registroVerificacionRepository: RegistroVerificacionRepository,
    @EcoGptApiKey private val apiKey: String,
) {
    suspend operator fun invoke(datos: DatosVerificacionFoto): ResultadoVerificarFotoConIA {
        val disponibilidad = aplicarTopeDiario.verificarDisponibilidad(datos.usuarioId, datos.categoria)
        if (disponibilidad.bloqueado) return ResultadoVerificarFotoConIA.TopeDiarioAlcanzado

        val huellaNueva = calculadorHuellaPerceptual.calcularDesdeArchivo(datos.archivoFoto)
            ?: return ResultadoVerificarFotoConIA.ErrorEcoGpt("No pudimos leer la imagen. Probá con otra foto.")

        val huellasPrevias = registroVerificacionRepository.huellasAprobadas(datos.usuarioId, datos.categoria)
        // RF-058/RF-059, pre-filtro local (research.md §3): evita gastar el presupuesto de 30s
        // de RNF-008 y la llamada a EcoGPT si ya sabemos que es un duplicado exacto/casi-exacto.
        val candidato = registroCandidato(datos, huellaNueva)
        val esDuplicadaLocalmente = huellasPrevias.any { huellaPrevia ->
            candidato.esDuplicadoDe(candidato.copy(huellaImagen = huellaPrevia))
        }
        if (esDuplicadaLocalmente) return ResultadoVerificarFotoConIA.DuplicadaLocalmente

        val respuesta = try {
            intentarConUnReintentoAutomatico {
                val imagenParte = MultipartBody.Part.createFormData(
                    "imagen",
                    datos.archivoFoto.name,
                    datos.archivoFoto.asRequestBody("image/*".toMediaType()),
                )
                ecoGptClient.verificarFotoActividad(
                    apiKey = apiKey,
                    imagen = imagenParte,
                    categoria = datos.categoria.name.toRequestBody("text/plain".toMediaType()),
                    resultadoEsperado = datos.resultadoEsperado.toRequestBody("text/plain".toMediaType()),
                    descripcionUsuario = datos.descripcionUsuario.toRequestBody("text/plain".toMediaType()),
                    huellasImagenesAprobadasPrevias = huellasPrevias.joinToString(",")
                        .toRequestBody("text/plain".toMediaType()),
                )
            }
        } catch (timeout: SocketTimeoutException) {
            return ResultadoVerificarFotoConIA.Timeout
        } catch (sinConexion: IOException) {
            return ResultadoVerificarFotoConIA.SinConexion
        } catch (errorHttp: HttpException) {
            // research.md §2: 5xx/formato inválido reciben el mismo tratamiento funcional que un
            // timeout en la UI (reintentar sin consumir tope), pero se modelan aparte para precisión.
            return ResultadoVerificarFotoConIA.ErrorEcoGpt(errorHttp.message() ?: "Error de EcoGPT")
        }

        val resultado = EcoGptResponseMapper.desde(respuesta)
        val registro = aplicarTopeDiario.registrarResultado(
            usuarioId = datos.usuarioId,
            actividadId = datos.actividadId,
            categoria = datos.categoria,
            resultado = resultado,
            motivoIA = respuesta.motivo,
            huellaImagen = huellaNueva,
            pasosRegistrados = null,
            cantidadParaPuntaje = 1,
        )
        return ResultadoVerificarFotoConIA.Completado(registro)
    }

    /**
     * Corrección post-QA (2026-09-23): "despertar" best-effort del backend
     * ANTES de que el usuario termine de sacar la foto/escribir la
     * descripción — la Screen la llama al entrar (research.md §2.1). Nunca
     * propaga errores: es solo una optimización de latencia, la llamada
     * real de todos modos hace su propio reintento si hace falta.
     */
    suspend fun precalentarBackend() {
        try {
            ecoGptClient.ping()
        } catch (_: Exception) {
            // best-effort, sin acción.
        }
    }

    /**
     * Reintenta UNA vez, tras una breve espera, ante cualquier
     * [IOException] (incluye [SocketTimeoutException], su subclase) — el
     * arranque en frío del backend free-tier (research.md §2.1) suele
     * resolverse solo entre el primer intento y el segundo. Si el segundo
     * intento también falla, la excepción se propaga tal cual para que
     * `invoke` la distinga como Timeout/SinConexion normalmente.
     */
    private suspend fun <T> intentarConUnReintentoAutomatico(llamada: suspend () -> T): T =
        try {
            llamada()
        } catch (primerError: IOException) {
            delay(ESPERA_ANTES_DE_REINTENTAR_MS)
            llamada()
        }

    private fun registroCandidato(datos: DatosVerificacionFoto, huella: String) = RegistroVerificacion(
        id = RegistroVerificacionId.nuevo(),
        usuarioId = datos.usuarioId,
        actividadId = datos.actividadId,
        categoria = datos.categoria,
        fecha = LocalDate.now(),
        resultado = ResultadoVerificacion.APROBADO,
        huellaImagen = huella,
    )
}
