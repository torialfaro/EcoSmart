package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.Coordenada
import com.ecosmart.domain.valueobject.PuntoVerdeId
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val RADIO_TIERRA_KM = 6371.0
private const val RADIO_POR_DEFECTO_KM = 3.0

/**
 * Entidad de dominio Punto Verde (data-model.md §2.4, US7). Las categorías
 * quedan como texto libre (`Set<String>`), tal como las publica la fuente
 * oficial de CABA (RF-019), sin intentar mapearlas al enum interno
 * [com.ecosmart.domain.valueobject.CategoriaActividad]. Lo mismo con
 * [barrio]: llega tal cual de la fuente externa (no es el enum `Barrio`
 * cerrado del dominio), y se compara por texto contra el barrio elegido
 * por el usuario en su perfil (`ObtenerPuntosVerdesDelBarrio`).
 */
data class PuntoVerde(
    val id: PuntoVerdeId,
    val nombre: String,
    val direccion: String,
    val barrio: String,
    val latitud: Double,
    val longitud: Double,
    val categoriasQueAcepta: Set<String>,
) {
    /**
     * RF-053 (versión original, por radio GPS) — distancia Haversine entre
     * [origen] y este punto ≤ [radioKm] (por defecto 3 km). Ya no es el
     * mecanismo activo de búsqueda (reemplazado por coincidencia de barrio,
     * corrección post-QA: ver `ObtenerPuntosVerdesDelBarrio`); se conserva
     * porque sigue siendo una regla de dominio válida y testeada
     * (`PuntoVerdeTest`), por si se retoma un filtro geográfico más
     * adelante.
     */
    fun estaDentroDelRadio(origen: Coordenada, radioKm: Double = RADIO_POR_DEFECTO_KM): Boolean =
        distanciaHaversineKm(origen, Coordenada(latitud, longitud)) <= radioKm

    private fun distanciaHaversineKm(a: Coordenada, b: Coordenada): Double {
        val deltaLat = Math.toRadians(b.latitud - a.latitud)
        val deltaLon = Math.toRadians(b.longitud - a.longitud)
        val latA = Math.toRadians(a.latitud)
        val latB = Math.toRadians(b.latitud)

        val senoMitadLat = sin(deltaLat / 2)
        val senoMitadLon = sin(deltaLon / 2)
        val formula = senoMitadLat * senoMitadLat + cos(latA) * cos(latB) * senoMitadLon * senoMitadLon
        val distanciaAngular = 2 * atan2(sqrt(formula), sqrt(1 - formula))
        return RADIO_TIERRA_KM * distanciaAngular
    }
}
