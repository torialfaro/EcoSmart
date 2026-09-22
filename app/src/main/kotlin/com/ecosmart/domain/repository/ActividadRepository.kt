package com.ecosmart.domain.repository

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de dominio hacia la persistencia de [Actividad]. La implementación
 * vive en `infrastructure/persistence/ActividadRepositoryImpl`.
 */
interface ActividadRepository {

    fun observarPorCategorias(categorias: Set<CategoriaActividad>): Flow<List<Actividad>>

    suspend fun buscarPorId(id: ActividadId): Actividad?

    /**
     * Carga el catálogo inicial la primera vez que se usa la app (no hay
     * backend propio ni fuente externa para Actividades — a diferencia de
     * Puntos Verdes, ver research.md §0). Es idempotente: no duplica si el
     * catálogo ya tiene datos.
     */
    suspend fun sembrarCatalogoSiEstaVacio()
}
