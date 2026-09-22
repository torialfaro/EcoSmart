package com.ecosmart.presentation.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ecosmart.domain.valueobject.TipoPermiso

/**
 * Alerta explicativa con pasos para habilitar un permiso denegado desde la
 * configuración del dispositivo (US13 AC2, RF-045). Tono amable y orientado
 * a la acción, nunca punitivo (RNF-001).
 */
@Composable
fun GuiaHabilitarPermisoScreen(
    tipoPermiso: TipoPermiso,
    onEntendido: () -> Unit,
) {
    val context = LocalContext.current
    val nombre = nombreAmigable(tipoPermiso)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Necesitamos un permiso más", style = MaterialTheme.typography.titleLarge)
            Text(text = "$nombre está desactivado. Para poder usar esta función, habilitalo desde la configuración de la app:")
            Text(text = "1. Tocá \"Abrir configuración\" abajo.")
            Text(text = "2. Entrá a \"Permisos\".")
            Text(text = "3. Activá $nombre.")

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Abrir configuración")
            }

            TextButton(onClick = onEntendido, modifier = Modifier.fillMaxWidth()) {
                Text("Entendido")
            }
        }
    }
}

private fun nombreAmigable(tipo: TipoPermiso): String = when (tipo) {
    TipoPermiso.GALERIA -> "el acceso a tu galería"
    TipoPermiso.CAMARA -> "el acceso a la cámara"
    TipoPermiso.GPS -> "tu ubicación"
    TipoPermiso.PODOMETRO -> "el contador de pasos"
}
