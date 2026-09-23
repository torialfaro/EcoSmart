package com.ecosmart.presentation.verification

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.sensors.SelectorGaleria
import com.ecosmart.presentation.permissions.GuiaHabilitarPermisoScreen

/**
 * US9 — formulario de foto (Cámara con preview en vivo o Galería) +
 * descripción (RF-025/RF-026).
 *
 * Corrección post-QA: antes esta pantalla intentaba abrir la cámara sin
 * chequear el permiso primero — si CAMARA no estaba otorgado,
 * `bindToLifecycle` de CameraX lanzaba `SecurityException` sin capturar y
 * la app se cerraba. Ahora se verifica el permiso real del sistema
 * operativo antes de tocar CameraX (RF-044/RF-045): si falta, se pide una
 * vez más con el diálogo del sistema y, si se rechaza, se muestra la guía
 * ya existente del Módulo 2 en vez de crashear.
 */
@Composable
fun VerificacionFotoScreen(
    viewModel: VerificacionFotoViewModel = hiltViewModel(),
    onAtras: () -> Unit,
    onVerProgreso: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val resultado = uiState.resultado
    if (resultado != null) {
        ResultadoVerificacionScreen(
            resultado = resultado,
            onVolverAIntentar = viewModel::reintentar,
            onAtras = onAtras,
            onVerProgreso = onVerProgreso,
        )
        return
    }

    var permisoCamaraOtorgado by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var yaSePidioElPermiso by remember { mutableStateOf(false) }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        permisoCamaraOtorgado = concedido
        yaSePidioElPermiso = true
        viewModel.registrarResultadoPermisoCamara(concedido)
    }

    LaunchedEffect(Unit) {
        if (!permisoCamaraOtorgado) {
            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    if (!permisoCamaraOtorgado) {
        if (yaSePidioElPermiso) {
            GuiaHabilitarPermisoScreen(tipoPermiso = TipoPermiso.CAMARA, onEntendido = onAtras)
        } else {
            // Esperando el resultado del diálogo del sistema operativo.
            Scaffold { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        return
    }

    var modoCamara by remember { mutableStateOf(true) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let(viewModel::adjuntarFotoDesdeGaleria)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { modoCamara = true }) { Text("Cámara") }
                OutlinedButton(
                    onClick = {
                        modoCamara = false
                        selectorGaleria.launch(SelectorGaleria.solicitudSoloImagenes)
                    },
                ) { Text("Galería") }
            }

            if (uiState.archivoFoto == null && modoCamara) {
                VistaCamara(imageCapture = imageCapture, modifier = Modifier.fillMaxWidth().height(300.dp))
                Button(onClick = { viewModel.capturarFoto(imageCapture) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Tomar foto")
                }
            } else if (uiState.archivoFoto != null) {
                Text("Foto lista ✓", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::actualizarDescripcion,
                label = { Text("Describí lo que hiciste") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                enabled = uiState.archivoFoto != null && uiState.descripcion.isNotBlank() && !uiState.enviando,
                onClick = viewModel::enviar,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.enviando) "Enviando…" else "Enviar")
            }
        }
    }
}

@Composable
private fun VistaCamara(imageCapture: ImageCapture, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // Corrección post-QA: el modo PERFORMANCE (default) usa un SurfaceView, que se
                // compone en una capa de hardware separada y se dibuja por encima de cualquier
                // otra vista de Compose sin respetar el orden del layout — tapaba los botones
                // "Cámara"/"Galería". COMPATIBLE usa un TextureView, que sí respeta el z-order.
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener(
                {
                    // Defensivo: aun con el permiso ya verificado en la Screen, un binding
                    // de CameraX puede fallar (dispositivo sin cámara trasera, etc.) — nunca
                    // debe crashear la app (RNF-001, tono nunca punitivo/abrupto).
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                        )
                    } catch (error: Exception) {
                        // Se deja la vista en blanco; el botón "Tomar foto" seguirá disponible
                        // pero fallará de forma controlada en VerificacionFotoViewModel.capturarFoto.
                    }
                },
                ContextCompat.getMainExecutor(ctx),
            )
            previewView
        },
    )
}
