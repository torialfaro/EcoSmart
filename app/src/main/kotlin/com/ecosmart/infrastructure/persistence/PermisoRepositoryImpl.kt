package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.PermisoDispositivo
import com.ecosmart.domain.repository.PermisoRepository
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoDao
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermisoRepositoryImpl @Inject constructor(
    private val permisoDao: PermisoDao,
) : PermisoRepository {

    override suspend fun obtenerTodos(): List<PermisoDispositivo> =
        permisoDao.obtenerTodos().map(PermisoMapper::toDomain)

    override suspend fun guardarEstado(tipo: TipoPermiso, estado: EstadoPermiso) {
        permisoDao.upsert(PermisoMapper.toRoomEntity(PermisoDispositivo(tipo, estado)))
    }

    override fun observar(tipo: TipoPermiso): Flow<PermisoDispositivo?> =
        permisoDao.observar(tipo.name).map { it?.let(PermisoMapper::toDomain) }
}
