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
