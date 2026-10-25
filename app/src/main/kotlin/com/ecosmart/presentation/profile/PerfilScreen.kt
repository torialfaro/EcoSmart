package com.ecosmart.presentation.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.app.R
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen100
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorGreen900
import com.ecosmart.presentation.theme.ColorLeaf
import com.ecosmart.presentation.theme.ColorRachaBgFin
import com.ecosmart.presentation.theme.ColorRachaBgInicio
import com.ecosmart.presentation.theme.ColorRachaBorde
import com.ecosmart.presentation.theme.ColorRachaLlamaActiva
import com.ecosmart.presentation.theme.ColorRachaLlamaApagada
import com.ecosmart.presentation.theme.ColorRachaLlamaGradienteFin
import com.ecosmart.presentation.theme.ColorRachaLlamaGradienteInicio
import com.ecosmart.presentation.theme.ColorRachaTextoMensaje
import com.ecosmart.presentation.theme.ColorRachaTextoNumero
import com.ecosmart.presentation.theme.ColorRecompensaCardBg
import com.ecosmart.presentation.theme.ColorSurfaceCard
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextSecondary
import com.ecosmart.presentation.theme.ColorTrack
import com.ecosmart.presentation.theme.CremaTexto
import com.ecosmart.presentation.theme.ElevacionTarjeta
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.FormaPill
import com.ecosmart.presentation.theme.FormaTarjeta
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeCardTitle
import com.ecosmart.presentation.theme.TypeMeta
import com.ecosmart.presentation.theme.TypeSection
import com.ecosmart.presentation.theme.TypeTitle

