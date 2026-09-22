package com.ecosmart.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ecosmart.domain.valueobject.CategoriaActividad
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
private data class ContenidoEducativo(
    val titulo: String,
    val categoria: CategoriaActividad,
    val esVideo: Boolean,
)

private val CATALOGO_CONTENIDO = listOf(
    ContenidoEducativo("Cómo separar tus residuos en casa", CategoriaActividad.RECICLAR, esVideo = false),
    ContenidoEducativo("El circuito del reciclaje en CABA", CategoriaActividad.RECICLAR, esVideo = true),
    ContenidoEducativo("5 ideas para reutilizar frascos de vidrio", CategoriaActividad.REUTILIZAR, esVideo = false),
    ContenidoEducativo("Upcycling: de la ropa vieja a algo nuevo", CategoriaActividad.REUTILIZAR, esVideo = true),
    ContenidoEducativo("Beneficios de caminar para el planeta y tu salud", CategoriaActividad.CAMINAR, esVideo = false),
    ContenidoEducativo("Cómo armar una rutina de caminata", CategoriaActividad.CAMINAR, esVideo = true),
)

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
fun ContenidoEducativoScreen(viewModel: ContenidoEducativoViewModel = hiltViewModel()) {
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val contenidoFiltrado = if (categoriaSeleccionada == null) {
        CATALOGO_CONTENIDO
    } else {
        CATALOGO_CONTENIDO.filter { it.categoria == categoriaSeleccionada }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = categoriaSeleccionada == null,
                    onClick = { viewModel.seleccionarCategoria(null) },
                    label = { Text("Todas") },
                )
                CategoriaActividad.entries.forEach { categoria ->
                    FilterChip(
                        selected = categoriaSeleccionada == categoria,
                        onClick = { viewModel.seleccionarCategoria(categoria) },
                        label = { Text(categoria.name) },
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(contenidoFiltrado) { contenido ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = contenido.titulo, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (contenido.esVideo) "Video" else "Artículo",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}
