package com.ecosmart.presentation.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.permission.EvaluarEstadoPermiso
import com.ecosmart.domain.repository.PermisoRepository
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SolicitudPermisosUiState(
    val permisos: Map<TipoPermiso, EstadoPermiso> =
        TipoPermiso.entries.associateWith { EstadoPermiso.NO_SOLICITADO },
    val permisoConGuiaPendiente: TipoPermiso? = null,
    val completado: Boolean = false,
)

/** US13 — solicitud de permisos post-login (RF-043 a RF-045). */
@HiltViewModel
class SolicitudPermisosViewModel @Inject constructor(
    private val permisoRepository: PermisoRepository,
    private val evaluarEstadoPermiso: EvaluarEstadoPermiso,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudPermisosUiState())
    val uiState: StateFlow<SolicitudPermisosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val guardados = permisoRepository.obtenerTodos().associate { it.tipo to it.estado }
            _uiState.value = _uiState.value.copy(permisos = _uiState.value.permisos + guardados)
        }
    }

    fun registrarResultado(tipo: TipoPermiso, concedido: Boolean) {
        viewModelScope.launch {
            val actualizado = evaluarEstadoPermiso(tipo, concedido)
            _uiState.value = _uiState.value.copy(
                permisos = _uiState.value.permisos + (tipo to actualizado.estado),
                permisoConGuiaPendiente = if (!concedido) tipo else _uiState.value.permisoConGuiaPendiente,
            )
        }
    }

    fun descartarGuia() {
        _uiState.value = _uiState.value.copy(permisoConGuiaPendiente = null)
    }

    /** RF-043 solo exige pedir los permisos, no obliga a otorgarlos todos para avanzar (US13 AC3). */
    fun continuar() {
        _uiState.value = _uiState.value.copy(completado = true)
    }
}
