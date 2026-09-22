package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad

/**
 * Entidad de dominio Actividad del catálogo de EcoSmart (data-model.md
 * §2.2).
 */
data class Actividad(
    val id: ActividadId,
    val categoria: CategoriaActividad,
    val descripcionCorta: String,
    val pasosASeguir: String,
    val resultadoEsperado: String,
    val fotoReferencialUrl: String,
    val puntosBase: Int,
) {
    /**
     * RF-056: toda actividad del catálogo está disponible 365 días sin
     * restricción horaria; el método existe para que la UI/casos de uso
     * nunca decidan esto por su cuenta, aunque hoy siempre sea `true`.
     */
    fun estaDisponibleHoy(): Boolean = true
}
