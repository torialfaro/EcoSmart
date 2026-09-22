package com.ecosmart.infrastructure.persistence.room.usuario

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Fila Room de Usuario (data-model.md §4). `contrasenaCifradaJwe` guarda el
 * JWE compacto (RNF-006); la clave JWK que lo cifra/descifra vive
 * exclusivamente en Android Keystore, nunca en esta tabla (RNF-007).
 */
@Entity(tableName = "usuarios")
data class UsuarioRoomEntity(
    @PrimaryKey val id: String,
    val email: String,
    val contrasenaCifradaJwe: String,
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: String?,
    val telefono: String,
    val categoriasDeInteres: String,
    val puntosHistoricos: Int,
    val rachaActual: Int,
    val ultimaActividadAprobadaEn: String?,
)
