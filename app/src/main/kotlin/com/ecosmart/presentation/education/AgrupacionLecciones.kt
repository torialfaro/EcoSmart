@file:Suppress("MatchingDeclarationName")

package com.ecosmart.presentation.education

import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.ordenSeccionesEducacion

data class SeccionLecciones(
    val categoria: CategoriaActividad,
    val lecciones: List<LeccionUi>,
)

const val MAXIMO_LECCIONES_INICIO = 2

fun agruparPorCategoria(lecciones: List<LeccionUi>): List<SeccionLecciones> =
    ordenSeccionesEducacion.mapNotNull { categoria ->
        lecciones.filter { it.categoria == categoria }
            .takeIf { it.isNotEmpty() }
            ?.let { SeccionLecciones(categoria, it) }
    }

fun leccionesParaInicio(lecciones: List<LeccionUi>): List<LeccionUi> = lecciones.take(MAXIMO_LECCIONES_INICIO)
