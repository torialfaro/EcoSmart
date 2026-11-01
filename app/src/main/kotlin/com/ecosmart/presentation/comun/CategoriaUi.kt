package com.ecosmart.presentation.comun

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.ColorChipCaminarFondo
import com.ecosmart.presentation.theme.ColorChipCaminarTexto
import com.ecosmart.presentation.theme.ColorChipReciclarFondo
import com.ecosmart.presentation.theme.ColorChipReciclarTexto
import com.ecosmart.presentation.theme.ColorChipReutilizarFondo
import com.ecosmart.presentation.theme.ColorChipReutilizarTexto
import com.ecosmart.presentation.theme.ColorPastelCaminar
import com.ecosmart.presentation.theme.ColorPastelReciclar
import com.ecosmart.presentation.theme.ColorPastelReutilizar

/** Nombre visible de cada categoría (primera letra mayúscula, no el `.name` del enum en mayúsculas). */
fun etiquetaCategoria(categoria: CategoriaActividad): String = when (categoria) {
    CategoriaActividad.RECICLAR -> "Reciclar"
    CategoriaActividad.REUTILIZAR -> "Reutilizar"
    CategoriaActividad.CAMINAR -> "Caminar"
}

/**
 * Icono por categoría, compartido entre las tarjetas de actividad de Home y las de
 * Historial. `ic_categoria_reciclar`/`ic_categoria_reutilizar` son placeholders propios
 * (sin la imagen real que mandó el usuario, que no se pudo incorporar como archivo);
 * reemplazarlos en `res/drawable/` por el diseño definitivo cuando esté disponible.
 */
@DrawableRes
fun iconoCategoria(categoria: CategoriaActividad): Int = when (categoria) {
    CategoriaActividad.RECICLAR -> R.drawable.ic_categoria_reciclar
    CategoriaActividad.REUTILIZAR -> R.drawable.ic_categoria_reutilizar
    CategoriaActividad.CAMINAR -> R.drawable.ic_caminar
}

/** Orden fijo de secciones en Educación; no depende del orden del enum. */
val ordenSeccionesEducacion: List<CategoriaActividad> = listOf(
    CategoriaActividad.RECICLAR,
    CategoriaActividad.REUTILIZAR,
    CategoriaActividad.CAMINAR,
)

fun colorFondoChipCategoria(categoria: CategoriaActividad): Color = when (categoria) {
    CategoriaActividad.RECICLAR -> ColorChipReciclarFondo
    CategoriaActividad.REUTILIZAR -> ColorChipReutilizarFondo
    CategoriaActividad.CAMINAR -> ColorChipCaminarFondo
}

fun colorTextoChipCategoria(categoria: CategoriaActividad): Color = when (categoria) {
    CategoriaActividad.RECICLAR -> ColorChipReciclarTexto
    CategoriaActividad.REUTILIZAR -> ColorChipReutilizarTexto
    CategoriaActividad.CAMINAR -> ColorChipCaminarTexto
}

fun colorFondoIlustracionCategoria(categoria: CategoriaActividad): Color = when (categoria) {
    CategoriaActividad.RECICLAR -> ColorPastelReciclar
    CategoriaActividad.REUTILIZAR -> ColorPastelReutilizar
    CategoriaActividad.CAMINAR -> ColorPastelCaminar
}
