package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate

/**
 * Entidad de dominio Usuario (data-model.md §2.1). El único rol posible es
 * EcoCiudadano (RF-054); no se modela como enum de un solo valor.
 */
data class Usuario(
    val id: UsuarioId,
    val email: String,
    val contrasenaCifrada: ContrasenaCifrada,
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    /**
     * Barrio de CABA elegido en un desplegable (RF-007/RF-063), no texto
     * libre — es el valor que usa la búsqueda de Puntos Verdes (RF-018/
     * RF-053 revisadas). Nulo solo para cuentas creadas vía Google que
     * todavía no completaron este dato desde su perfil.
     */
    val barrio: Barrio?,
    val telefono: String,
    val categoriasDeInteres: Set<CategoriaActividad>,
    val puntosHistoricos: Int = 0,
    val rachaActual: Int = 0,
    val ultimaActividadAprobadaEn: LocalDate? = null,
) {

    fun nivel(): NivelUsuario = NivelUsuario.desdePuntaje(puntosHistoricos)

    /** RF-047: el puntaje histórico nunca debe descender; no existe un setter directo. */
    fun sumarPuntos(cantidad: Int): Usuario {
        require(cantidad >= 0) { "No se pueden sumar puntos negativos (RF-047)" }
        return copy(puntosHistoricos = puntosHistoricos + cantidad)
    }

    /** RF-038/RF-039: recalcula la racha de días consecutivos con actividad aprobada. */
    fun registrarActividadAprobadaHoy(hoy: LocalDate): Usuario {
        val nuevaRacha = when (ultimaActividadAprobadaEn) {
            hoy -> rachaActual
            hoy.minusDays(1) -> rachaActual + 1
            else -> 1
        }
        return copy(rachaActual = nuevaRacha, ultimaActividadAprobadaEn = hoy)
    }

    /**
     * RF-039 — la racha guardada (`rachaActual`) solo se recalcula en la
     * próxima actividad aprobada ([registrarActividadAprobadaHoy]); este
     * método resuelve el valor que corresponde MOSTRAR en cualquier
     * momento: si ya pasó un día calendario completo sin actividad
     * aprobada, la racha se considera rota aunque el usuario todavía no
     * haya vuelto a completar ninguna actividad.
     */
    fun rachaVigente(hoy: LocalDate = LocalDate.now()): Int {
        val ultima = ultimaActividadAprobadaEn ?: return 0
        return if (ultima == hoy || ultima == hoy.minusDays(1)) rachaActual else 0
    }
}
