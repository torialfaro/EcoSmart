package com.ecosmart.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad

/** US4 — Home con actividades agrupadas por categoría de interés (RF-011 a RF-013). */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSeleccionarActividad: (ActividadId) -> Unit,
    onVerPuntosVerdes: () -> Unit,
    onVerContenidoEducativo: () -> Unit,
    onVerPerfil: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EcoSmart") },
                // Corrección post-QA: atajo al perfil desde la Home (avatar circular, arriba a la derecha).
                actions = { AvatarUsuario(nombreUsuario = uiState.nombreUsuario, onClick = onVerPerfil) },
            )
        },
    ) { padding ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = onVerPuntosVerdes) { Text("Puntos Verdes") }
                    Button(onClick = onVerContenidoEducativo) { Text("Aprender más") }
                }
            }
            uiState.actividadesPorCategoria.forEach { (categoria, actividades) ->
                item { Text(text = etiquetaCategoria(categoria), style = MaterialTheme.typography.titleLarge) }
                items(actividades, key = { it.id.valor }) { actividad ->
                    TarjetaActividad(actividad = actividad, onClick = { onSeleccionarActividad(actividad.id) })
                }
            }
        }
    }
}

@Composable
private fun AvatarUsuario(nombreUsuario: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nombreUsuario.take(1).uppercase().ifBlank { "?" },
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun TarjetaActividad(actividad: Actividad, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // RF-013 pide una foto referencial; sin librería de carga de imágenes en el
            // catálogo de dependencias todavía, se deja este espacio reservado.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Text(text = actividad.descripcionCorta, style = MaterialTheme.typography.bodyLarge)
            Text(text = "+${actividad.puntosBase} pts", style = MaterialTheme.typography.labelLarge)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Realizarla")
            }
        }
    }
}

private fun etiquetaCategoria(categoria: CategoriaActividad): String = when (categoria) {
    CategoriaActividad.RECICLAR -> "Reciclar"
    CategoriaActividad.REUTILIZAR -> "Reutilizar"
    CategoriaActividad.CAMINAR -> "Caminar"
}
