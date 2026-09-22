package com.ecosmart.domain.repository

import com.ecosmart.domain.model.PermisoDispositivo
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de dominio hacia la persistencia de [PermisoDispositivo]. La
 * implementación vive en `infrastructure/persistence/PermisoRepositoryImpl`.
 */
interface PermisoRepository {

    suspend fun obtenerTodos(): List<PermisoDispositivo>

    suspend fun guardarEstado(tipo: TipoPermiso, estado: EstadoPermiso)

    fun observar(tipo: TipoPermiso): Flow<PermisoDispositivo?>
}
