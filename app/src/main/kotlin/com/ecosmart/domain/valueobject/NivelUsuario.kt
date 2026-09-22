package com.ecosmart.domain.valueobject

/**
 * Nivel de usuario según puntaje histórico acumulado (RF-046/RF-047,
 * resuelto en `/speckit.clarify` como `[AMBIGÜEDAD-001]`). Los umbrales
 * viven en el propio enum, no dispersos en un `when`; como se calcula
 * siempre desde el puntaje histórico (que solo crece, ver
 * [com.ecosmart.domain.model.Usuario.sumarPuntos]), el nivel nunca
 * desciende sin necesidad de lógica adicional. Ver data-model.md §1.
 */
enum class NivelUsuario(val puntosMinimos: Int) {
    SEMILLA(0),
    BROTE(500),
    PLANTA(1500),
    ARBOL(3500),
    ;

    companion object {
        fun desdePuntaje(puntos: Int): NivelUsuario =
            entries.sortedByDescending { it.puntosMinimos }
                .first { puntos >= it.puntosMinimos }
    }
}
