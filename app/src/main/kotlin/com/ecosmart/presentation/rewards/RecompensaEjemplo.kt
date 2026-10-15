package com.ecosmart.presentation.rewards

import com.ecosmart.app.R

/**
 * Categorías sugeridas para las recompensas de ejemplo (data-model.md §3).
 * Chips de filtro en RecompensasScreen (C-04).
 */
enum class CategoriaRecompensaEjemplo(val etiqueta: String) {
    TODOS("Todos"),
    COMIDA("Comida"),
    DESCUENTOS("Descuentos"),
    PRODUCTOS("Productos"),
}

/**
 * Modelo de datos de muestra estática de Recompensas (data-model.md §3 / RF-UI-003b).
 * No es una entidad de dominio: vive únicamente en presentation/rewards/.
 * Corrección post-QA (2026-10-14): se elimina el segmento "Para adultos"/"Para Kids" —
 * el catálogo es único, sin distinción de público (ver spec.md → Clarifications).
 */
data class RecompensaEjemplo(
    val id: String,
    val titulo: String,
    val categoria: CategoriaRecompensaEjemplo,
    val costoPuntos: Int,
    val imagenDrawableRes: Int? = null,
)

private fun imagenPorCategoria(categoria: CategoriaRecompensaEjemplo): Int = when (categoria) {
    CategoriaRecompensaEjemplo.COMIDA -> R.drawable.ic_recompensa_comida
    CategoriaRecompensaEjemplo.DESCUENTOS -> R.drawable.ic_recompensa_descuentos
    CategoriaRecompensaEjemplo.PRODUCTOS -> R.drawable.ic_recompensa_productos
    CategoriaRecompensaEjemplo.TODOS -> R.drawable.ic_recompensa_productos
}

/**
 * Catálogo estático de recompensas de ejemplo (§7.2) con erratas de copy corregidas (RF-UI-003b).
 * Fotos de ejemplo asignadas por categoría (feedback post-QA): placeholders locales hasta que
 * el equipo de diseño entregue imágenes reales (ver research.md §1).
 */
val RECOMPENSAS_EJEMPLO = listOf(
    RecompensaEjemplo(
        id = "cafe-medialuna",
        titulo = "Combo café + medialuna",
        categoria = CategoriaRecompensaEjemplo.COMIDA,
        costoPuntos = 800,
        imagenDrawableRes = imagenPorCategoria(CategoriaRecompensaEjemplo.COMIDA),
    ),
    RecompensaEjemplo(
        id = "descuento-tiendas-eco",
        titulo = "15% de descuento en tiendas eco",
        categoria = CategoriaRecompensaEjemplo.DESCUENTOS,
        costoPuntos = 1000,
        imagenDrawableRes = imagenPorCategoria(CategoriaRecompensaEjemplo.DESCUENTOS),
    ),
    RecompensaEjemplo(
        id = "cine",
        titulo = "Cine",
        categoria = CategoriaRecompensaEjemplo.DESCUENTOS,
        costoPuntos = 1200,
        imagenDrawableRes = imagenPorCategoria(CategoriaRecompensaEjemplo.DESCUENTOS),
    ),
    RecompensaEjemplo(
        id = "producto-sustentable",
        titulo = "Producto sustentable",
        categoria = CategoriaRecompensaEjemplo.PRODUCTOS,
        costoPuntos = 1500,
        imagenDrawableRes = imagenPorCategoria(CategoriaRecompensaEjemplo.PRODUCTOS),
    ),
    RecompensaEjemplo(
        id = "descuento-ropa",
        titulo = "20% de descuento en ropa",
        categoria = CategoriaRecompensaEjemplo.DESCUENTOS,
        costoPuntos = 1000,
        imagenDrawableRes = imagenPorCategoria(CategoriaRecompensaEjemplo.DESCUENTOS),
    ),
    RecompensaEjemplo(
        id = "promocion-gimnasio",
        titulo = "Promoción en gimnasio",
        categoria = CategoriaRecompensaEjemplo.DESCUENTOS,
        costoPuntos = 1200,
        imagenDrawableRes = imagenPorCategoria(CategoriaRecompensaEjemplo.DESCUENTOS),
    ),
)
