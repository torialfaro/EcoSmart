package com.ecosmart.presentation.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FilaChipsFiltro
import com.ecosmart.presentation.theme.SpaceGapCard
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TarjetaRecompensa
import com.ecosmart.presentation.theme.TypeBody

/**
 * Pantalla Recompensas (S-04 — especificacion-ui-app-eco.md §7):
 * Encabezado "Recompensas" / "Canjeá tus puntos y disfrutá",
 * chips de categoría con scroll horizontal y grilla de 2 columnas de TarjetaRecompensa (C-10).
 * Corrección post-QA (2026-10-14): catálogo único, sin segmento "Para adultos"/"Para Kids".
 */
@Composable
fun RecompensasScreen(
    onSeleccionarRecompensa: (RecompensaEjemplo) -> Unit = {},
    onVerPerfil: (() -> Unit)? = null,
    barraInferior: @Composable () -> Unit = {},
    recompensas: List<RecompensaEjemplo> = RECOMPENSAS_EJEMPLO,
) {
    Scaffold(
        containerColor = ColorBgScreen,
        topBar = {
            EncabezadoEcoSmart(
                titulo = "Recompensas",
                subtitulo = "Canjeá tus puntos y disfrutá",
                onAvatar = onVerPerfil,
            )
        },
        bottomBar = barraInferior,
    ) { padding ->
        RecompensasContenido(
            recompensas = recompensas,
            onSeleccionarRecompensa = onSeleccionarRecompensa,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun RecompensasContenido(
    recompensas: List<RecompensaEjemplo>,
    onSeleccionarRecompensa: (RecompensaEjemplo) -> Unit = {},
    categoriaInicial: CategoriaRecompensaEjemplo = CategoriaRecompensaEjemplo.TODOS,
    modifier: Modifier = Modifier,
) {
    var categoriaSeleccionada by remember { mutableStateOf(categoriaInicial) }

    val recompensasFiltradas = remember(recompensas, categoriaSeleccionada) {
        recompensas.filter { rec ->
            categoriaSeleccionada == CategoriaRecompensaEjemplo.TODOS || rec.categoria == categoriaSeleccionada
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpaceScreenX),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // S04-D: Chips de categoría (Todos / Comida / Descuentos / Productos)
        FilaChipsFiltro(
            opciones = CategoriaRecompensaEjemplo.entries,
            seleccionado = categoriaSeleccionada,
            onSeleccionar = { categoriaSeleccionada = it },
            etiqueta = { it.etiqueta },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        // S04-E: Grilla de recompensas (2 columnas)
        if (recompensasFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Todavía no hay recompensas acá",
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
                items(recompensasFiltradas, key = { it.id }) { recompensa ->
                    TarjetaRecompensa(
                        titulo = recompensa.titulo,
                        costoPuntos = recompensa.costoPuntos,
                        onClick = { onSeleccionarRecompensa(recompensa) },
                        imagenDrawableRes = recompensa.imagenDrawableRes,
                    )
                }
            }
        }
    }
}
