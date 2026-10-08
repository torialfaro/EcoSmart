package com.ecosmart.presentation.comun

import androidx.annotation.DrawableRes
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad

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
