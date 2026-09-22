package com.ecosmart.infrastructure.persistence.room.puntoverde

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PuntoVerdeDao {

    @Query("SELECT * FROM puntos_verdes")
    suspend fun obtenerTodos(): List<PuntoVerdeRoomEntity>

    @Query("SELECT COUNT(*) FROM puntos_verdes")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoVerdeRoomEntity>)

    @Query("DELETE FROM puntos_verdes")
    suspend fun borrarTodos()

    /** Reemplaza la copia local completa en una sola transacción (usado por el Worker de sincronización). */
    @Transaction
    suspend fun reemplazarTodos(puntos: List<PuntoVerdeRoomEntity>) {
        borrarTodos()
        insertarTodos(puntos)
    }
}
