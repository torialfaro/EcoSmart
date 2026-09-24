package com.ecosmart.presentation.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.presentation.theme.CremaTexto
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.TarjetaEcoSmart
import com.ecosmart.presentation.theme.VerdeOscuro

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoEcoSmart() },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Necesitamos algunos permisos", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Para verificar tus actividades automáticamente, EcoSmart necesita acceso " +
                    "a la cámara, la galería, tu ubicación y el podómetro del teléfono.",
                style = MaterialTheme.typography.bodyMedium,
            )

            uiState.permisos.forEach { (tipo, estado) ->
                TarjetaEcoSmart {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(VerdeOscuro),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(iconoPermiso(tipo), contentDescription = null, tint = CremaTexto, modifier = Modifier.size(24.dp))
                        }
                        Text(text = etiqueta(tipo), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text(text = etiquetaEstado(estado), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = { lanzadorPermisos.launch(permisosAndroidPorTipo.map { it.second }.toTypedArray()) },
                shape = FormaBotonPildora,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Dar permisos")
            }

            TextButton(onClick = viewModel::continuar, modifier = Modifier.fillMaxWidth()) {
                Text("Continuar", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun iconoPermiso(tipo: TipoPermiso): ImageVector = when (tipo) {
    TipoPermiso.CAMARA -> ImageVector.vectorResource(R.drawable.ic_camara)
    TipoPermiso.GALERIA -> ImageVector.vectorResource(R.drawable.ic_galeria)
    TipoPermiso.GPS -> Icons.Filled.Place
    TipoPermiso.PODOMETRO -> ImageVector.vectorResource(R.drawable.ic_caminar)
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
