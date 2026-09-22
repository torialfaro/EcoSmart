package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionDao
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionMapper
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistroVerificacionRepositoryImpl @Inject constructor(
    private val registroVerificacionDao: RegistroVerificacionDao,
) : RegistroVerificacionRepository {

    override suspend fun guardar(registro: RegistroVerificacion) {
        registroVerificacionDao.insertar(RegistroVerificacionMapper.toRoomEntity(registro))
    }

    override suspend fun contarAprobadosDelDia(
        usuarioId: UsuarioId,
        categoria: CategoriaActividad,
        fecha: LocalDate,
    ): Int = registroVerificacionDao.contarAprobadosDelDia(usuarioId.valor, categoria.name, fecha.toString())

    override suspend fun ultimosN(usuarioId: UsuarioId, n: Int): List<RegistroVerificacion> =
        registroVerificacionDao.ultimosN(usuarioId.valor, n).map(RegistroVerificacionMapper::toDomain)

    override suspend fun todos(usuarioId: UsuarioId): List<RegistroVerificacion> =
        registroVerificacionDao.todos(usuarioId.valor).map(RegistroVerificacionMapper::toDomain)

    override suspend fun huellasAprobadas(usuarioId: UsuarioId, categoria: CategoriaActividad): List<String> =
        registroVerificacionDao.huellasAprobadas(usuarioId.valor, categoria.name)
}
