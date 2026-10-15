package com.ecosmart.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.iconoCategoria
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorSurfaceCard
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaPill
import com.ecosmart.presentation.theme.SegmentoEcoSmart
import com.ecosmart.presentation.theme.SpaceGapCard
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TarjetaActividad
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeChip

/** Filtros disponibles en la pantalla de Actividades (S-02 / C-04). */
enum class FiltroActividades(val etiqueta: String) {
    TODAS("Todas"),
    EN_PROGRESO("En progreso"),
    COMPLETADAS("Completadas"),
}

/** Filtro de categoría adicional (feedback post-QA): "Todas" + las 3 categorías reales. */
enum class FiltroCategoria(val etiqueta: String, val categoria: CategoriaActividad?) {
    TODAS("Todas", null),
    RECICLAR("Reciclar", CategoriaActividad.RECICLAR),
    REUTILIZAR("Reutilizar", CategoriaActividad.REUTILIZAR),
    CAMINAR("Caminar", CategoriaActividad.CAMINAR),
}

/** Meta de pasos por defecto cuando todavía no se inició un seguimiento (igual a VerificacionCaminataScreen). */
const val META_PASOS_POR_DEFECTO_DISPLAY = 1000

/** Modelo visual de tarjeta para la pantalla Actividades. */
data class ActividadItemUi(
    val id: String,
    val nombre: String,
    val categoria: CategoriaActividad,
    val iconoRes: Int,
    val progresoActual: Int,
    val meta: Int,
    val puntosBase: Int,
    val esMedible: Boolean = meta > 1,
    val unidad: String = "pasos",
)

/**
 * Pantalla Actividades (S-02 — especificacion-ui-app-eco.md §5):
 * Encabezado "Actividades" / "Elegí qué querés hacer hoy" + hoja,
 * segmento de filtro por estado (Todas / En progreso / Completadas), dropdown de
 * categoría (Reciclar/Reutilizar/Caminar) y grilla de 2 columnas de TarjetaActividad.
 */
@Composable
fun ActividadesScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSeleccionarActividad: (String) -> Unit,
    onVerPerfil: (() -> Unit)? = null,
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

    val actividades = remember(uiState.todasLasActividades, uiState.actividadesPorCategoria, uiState.pasosHoy) {
        val lista = uiState.todasLasActividades.ifEmpty { uiState.actividadesPorCategoria.values.flatten() }
        lista.map { act ->
            val esCaminar = act.categoria == CategoriaActividad.CAMINAR
            // Corrección post-QA: el progreso de Caminar sale únicamente del podómetro real (uiState.pasosHoy),
            // nunca de un valor fijo; el resto del catálogo sigue siendo 0/1 (no medible).
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
            EncabezadoEcoSmart(
                titulo = "Actividades",
                subtitulo = "Elegí qué querés hacer hoy",
                inicialUsuario = uiState.nombreUsuario.ifBlank { "U" },
                onAvatar = onVerPerfil,
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

        ActividadesContenido(
            actividades = actividades,
            onSeleccionarActividad = onSeleccionarActividad,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun ActividadesContenido(
    actividades: List<ActividadItemUi>,
    onSeleccionarActividad: (String) -> Unit,
    filtroInicial: FiltroActividades = FiltroActividades.TODAS,
    modifier: Modifier = Modifier,
) {
    var filtroSeleccionado by remember { mutableStateOf(filtroInicial) }
    var categoriaSeleccionada by remember { mutableStateOf(FiltroCategoria.TODAS) }

    val actividadesFiltradas = remember(actividades, filtroSeleccionado, categoriaSeleccionada) {
        actividades
            .filter { categoriaSeleccionada.categoria == null || it.categoria == categoriaSeleccionada.categoria }
            .filter { actividad ->
                when (filtroSeleccionado) {
                    FiltroActividades.TODAS -> true
                    FiltroActividades.EN_PROGRESO -> actividad.progresoActual in 1 until actividad.meta
                    FiltroActividades.COMPLETADAS -> actividad.progresoActual >= actividad.meta && actividad.meta > 0
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpaceScreenX),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Segmento de estado (mismo diseño que el segmento Videos/Artículos/IA de Educación — feedback post-QA).
        SegmentoEcoSmart(
            opciones = FiltroActividades.entries,
            seleccionado = filtroSeleccionado,
            onSeleccionar = { filtroSeleccionado = it },
            etiqueta = { it.etiqueta },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        // Dropdown de categoría (feedback post-QA): filtra adicionalmente por Reciclar/Reutilizar/Caminar.
        DropdownCategoria(
            seleccionada = categoriaSeleccionada,
            onSeleccionar = { categoriaSeleccionada = it },
        )

        if (actividadesFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Todavía no hay actividades acá",
                    style = TypeBody,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(SpaceGapCard),
                verticalArrangement = Arrangement.spacedBy(SpaceGapCard),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(actividadesFiltradas, key = { it.id }) { actividad ->
                    TarjetaActividad(
                        nombre = actividad.nombre,
                        iconoRes = actividad.iconoRes,
                        progresoActual = actividad.progresoActual,
                        meta = actividad.meta,
                        puntosBase = actividad.puntosBase,
                        onClick = { onSeleccionarActividad(actividad.id) },
                        esCompacta = false,
                        esMedible = actividad.esMedible,
                        unidad = actividad.unidad,
                    )
                }
            }
        }
    }
}

/** Dropdown para filtrar la grilla por categoría (Reciclar/Reutilizar/Caminar) — feedback post-QA. */
@Composable
private fun DropdownCategoria(
    seleccionada: FiltroCategoria,
    onSeleccionar: (FiltroCategoria) -> Unit,
) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clickable(onClick = { expandido = true })
                .background(ColorSurfaceCard, FormaPill)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = if (seleccionada.categoria == null) "Categoría: Todas" else "Categoría: ${seleccionada.etiqueta}",
                style = TypeChip,
                color = ColorTextPrimary,
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ColorGreen700,
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            FiltroCategoria.entries.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.etiqueta) },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    },
                )
            }
        }
    }
}
