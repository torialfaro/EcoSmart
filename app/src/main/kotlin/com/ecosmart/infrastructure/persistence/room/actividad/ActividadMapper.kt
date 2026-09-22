package com.ecosmart.infrastructure.persistence.room.actividad

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad

object ActividadMapper {

    fun toDomain(entity: ActividadRoomEntity): Actividad = Actividad(
        id = ActividadId(entity.id),
        categoria = CategoriaActividad.valueOf(entity.categoria),
        descripcionCorta = entity.descripcionCorta,
        pasosASeguir = entity.pasosASeguir,
        resultadoEsperado = entity.resultadoEsperado,
        fotoReferencialUrl = entity.fotoReferencialUrl,
        puntosBase = entity.puntosBase,
    )

    fun toRoomEntity(actividad: Actividad): ActividadRoomEntity = ActividadRoomEntity(
        id = actividad.id.valor,
        categoria = actividad.categoria.name,
        descripcionCorta = actividad.descripcionCorta,
        pasosASeguir = actividad.pasosASeguir,
        resultadoEsperado = actividad.resultadoEsperado,
        fotoReferencialUrl = actividad.fotoReferencialUrl,
        puntosBase = actividad.puntosBase,
    )
}
