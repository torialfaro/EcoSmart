package com.ecosmart.application.profile

import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.UsuarioId
import javax.inject.Inject

/** `porcentajePorCategoria` está en 0..100; mapa vacío si el usuario todavía no realizó ninguna actividad. */
data class MetricasPerfil(
    val puntosHistoricos: Int,
    val nivel: NivelUsuario,
    val rachaActual: Int,
    val porcentajePorCategoria: Map<CategoriaActividad, Double>,
)

/** US11 — puntos, % de participación por categoría, racha y nivel del perfil (RF-036 a RF-040). */
class CalcularMetricasPerfil @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val registroVerificacionRepository: RegistroVerificacionRepository,
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

        return MetricasPerfil(
            puntosHistoricos = usuario.puntosHistoricos,
            nivel = usuario.nivel(),
            rachaActual = usuario.rachaVigente(),
            porcentajePorCategoria = porcentajePorCategoria,
        )
    }
}
