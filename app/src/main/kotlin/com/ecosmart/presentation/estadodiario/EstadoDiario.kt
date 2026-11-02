package com.ecosmart.presentation.estadodiario

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.model.EstrategiaDePuntaje
import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.ordenSeccionesEducacion
import java.time.LocalDate

/** Estado de una actividad para hoy, derivado de los registros aprobados (contrato `estado-actividad.md`). */
enum class EstadoActividadUi { PARA_HACER, COMPLETADA_HOY, NO_DISPONIBLE_HOY }

/** `hecha`: al menos una verificación aprobada hoy. `topeAlcanzado`: ya se llegó al tope diario real. */
data class EstadoCategoriaUi(
    val categoria: CategoriaActividad,
    val hecha: Boolean,
    val topeAlcanzado: Boolean,
)

data class EstadoDiarioUi(
    val categorias: List<EstadoCategoriaUi>,
    val estadoPorActividad: Map<ActividadId, EstadoActividadUi>,
) {
    val total: Int get() = categorias.size
    val hechas: Int get() = categorias.count { it.hecha }
}

data class ActividadInicioUi(
    val actividad: Actividad,
    val estado: EstadoActividadUi,
)

/** Todas las categorías del dominio: primero el orden fijo de la UI y luego las que se agreguen en el futuro. */
private fun categoriasEnOrden(): List<CategoriaActividad> =
    ordenSeccionesEducacion + (CategoriaActividad.entries - ordenSeccionesEducacion.toSet())

/**
 * Deriva el estado diario sin reglas propias: los topes se leen de [EstrategiaDePuntaje] y solo cuentan
 * los registros aprobados de [hoy] (los mismos que consumen el tope en `AplicarTopeDiario`).
 */
fun calcularEstadoDiario(
    actividades: List<Actividad>,
    registros: List<RegistroVerificacion>,
    hoy: LocalDate,
): EstadoDiarioUi {
    val aprobadosHoy = registros.filter { it.fecha == hoy && it.consumeTopeDiario() }
    val categorias = categoriasEnOrden().map { categoria ->
        val aprobadas = aprobadosHoy.count { it.categoria == categoria }
        val tope = EstrategiaDePuntaje.para(categoria).topeDiario
        EstadoCategoriaUi(
            categoria = categoria,
            hecha = aprobadas >= 1,
            topeAlcanzado = tope != null && aprobadas >= tope,
        )
    }
    val completadas = aprobadosHoy.map { it.actividadId }.toSet()
    val categoriasConTope = categorias.filter { it.topeAlcanzado }.map { it.categoria }.toSet()
    val estadoPorActividad = actividades.associate { actividad ->
        actividad.id to when {
            actividad.id in completadas -> EstadoActividadUi.COMPLETADA_HOY
            actividad.categoria in categoriasConTope -> EstadoActividadUi.NO_DISPONIBLE_HOY
            else -> EstadoActividadUi.PARA_HACER
        }
    }
    return EstadoDiarioUi(categorias = categorias, estadoPorActividad = estadoPorActividad)
}

/** Una tarjeta por categoría (RF-IA-006a): primera para hacer, si no la primera completada, si no la primera. */
fun seleccionarActividadesDeHoy(
    actividades: List<Actividad>,
    estado: EstadoDiarioUi,
): List<ActividadInicioUi> = estado.categorias.mapNotNull { categoriaEstado ->
    val candidatas = actividades
        .filter { it.categoria == categoriaEstado.categoria }
        .map { ActividadInicioUi(it, estado.estadoPorActividad[it.id] ?: EstadoActividadUi.PARA_HACER) }
    candidatas.firstOrNull { it.estado == EstadoActividadUi.PARA_HACER }
        ?: candidatas.firstOrNull { it.estado == EstadoActividadUi.COMPLETADA_HOY }
        ?: candidatas.firstOrNull()
}
