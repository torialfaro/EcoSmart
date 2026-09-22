package com.ecosmart.infrastructure.persistence.room.verificacion

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate

object RegistroVerificacionMapper {

    fun toDomain(entity: RegistroVerificacionRoomEntity): RegistroVerificacion = RegistroVerificacion(
        id = RegistroVerificacionId(entity.id),
        usuarioId = UsuarioId(entity.usuarioId),
        actividadId = ActividadId(entity.actividadId),
        categoria = CategoriaActividad.valueOf(entity.categoria),
        fecha = LocalDate.parse(entity.fecha),
        resultado = ResultadoVerificacion.valueOf(entity.resultado),
        motivoIA = entity.motivoIA,
        puntosOtorgados = entity.puntosOtorgados,
        huellaImagen = entity.huellaImagen,
        pasosRegistrados = entity.pasosRegistrados,
    )

    fun toRoomEntity(registro: RegistroVerificacion): RegistroVerificacionRoomEntity = RegistroVerificacionRoomEntity(
        id = registro.id.valor,
        usuarioId = registro.usuarioId.valor,
        actividadId = registro.actividadId.valor,
        categoria = registro.categoria.name,
        fecha = registro.fecha.toString(),
        resultado = registro.resultado.name,
        motivoIA = registro.motivoIA,
        puntosOtorgados = registro.puntosOtorgados,
        huellaImagen = registro.huellaImagen,
        pasosRegistrados = registro.pasosRegistrados,
    )
}
