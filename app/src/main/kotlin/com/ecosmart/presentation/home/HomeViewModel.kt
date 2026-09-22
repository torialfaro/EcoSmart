package com.ecosmart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.activity.ObtenerActividadesFiltradas
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val actividadesPorCategoria: Map<CategoriaActividad, List<Actividad>> = emptyMap(),
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
)

/** US4 — Home filtrada por categorías de interés, agrupada por sección (RF-011/RF-012). */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val actividadRepository: ActividadRepository,
    private val usuarioRepository: UsuarioRepository,
    private val obtenerActividadesFiltradas: ObtenerActividadesFiltradas,
    private val sesionUsuario: SesionUsuario,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            actividadRepository.sembrarCatalogoSiEstaVacio()
            val usuarioId = sesionUsuario.usuarioActualId.value ?: return@launch
            val usuario = usuarioRepository.buscarPorId(usuarioId) ?: return@launch
            obtenerActividadesFiltradas(usuario.categoriasDeInteres).collect { actividades ->
                _uiState.value = HomeUiState(
                    // RF-012: la Home agrupa en secciones separadas por categoría.
                    actividadesPorCategoria = actividades.groupBy { it.categoria },
                    cargando = false,
                    nombreUsuario = usuario.nombreUsuario,
                )
            }
        }
    }
}
