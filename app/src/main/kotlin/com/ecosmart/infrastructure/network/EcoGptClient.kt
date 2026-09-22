package com.ecosmart.infrastructure.network

import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
