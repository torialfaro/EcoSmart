package com.ecosmart.presentation.verification

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.sensors.SelectorGaleria
import com.ecosmart.presentation.permissions.GuiaHabilitarPermisoScreen
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen100
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorGreen900
import com.ecosmart.presentation.theme.ColorLeaf
import com.ecosmart.presentation.theme.ColorTextSecondary
import com.ecosmart.presentation.theme.ColorTrack
import com.ecosmart.presentation.theme.FormaPill
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeChip
import com.ecosmart.presentation.theme.TypeMeta
import com.ecosmart.presentation.theme.TypeSection
import com.ecosmart.presentation.theme.TypeTitle

/**
 * US9 — formulario de foto (Cámara inmersiva o Galería) + descripción (RF-025/RF-026).
 * Rediseño inmersivo RF-UI-002d:
 * - Paso 1: Cámara en vista inmersiva de pantalla completa con controles flotantes
 * - Paso 2: Foto real capturada/elegida a pantalla completa + panel inferior desplegable
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
            Scaffold { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        return
    }

    var modoCamara by remember { mutableStateOf(true) }
    var selectorCamara by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashEncendido by remember { mutableStateOf(false) }
    var panelExpandido by remember { mutableStateOf(true) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(uiState.archivoFoto) {
        if (uiState.archivoFoto != null) {
            panelExpandido = true
        }
    }

    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let(viewModel::adjuntarFotoDesdeGaleria)
    }

    VerificacionFotoContenido(
        uiState = uiState,
        modoCamara = modoCamara,
        flashEncendido = flashEncendido,
        panelExpandido = panelExpandido,
        onAlternarPanel = { panelExpandido = !panelExpandido },
        onSeleccionarCamara = { modoCamara = true },
        onSeleccionarGaleria = {
            modoCamara = false
            selectorGaleria.launch(SelectorGaleria.solicitudSoloImagenes)
        },
        onCambiarCamara = {
            selectorCamara = if (selectorCamara == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        },
        onAlternarFlash = {
            flashEncendido = !flashEncendido
            imageCapture.flashMode = if (flashEncendido) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
        },
        onDisparar = { viewModel.capturarFoto(imageCapture) },
        onDescripcionCambio = viewModel::actualizarDescripcion,
        onRepetirFoto = viewModel::repetirFoto,
        onEnviar = viewModel::enviar,
        onAtras = onAtras,
        cameraPreview = { modifier ->
            key(selectorCamara) {
                VistaCamara(
                    imageCapture = imageCapture,
                    cameraSelector = selectorCamara,
                    modifier = modifier,
                )
            }
        },
    )
}

/**
 * Contenido desacoplado y comprobable de VerificacionFotoScreen (Paso 1 y Paso 2).
 */