/**
 * Pantalla Perfil reconstruida según S-06 (RF-UI-014, Clarifications 2026-10-18):
 * 1. Encabezado verde de ancho completo con degradé (#1F5E34 a #0F4D2E), esquinas 28 dp y avatar 68 dp a la izquierda.
 * 2. Tarjeta destacada de racha con llama de 64 dp, mensaje positivo y tira semanal de 7 días.
 * 3. Tarjetas de estadísticas en 2 columnas (Actividades y Recompensas).
 * 4. Sección "Actividades realizadas" con enlace "Ver todas" (área táctil 48 dp) y estado vacío.
 * 5. Menú de cuenta con 5 filas de 56 dp y nuevos íconos.
 * 6. Botón "Cerrar sesión" píldora con contorno verde.
 * 7. Barra inferior con píldora detrás del ícono seleccionado.
 */
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel(),
    onVerHistorial: () -> Unit,
    onEditarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    barraInferior: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    // RF-D012: la cuenta eliminada ya no tiene sesión válida, se navega igual que al cerrar sesión.
    LaunchedEffect(uiState.cuentaEliminada) {
        if (uiState.cuentaEliminada) onCerrarSesion()
    }

    // Esta pantalla queda viva en el back stack mientras se edita el perfil: se refresca al volver (RF-077).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) viewModel.refrescar()
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    Scaffold(
        containerColor = ColorBgScreen,
        bottomBar = barraInferior,
    ) { padding ->
        if (uiState.cargando || uiState.usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorGreen700)
            }
            return@Scaffold
        }

        PerfilContenido(
            uiState = uiState,
            onVerHistorial = onVerHistorial,
            onEditarPerfil = onEditarPerfil,
            onCerrarSesion = {
                viewModel.cerrarSesion()
                onCerrarSesion()
            },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun PerfilContenido(
    uiState: PerfilUiState,
    onVerHistorial: () -> Unit,
    onEditarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val usuario = uiState.usuario ?: return
    val primerNombre = usuario.nombre.ifBlank { usuario.nombreUsuario }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // 1. Encabezado verde de ancho completo con degradé y hojas decorativas
        EncabezadoPerfilVerde(
            primerNombre = primerNombre,
            nivel = uiState.nivel,
            puntosHistoricos = uiState.puntosHistoricos,
            onEditarPerfil = onEditarPerfil,
        )

        // Contenido con márgenes horizontales, superpuesto sobre el encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-36).dp)
                .padding(horizontal = SpaceScreenX),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 2. Tarjeta destacada de Racha con tira semanal
            TarjetaRachaPerfil(
                rachaActual = uiState.rachaActual,
                diasSemanaRacha = uiState.diasSemanaRacha,
            )

            // 3. Estadísticas: 2 tarjetas lado a lado
            FilaEstadisticasDosColumnas(
                totalActividades = uiState.totalActividadesRealizadas,
                totalRecompensas = uiState.totalRecompensas,
            )

            // 4. Sección "Actividades realizadas"
            SeccionActividadesRealizadas(
                actividades = uiState.actividadesRealizadas,
                onVerTodas = onVerHistorial,
            )

            // 5. Menú de cuenta (5 filas)
            TarjetaMenuCuenta(
                onEditarPerfil = onEditarPerfil,
                onVerHistorial = onVerHistorial,
            )

            // 6. Botón Cerrar sesión
            OutlinedButton(
                onClick = onCerrarSesion,
                shape = FormaBotonPildora,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorGreen700),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cerrar_sesion),
                        contentDescription = null,
                        tint = ColorGreen700,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Cerrar sesión",
                        color = ColorGreen700,
                        style = TypeCardTitle.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EncabezadoPerfilVerde(
    primerNombre: String,
    nivel: NivelUsuario,
    puntosHistoricos: Int,
    onEditarPerfil: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(ColorGreen700, ColorGreen900),
                ),
            )
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        // Hojas decorativas sutiles de fondo
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
        ) {
            val leafPath = Path().apply {
                moveTo(size.width * 0.85f, size.height * 0.1f)
                cubicTo(
                    size.width * 1.1f, size.height * 0.2f,
                    size.width * 1.05f, size.height * 0.7f,
                    size.width * 0.75f, size.height * 0.85f,
                )
                cubicTo(
                    size.width * 0.65f, size.height * 0.5f,
                    size.width * 0.7f, size.height * 0.2f,
                    size.width * 0.85f, size.height * 0.1f,
                )
            }
            drawPath(leafPath, color = Color.White.copy(alpha = 0.06f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Avatar de 68 dp con borde claro
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ColorGreen100)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable(onClick = onEditarPerfil),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = primerNombre.take(1).uppercase(),
                    style = TypeTitle.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    color = ColorGreen900,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = primerNombre,
                    style = TypeTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Píldora translúcida con nivel y puntos reales
                Surface(
                    shape = FormaPill,
                    color = Color.White.copy(alpha = 0.15f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_hoja),
                            contentDescription = null,
                            tint = ColorGreen100,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "${etiquetaNivel(nivel)} · ${formatearPuntos(puntosHistoricos)} pts",
                            style = TypeMeta.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRachaPerfil(
    rachaActual: Int,
    diasSemanaRacha: List<DiaSemanaRachaUi>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(ElevacionTarjeta, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorRachaBgInicio),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorRachaBorde),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ColorRachaBgInicio, ColorRachaBgFin),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Círculo de llama de 64 dp
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (rachaActual > 0) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        ColorRachaLlamaGradienteInicio,
                                        ColorRachaLlamaGradienteFin,
                                    ),
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(ColorTrack, ColorTrack),
                                )
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_llama),
                        contentDescription = null,
                        tint = if (rachaActual > 0) Color.White else ColorRachaLlamaApagada,
                        modifier = Modifier.size(34.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = rachaActual.toString(),
                            style = TypeTitle.copy(
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorRachaTextoNumero,
                            ),
                        )
                        Text(
                            text = "días de racha",
                            style = TypeSection.copy(
                                fontWeight = FontWeight.Bold,
                                color = ColorRachaTextoNumero,
                            ),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }

                    val hoyCumplido = diasSemanaRacha.find { it.esHoy }?.cumplido == true
                    val mensajeApoyo = when {
                        rachaActual > 0 && hoyCumplido -> "¡Hoy ya sumaste! Mañana llegás a ${rachaActual + 1}."
                        rachaActual > 0 -> "Completá una actividad hoy y llegás a ${rachaActual + 1}."
                        else -> "Completá una actividad hoy para encender tu racha."
                    }

                    Text(
                        text = mensajeApoyo,
                        style = TypeMeta.copy(fontWeight = FontWeight.Medium),
                        color = ColorRachaTextoMensaje,
                    )
                }
            }

            // Tira semanal de 7 días (L a D)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                diasSemanaRacha.forEach { dia ->
                    ItemDiaSemanaRacha(dia = dia)
                }
            }
        }
    }
}

