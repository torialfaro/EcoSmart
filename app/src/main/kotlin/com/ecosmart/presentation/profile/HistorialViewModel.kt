package com.ecosmart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.profile.ObtenerHistorial
import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistorialUiState(
    val cargando: Boolean = true,
    val recientes: List<RegistroVerificacion> = emptyList(),
    val completo: List<RegistroVerificacion> = emptyList(),
    val mostrandoCompleto: Boolean = false,
)

/** US12 — historial: 3 recientes por defecto, listado completo bajo demanda (RF-041/RF-042). */
@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val obtenerHistorial: ObtenerHistorial,
    private val sesionUsuario: SesionUsuario,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        refrescar()
    }

    /**
     * RF-D017 (spec 002-firestore-datos-usuario): sin listener en tiempo real de
     * Firestore, el historial se recarga bajo demanda al volver a primer plano
     * (`HistorialScreen`, ON_RESUME) — mismo patrón que `PerfilViewModel.refrescar()`
     * ya usaba para RF-077 de spec 001.
     */
    fun refrescar() {
        viewModelScope.launch {
            val usuarioId = sesionUsuario.usuarioActualId.value ?: return@launch
            val mostrandoCompleto = _uiState.value.mostrandoCompleto
            _uiState.value = _uiState.value.copy(
                cargando = false,
                recientes = obtenerHistorial.recientes(usuarioId),
                completo = if (mostrandoCompleto) obtenerHistorial.completo(usuarioId) else _uiState.value.completo,
            )
        }
    }

    /** RF-042 — despliega el listado completo. */
    fun verMas() {
        val usuarioId = sesionUsuario.usuarioActualId.value ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                mostrandoCompleto = true,
                completo = obtenerHistorial.completo(usuarioId),
            )
        }
    }
}
