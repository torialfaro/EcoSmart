package com.ecosmart.presentation.comun

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.ColorChipCaminarFondo
import com.ecosmart.presentation.theme.ColorChipCaminarTexto
import com.ecosmart.presentation.theme.ColorChipReciclarFondo
import com.ecosmart.presentation.theme.ColorChipReciclarTexto
import com.ecosmart.presentation.theme.ColorChipReutilizarFondo
import com.ecosmart.presentation.theme.ColorChipReutilizarTexto
import com.ecosmart.presentation.theme.ColorPanelCaminarFin
import com.ecosmart.presentation.theme.ColorPanelCaminarInicio
import com.ecosmart.presentation.theme.ColorPanelReciclarFin
import com.ecosmart.presentation.theme.ColorPanelReciclarInicio
import com.ecosmart.presentation.theme.ColorPanelReutilizarFin
import com.ecosmart.presentation.theme.ColorPanelReutilizarInicio
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

/** Logo de categoría de Inicio, Actividades y el detalle de actividad (spec 005, `C-19`); `iconoCategoria()` sigue para el Historial. */
@DrawableRes
fun logoCategoria(categoria: CategoriaActividad): Int = when (categoria) {
    CategoriaActividad.RECICLAR -> R.drawable.logo_categoria_reciclar
    CategoriaActividad.REUTILIZAR -> R.drawable.logo_categoria_reutilizar
    CategoriaActividad.CAMINAR -> R.drawable.logo_categoria_caminar
}

/** Degradé del panel de la tarjeta y del detalle de actividad (RF-IA-021). */
fun degradePanelCategoria(categoria: CategoriaActividad): Brush = when (categoria) {
    CategoriaActividad.RECICLAR -> Brush.verticalGradient(listOf(ColorPanelReciclarInicio, ColorPanelReciclarFin))
    CategoriaActividad.REUTILIZAR -> Brush.verticalGradient(listOf(ColorPanelReutilizarInicio, ColorPanelReutilizarFin))
    CategoriaActividad.CAMINAR -> Brush.verticalGradient(listOf(ColorPanelCaminarInicio, ColorPanelCaminarFin))
}
