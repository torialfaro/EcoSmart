package com.ecosmart.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.NivelUsuario

/**
 * US11 + US3 — perfil consolidado, de solo lectura (RF-068): datos de
 * cuenta completos y métricas de actividad, con un botón "Editar perfil"
 * hacia la pantalla de edición.
 */
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel(),
    onVerHistorial: () -> Unit,
    onEditarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuario = uiState.usuario

    Scaffold { padding ->
        if (uiState.cargando || usuario == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Tu perfil", style = MaterialTheme.typography.titleLarge)

            // RF-068: datos de cuenta completos, no solo lo relacionado con actividades.
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DatoDePerfil("Nombre", "${usuario.nombre} ${usuario.apellido}")
                    DatoDePerfil("Usuario", "@${usuario.nombreUsuario}")
                    DatoDePerfil("Correo", usuario.email)
                    DatoDePerfil("Barrio", usuario.barrio?.nombreVisible ?: "No configurado")
                    DatoDePerfil("Teléfono", usuario.telefono.ifBlank { "No configurado" })
                    DatoDePerfil("Categorías de interés", etiquetaCategorias(usuario))
                }
            }

            Button(onClick = onEditarPerfil, modifier = Modifier.fillMaxWidth()) {
                Text("Editar perfil")
            }

            Text(text = "Tu progreso", style = MaterialTheme.typography.titleLarge)

            // RF-070, corrección post-QA: pasos de hoy, visibles solo si el permiso de Podómetro está otorgado.
            uiState.pasosHoy?.let { pasosHoy ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Pasos hoy", style = MaterialTheme.typography.labelLarge)
                        Text(text = "$pasosHoy", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${uiState.puntosHistoricos} puntos", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Nivel: ${etiquetaNivel(uiState.nivel)}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (uiState.rachaActual > 0) {
                            "Racha: ${uiState.rachaActual} día(s) consecutivos"
                        } else {
                            "Todavía no tenés una racha activa. ¡Arrancá hoy!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Text(text = "Participación por categoría", style = MaterialTheme.typography.labelLarge)
            if (uiState.porcentajePorCategoria.isEmpty()) {
                Text("Todavía no completaste ninguna actividad.")
            } else {
                uiState.porcentajePorCategoria.forEach { (categoria, porcentaje) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(etiquetaCategoria(categoria))
                            Text("${porcentaje.toInt()}%")
                        }
                        LinearProgressIndicator(
                            progress = { (porcentaje / 100).toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Button(onClick = onVerHistorial, modifier = Modifier.fillMaxWidth()) {
                Text("Ver historial")
            }

            OutlinedButton(
                onClick = {
                    viewModel.cerrarSesion()
                    onCerrarSesion()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun DatoDePerfil(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = etiqueta, style = MaterialTheme.typography.labelLarge)
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun etiquetaCategorias(usuario: Usuario): String =
    if (usuario.categoriasDeInteres.isEmpty()) {
        "Ninguna"
    } else {
        usuario.categoriasDeInteres.joinToString(", ") { etiquetaCategoria(it) }
    }

private fun etiquetaNivel(nivel: NivelUsuario): String = when (nivel) {
    NivelUsuario.SEMILLA -> "Semilla"
    NivelUsuario.BROTE -> "Brote"
    NivelUsuario.PLANTA -> "Planta"
    NivelUsuario.ARBOL -> "Árbol"
}

private fun etiquetaCategoria(categoria: CategoriaActividad): String = when (categoria) {
    CategoriaActividad.RECICLAR -> "Reciclar"
    CategoriaActividad.REUTILIZAR -> "Reutilizar"
    CategoriaActividad.CAMINAR -> "Caminar"
}
