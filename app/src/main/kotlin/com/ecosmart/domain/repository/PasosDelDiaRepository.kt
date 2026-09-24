package com.ecosmart.domain.repository

/**
 * Puerto de dominio hacia el conteo diario de pasos del dispositivo (RF-070,
 * corrección post-QA 2026-09-24). La implementación vive en
 * `infrastructure/sensors/PasosDelDiaRepositoryImpl`.
 */
interface PasosDelDiaRepository {

    /**
     * Pasos dados hoy según el podómetro del dispositivo, o `null` si no se
     * pudo leer (sin sensor, sin permiso, o sin lectura disponible).
     */
    suspend fun pasosDeHoy(): Int?
}
