package com.ecosmart.presentation.verification

import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.activity.DatosVerificacionFoto
import com.ecosmart.application.activity.ResultadoVerificarFotoConIA
import com.ecosmart.application.activity.VerificarFotoConIA
import com.ecosmart.application.permission.EvaluarEstadoPermiso
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.sensors.CamaraProvider
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ResultadoUiFoto {
    data class Aprobado(val puntosOtorgados: Int) : ResultadoUiFoto()
    data class Rechazado(val motivo: String) : ResultadoUiFoto()
    data class Indeterminado(val motivo: String) : ResultadoUiFoto()
    data object TopeDiarioAlcanzado : ResultadoUiFoto()
    data object DuplicadaLocalmente : ResultadoUiFoto()
    data object Timeout : ResultadoUiFoto()
    data object SinConexion : ResultadoUiFoto()
    data class Error(val mensaje: String) : ResultadoUiFoto()
}

data class VerificacionFotoUiState(
    val descripcion: String = "",
    val archivoFoto: File? = null,
    val enviando: Boolean = false,
    val resultado: ResultadoUiFoto? = null,
)

/** US9 — formulario y envío de la verificación de foto a EcoGPT (RF-025 a RF-030). */
@HiltViewModel
class VerificacionFotoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val actividadRepository: ActividadRepository,
    private val verificarFotoConIA: VerificarFotoConIA,
    private val camaraProvider: CamaraProvider,
    private val sesionUsuario: SesionUsuario,
    private val evaluarEstadoPermiso: EvaluarEstadoPermiso,
) : ViewModel() {

    private val actividadId = ActividadId(checkNotNull(savedStateHandle.get<String>("actividadId")))
    private val _uiState = MutableStateFlow(VerificacionFotoUiState())
    val uiState: StateFlow<VerificacionFotoUiState> = _uiState.asStateFlow()

    fun actualizarDescripcion(texto: String) {
        _uiState.value = _uiState.value.copy(descripcion = texto)
    }

    /**
     * Corrección post-QA: sincroniza en Room el resultado del diálogo de
     * permiso de Cámara que la Screen pide al entrar (RF-044/RF-045). El
     * chequeo que efectivamente bloquea la vista de cámara usa el estado
     * real del sistema operativo (`ContextCompat.checkSelfPermission` en la
     * Screen), no este caché; esto solo mantiene consistente el resto del
     * Módulo 2 (p. ej. `SolicitudPermisosScreen`) con lo que pasó acá.
     */
    fun registrarResultadoPermisoCamara(concedido: Boolean) {
        viewModelScope.launch { evaluarEstadoPermiso(TipoPermiso.CAMARA, concedido) }
    }

    /** RF-025 — captura con CameraX; el binding de la preview vive en la Screen. */
    fun capturarFoto(imageCapture: ImageCapture) {
        viewModelScope.launch {
            val archivo = camaraProvider.crearArchivoTemporalParaFoto()
            try {
                camaraProvider.capturarFoto(imageCapture, archivo)
                _uiState.value = _uiState.value.copy(archivoFoto = archivo)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    resultado = ResultadoUiFoto.Error("No pudimos tomar la foto. Probá de nuevo."),
                )
            }
        }
    }

    /** RF-025 — selección desde la Galería (Photo Picker). */
    fun adjuntarFotoDesdeGaleria(uri: Uri) {
        viewModelScope.launch {
            val archivo = camaraProvider.copiarDesdeGaleria(uri)
            _uiState.value = _uiState.value.copy(archivoFoto = archivo)
        }
    }

    /**
     * "Volver a intentar": conserva foto/descripción ante fallas técnicas
     * (Timeout/sin conexión/error — Edge Cases correspondientes), pero
     * limpia el formulario ante un Rechazado/Indeterminado/Duplicada, donde
     * RF-030 pide "reingresar foto/descripción".
     */
    fun reintentar() {
        val conservarDatos = when (_uiState.value.resultado) {
            ResultadoUiFoto.Timeout, ResultadoUiFoto.SinConexion, is ResultadoUiFoto.Error -> true
            else -> false
        }
        _uiState.value = if (conservarDatos) {
            _uiState.value.copy(resultado = null, enviando = false)
        } else {
            VerificacionFotoUiState()
        }
    }

    /** RF-026 — exige foto + descripción antes de habilitar el envío (deshabilitado en la Screen). */
    fun enviar() {
        val usuarioId = sesionUsuario.usuarioActualId.value ?: return
        val archivo = _uiState.value.archivoFoto ?: return
        _uiState.value = _uiState.value.copy(enviando = true)
        viewModelScope.launch {
            val actividad = actividadRepository.buscarPorId(actividadId) ?: return@launch
            val resultado = verificarFotoConIA(
                DatosVerificacionFoto(
                    usuarioId = usuarioId,
                    actividadId = actividadId,
                    categoria = actividad.categoria,
                    resultadoEsperado = actividad.resultadoEsperado,
                    descripcionUsuario = _uiState.value.descripcion,
                    archivoFoto = archivo,
                ),
            )
            _uiState.value = _uiState.value.copy(enviando = false, resultado = resultado.aUiState())
        }
    }
}

private fun ResultadoVerificarFotoConIA.aUiState(): ResultadoUiFoto = when (this) {
    is ResultadoVerificarFotoConIA.Completado -> when (registro.resultado) {
        ResultadoVerificacion.APROBADO -> ResultadoUiFoto.Aprobado(registro.puntosOtorgados)
        ResultadoVerificacion.RECHAZADO -> ResultadoUiFoto.Rechazado(registro.motivoIA ?: "")
        ResultadoVerificacion.INDETERMINADO -> ResultadoUiFoto.Indeterminado(registro.motivoIA ?: "")
    }
    ResultadoVerificarFotoConIA.TopeDiarioAlcanzado -> ResultadoUiFoto.TopeDiarioAlcanzado
    ResultadoVerificarFotoConIA.DuplicadaLocalmente -> ResultadoUiFoto.DuplicadaLocalmente
    ResultadoVerificarFotoConIA.Timeout -> ResultadoUiFoto.Timeout
    ResultadoVerificarFotoConIA.SinConexion -> ResultadoUiFoto.SinConexion
    is ResultadoVerificarFotoConIA.ErrorEcoGpt -> ResultadoUiFoto.Error(mensaje)
}
