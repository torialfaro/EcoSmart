package com.ecosmart.domain.repository

import com.ecosmart.domain.model.PuntoVerde

/**
 * Puerto de dominio hacia la persistencia de [PuntoVerde]. La
 * implementación vive en `infrastructure/persistence/PuntoVerdeRepositoryImpl`.
 */
interface PuntoVerdeRepository {

    suspend fun obtenerTodos(): List<PuntoVerde>

    /** Reemplaza la copia local completa (usado por el Worker de sincronización, RF-052). */
    suspend fun reemplazarTodos(puntos: List<PuntoVerde>)

    /**
     * Siembra un puñado de Puntos Verdes de ejemplo la primera vez que se
     * usa la app, para que la búsqueda por barrio sea demostrable antes de
     * que el Worker de sincronización (RF-052) haya corrido contra una
     * fuente real. Idempotente: no duplica si ya hay datos.
     */
    suspend fun sembrarSiEstaVacio()
}
