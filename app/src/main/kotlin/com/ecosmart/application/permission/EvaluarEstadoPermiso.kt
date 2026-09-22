package com.ecosmart.application.permission

import com.ecosmart.domain.model.PermisoDispositivo
import com.ecosmart.domain.repository.PermisoRepository
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import javax.inject.Inject

/**
 * Traduce el resultado crudo del diálogo de permisos del sistema operativo
 * a un [EstadoPermiso] de dominio y lo persiste (US13, RF-043/RF-044).
 */
class EvaluarEstadoPermiso @Inject constructor(
    private val permisoRepository: PermisoRepository,
) {
    suspend operator fun invoke(tipo: TipoPermiso, concedido: Boolean): PermisoDispositivo {
        val estado = if (concedido) EstadoPermiso.OTORGADO else EstadoPermiso.DENEGADO
        permisoRepository.guardarEstado(tipo, estado)
        return PermisoDispositivo(tipo, estado)
    }
}
