package com.ecosmart.presentation.verification

import android.Manifest
import android.content.Context
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.ecosmart.application.activity.RegistrarCaminata
import com.ecosmart.application.permission.EvaluarEstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.firebase.DispositivoIdProvider
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.SolicitudCaminarDto
import com.ecosmart.infrastructure.sensors.CaminataEnCursoStore
import com.ecosmart.infrastructure.sensors.CaminataService
import com.ecosmart.infrastructure.sensors.EstadoCaminata
import com.ecosmart.infrastructure.sensors.PodometroProvider
import com.ecosmart.infrastructure.session.SesionUsuario
import com.ecosmart.presentation.comun.EncabezadoConUsuario
import com.ecosmart.presentation.permissions.GuiaHabilitarPermisoScreen
import com.ecosmart.presentation.theme.FormaBotonPildora
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

private const val METROS_POR_PASO = 0.75
private const val META_PASOS_POR_DEFECTO = 1000
private const val MENSAJE_CAMINATA_ACTIVA_EN_OTRO_DISPOSITIVO =
    "Ya tenés una caminata en curso en otro de tus dispositivos. Esperá a que termine o a que " +
        "venza a medianoche para iniciar una nueva acá."
private const val MENSAJE_ERROR_GENERICO_INICIAR = "No pudimos iniciar la caminata. Probá de nuevo en un momento."

private data class EntradasLocales(
    val metaPasos: Int = META_PASOS_POR_DEFECTO,
    val sinPodometro: Boolean = false,
    val mensajeError: String? = null,
)

data class VerificacionCaminataUiState(
    val metaPasos: Int = META_PASOS_POR_DEFECTO,
    val sinPodometro: Boolean = false,
    val estado: EstadoCaminata = EstadoCaminata.Ninguna,
    val actividadId: String = "",
    val mensajeError: String? = null,
)

/**
 * US8 — caminata en segundo plano (RF-021 a RF-024, RF-031, RF-048, RF-049,
 * RF-082). "Realizar" arranca [CaminataService], que sigue contando pasos con
 * la app cerrada o en segundo plano y aprueba sola al llegar a la meta; esta
 * pantalla solo muestra el estado de [CaminataEnCursoStore].
 */
@HiltViewModel
class VerificacionCaminataViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val podometroProvider: PodometroProvider,
    private val store: CaminataEnCursoStore,
    private val sesionUsuario: SesionUsuario,
    private val evaluarEstadoPermiso: EvaluarEstadoPermiso,
    private val backendConfianzaClient: BackendConfianzaClient,
    private val dispositivoIdProvider: DispositivoIdProvider,
    private val registrarCaminata: RegistrarCaminata,
) : ViewModel() {

    private val actividadId = checkNotNull(savedStateHandle.get<String>("actividadId"))
    private val entradas = MutableStateFlow(EntradasLocales())

    val uiState: StateFlow<VerificacionCaminataUiState> = combine(store.estado, entradas) { estado, locales ->
        VerificacionCaminataUiState(locales.metaPasos, locales.sinPodometro, estado, actividadId, locales.mensajeError)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VerificacionCaminataUiState(actividadId = actividadId))

    init {
        store.descartarSiVencio()
        // RF-D015/RF-079: empieza a despertar el backend compartido apenas se abre esta pantalla.
        viewModelScope.launch { registrarCaminata.precalentarBackend() }
    }

    fun ajustarMeta(nuevaMeta: Int) {
        entradas.value = entradas.value.copy(metaPasos = nuevaMeta)
    }

    /** Corrección post-QA: sincroniza en Room el resultado del diálogo de permiso de Podómetro. */
    fun registrarResultadoPermisoPodometro(concedido: Boolean) {
        viewModelScope.launch { evaluarEstadoPermiso(TipoPermiso.PODOMETRO, concedido) }
    }

    /**
     * RF-021/RF-082 — al presionar "Realizar" se avisa primero al backend de confianza
     * (RF-D008: arbitra `caminataEnCurso` entre dispositivos de la misma cuenta) antes de
     * arrancar el seguimiento local; si ya hay una caminata activa en otro dispositivo, el
     * backend responde `409` y se lo comunicamos sin tono punitivo (Principio IX).
     */
    fun iniciarCaminata() {
        if (!podometroProvider.hayPodometro()) {
            entradas.value = entradas.value.copy(sinPodometro = true)
            return
        }
        val usuarioId = sesionUsuario.usuarioActualId.value?.valor ?: return
        val meta = entradas.value.metaPasos
        if (meta <= 0) return
        entradas.value = entradas.value.copy(mensajeError = null)
        viewModelScope.launch {
            try {
                backendConfianzaClient.iniciarOActualizarCaminata(
                    SolicitudCaminarDto(
                        accion = "INICIAR",
                        actividadId = actividadId,
                        metaPasos = meta,
                        dispositivoId = dispositivoIdProvider.dispositivoId,
                    ),
                )
                if (store.iniciar(usuarioId, actividadId, meta)) CaminataService.iniciar(context)
            } catch (error: HttpException) {
                val mensaje = if (error.code() == 409) {
                    MENSAJE_CAMINATA_ACTIVA_EN_OTRO_DISPOSITIVO
                } else {
                    MENSAJE_ERROR_GENERICO_INICIAR
                }
                entradas.value = entradas.value.copy(mensajeError = mensaje)
            }
        }
    }

    /** Cierra el resultado ya mostrado ("Aprobado") para poder iniciar otra caminata. */
    fun aceptarResultado() = store.limpiar()
}

