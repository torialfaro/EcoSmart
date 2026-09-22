package com.ecosmart.application.activity

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.valueobject.CategoriaActividad
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * US4 — actividades filtradas por las categorías de interés del usuario
 * (RF-011). El agrupamiento en secciones por categoría (RF-012) es una
 * decisión de presentación (`HomeViewModel` agrupa el resultado), no una
 * regla de negocio adicional.
 */
class ObtenerActividadesFiltradas @Inject constructor(
    private val actividadRepository: ActividadRepository,
) {
    operator fun invoke(categoriasDeInteres: Set<CategoriaActividad>): Flow<List<Actividad>> =
        actividadRepository.observarPorCategorias(categoriasDeInteres)
}
