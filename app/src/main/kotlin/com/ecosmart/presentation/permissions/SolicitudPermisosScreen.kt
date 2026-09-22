package com.ecosmart.presentation.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso

/** US13 — solicitud de permisos post-login (RF-043). */
@Composable
fun SolicitudPermisosScreen(
    viewModel: SolicitudPermisosViewModel = hiltViewModel(),
    onContinuar: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.completado) {
        if (uiState.completado) onContinuar()
    }

    val guiaPendiente = uiState.permisoConGuiaPendiente
    if (guiaPendiente != null) {
        GuiaHabilitarPermisoScreen(tipoPermiso = guiaPendiente, onEntendido = viewModel::descartarGuia)
        return
    }

    val permisosAndroidPorTipo = remember {
        TipoPermiso.entries.mapNotNull { tipo -> tipo.permisoAndroidRequerido()?.let { tipo to it } }
    }

    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { resultados ->
        permisosAndroidPorTipo.forEach { (tipo, permisoAndroid) ->
            viewModel.registrarResultado(tipo, resultados[permisoAndroid] == true)
        }
        val tiposSinPermisoRuntime = TipoPermiso.entries - permisosAndroidPorTipo.map { it.first }.toSet()
        tiposSinPermisoRuntime.forEach { viewModel.registrarResultado(it, concedido = true) }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Necesitamos algunos permisos", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Para verificar tus actividades automáticamente, EcoSmart necesita acceso " +
                    "a la cámara, la galería, tu ubicación y el podómetro del teléfono.",
            )

            uiState.permisos.forEach { (tipo, estado) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = etiqueta(tipo))
                    Text(text = etiquetaEstado(estado))
                }
            }

            Button(
                onClick = { lanzadorPermisos.launch(permisosAndroidPorTipo.map { it.second }.toTypedArray()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Dar permisos")
            }

            TextButton(onClick = viewModel::continuar, modifier = Modifier.fillMaxWidth()) {
                Text("Continuar")
            }
        }
    }
}

/**
 * Mapea cada [TipoPermiso] al permiso runtime de Android que corresponde
 * pedir en esta versión del SO, o `null` si esa versión no lo requiere
 * explícitamente (research.md §1): la Galería usa el Photo Picker sin
 * permiso desde API 33, y el podómetro no requiere `ACTIVITY_RECOGNITION`
 * antes de API 29.
 */
private fun TipoPermiso.permisoAndroidRequerido(): String? = when (this) {
    TipoPermiso.CAMARA -> Manifest.permission.CAMERA
    TipoPermiso.GPS -> Manifest.permission.ACCESS_FINE_LOCATION
    TipoPermiso.PODOMETRO ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACTIVITY_RECOGNITION else null
    TipoPermiso.GALERIA ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_EXTERNAL_STORAGE else null
}

private fun etiqueta(tipo: TipoPermiso): String = when (tipo) {
    TipoPermiso.GALERIA -> "Galería"
    TipoPermiso.CAMARA -> "Cámara"
    TipoPermiso.GPS -> "Ubicación"
    TipoPermiso.PODOMETRO -> "Podómetro"
}

private fun etiquetaEstado(estado: EstadoPermiso): String = when (estado) {
    EstadoPermiso.NO_SOLICITADO -> "Pendiente"
    EstadoPermiso.OTORGADO -> "Otorgado"
    EstadoPermiso.DENEGADO -> "Denegado"
}
