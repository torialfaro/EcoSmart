package com.ecosmart.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.presentation.education.LeccionUi
import com.ecosmart.presentation.education.LeccionesInicioViewModel
import com.ecosmart.presentation.education.TarjetasEducativasInicio
import com.ecosmart.presentation.education.leccionesParaInicio
import com.ecosmart.presentation.estadodiario.ActividadInicioUi
import com.ecosmart.presentation.estadodiario.EstadoDiarioUi
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.EncabezadoInicio
import com.ecosmart.presentation.theme.FormaPill
import com.ecosmart.presentation.theme.SpaceGapCard
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TarjetaActividadConBoton
import com.ecosmart.presentation.theme.TarjetaProgresoDiario
import com.ecosmart.presentation.theme.TarjetaPuntosInicio
import com.ecosmart.presentation.theme.TypeChip
import com.ecosmart.presentation.theme.TypeSection
import com.ecosmart.presentation.theme.ZonaTactilMinima

const val TAG_LISTA_INICIO = "ListaInicio"

private const val SUBTITULO_MARCA = "Pequeñas acciones, grandes cambios"

/**
 * Pantalla Inicio rediseñada (spec 005, tableros I1/I2): encabezado verde con la tarjeta de puntos superpuesta,
 * "Tu progreso de hoy", carrusel "Actividades de hoy", botón "Ver todas las actividades" y "Contenido para vos".
 * Todos los valores salen de [HomeViewModel]; la barra inferior la aporta [barraInferior].
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSeleccionarActividad: (ActividadId) -> Unit,
    onVerTodasLasActividades: () -> Unit = {},
    onVerLeccion: (String) -> Unit = {},
    onVerPerfil: () -> Unit,
    barraInferior: @Composable () -> Unit = {},
    leccionesViewModel: LeccionesInicioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val leccionesInicio by leccionesViewModel.lecciones.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) viewModel.refrescar()
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    Scaffold(containerColor = ColorBgScreen, bottomBar = barraInferior) { padding ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorGreen700)
            }
            return@Scaffold
        }

        // El bloque verde llega hasta el borde superior; solo se respeta el espacio de la barra inferior.
        HomeContenido(
            nombreUsuario = uiState.nombreUsuario,
            puntosTotales = uiState.puntosHistoricos,
            estadoDiario = uiState.estadoDiario,
            actividades = uiState.actividadesInicio,
            onSeleccionarActividad = onSeleccionarActividad,
            onVerTodasLasActividades = onVerTodasLasActividades,
            onVerLeccion = onVerLeccion,
            onVerPerfil = onVerPerfil,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            leccionesInicio = leccionesInicio,
        )
    }
}

@Composable
fun HomeContenido(
    nombreUsuario: String,
    puntosTotales: Int,
    estadoDiario: EstadoDiarioUi?,
    actividades: List<ActividadInicioUi>,
    onSeleccionarActividad: (ActividadId) -> Unit,
    onVerTodasLasActividades: () -> Unit = {},
    onVerLeccion: (String) -> Unit = {},
    onVerPerfil: () -> Unit = {},
    modifier: Modifier = Modifier,
    leccionesInicio: List<LeccionUi> = emptyList(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(TAG_LISTA_INICIO),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            EncabezadoInicio(
                saludo = if (nombreUsuario.isNotBlank()) "¡Hola, $nombreUsuario!" else "¡Hola!",
                subtitulo = SUBTITULO_MARCA,
                inicialUsuario = nombreUsuario.ifBlank { "U" },
                onAvatar = onVerPerfil,
            ) {
                TarjetaPuntosInicio(puntos = puntosTotales, onClick = onVerPerfil)
            }
        }

        // Sin lectura del estado del día no se muestra ningún número: la UI no inventa totales.
        if (estadoDiario != null) {
            item { TarjetaProgresoDiario(estado = estadoDiario, modifier = Modifier.padding(horizontal = SpaceScreenX)) }
        }

        item { TituloSeccion("Actividades de hoy") }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = SpaceScreenX),
                horizontalArrangement = Arrangement.spacedBy(SpaceGapCard),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(actividades, key = { it.actividad.id.valor }) { tarjeta ->
                    TarjetaActividadConBoton(
                        actividad = tarjeta,
                        onClick = { onSeleccionarActividad(tarjeta.actividad.id) },
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onVerTodasLasActividades,
                modifier = Modifier
                    .padding(horizontal = SpaceScreenX)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = ZonaTactilMinima),
                shape = FormaPill,
                border = BorderStroke(1.5.dp, ColorGreen700),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorGreen700),
            ) {
                Text(text = "Ver todas las actividades", style = TypeChip.copy(fontWeight = FontWeight.SemiBold))
            }
        }

        if (leccionesInicio.isNotEmpty()) {
            item { TituloSeccion("Contenido para vos") }
            item {
                Box(modifier = Modifier.padding(horizontal = SpaceScreenX)) {
                    TarjetasEducativasInicio(lecciones = leccionesParaInicio(leccionesInicio), onVerLeccion = onVerLeccion)
                }
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = TypeSection,
        color = ColorTextPrimary,
        modifier = Modifier.padding(horizontal = SpaceScreenX),
    )
}
