package com.ecosmart.infrastructure.network

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Cliente Retrofit del contrato `GET /puntos-verdes` (ver
 * `contracts/openapi.yaml`, research.md §5). Devuelve [Response] envuelto
 * (no el cuerpo directo) para poder distinguir un 200 con datos de un 304
 * "sin cambios" sin que eso se trate como una excepción de red.
 */
interface PuntosVerdesSyncClient {

    @GET("puntos-verdes")
    suspend fun sincronizarPuntosVerdes(
        @Query("actualizadoDesde") actualizadoDesde: String? = null,
    ): Response<RespuestaPuntosVerdesDto>
}

@JsonClass(generateAdapter = true)
data class RespuestaPuntosVerdesDto(
    val puntos: List<PuntoVerdeDto>,
)

@JsonClass(generateAdapter = true)
data class PuntoVerdeDto(
    val id: String,
    val nombre: String,
    val direccion: String,
    val barrio: String,
    val latitud: Double,
    val longitud: Double,
    val categoriasQueAcepta: List<String>? = null,
)
