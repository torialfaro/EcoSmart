package com.ecosmart.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.app.R
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.presentation.theme.AmbarPasos
import com.ecosmart.presentation.theme.CremaTexto
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.TarjetaEcoSmart
import com.ecosmart.presentation.theme.VerdeBarra
import com.ecosmart.presentation.theme.VerdeBoton
import com.ecosmart.presentation.theme.VerdeOscuro

/**
 * US11 + US3 — perfil consolidado, de solo lectura (RF-068), con la
 * distribución del mockup (RF-083): tarjeta de usuario, filas de datos con
 * ícono, "Tu progreso" (pasos hoy en ámbar, puntos y nivel, racha) y, debajo,
 * la participación por categoría y las acciones ya existentes.
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
    val usuario = uiState.usuario

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EncabezadoEcoSmart(
                inicialUsuario = usuario?.nombreUsuario?.ifBlank { "U" } ?: "U",
                saludo = usuario?.let { "Hola, ${it.nombreUsuario}" },
            )
        },
        bottomBar = barraInferior,
    ) { padding ->
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // RF-068: datos de cuenta completos, no solo lo relacionado con actividades.
            TarjetaEcoSmart {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(VerdeOscuro),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = CremaTexto, modifier = Modifier.size(34.dp))
                    }
                    Column {
                        Text("${usuario.nombre} ${usuario.apellido}", style = MaterialTheme.typography.titleMedium)
                        Text("@${usuario.nombreUsuario}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            FilaDato(Icons.Filled.Email, usuario.email)
            FilaDato(Icons.Filled.Place, usuario.barrio?.nombreVisible ?: "No configurado")
            FilaDato(Icons.Filled.Phone, usuario.telefono.ifBlank { "No configurado" })
            FilaDato(
                ImageVector.vectorResource(R.drawable.ic_hoja),
                "Categorías de interés\n${etiquetaCategorias(usuario)}",
            )

            Text(text = "Tu progreso", style = MaterialTheme.typography.titleMedium)

            // RF-070: solo se muestra con el permiso de Podómetro otorgado.
            uiState.pasosHoy?.let { pasosHoy ->
                TarjetaEcoSmart(color = AmbarPasos) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = VerdeOscuro, modifier = Modifier.size(34.dp))
                        Column {
                            Text("Pasos hoy", style = MaterialTheme.typography.bodySmall)
                            Text("$pasosHoy", style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp))
                        }
                    }
                }
            }

            TarjetaEcoSmart {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = VerdeBoton, modifier = Modifier.size(34.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${uiState.puntosHistoricos} puntos", style = MaterialTheme.typography.titleMedium)
                        Text("Nivel: ${etiquetaNivel(uiState.nivel)}", style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(
                            progress = { progresoDeNivel(uiState.puntosHistoricos, uiState.nivel) },
                            modifier = Modifier.fillMaxWidth(),
                            color = VerdeBoton,
                            trackColor = VerdeBarra,
                        )
                    }
                }
            }

            Text(
                text = if (uiState.rachaActual > 0) {
                    "Racha: ${uiState.rachaActual} día(s) consecutivos"
                } else {
                    "Todavía no tenés una racha activa. ¡Arrancá hoy!"
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(text = "Participación por categoría", style = MaterialTheme.typography.titleSmall)
            if (uiState.porcentajePorCategoria.isEmpty()) {
                Text("Todavía no completaste ninguna actividad.", style = MaterialTheme.typography.bodySmall)
            } else {
                uiState.porcentajePorCategoria.forEach { (categoria, porcentaje) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(etiquetaCategoria(categoria), style = MaterialTheme.typography.bodyMedium)
                            Text("${porcentaje.toInt()}%", style = MaterialTheme.typography.bodyMedium)
                        }
                        LinearProgressIndicator(
                            progress = { (porcentaje / 100).toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = VerdeBoton,
                            trackColor = VerdeBarra,
                        )
                    }
                }
            }

            Button(onClick = onEditarPerfil, shape = FormaBotonPildora, modifier = Modifier.fillMaxWidth()) {
                Text("Editar perfil")
            }
            Button(onClick = onVerHistorial, shape = FormaBotonPildora, modifier = Modifier.fillMaxWidth()) {
                Text("Ver historial")
            }
            OutlinedButton(
                onClick = {
                    viewModel.cerrarSesion()
                    onCerrarSesion()
                },
                shape = FormaBotonPildora,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cerrar sesión")
            }
            OutlinedButton(
                onClick = viewModel::solicitarEliminarCuenta,
                shape = FormaBotonPildora,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Eliminar cuenta")
            }
            Box(modifier = Modifier.size(8.dp))
        }
    }

    if (uiState.mostrarConfirmacionEliminarCuenta) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminarCuenta,
            title = { Text("¿Eliminar tu cuenta?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se borra de inmediato tu perfil, tu historial y tu acceso. No hay forma de deshacerlo.")
                    uiState.mensajeErrorEliminarCuenta?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarEliminarCuenta, enabled = !uiState.eliminandoCuenta) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminarCuenta, enabled = !uiState.eliminandoCuenta) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun FilaDato(icono: ImageVector, texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icono, contentDescription = null, tint = VerdeBoton, modifier = Modifier.size(24.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}

private fun progresoDeNivel(puntos: Int, nivel: NivelUsuario): Float = when (nivel) {
    NivelUsuario.SEMILLA -> puntos / 500f
    NivelUsuario.BROTE -> (puntos - 500) / 1000f
    NivelUsuario.PLANTA -> (puntos - 1500) / 2000f
    NivelUsuario.ARBOL -> 1f
}.coerceIn(0f, 1f)

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