/**
 * US8 — pantalla de caminata. Pide `ACTIVITY_RECOGNITION` (obligatorio desde
 * API 29 para `TYPE_STEP_COUNTER`, research.md §1.1) y, desde API 33, el
 * permiso de notificaciones para mostrar el avance (su denegación no
 * bloquea la caminata).
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

    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { resultados ->
        val concedido = !permisoPodometroRequerido || resultados[Manifest.permission.ACTIVITY_RECOGNITION] == true
        permisoPodometroOtorgado = concedido
        permisoPodometroDenegado = !concedido
        viewModel.registrarResultadoPermisoPodometro(concedido)
        if (concedido) viewModel.iniciarCaminata()
    }

    if (permisoPodometroDenegado) {
        GuiaHabilitarPermisoScreen(tipoPermiso = TipoPermiso.PODOMETRO, onEntendido = onVolver)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoConUsuario(onVolver = onVolver) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (val estado = uiState.estado) {
                EstadoCaminata.Ninguna -> {
                    Text(text = "Meta: ${uiState.metaPasos} pasos", style = MaterialTheme.typography.titleLarge)
                    if (uiState.sinPodometro) {
                        Text("Este dispositivo no tiene podómetro, así que no podemos contar tus pasos.")
                    }
                    uiState.mensajeError?.let { mensajeError ->
                        Text(mensajeError, color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedTextField(
                        value = uiState.metaPasos.toString(),
                        onValueChange = { texto -> texto.toIntOrNull()?.let(viewModel::ajustarMeta) },
                        label = { Text("Meta de pasos") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        shape = FormaBotonPildora,
                        enabled = uiState.metaPasos > 0,
                        onClick = {
                            if (permisoPodometroOtorgado) {
                                viewModel.iniciarCaminata()
                            } else {
                                val pedir = buildList {
                                    if (permisoPodometroRequerido) add(Manifest.permission.ACTIVITY_RECOGNITION)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                lanzadorPermisos.launch(pedir.toTypedArray())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Realizar")
                    }
                    TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                is EstadoCaminata.EnCurso -> {
                    if (estado.actividadId != uiState.actividadId) {
                        Text("Ya tenés otra caminata en curso; se muestra su avance.")
                    }
                    val metros = (estado.pasosLogrados * METROS_POR_PASO).toInt()
                    Text("Llevás ${estado.pasosLogrados} de ${estado.metaPasos} pasos (~$metros m)", style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(
                        progress = { (estado.pasosLogrados.toFloat() / estado.metaPasos).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("La caminata sigue en segundo plano: podés salir de esta pantalla o de la app. Se completa sola al llegar a la meta y, si no llegás hoy, se descarta a medianoche.")
                    TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                is EstadoCaminata.Completada -> {
                    Text("¡Aprobado! Sumaste ${estado.puntosOtorgados} puntos.", style = MaterialTheme.typography.titleLarge)
                    Button(
                        shape = FormaBotonPildora,
                        onClick = {
                            viewModel.aceptarResultado()
                            onVerProgreso()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Ver progreso") }
                    TextButton(
                        onClick = {
                            viewModel.aceptarResultado()
                            onVolver()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Atrás") }
                }
            }
        }
    }
}
