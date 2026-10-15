package com.ecosmart.presentation.activitydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.repository.PasosDelDiaRepository
import com.ecosmart.domain.valueobject.ActividadId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActividadDetalleUiState(
    val actividad: Actividad? = null,
    val cargando: Boolean = true,
    val pasosHoy: Int = 0,
)

/** US5 — pasos a seguir y resultado esperado de una actividad (RF-014/RF-015). */
@HiltViewModel
class ActividadDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val actividadRepository: ActividadRepository,
    private val pasosDelDiaRepository: PasosDelDiaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActividadDetalleUiState())
    val uiState: StateFlow<ActividadDetalleUiState> = _uiState.asStateFlow()

    init {
        val actividadIdArg = checkNotNull(savedStateHandle.get<String>("actividadId")) {
            "ActividadDetalleScreen requiere el argumento de ruta actividadId"
        }
        viewModelScope.launch {
            val actividad = actividadRepository.buscarPorId(ActividadId(actividadIdArg))
            // Corrección post-QA: el progreso de Caminar sale únicamente del podómetro real, nunca hardcodeado.
            val pasosHoy = pasosDelDiaRepository.pasosDeHoy() ?: 0
            _uiState.value = ActividadDetalleUiState(actividad = actividad, cargando = false, pasosHoy = pasosHoy)
        }
    }
}
