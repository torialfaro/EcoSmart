package com.ecosmart.infrastructure.persistence.room.usuario

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate

object UsuarioMapper {

    fun toDomain(entity: UsuarioRoomEntity): Usuario = Usuario(
        id = UsuarioId(entity.id),
        email = entity.email,
        contrasenaCifrada = ContrasenaCifrada(entity.contrasenaCifradaJwe),
        nombre = entity.nombre,
        apellido = entity.apellido,
        nombreUsuario = entity.nombreUsuario,
        barrio = entity.barrio?.let { Barrio.valueOf(it) },
        telefono = entity.telefono,
        categoriasDeInteres = entity.categoriasDeInteres
            .split(",")
            .filter { it.isNotBlank() }
            .map { CategoriaActividad.valueOf(it) }
            .toSet(),
        puntosHistoricos = entity.puntosHistoricos,
        rachaActual = entity.rachaActual,
        ultimaActividadAprobadaEn = entity.ultimaActividadAprobadaEn?.let(LocalDate::parse),
    )

    fun toRoomEntity(usuario: Usuario): UsuarioRoomEntity = UsuarioRoomEntity(
        id = usuario.id.valor,
        email = usuario.email,
        contrasenaCifradaJwe = usuario.contrasenaCifrada.jweCompacto,
        nombre = usuario.nombre,
        apellido = usuario.apellido,
        nombreUsuario = usuario.nombreUsuario,
        barrio = usuario.barrio?.name,
        telefono = usuario.telefono,
        categoriasDeInteres = usuario.categoriasDeInteres.joinToString(",") { it.name },
        puntosHistoricos = usuario.puntosHistoricos,
        rachaActual = usuario.rachaActual,
        ultimaActividadAprobadaEn = usuario.ultimaActividadAprobadaEn?.toString(),
    )
}
