package com.ecosmart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.profile.CalcularMetricasPerfil
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
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
    val usuario: Usuario? = null,
    val puntosHistoricos: Int = 0,
    val nivel: NivelUsuario = NivelUsuario.SEMILLA,
    val rachaActual: Int = 0,
    val porcentajePorCategoria: Map<CategoriaActividad, Double> = emptyMap(),
    val pasosHoy: Int? = null,
)

/**
 * US11 + US3 — perfil consolidado (RF-068, corrección post-QA): datos de
 * cuenta completos (nombre, apellido, nombre de usuario, correo, barrio,
 * teléfono, categorías de interés — RF-007) más las métricas de actividad
 * (puntos, % por categoría, racha, nivel — RF-036 a RF-040). La edición en
 * sí vive en una pantalla separada (`PerfilEdicionScreen`), a la que esta
 * pantalla solo enlaza con un botón "Editar perfil".
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val calcularMetricasPerfil: CalcularMetricasPerfil,
    private val sesionUsuario: SesionUsuario,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuarioId = sesionUsuario.usuarioActualId.value ?: return@launch
            val usuario = usuarioRepository.buscarPorId(usuarioId) ?: return@launch
            val metricas = calcularMetricasPerfil(usuarioId)
            _uiState.value = PerfilUiState(
                cargando = false,
                usuario = usuario,
                puntosHistoricos = metricas?.puntosHistoricos ?: 0,
                nivel = metricas?.nivel ?: usuario.nivel(),
                rachaActual = metricas?.rachaActual ?: 0,
                porcentajePorCategoria = metricas?.porcentajePorCategoria ?: emptyMap(),
                pasosHoy = metricas?.pasosHoy,
            )
        }
    }

    /** Corrección post-QA: con la sesión persistida (RF-010), hace falta un botón explícito para salir. */
    fun cerrarSesion() {
        sesionUsuario.cerrarSesion()
    }
}
