package com.ecosmart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.profile.ObtenerHistorial
import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.valueobject.ActividadId
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
    val titulosActividad: Map<String, String> = emptyMap(),
)

/** US12 — historial: 3 recientes por defecto, listado completo bajo demanda (RF-041/RF-042). */
@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val obtenerHistorial: ObtenerHistorial,
    private val sesionUsuario: SesionUsuario,
    private val actividadRepository: ActividadRepository,
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
            val recientes = obtenerHistorial.recientes(usuarioId)
            val completo = if (mostrandoCompleto) obtenerHistorial.completo(usuarioId) else _uiState.value.completo
            _uiState.value = _uiState.value.copy(
                cargando = false,
                recientes = recientes,
                completo = completo,
                titulosActividad = resolverTitulos(recientes + completo),
            )
        }
    }

    /** RF-042 — despliega el listado completo. */
    fun verMas() {
        val usuarioId = sesionUsuario.usuarioActualId.value ?: return
        viewModelScope.launch {
            val completo = obtenerHistorial.completo(usuarioId)
            _uiState.value = _uiState.value.copy(
                mostrandoCompleto = true,
                completo = completo,
                titulosActividad = resolverTitulos(_uiState.value.recientes + completo),
            )
        }
    }

    /**
     * Resuelve el nombre real de la actividad de cada registro (en vez de mostrar su
     * categoría como título) consultando el catálogo local de Actividades. Si una
     * actividad ya no existe en el catálogo, queda sin entrada acá y la pantalla cae al
     * nombre de la categoría como respaldo. Conserva lo ya resuelto entre refrescos para
     * no volver a consultar el catálogo innecesariamente (es estático, research.md §0).
     */
    private suspend fun resolverTitulos(registros: List<RegistroVerificacion>): Map<String, String> {
        val existentes = _uiState.value.titulosActividad
        val faltantes = registros.map { it.actividadId }.distinct().filterNot { existentes.containsKey(it.valor) }
        if (faltantes.isEmpty()) return existentes
        val nuevos = faltantes.mapNotNull { id: ActividadId -> actividadRepository.buscarPorId(id)?.let { id.valor to it.descripcionCorta } }
        return existentes + nuevos
    }
}
