package com.ecosmart.infrastructure.persistence.room.actividad

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fila Room de Actividad (data-model.md §4). */
@Entity(tableName = "actividades")
data class ActividadRoomEntity(
    @PrimaryKey val id: String,
    val categoria: String,
    val descripcionCorta: String,
    val pasosASeguir: String,
    val resultadoEsperado: String,
    val fotoReferencialUrl: String,
    val puntosBase: Int,
)
