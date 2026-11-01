package com.ecosmart.presentation.education

import com.ecosmart.domain.valueobject.CategoriaActividad

data class LeccionUi(
    val id: String,
    val titulo: String,
    val categoria: CategoriaActividad,
    val duracionMinutos: Int,
    val resumen: String,
    val introduccion: String,
    val ilustracionPortada: String?,
    val descripcionPortada: String,
    val pasos: List<PasoLeccionUi>,
    val tip: TipLeccionUi?,
    val textoCierre: String?,
) {
    val cantidadPasos: Int get() = pasos.size
}

data class PasoLeccionUi(
    val titulo: String,
    val texto: String,
    val ilustracion: String?,
    val descripcionIlustracion: String,
)

data class TipLeccionUi(
    val titulo: String,
    val texto: String,
)

sealed class ResultadoCatalogo {
    data class Cargado(val lecciones: List<LeccionUi>) : ResultadoCatalogo()
    data object Vacio : ResultadoCatalogo()
    data object Error : ResultadoCatalogo()
}