@Composable
private fun ItemDiaSemanaRacha(dia: DiaSemanaRachaUi) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "${dia.inicialDia}: ${if (dia.cumplido) "actividad completada" else "sin actividad"}"
        },
    ) {
        Text(
            text = dia.inicialDia,
            style = TypeMeta.copy(fontWeight = FontWeight.SemiBold),
            color = ColorRachaTextoMensaje,
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp),
        ) {
            // Anillo para hoy
            if (dia.esHoy) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(
                            width = 2.dp,
                            color = ColorRachaLlamaActiva,
                            shape = CircleShape,
                        ),
                )
            }

            // Círculo central del día
            if (dia.cumplido) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ColorRachaLlamaActiva),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                        .border(1.dp, ColorRachaBorde, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun FilaEstadisticasDosColumnas(
    totalActividades: Int,
    totalRecompensas: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tarjeta Actividades
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(ElevacionTarjeta, FormaTarjeta),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorGreen100),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circulo),
                        contentDescription = null,
                        tint = ColorGreen700,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column {
                    Text(
                        text = totalActividades.toString(),
                        style = TypeTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = ColorTextPrimary,
                    )
                    Text(
                        text = "Actividades",
                        style = TypeMeta.copy(fontSize = 12.sp),
                        color = ColorTextSecondary,
                        maxLines = 1,
                    )
                }
            }
        }

        // Tarjeta Recompensas
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(ElevacionTarjeta, FormaTarjeta),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorRecompensaCardBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column {
                    Text(
                        text = totalRecompensas.toString(),
                        style = TypeTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = ColorTextPrimary,
                    )
                    Text(
                        text = "Recompensas",
                        style = TypeMeta.copy(fontSize = 12.sp),
                        color = ColorTextSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionActividadesRealizadas(
    actividades: List<ActividadRealizadaResumenUi>,
    onVerTodas: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Actividades realizadas",
                style = TypeSection.copy(fontWeight = FontWeight.Bold),
                color = ColorGreen900,
            )

            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(onClick = onVerTodas)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Ver todas",
                    style = TypeMeta.copy(fontWeight = FontWeight.Bold),
                    color = ColorGreen700,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ColorGreen700,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
        ) {
            if (actividades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Comenzá a realizar actividades",
                        style = TypeBody,
                        color = ColorTextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    actividades.forEachIndexed { index, actividad ->
                        FilaActividadRealizada(actividad = actividad)
                        if (index < actividades.size - 1) {
                            HorizontalDivider(color = ColorTrack, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaActividadRealizada(actividad: ActividadRealizadaResumenUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Icono de categoría en círculo de 40 dp
            val (iconoRes, fondoColor, tintColor) = when (actividad.categoria) {
                CategoriaActividad.RECICLAR -> Triple(R.drawable.ic_categoria_reciclar, ColorGreen100, ColorGreen700)
                CategoriaActividad.REUTILIZAR -> Triple(R.drawable.ic_categoria_reutilizar, Color(0xFFFFF1D6), Color(0xFFD97706))
                CategoriaActividad.CAMINAR -> Triple(R.drawable.ic_caminar, Color(0xFFE3F1EE), ColorGreen700)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(fondoColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconoRes),
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = actividad.titulo,
                    style = TypeCardTitle.copy(fontWeight = FontWeight.Bold),
                    color = ColorTextPrimary,
                    maxLines = 2,
                )
                Text(
                    text = actividad.etiquetaFecha,
                    style = TypeMeta,
                    color = ColorTextSecondary,
                )
            }
        }

        // Tilde verde de completada
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(ColorGreen700),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Completada",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun TarjetaMenuCuenta(
    onEditarPerfil: () -> Unit,
    onVerHistorial: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FilaMenuPerfil(
                iconoRes = R.drawable.ic_check_circulo,
                iconoVector = null,
                texto = "Mis actividades",
                onClick = onVerHistorial,
            )
            HorizontalDivider(color = ColorTrack, thickness = 1.dp)
            FilaMenuPerfil(
                iconoRes = R.drawable.ic_menu_regalo,
                iconoVector = null,
                texto = "Mis recompensas",
                onClick = onVerHistorial,
            )
            HorizontalDivider(color = ColorTrack, thickness = 1.dp)
            FilaMenuPerfil(
                iconoRes = R.drawable.ic_menu_configuracion,
                iconoVector = null,
                texto = "Configuración",
                onClick = onEditarPerfil,
            )
            HorizontalDivider(color = ColorTrack, thickness = 1.dp)
            FilaMenuPerfil(
                iconoRes = R.drawable.ic_menu_ayuda,
                iconoVector = null,
                texto = "Ayuda",
                onClick = null,
            )
        }
    }
}

@Composable
private fun FilaMenuPerfil(
    iconoRes: Int?,
    iconoVector: ImageVector?,
    texto: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) {
                contentDescription = texto
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorGreen100),
                contentAlignment = Alignment.Center,
            ) {
                if (iconoRes != null) {
                    Icon(
                        painter = painterResource(iconoRes),
                        contentDescription = null,
                        tint = ColorGreen700,
                        modifier = Modifier.size(20.dp),
                    )
                } else if (iconoVector != null) {
                    Icon(
                        imageVector = iconoVector,
                        contentDescription = null,
                        tint = ColorGreen700,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Text(
                text = texto,
                style = TypeCardTitle.copy(fontWeight = FontWeight.SemiBold),
                color = ColorTextPrimary,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ColorTextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Corrección post-QA: el nivel se muestra con el nombre ya definido en el dominio (NivelUsuario), no un número inventado. */
private fun etiquetaNivel(nivel: NivelUsuario): String = when (nivel) {
    NivelUsuario.SEMILLA -> "Semilla"
    NivelUsuario.BROTE -> "Brote"
    NivelUsuario.PLANTA -> "Planta"
    NivelUsuario.ARBOL -> "Árbol"
}

private fun formatearPuntos(puntos: Int): String {
    val texto = puntos.toString()
    if (texto.length <= 3) return texto
    val prefix = texto.substring(0, texto.length - 3)
    val suffix = texto.substring(texto.length - 3)
    return "$prefix.$suffix"
}