@Composable
fun VerificacionFotoContenido(
    uiState: VerificacionFotoUiState,
    modoCamara: Boolean,
    flashEncendido: Boolean,
    panelExpandido: Boolean,
    onAlternarPanel: () -> Unit,
    onSeleccionarCamara: () -> Unit,
    onSeleccionarGaleria: () -> Unit,
    onCambiarCamara: () -> Unit,
    onAlternarFlash: () -> Unit,
    onDisparar: () -> Unit,
    onDescripcionCambio: (String) -> Unit,
    onRepetirFoto: () -> Unit,
    onEnviar: () -> Unit,
    onAtras: () -> Unit,
    modifier: Modifier = Modifier,
    cameraPreview: @Composable (Modifier) -> Unit = {},
) {
    val esPaso2 = uiState.archivoFoto != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (!esPaso2) {
            // Paso 1 de 2: Cámara inmersiva a pantalla completa
            cameraPreview(Modifier.fillMaxSize())

            // Retícula de enfoque y guía visual central
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center,
            ) {
                MarcoEnfoqueCamara(modifier = Modifier.size(250.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 170.dp)
                        .padding(horizontal = 24.dp),
                ) {
                    Text(
                        text = "Tomá una foto del material que reciclaste o reutilizaste",
                        style = TypeMeta.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Controles inferiores flotantes
            ControlesInferioresCamara(
                modoCamara = modoCamara,
                flashEncendido = flashEncendido,
                onSeleccionarCamara = onSeleccionarCamara,
                onSeleccionarGaleria = onSeleccionarGaleria,
                onCambiarCamara = onCambiarCamara,
                onAlternarFlash = onAlternarFlash,
                onDisparar = onDisparar,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else {
            // Paso 2 de 2: Foto real capturada/elegida a pantalla completa
            val bitmap = remember(uiState.archivoFoto) {
                uiState.archivoFoto?.let { file ->
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto capturada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ColorGreen700)
                }
            }

            // Panel inferior desplegable para completar actividad
            PanelInferiorEnvioActividad(
                descripcion = uiState.descripcion,
                enviando = uiState.enviando,
                habilitadoParaEnviar = uiState.descripcion.isNotBlank() && !uiState.enviando,
                panelExpandido = panelExpandido,
                bitmap = bitmap,
                onAlternarPanel = onAlternarPanel,
                onDescripcionCambio = onDescripcionCambio,
                onRepetirFoto = onRepetirFoto,
                onEnviar = onEnviar,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        // Encabezado superior inmersivo (común a ambos pasos)
        EncabezadoInmersivoFoto(
            esPaso2 = esPaso2,
            nombreActividad = uiState.nombreActividad,
            puntosBase = uiState.puntosBase,
            onVolver = {
                if (esPaso2) {
                    onRepetirFoto()
                } else {
                    onAtras()
                }
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun EncabezadoInmersivoFoto(
    esPaso2: Boolean,
    nombreActividad: String,
    puntosBase: Int,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.8f),
                        Color.Black.copy(alpha = 0.5f),
                        Color.Transparent,
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                    )
                }

                Surface(
                    shape = FormaPill,
                    color = Color.Black.copy(alpha = 0.5f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "--",
                            color = Color.White.copy(alpha = 0.7f),
                            style = TypeMeta.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = if (esPaso2) "Paso 2 de 2" else "Paso 1 de 2",
                            color = Color.White,
                            style = TypeMeta.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hoja),
                    contentDescription = null,
                    tint = ColorLeaf,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Actividad de hoy",
                    style = TypeBody.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
                Surface(
                    shape = FormaPill,
                    color = ColorGreen100,
                ) {
                    Text(
                        text = if (puntosBase > 0) "+$puntosBase pts" else "+100 pts",
                        style = TypeMeta.copy(fontWeight = FontWeight.Bold),
                        color = ColorGreen700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            if (nombreActividad.isNotBlank()) {
                Text(
                    text = nombreActividad,
                    style = TypeTitle.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MarcoEnfoqueCamara(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val cornerLength = 28.dp.toPx()
        val w = size.width
        val h = size.height

        // Top-left
        drawLine(Color.White, start = Offset(0f, 0f), end = Offset(cornerLength, 0f), strokeWidth = strokeWidth)
        drawLine(Color.White, start = Offset(0f, 0f), end = Offset(0f, cornerLength), strokeWidth = strokeWidth)

        // Top-right
        drawLine(Color.White, start = Offset(w, 0f), end = Offset(w - cornerLength, 0f), strokeWidth = strokeWidth)
        drawLine(Color.White, start = Offset(w, 0f), end = Offset(w, cornerLength), strokeWidth = strokeWidth)

        // Bottom-left
        drawLine(Color.White, start = Offset(0f, h), end = Offset(cornerLength, h), strokeWidth = strokeWidth)
        drawLine(Color.White, start = Offset(0f, h), end = Offset(0f, h - cornerLength), strokeWidth = strokeWidth)

        // Bottom-right
        drawLine(Color.White, start = Offset(w, h), end = Offset(w - cornerLength, h), strokeWidth = strokeWidth)
        drawLine(Color.White, start = Offset(w, h), end = Offset(w, h - cornerLength), strokeWidth = strokeWidth)
    }
}

@Composable
private fun SelectorModoCaptura(
    modoCamara: Boolean,
    onSeleccionarCamara: () -> Unit,
    onSeleccionarGaleria: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = FormaPill,
        color = Color.Black.copy(alpha = 0.6f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                shape = FormaPill,
                color = if (modoCamara) Color.White else Color.Transparent,
                modifier = Modifier.clickable(onClick = onSeleccionarCamara),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camara),
                        contentDescription = null,
                        tint = if (modoCamara) ColorGreen700 else Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Cámara",
                        color = if (modoCamara) ColorGreen700 else Color.White,
                        style = TypeChip.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            Surface(
                shape = FormaPill,
                color = if (!modoCamara) Color.White else Color.Transparent,
                modifier = Modifier.clickable(onClick = onSeleccionarGaleria),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_galeria),
                        contentDescription = null,
                        tint = if (!modoCamara) ColorGreen700 else Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Galería",
                        color = if (!modoCamara) ColorGreen700 else Color.White,
                        style = TypeChip.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlesInferioresCamara(
    modoCamara: Boolean,
    flashEncendido: Boolean,
    onSeleccionarCamara: () -> Unit,
    onSeleccionarGaleria: () -> Unit,
    onCambiarCamara: () -> Unit,
    onAlternarFlash: () -> Unit,
    onDisparar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black.copy(alpha = 0.85f),
                    ),
                ),
            )
            .navigationBarsPadding()
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SelectorModoCaptura(
            modoCamara = modoCamara,
            onSeleccionarCamara = onSeleccionarCamara,
            onSeleccionarGaleria = onSeleccionarGaleria,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onCambiarCamara,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cambiar_camara),
                    contentDescription = "Cambiar cámara",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .background(ColorGreen100, CircleShape)
                    .clickable(onClick = onDisparar),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camara),
                    contentDescription = "Tomar foto",
                    tint = ColorGreen700,
                    modifier = Modifier.size(30.dp),
                )
            }

            IconButton(
                onClick = onAlternarFlash,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_flash_off),
                    contentDescription = if (flashEncendido) "Flash activado" else "Flash desactivado",
                    tint = if (flashEncendido) Color(0xFFF2B855) else Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun PanelInferiorEnvioActividad(
    descripcion: String,
    enviando: Boolean,
    habilitadoParaEnviar: Boolean,
    panelExpandido: Boolean,
    onAlternarPanel: () -> Unit,
    onDescripcionCambio: (String) -> Unit,
    onRepetirFoto: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
    bitmap: androidx.compose.ui.graphics.ImageBitmap? = null,
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = ColorBgScreen,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .background(ColorTrack, FormaPill)
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onAlternarPanel),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onAlternarPanel),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (bitmap != null) {
                        Box(modifier = Modifier.size(38.dp)) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(ColorGreen700, CircleShape)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp),
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = FormaPill,
                            color = ColorGreen100,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = ColorGreen700,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "Foto lista",
                        color = ColorGreen700,
                        style = TypeChip.copy(fontWeight = FontWeight.Bold),
                    )
                }

                Row(
                    modifier = Modifier.clickable(onClick = onRepetirFoto),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camara),
                        contentDescription = null,
                        tint = ColorGreen700,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Repetir foto",
                        color = ColorGreen700,
                        style = TypeChip.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            AnimatedVisibility(
                visible = panelExpandido,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Describí lo que hiciste",
                        style = TypeSection.copy(fontWeight = FontWeight.Bold),
                        color = ColorGreen900,
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = onDescripcionCambio,
                        placeholder = {
                            Text(
                                text = "Ej.: Llevé 6 botellas y 3 latas limpias al Punto Verde de mi barrio",
                                style = TypeBody,
                                color = ColorTextSecondary,
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGreen700,
                            unfocusedBorderColor = ColorTrack,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    )

                    Text(
                        text = "Contá qué material era y dónde lo dejaste.",
                        style = TypeMeta,
                        color = ColorTextSecondary,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (descripcion.isBlank()) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = ColorTextSecondary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "Describí lo que hiciste para continuar",
                                style = TypeMeta,
                                color = ColorTextSecondary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = ColorGreen700,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "Listo para enviar",
                                style = TypeMeta.copy(fontWeight = FontWeight.Bold),
                                color = ColorGreen700,
                            )
                        }
                    }

                    Button(
                        onClick = onEnviar,
                        enabled = habilitadoParaEnviar,
                        shape = FormaPill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorGreen700,
                            contentColor = Color.White,
                            disabledContainerColor = ColorGreen100,
                            disabledContentColor = Color(0xFF88A28E),
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = if (enviando) "Enviando…" else "Enviar actividad",
                                style = TypeChip.copy(fontWeight = FontWeight.SemiBold),
                            )
                            if (!enviando) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(R.drawable.ic_enviar_actividad),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VistaCamara(
    imageCapture: ImageCapture,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener(
                {
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
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

