package com.ecosmart.infrastructure.persistence.room.verificacion

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadRoomEntity
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioRoomEntity

/** Fila Room de Registro de Verificación (data-model.md §4). */
@Entity(
    tableName = "registros_verificacion",
    foreignKeys = [
        ForeignKey(entity = UsuarioRoomEntity::class, parentColumns = ["id"], childColumns = ["usuarioId"]),
        ForeignKey(entity = ActividadRoomEntity::class, parentColumns = ["id"], childColumns = ["actividadId"]),
    ],
    indices = [Index("usuarioId"), Index(value = ["usuarioId", "categoria", "fecha"])],
)
data class RegistroVerificacionRoomEntity(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val actividadId: String,
    val categoria: String,
    val fecha: String,
    val resultado: String,
    val motivoIA: String?,
    val puntosOtorgados: Int,
    val huellaImagen: String?,
    val pasosRegistrados: Int?,
)
