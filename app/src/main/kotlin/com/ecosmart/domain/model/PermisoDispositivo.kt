package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso

/**
 * Entidad de dominio que representa el estado de un permiso del
 * dispositivo (data-model.md §2.5, US13).
 */
data class PermisoDispositivo(
    val tipo: TipoPermiso,
    val estado: EstadoPermiso,
) {
    /**
     * RF-044/RF-045 — un formulario que requiere [tipoRequerido] queda
     * bloqueado si este permiso es el que corresponde y no está otorgado.
     * Un permiso de otro tipo nunca bloquea (US13 AC3: denegar Podómetro no
     * debe afectar una actividad que solo depende de Cámara).
     */
    fun bloqueaFormulario(tipoRequerido: TipoPermiso): Boolean =
        tipo == tipoRequerido && estado != EstadoPermiso.OTORGADO
}
