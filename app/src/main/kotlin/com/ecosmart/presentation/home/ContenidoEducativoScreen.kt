package com.ecosmart.presentation.home

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.EncabezadoConUsuario
import com.ecosmart.presentation.comun.etiquetaCategoria
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.TarjetaEcoSmart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Contenido educativo estático (US6, RF-016/RF-017). No hay una entidad de
 * dominio ni fuente externa para esto en data-model.md/spec.md — es
 * contenido curado por EcoSmart, embebido en el cliente. RF-017 exige que
 * el layout soporte agregar contenido sin rediseño estructural, no que los
 * datos sean dinámicos.
 */
private data class SubtemaEducativo(
    val emoji: String,
    val subtitulo: String,
    val descripcion: String,
)

private data class ContenidoEducativo(
    val id: String,
    val titulo: String,
    val categoria: CategoriaActividad,
    val esVideo: Boolean,
    val subtemas: List<SubtemaEducativo>,
)

// (-soff) Los emojis de abajo son un placeholder: el usuario va a mandar imágenes propias
// para reemplazarlos más adelante — no son el diseño final.
private val CATALOGO_CONTENIDO = listOf(
    ContenidoEducativo(
        id = "separar-residuos",
        titulo = "Cómo separar tus residuos en casa",
        categoria = CategoriaActividad.RECICLAR,
        esVideo = false,
        subtemas = listOf(
            SubtemaEducativo("🟦", "Reciclables", "Papel, cartón, plástico, vidrio y metal limpios y secos."),
            SubtemaEducativo("🟫", "Orgánicos", "Restos de comida y otros residuos biodegradables."),
            SubtemaEducativo("⬛", "No reciclables", "Todo lo que no puede reciclarse."),
        ),
    ),
    ContenidoEducativo(
        id = "circuito-reciclaje",
        titulo = "El circuito del reciclaje en CABA",
        categoria = CategoriaActividad.RECICLAR,
        esVideo = true,
        subtemas = listOf(
            SubtemaEducativo("🏠", "Separá", "Separá los residuos en tu casa."),
            SubtemaEducativo("📍", "Llevá", "Llevá los reciclables a un Punto Verde o entregalos a recuperadores urbanos."),
            SubtemaEducativo("♻️", "Clasificá", "Los materiales se separan según su tipo."),
            SubtemaEducativo("🏭", "Reciclá", "Se transforman en nuevos materiales y productos."),
        ),
    ),
    ContenidoEducativo(
        id = "reutilizar-frascos",
        titulo = "5 ideas para reutilizar frascos de vidrio",
        categoria = CategoriaActividad.REUTILIZAR,
        esVideo = false,
        subtemas = listOf(
            SubtemaEducativo("🌱", "Maceta", "Usalo para una planta pequeña."),
            SubtemaEducativo("✏️", "Portalápices", "Guardá lápices, fibras o pinceles."),
            SubtemaEducativo("🕯️", "Portavelas", "Convertí el frasco en una decoración."),
            SubtemaEducativo("🍪", "Almacenamiento", "Guardá alimentos u otros objetos pequeños."),
            SubtemaEducativo("🎨", "Materiales", "Usalo para guardar cosas de arte."),
        ),
    ),
    ContenidoEducativo(
        id = "upcycling",
        titulo = "Upcycling: de la ropa vieja a algo nuevo",
        categoria = CategoriaActividad.REUTILIZAR,
        esVideo = true,
        subtemas = listOf(
            SubtemaEducativo("♻️", "Reutilizá", "Dale un nuevo uso a algo que ya tenés."),
            SubtemaEducativo("✂️", "Transformá", "Cortá, cosé o modificá la ropa."),
            SubtemaEducativo("👜", "Creá", "Una remera puede convertirse en una bolsa."),
            SubtemaEducativo("✨", "Estrená", "Obtené algo nuevo sin comprarlo."),
        ),
    ),
    ContenidoEducativo(
        id = "beneficios-caminar",
        titulo = "Beneficios de caminar para el planeta y tu salud",
        categoria = CategoriaActividad.CAMINAR,
        esVideo = false,
        subtemas = listOf(
            SubtemaEducativo("🌎", "Cuidás el planeta", "Caminar genera menos contaminación que usar un auto."),
            SubtemaEducativo("❤️", "Cuidás tu salud", "Ayuda a mantenerte activo/a."),
            SubtemaEducativo("😊", "Mejorás tu bienestar", "Puede ayudarte a sentirte mejor y despejar la mente."),
            SubtemaEducativo("🚗", "Menos autos", "Más personas caminando significa menos viajes en auto."),
        ),
    ),
    ContenidoEducativo(
        id = "rutina-caminata",
        titulo = "Cómo armar una rutina de caminata",
        categoria = CategoriaActividad.CAMINAR,
        esVideo = true,
        subtemas = listOf(
            SubtemaEducativo("📅", "Elegí tus días", "Buscá momentos que puedas mantener."),
            SubtemaEducativo("🚶", "Empezá de a poco", "Comenzá con caminatas cortas."),
            SubtemaEducativo("⏱️", "Aumentá el tiempo", "Sumá unos minutos gradualmente."),
            SubtemaEducativo("💧", "Tomá agua", "Llevá agua si la caminata es larga."),
            SubtemaEducativo("🛑", "Escuchá tu cuerpo", "Descansá si te sentís mal o demasiado cansado/a."),
        ),
    ),
)

