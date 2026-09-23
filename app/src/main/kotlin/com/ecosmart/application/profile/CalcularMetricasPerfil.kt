package com.ecosmart.application.profile

import com.ecosmart.domain.repository.PermisoRepository
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate
import javax.inject.Inject

/**
 * `porcentajePorCategoria` está en 0..100; mapa vacío si el usuario todavía
 * no realizó ninguna actividad. `pasosHoy` es `null` si el permiso de
 * Podómetro no está otorgado ("si es que tiene esa opción activada
 * también", corrección post-QA 2026-09-22, RF-070) — en ese caso la UI no
 * debe mostrar el dato en vez de mostrar 0.
 */
data class MetricasPerfil(
    val puntosHistoricos: Int,
    val nivel: NivelUsuario,
    val rachaActual: Int,
    val porcentajePorCategoria: Map<CategoriaActividad, Double>,
    val pasosHoy: Int?,
)

/** US11 — puntos, % de participación por categoría, racha, nivel y pasos de hoy del perfil (RF-036 a RF-040, RF-070). */
class CalcularMetricasPerfil @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val registroVerificacionRepository: RegistroVerificacionRepository,
    private val permisoRepository: PermisoRepository,
) {
    suspend operator fun invoke(usuarioId: UsuarioId): MetricasPerfil? {
        val usuario = usuarioRepository.buscarPorId(usuarioId) ?: return null
        val registros = registroVerificacionRepository.todos(usuarioId)

        // RF-037: porcentaje de participación por categoría sobre el total de actividades realizadas
        // (todo intento registrado, no solo los Aprobados — a diferencia de puntos/racha).
        val porcentajePorCategoria = if (registros.isEmpty()) {
            emptyMap()
        } else {
            registros.groupBy { it.categoria }
                .mapValues { (_, registrosDeCategoria) -> registrosDeCategoria.size * 100.0 / registros.size }
        }

        val permisoPodometro = permisoRepository.obtenerTodos().find { it.tipo == TipoPermiso.PODOMETRO }
        val pasosHoy = if (permisoPodometro?.estado == EstadoPermiso.OTORGADO) {
            registroVerificacionRepository.sumaPasosDelDia(usuarioId, LocalDate.now())
        } else {
            null
        }

        return MetricasPerfil(
            puntosHistoricos = usuario.puntosHistoricos,
            nivel = usuario.nivel(),
            rachaActual = usuario.rachaVigente(),
            porcentajePorCategoria = porcentajePorCategoria,
            pasosHoy = pasosHoy,
        )
    }
}
