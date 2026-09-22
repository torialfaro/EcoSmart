package com.ecosmart.domain.valueobject

/**
 * Categoría de una [com.ecosmart.domain.model.Actividad] y preferencia de
 * interés de un [com.ecosmart.domain.model.Usuario] (RF-005, RF-011).
 * Valor cerrado — Principio VI de constitution.md.
 */
enum class CategoriaActividad {
    RECICLAR,
    REUTILIZAR,
    CAMINAR,
}
