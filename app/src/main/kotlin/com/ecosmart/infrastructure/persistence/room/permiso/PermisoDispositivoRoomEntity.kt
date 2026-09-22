package com.ecosmart.infrastructure.persistence.room.permiso

import androidx.room.Entity

/** Fila Room de PermisoDispositivo (data-model.md §4), una por [TipoPermiso]. */
@Entity(tableName = "permisos_dispositivo", primaryKeys = ["tipo"])
data class PermisoDispositivoRoomEntity(
    val tipo: String,
    val estado: String,
)
