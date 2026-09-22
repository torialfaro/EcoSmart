package com.ecosmart.infrastructure.persistence.room.permiso

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PermisoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(permiso: PermisoDispositivoRoomEntity)

    @Query("SELECT * FROM permisos_dispositivo")
    suspend fun obtenerTodos(): List<PermisoDispositivoRoomEntity>

    @Query("SELECT * FROM permisos_dispositivo WHERE tipo = :tipo LIMIT 1")
    fun observar(tipo: String): Flow<PermisoDispositivoRoomEntity?>
}