private fun enlaceMasInfo(titulo: String): String = "https://www.google.com/search?q=" + Uri.encode(titulo)

@HiltViewModel
class ContenidoEducativoViewModel @Inject constructor() : ViewModel() {

    private val _categoriaSeleccionada = MutableStateFlow<CategoriaActividad?>(null)
    val categoriaSeleccionada: StateFlow<CategoriaActividad?> = _categoriaSeleccionada.asStateFlow()

    fun seleccionarCategoria(categoria: CategoriaActividad?) {
        _categoriaSeleccionada.value = categoria
    }
}

/** US6 — artículos y videos filtrables por categoría (RF-016/RF-017). */
@Composable
fun ContenidoEducativoScreen(
    viewModel: ContenidoEducativoViewModel = hiltViewModel(),
    onSeleccionarContenido: (String) -> Unit = {},
    onVolver: () -> Unit = {},
) {
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val contenidoFiltrado = if (categoriaSeleccionada == null) {
        CATALOGO_CONTENIDO
    } else {
        CATALOGO_CONTENIDO.filter { it.categoria == categoriaSeleccionada }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoConUsuario(onVolver = onVolver) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = categoriaSeleccionada == null,
                    onClick = { viewModel.seleccionarCategoria(null) },
                    label = { Text("Todas", maxLines = 1, softWrap = false) },
                    modifier = Modifier.weight(1f),
                )
                CategoriaActividad.entries.forEach { categoria ->
                    FilterChip(
                        selected = categoriaSeleccionada == categoria,
                        onClick = { viewModel.seleccionarCategoria(categoria) },
                        label = { Text(etiquetaCategoria(categoria), maxLines = 1, softWrap = false) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(contenidoFiltrado, key = { it.id }) { contenido ->
                    TarjetaEcoSmart(modifier = Modifier.clickable { onSeleccionarContenido(contenido.id) }) {
                        Text(text = contenido.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = if (contenido.esVideo) "Video" else "Artículo",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

/** Detalle de un tema de "Aprender más": subtemas (emoji + subtítulo + descripción) y un enlace externo con más info. */
@Composable
fun ContenidoEducativoDetalleScreen(id: String, onVolver: () -> Unit = {}) {
    val contenido = CATALOGO_CONTENIDO.firstOrNull { it.id == id }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoConUsuario(onVolver = onVolver) },
    ) { padding ->
        if (contenido == null) {
            Text(
                text = "No encontramos este contenido.",
                modifier = Modifier.padding(padding).padding(20.dp),
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = contenido.titulo, style = MaterialTheme.typography.titleLarge)
            contenido.subtemas.forEach { subtema ->
                TarjetaEcoSmart {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = subtema.emoji, style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text(text = subtema.subtitulo, style = MaterialTheme.typography.titleSmall)
                            Text(text = subtema.descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Button(
                onClick = { uriHandler.openUri(enlaceMasInfo(contenido.titulo)) },
                shape = FormaBotonPildora,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ver más")
            }
        }
    }
}
