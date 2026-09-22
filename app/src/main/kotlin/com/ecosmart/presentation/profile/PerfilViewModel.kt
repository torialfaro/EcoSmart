package com.ecosmart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.profile.CalcularMetricasPerfil
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilUiState(
    val cargando: Boolean = true,
    val puntosHistoricos: Int = 0,
    val nivel: NivelUsuario = NivelUsuario.SEMILLA,
    val rachaActual: Int = 0,
    val porcentajePorCategoria: Map<CategoriaActividad, Double> = emptyMap(),
)

/** US11 — métricas del perfil: puntos, % por categoría, racha y nivel (RF-036 a RF-040). */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val calcularMetricasPerfil: CalcularMetricasPerfil,
    private val sesionUsuario: SesionUsuario,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuarioId = sesionUsuario.usuarioActualId.value ?: return@launch
            val metricas = calcularMetricasPerfil(usuarioId) ?: return@launch
            _uiState.value = PerfilUiState(
                cargando = false,
                puntosHistoricos = metricas.puntosHistoricos,
                nivel = metricas.nivel,
                rachaActual = metricas.rachaActual,
                porcentajePorCategoria = metricas.porcentajePorCategoria,
            )
        }
    }

    /** Corrección post-QA: con la sesión persistida (RF-010), hace falta un botón explícito para salir. */
    fun cerrarSesion() {
        sesionUsuario.cerrarSesion()
    }
}
