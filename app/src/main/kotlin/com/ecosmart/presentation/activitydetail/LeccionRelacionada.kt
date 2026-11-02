package com.ecosmart.presentation.activitydetail

import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.education.ResultadoCatalogo

/** Id de la primera lección de [categoria] según el orden del catálogo; `null` si no hay o el catálogo falló. */
fun leccionRelacionadaId(catalogo: ResultadoCatalogo, categoria: CategoriaActividad): String? = when (catalogo) {
    is ResultadoCatalogo.Cargado -> catalogo.lecciones.firstOrNull { it.categoria == categoria }?.id
    ResultadoCatalogo.Vacio, ResultadoCatalogo.Error -> null
}
