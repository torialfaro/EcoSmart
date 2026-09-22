package com.ecosmart.infrastructure.persistence.room.permiso

import com.ecosmart.domain.model.PermisoDispositivo
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso

object PermisoMapper {

    fun toDomain(entity: PermisoDispositivoRoomEntity): PermisoDispositivo = PermisoDispositivo(
        tipo = TipoPermiso.valueOf(entity.tipo),
        estado = EstadoPermiso.valueOf(entity.estado),
    )

    fun toRoomEntity(permiso: PermisoDispositivo): PermisoDispositivoRoomEntity = PermisoDispositivoRoomEntity(
        tipo = permiso.tipo.name,
        estado = permiso.estado.name,
    )
}
