package com.ecosmart.infrastructure.network

import com.squareup.moshi.JsonClass
import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import java.io.IOException

private const val ESPERA_ANTES_DE_REINTENTAR_MS = 3_000L

/**
 * Cliente Retrofit de los endpoints nuevos del backend de confianza (ver
 * `specs/002-firestore-datos-usuario/contracts/openapi.yaml`). Autenticado con
 * `FirebaseIdTokenInterceptor` (T012), no con `X-EcoGPT-Api-Key` como [EcoGptClient] —
 * el backend necesita saber *qué usuario* llama, no solo "es la app" (research.md §3).
 */
interface BackendConfianzaClient {

    @POST("registros-verificacion/reciclar-reutilizar")
    suspend fun otorgarPuntosReciclarReutilizar(
        @Body datos: SolicitudRegistroReciclarReutilizarDto,
    ): RegistroVerificacionOtorgadoDto

    @POST("registros-verificacion/caminar")
    suspend fun otorgarPuntosCaminar(
        @Body datos: SolicitudCaminarDto,
    ): RegistroVerificacionOtorgadoDto

    /**
     * Mismo endpoint que [otorgarPuntosCaminar], para `accion = "INICIAR" | "ACTUALIZAR_PROGRESO"`:
     * esas dos acciones no otorgan puntos todavía, así que la respuesta no tiene el shape de
     * [RegistroVerificacionOtorgadoDto] (ver T036, `backend/app/puntos.py`).
     */
    @POST("registros-verificacion/caminar")
    suspend fun iniciarOActualizarCaminata(
        @Body datos: SolicitudCaminarDto,
    ): EstadoCaminataEnCursoDto

    @PATCH("pasos-del-dia")
    suspend fun actualizarPasosDelDia(
        @Body datos: SolicitudPasosDelDiaDto,
    )

    @POST("migracion/subir-datos-locales")
    suspend fun migrarDatosLocales(
        @Body datos: SolicitudMigracionDto,
    )

    /** RF-D012: borrado inmediato y definitivo, sin período de gracia. */
    @DELETE("cuenta")
    suspend fun eliminarCuenta()
}

/**
 * RF-D016: decora [BackendConfianzaClient] con 1 reintento automático y transparente ante
 * fallas transitorias (sin conexión, cold-start del backend compartido con EcoGPT, 5xx) —
 * mismo patrón que `VerificarFotoConIA.intentarConUnReintentoAutomatico` (spec 001,
 * RF-079). Las fallas no transitorias (4xx, p. ej. 401/409) se propagan de inmediato sin
 * reintentar, para que el llamador las siga distinguiendo (p. ej. el 409 de caminata
 * activa en otro dispositivo).
 */
class BackendConfianzaClientConReintento(
    private val delegado: BackendConfianzaClient,
) : BackendConfianzaClient {

    override suspend fun otorgarPuntosReciclarReutilizar(datos: SolicitudRegistroReciclarReutilizarDto) =
        conReintento { delegado.otorgarPuntosReciclarReutilizar(datos) }

    override suspend fun otorgarPuntosCaminar(datos: SolicitudCaminarDto) =
        conReintento { delegado.otorgarPuntosCaminar(datos) }

    override suspend fun iniciarOActualizarCaminata(datos: SolicitudCaminarDto) =
        conReintento { delegado.iniciarOActualizarCaminata(datos) }

    override suspend fun actualizarPasosDelDia(datos: SolicitudPasosDelDiaDto) =
        conReintento { delegado.actualizarPasosDelDia(datos) }

    override suspend fun migrarDatosLocales(datos: SolicitudMigracionDto) =
        conReintento { delegado.migrarDatosLocales(datos) }

    override suspend fun eliminarCuenta() =
        conReintento { delegado.eliminarCuenta() }

    private suspend fun <T> conReintento(llamada: suspend () -> T): T =
        try {
            llamada()
        } catch (fallaTransitoria: IOException) {
            delay(ESPERA_ANTES_DE_REINTENTAR_MS)
            llamada()
        } catch (errorHttp: HttpException) {
            if (errorHttp.code() !in 500..599) throw errorHttp
            delay(ESPERA_ANTES_DE_REINTENTAR_MS)
            llamada()
        }
}

@JsonClass(generateAdapter = true)
data class SolicitudRegistroReciclarReutilizarDto(
    val actividadId: String,
    val categoria: String,
    val fecha: String,
    val resultado: String,
    val motivoIA: String?,
    val huellaImagen: String,
)

@JsonClass(generateAdapter = true)
data class SolicitudCaminarDto(
    val accion: String,
    val actividadId: String? = null,
    val metaPasos: Int? = null,
    val pasosLogrados: Int? = null,
    val dispositivoId: String? = null,
)

@JsonClass(generateAdapter = true)
data class SolicitudPasosDelDiaDto(
    val pasosHoy: Int,
    val fecha: String,
)

@JsonClass(generateAdapter = true)
data class SolicitudMigracionDto(
    val perfil: PerfilMigracionDto,
    val historial: List<RegistroMigracionDto>,
)

/** Campos de `UsuarioRoomEntity` (spec 001) a subir una única vez (RF-D011). */
@JsonClass(generateAdapter = true)
data class PerfilMigracionDto(
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: String?,
    val telefono: String,
    val categoriasDeInteres: List<String>,
    val puntosHistoricos: Int,
    val rachaActual: Int,
    val ultimaActividadAprobadaEn: String?,
)

/** `id` es la PK local de `RegistroVerificacionRoomEntity` (spec 001), reusada como ID
 * determinístico del documento Firestore migrado (idempotencia, ver `puntos.py`). */
@JsonClass(generateAdapter = true)
data class RegistroMigracionDto(
    val id: String,
    val actividadId: String,
    val categoria: String,
    val fecha: String,
    val resultado: String,
    val motivoIA: String?,
    val puntosOtorgados: Int,
    val huellaImagen: String?,
    val pasosRegistrados: Int?,
)

/** DTO 1:1 con el schema `RegistroVerificacionOtorgado` de `contracts/openapi.yaml`. */
@JsonClass(generateAdapter = true)
data class RegistroVerificacionOtorgadoDto(
    val id: String,
    val resultado: String,
    val puntosOtorgados: Int,
    val puntosHistoricosActualizados: Int,
    val rachaActual: Int,
    val nivel: String,
)

/** Respuesta de `accion = "INICIAR" | "ACTUALIZAR_PROGRESO"` en `/registros-verificacion/caminar`. */
@JsonClass(generateAdapter = true)
data class EstadoCaminataEnCursoDto(
    val estado: String,
    val pasosLogrados: Int,
    val metaPasos: Int?,
)
