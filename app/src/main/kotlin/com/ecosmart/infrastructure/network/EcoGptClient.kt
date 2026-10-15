package com.ecosmart.infrastructure.network

import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Cliente Retrofit del contrato `POST /verificaciones` de EcoGPT (ver
 * `contracts/openapi.yaml`). El timeout de 30s (RNF-008/SC-008) se
 * configura en el [okhttp3.OkHttpClient] inyectado por [NetworkModule]
 * (T018), no acá — esta interfaz solo describe la forma de la llamada.
 */
interface EcoGptClient {

    @Multipart
    @POST("verificaciones")
    suspend fun verificarFotoActividad(
        @Header("X-EcoGPT-Api-Key") apiKey: String,
        @Part imagen: MultipartBody.Part,
        @Part("categoria") categoria: RequestBody,
        @Part("resultadoEsperado") resultadoEsperado: RequestBody,
        @Part("descripcionUsuario") descripcionUsuario: RequestBody,
        @Part("huellasImagenesAprobadasPrevias") huellasImagenesAprobadasPrevias: RequestBody,
    ): VeredictoEcoGptDto

    /**
     * Corrección post-QA (2026-09-23): "despertar" best-effort del backend
     * (`backend/app/main.py` → `/health`) antes del envío real — mitiga el
     * cold start del hosting free-tier (research.md §2.1). `Void` evita que
     * Retrofit intente convertir el body con Moshi; no nos importa la
     * respuesta, solo que la request haya llegado.
     */
    @GET("health")
    suspend fun ping(): Response<Void>
}

/**
 * DTO 1:1 con el schema `VeredictoEcoGpt` de `contracts/openapi.yaml`. El
 * mapeo a [com.ecosmart.domain.valueobject.ResultadoVerificacion] (Factory
 * `ResultadoVerificacion.desde(...)`) se implementa en el Módulo 4 (T076),
 * no acá — esta clase es un DTO de infraestructura, no un tipo de dominio.
 */
@JsonClass(generateAdapter = true)
data class VeredictoEcoGptDto(
    val veredicto: String,
    val motivo: String?,
)

/**
 * DTO 1:1 con el schema `ErrorEcoGpt` de `contracts/openapi.yaml` — el body
 * de error que el backend devuelve ante respuestas no-2xx (`main.py` →
 * `_error(...)`). Corrección post-QA (2026-10-14): antes se descartaba este
 * body y se mostraba el reason phrase HTTP crudo (`HttpException.message()`,
 * p. ej. "Response.error()") en vez del mensaje real en español pensado
 * para el usuario (ver `VerificarFotoConIA.mensajeDeErrorEcoGpt`).
 */
@JsonClass(generateAdapter = true)
data class ErrorEcoGptDto(
    val codigo: String,
    val mensaje: String,
)
