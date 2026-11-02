package com.ecosmart.presentation.activitydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.repository.PasosDelDiaRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.presentation.education.CatalogoLecciones
import com.ecosmart.presentation.estadodiario.CargadorEstadoDiario
import com.ecosmart.presentation.estadodiario.EstadoActividadUi
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
    /** Mientras carga, o si no se pudo leer el día, la actividad se ofrece como para hacer (RF-IA-019). */
    val estado: EstadoActividadUi = EstadoActividadUi.PARA_HACER,
    val pasos: List<String> = emptyList(),
    /** Resultado esperado de la actividad; `null` si está vacío (la tarjeta del tip no se muestra). */
    val tip: String? = null,
    /** Primera lección de la categoría según el catálogo; `null` oculta "Aprendé primero" (RF-IA-017). */
    val leccionRelacionadaId: String? = null,
)

/** US5 — pasos a seguir y resultado esperado de una actividad (RF-014/RF-015) y su estado del día (spec 005). */
@HiltViewModel
class ActividadDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val actividadRepository: ActividadRepository,
    private val pasosDelDiaRepository: PasosDelDiaRepository,
    private val cargadorEstadoDiario: CargadorEstadoDiario,
    private val catalogoLecciones: CatalogoLecciones,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActividadDetalleUiState())
    val uiState: StateFlow<ActividadDetalleUiState> = _uiState.asStateFlow()
    private val actividadId: ActividadId

    init {
        val actividadIdArg = checkNotNull(savedStateHandle.get<String>("actividadId")) {
            "ActividadDetalleScreen requiere el argumento de ruta actividadId"
        }
        actividadId = ActividadId(actividadIdArg)
        refrescar()
    }

    /** Se vuelve a llamar al volver a primer plano: tras verificar la actividad, su estado del día cambia. */
    fun refrescar() {
        viewModelScope.launch {
            val actividad = actividadRepository.buscarPorId(actividadId)
            // Corrección post-QA: el progreso de Caminar sale únicamente del podómetro real, nunca hardcodeado.
            val pasosHoy = pasosDelDiaRepository.pasosDeHoy() ?: 0
            val estadoDiario = actividad?.let { cargadorEstadoDiario.cargar(listOf(it)) }
            _uiState.value = ActividadDetalleUiState(
                actividad = actividad,
                cargando = false,
                pasosHoy = pasosHoy,
                estado = estadoDiario?.estadoPorActividad?.get(actividadId) ?: EstadoActividadUi.PARA_HACER,
                pasos = actividad?.let { PasosActividadParser.parsear(it.pasosASeguir) }.orEmpty(),
                tip = actividad?.resultadoEsperado?.takeIf { it.isNotBlank() },
                leccionRelacionadaId = actividad?.let { leccionRelacionadaId(catalogoLecciones.obtener(), it.categoria) },
            )
        }
    }
}
