package com.ecosmart.infrastructure.persistence.room.usuario

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** @see com.ecosmart.infrastructure.persistence.room.AppDatabase para el porqué del `@Deprecated`. */
@Deprecated("Sin uso de negocio tras spec 002-firestore-datos-usuario; solo lo lee MigracionDatosLocales (T043).")
@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(usuario: UsuarioRoomEntity)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UsuarioRoomEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UsuarioRoomEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<UsuarioRoomEntity?>
}
