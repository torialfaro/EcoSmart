package com.ecosmart.infrastructure.persistence.room.verificacion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RegistroVerificacionDao {

    @Insert
    suspend fun insertar(registro: RegistroVerificacionRoomEntity)

    @Query(
        "SELECT COUNT(*) FROM registros_verificacion " +
            "WHERE usuarioId = :usuarioId AND categoria = :categoria AND fecha = :fecha AND resultado = 'APROBADO'",
    )
    suspend fun contarAprobadosDelDia(usuarioId: String, categoria: String, fecha: String): Int

    @Query("SELECT * FROM registros_verificacion WHERE usuarioId = :usuarioId ORDER BY fecha DESC LIMIT :n")
    suspend fun ultimosN(usuarioId: String, n: Int): List<RegistroVerificacionRoomEntity>

    @Query("SELECT * FROM registros_verificacion WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    suspend fun todos(usuarioId: String): List<RegistroVerificacionRoomEntity>

    @Query(
        "SELECT huellaImagen FROM registros_verificacion " +
            "WHERE usuarioId = :usuarioId AND categoria = :categoria AND resultado = 'APROBADO' " +
            "AND huellaImagen IS NOT NULL",
    )
    suspend fun huellasAprobadas(usuarioId: String, categoria: String): List<String>

    /** Corrección post-QA: pasos de Caminata registrados hoy (RF-070), para mostrarlos en el Perfil. */
    @Query(
        "SELECT COALESCE(SUM(pasosRegistrados), 0) FROM registros_verificacion " +
            "WHERE usuarioId = :usuarioId AND categoria = 'CAMINAR' AND resultado = 'APROBADO' AND fecha = :fecha",
    )
    suspend fun sumaPasosDelDia(usuarioId: String, fecha: String): Int
}
