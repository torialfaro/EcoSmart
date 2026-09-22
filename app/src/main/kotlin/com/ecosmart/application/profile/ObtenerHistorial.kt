package com.ecosmart.application.profile

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.valueobject.UsuarioId
import javax.inject.Inject

private const val RECIENTES_INICIALES = 3

/** US12 — últimas 3 actividades + historial completo bajo demanda (RF-041/RF-042). */
class ObtenerHistorial @Inject constructor(
    private val registroVerificacionRepository: RegistroVerificacionRepository,
) {
    /** RF-041 — contenedor inicial del historial. */
    suspend fun recientes(usuarioId: UsuarioId): List<RegistroVerificacion> =
        registroVerificacionRepository.ultimosN(usuarioId, RECIENTES_INICIALES)

    /** RF-042 — listado completo, detrás del botón "Ver más". */
    suspend fun completo(usuarioId: UsuarioId): List<RegistroVerificacion> =
        registroVerificacionRepository.todos(usuarioId)
}
