package com.ecosmart.infrastructure.persistence.room.actividad

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(actividades: List<ActividadRoomEntity>)

    @Query("SELECT COUNT(*) FROM actividades")
    suspend fun contar(): Int

    @Query("SELECT * FROM actividades WHERE categoria IN (:categorias)")
    fun findByCategorias(categorias: List<String>): Flow<List<ActividadRoomEntity>>

    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ActividadRoomEntity?
}
