package com.ecosmart.presentation.verification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.activity.DatosCaminata
import com.ecosmart.application.activity.RegistrarCaminata
import com.ecosmart.application.activity.ResultadoCaminata
import com.ecosmart.application.permission.EvaluarEstadoPermiso
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.sensors.PodometroProvider
import com.ecosmart.infrastructure.session.SesionUsuario
import com.ecosmart.presentation.permissions.GuiaHabilitarPermisoScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val METROS_POR_PASO = 0.75
private const val META_PASOS_POR_DEFECTO = 1000

sealed class ResultadoUiCaminata {
    data class Aprobada(val puntosOtorgados: Int, val pasosCaminados: Int) : ResultadoUiCaminata()
    data class MetaNoAlcanzada(val pasosCaminados: Int, val metaPasos: Int) : ResultadoUiCaminata()
}

data class VerificacionCaminataUiState(
    val metaPasos: Int = META_PASOS_POR_DEFECTO,
    val enCurso: Boolean = false,
    val pasosCaminados: Int = 0,
    val resultado: ResultadoUiCaminata? = null,
)

/** US8 — verificación de caminata vía podómetro (RF-021 a RF-024, RF-031, RF-048, RF-049). */
@HiltViewModel
class VerificacionCaminataViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val podometroProvider: PodometroProvider,
    private val registrarCaminata: RegistrarCaminata,
    private val sesionUsuario: SesionUsuario,
    private val evaluarEstadoPermiso: EvaluarEstadoPermiso,
) : ViewModel() {

    private val actividadId = ActividadId(checkNotNull(savedStateHandle.get<String>("actividadId")))
    private val _uiState = MutableStateFlow(VerificacionCaminataUiState())
    val uiState: StateFlow<VerificacionCaminataUiState> = _uiState.asStateFlow()

    private var pasosBase: Int? = null
    private var observacionJob: Job? = null

    fun ajustarMeta(nuevaMeta: Int) {
        _uiState.value = _uiState.value.copy(metaPasos = nuevaMeta)
    }

    /** Corrección post-QA: sincroniza en Room el resultado del diálogo de permiso de Podómetro. */
    fun registrarResultadoPermisoPodometro(concedido: Boolean) {
        viewModelScope.launch { evaluarEstadoPermiso(TipoPermiso.PODOMETRO, concedido) }
    }

    /** RF-021 — al presionar "Realizar" empieza a comparar el progreso del podómetro contra la meta. */
    fun iniciarCaminata() {
        _uiState.value = _uiState.value.copy(enCurso = true, resultado = null, pasosCaminados = 0)
        pasosBase = null
        observacionJob = viewModelScope.launch {
            podometroProvider.observarPasosAcumulados().collect { valorAcumulado ->
                val base = pasosBase ?: valorAcumulado.also { pasosBase = it }
                val pasosCaminados = (valorAcumulado - base).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(pasosCaminados = pasosCaminados)
            }
        }
    }

    fun finalizarCaminata() {
        observacionJob?.cancel()
        _uiState.value = _uiState.value.copy(enCurso = false)
        val usuarioId = sesionUsuario.usuarioActualId.value ?: return
        val base = pasosBase ?: return
        val pasosCaminadosActuales = _uiState.value.pasosCaminados
        viewModelScope.launch {
            val resultado = registrarCaminata(
                DatosCaminata(
                    usuarioId = usuarioId,
                    actividadId = actividadId,
                    metaPasos = _uiState.value.metaPasos,
                    pasosBase = base,
                    pasosActuales = base + pasosCaminadosActuales,
                ),
            )
            _uiState.value = _uiState.value.copy(
                resultado = when (resultado) {
                    is ResultadoCaminata.Aprobada ->
                        ResultadoUiCaminata.Aprobada(resultado.registro.puntosOtorgados, resultado.pasosCaminados)
                    is ResultadoCaminata.MetaNoAlcanzada ->
                        ResultadoUiCaminata.MetaNoAlcanzada(resultado.pasosCaminados, resultado.metaPasos)
                },
            )
        }
    }
}

/**
 * US8 — pantalla de verificación de caminata.
 *
 * Corrección post-QA: el mismo tipo de riesgo que en `VerificacionFotoScreen`
 * (usar un sensor/permiso sin chequearlo antes) se corrige acá también,
 * pidiendo `ACTIVITY_RECOGNITION` (obligatorio desde API 29 para
 * `TYPE_STEP_COUNTER`, research.md §1.1) antes de arrancar a escuchar el
 * podómetro.
 */
@Composable
fun VerificacionCaminataScreen(
    viewModel: VerificacionCaminataViewModel = hiltViewModel(),
    onVerProgreso: () -> Unit,
    onVolver: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val permisoPodometroRequerido = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    var permisoPodometroOtorgado by remember {
        mutableStateOf(
            !permisoPodometroRequerido ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permisoPodometroDenegado by remember { mutableStateOf(false) }

    val lanzadorPermisoPodometro = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        permisoPodometroOtorgado = concedido
        permisoPodometroDenegado = !concedido
        viewModel.registrarResultadoPermisoPodometro(concedido)
        if (concedido) viewModel.iniciarCaminata()
    }

    if (permisoPodometroDenegado) {
        GuiaHabilitarPermisoScreen(tipoPermiso = TipoPermiso.PODOMETRO, onEntendido = onVolver)
        return
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (val resultado = uiState.resultado) {
                null -> {
                    Text(text = "Meta: ${uiState.metaPasos} pasos", style = MaterialTheme.typography.titleLarge)
                    if (!uiState.enCurso) {
                        OutlinedTextField(
                            value = uiState.metaPasos.toString(),
                            onValueChange = { texto -> texto.toIntOrNull()?.let(viewModel::ajustarMeta) },
                            label = { Text("Meta de pasos") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                if (permisoPodometroOtorgado) {
                                    viewModel.iniciarCaminata()
                                } else {
                                    lanzadorPermisoPodometro.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Realizar")
                        }
                    } else {
                        val metros = (uiState.pasosCaminados * METROS_POR_PASO).toInt()
                        Text("Llevás ${uiState.pasosCaminados} pasos (~$metros m)")
                        Button(onClick = viewModel::finalizarCaminata, modifier = Modifier.fillMaxWidth()) {
                            Text("Terminar")
                        }
                    }
                }
                is ResultadoUiCaminata.Aprobada -> {
                    Text("¡Aprobado! Sumaste ${resultado.puntosOtorgados} puntos.", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = onVerProgreso, modifier = Modifier.fillMaxWidth()) { Text("Ver progreso") }
                    TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                is ResultadoUiCaminata.MetaNoAlcanzada -> {
                    Text(
                        "Todavía no llegaste a la meta (${resultado.pasosCaminados}/${resultado.metaPasos} pasos). " +
                            "Podés reintentar más tarde.",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
            }
        }
    }
}
