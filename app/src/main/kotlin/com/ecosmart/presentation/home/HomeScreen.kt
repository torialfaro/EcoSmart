package com.ecosmart.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.app.R
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.iconoCategoria
import com.ecosmart.presentation.theme.BarraProgresoLineal
import com.ecosmart.presentation.theme.BotonPrincipal
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorSurfaceCard
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextSecondary
import com.ecosmart.presentation.theme.ElevacionTarjeta
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.EnlaceSeccion
import com.ecosmart.presentation.theme.FormaTarjeta
import com.ecosmart.presentation.theme.SpaceGapCard
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TarjetaActividad
import com.ecosmart.presentation.theme.TarjetaPuntos
import com.ecosmart.presentation.theme.TarjetaVideo
import com.ecosmart.presentation.theme.TypeCardTitle
import com.ecosmart.presentation.theme.TypeMeta
import com.ecosmart.presentation.theme.TypeSection

/**
 * Pantalla Inicio reconstruida según S-01 (especificacion-ui-app-eco.md §4):
 * Encabezado con saludo y avatar, subtítulo con hoja, tarjeta de puntos C-11,
 * tarjeta de progreso general C-05 ("X/Y actividades" y barra %), carrusel horizontal C-06,
 * botón principal "Ver todas las actividades →" C-07 y carrusel horizontal de videos C-09.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSeleccionarActividad: (ActividadId) -> Unit,
    onVerTodasLasActividades: () -> Unit = {},
    onVerPuntosVerdes: () -> Unit = {},
    onVerContenidoEducativo: () -> Unit = {},
    onVerPerfil: () -> Unit,
    barraInferior: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) viewModel.refrescar()
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    val actividadesDelDia = remember(uiState.todasLasActividades, uiState.actividadesPorCategoria, uiState.pasosHoy) {
        val lista = uiState.todasLasActividades.ifEmpty { uiState.actividadesPorCategoria.values.flatten() }
        lista.map { act ->
            val esCaminar = act.categoria == CategoriaActividad.CAMINAR
            // Corrección post-QA: el progreso de Caminar sale únicamente del podómetro real (uiState.pasosHoy).
            val progreso = if (esCaminar) uiState.pasosHoy else 0
            val meta = if (esCaminar) META_PASOS_POR_DEFECTO_DISPLAY else 1
            ActividadItemUi(
                id = act.id.valor,
                nombre = act.descripcionCorta,
                categoria = act.categoria,
                iconoRes = iconoCategoria(act.categoria),
                progresoActual = progreso,
                meta = meta,
                puntosBase = act.puntosBase,
                esMedible = meta > 1,
            )
        }
    }

    Scaffold(
        containerColor = ColorBgScreen,
        topBar = {
            // S01-A, S01-B, S01-C: Saludo + Avatar + Subtítulo
            EncabezadoEcoSmart(
                saludo = if (uiState.nombreUsuario.isNotBlank()) "¡Hola, ${uiState.nombreUsuario}!" else "¡Hola!",
                subtitulo = "Pequeñas acciones, grandes cambios",
                inicialUsuario = uiState.nombreUsuario.ifBlank { "U" },
                onAvatar = onVerPerfil,
                mostrarHojaSubtitulo = true,
            )
        },
        bottomBar = barraInferior,
    ) { padding ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorGreen700)
            }
            return@Scaffold
        }

        HomeContenido(
            actividades = actividadesDelDia,
            puntosTotales = uiState.puntosHistoricos,
            onSeleccionarActividad = { id -> onSeleccionarActividad(ActividadId(id)) },
            onVerTodasLasActividades = onVerTodasLasActividades,
            onVerContenidoEducativo = onVerContenidoEducativo,
            onVerPerfil = onVerPerfil,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun HomeContenido(
    actividades: List<ActividadItemUi>,
    puntosTotales: Int = 0,
    onSeleccionarActividad: (String) -> Unit,
    onVerTodasLasActividades: () -> Unit = {},
    onVerContenidoEducativo: () -> Unit = {},
    onVerPerfil: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val totalActividades = actividades.size.coerceAtLeast(1)
    val completadas = actividades.count { it.progresoActual >= it.meta && it.meta > 0 }
    val fraccionProgreso = (completadas.toFloat() / totalActividades.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = SpaceScreenX, end = SpaceScreenX, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // S01-D: Tarjeta de puntos C-11 (planeta ilustrado decorativo RF-UI-018)
        item {
            TarjetaPuntos(
                puntos = puntosTotales,
                onClick = onVerPerfil,
                iconoIzquierda = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ColorSurfaceCard)
                            .clearAndSetSemantics {},
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_ecosmart_icono),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        }

        // S01-E: Tarjeta de progreso general C-05
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = FormaTarjeta,
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Tu progreso general",
                        style = TypeCardTitle,
                        color = ColorTextPrimary,
                    )

                    BarraProgresoLineal(
                        progreso = fraccionProgreso,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$completadas/$totalActividades actividades",
                            style = TypeMeta,
                            color = ColorTextSecondary,
                        )

                        EnlaceSeccion(
                            texto = "Ver todas",
                            onClick = onVerTodasLasActividades,
                        )
                    }
                }
            }
        }

        // S01-F: Título "Actividades de hoy" + Enlace "Ver todas"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Actividades de hoy",
                    style = TypeSection,
                    color = ColorTextPrimary,
                )
                EnlaceSeccion(
                    texto = "Ver todas",
                    onClick = onVerTodasLasActividades,
                )
            }
        }

        // S01-G: Carrusel horizontal de actividades C-06 (compactas)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(SpaceGapCard),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(actividades, key = { it.id }) { actividad ->
                    TarjetaActividad(
                        nombre = actividad.nombre,
                        iconoRes = actividad.iconoRes,
                        progresoActual = actividad.progresoActual,
                        meta = actividad.meta,
                        puntosBase = actividad.puntosBase,
                        onClick = { onSeleccionarActividad(actividad.id) },
                        esCompacta = true,
                        esMedible = actividad.esMedible,
                        unidad = actividad.unidad,
                    )
                }
            }
        }

        // S01-H: Botón "Ver todas las actividades →" C-07
        item {
            BotonPrincipal(
                texto = "Ver todas las actividades",
                onClick = onVerTodasLasActividades,
                conFlecha = true,
            )
        }

        // S01-I: Título "Contenido para vos"
        item {
            Text(
                text = "Contenido para vos",
                style = TypeSection,
                color = ColorTextPrimary,
            )
        }

        // S01-J: Carrusel horizontal de videos C-09 (compactas)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SpaceGapCard),
            ) {
                TarjetaVideo(
                    titulo = "¿Cómo afecta la contaminación?",
                    onClick = onVerContenidoEducativo,
                    esCompacta = true,
                )
                TarjetaVideo(
                    titulo = "Tu guía de reciclaje",
                    onClick = onVerContenidoEducativo,
                    esCompacta = true,
                )
            }
        }
    }
}
