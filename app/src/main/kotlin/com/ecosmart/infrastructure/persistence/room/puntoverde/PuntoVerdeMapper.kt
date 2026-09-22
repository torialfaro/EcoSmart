package com.ecosmart.infrastructure.persistence.room.puntoverde

import com.ecosmart.domain.model.PuntoVerde
import com.ecosmart.domain.valueobject.PuntoVerdeId

object PuntoVerdeMapper {

    fun toDomain(entity: PuntoVerdeRoomEntity): PuntoVerde = PuntoVerde(
        id = PuntoVerdeId(entity.id),
        nombre = entity.nombre,
        direccion = entity.direccion,
        barrio = entity.barrio,
        latitud = entity.latitud,
        longitud = entity.longitud,
        categoriasQueAcepta = entity.categoriasQueAcepta.split(",").filter { it.isNotBlank() }.toSet(),
    )

    fun toRoomEntity(puntoVerde: PuntoVerde): PuntoVerdeRoomEntity = PuntoVerdeRoomEntity(
        id = puntoVerde.id.valor,
        nombre = puntoVerde.nombre,
        direccion = puntoVerde.direccion,
        barrio = puntoVerde.barrio,
        latitud = puntoVerde.latitud,
        longitud = puntoVerde.longitud,
        categoriasQueAcepta = puntoVerde.categoriasQueAcepta.joinToString(","),
    )
}
