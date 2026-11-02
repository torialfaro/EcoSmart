package com.ecosmart.presentation.estadodiario

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.infrastructure.session.SesionUsuario
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Lee los registros de verificación del usuario y calcula el [EstadoDiarioUi]. Ante cualquier falla
 * (sin sesión o sin red) devuelve `null` para que la UI no muestre valores inventados.
 */
class CargadorEstadoDiario @Inject constructor(
    private val registroVerificacionRepository: RegistroVerificacionRepository,
    private val sesionUsuario: SesionUsuario,
) {
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    suspend fun cargar(actividades: List<Actividad>, hoy: LocalDate = LocalDate.now()): EstadoDiarioUi? {
        val usuarioId = sesionUsuario.usuarioActualId.value ?: return null
        return try {
            calcularEstadoDiario(actividades, registroVerificacionRepository.todos(usuarioId), hoy)
        } catch (error: Exception) {
            currentCoroutineContext().ensureActive()
            null
        }
    }
}
