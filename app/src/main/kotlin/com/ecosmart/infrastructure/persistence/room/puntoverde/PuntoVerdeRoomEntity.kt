package com.ecosmart.infrastructure.persistence.room.puntoverde

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fila Room de Punto Verde (data-model.md §4). */
@Entity(tableName = "puntos_verdes")
data class PuntoVerdeRoomEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val direccion: String,
    val barrio: String,
    val latitud: Double,
    val longitud: Double,
    val categoriasQueAcepta: String,
)
