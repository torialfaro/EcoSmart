package com.ecosmart.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.app.R
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorGreen900
import com.ecosmart.presentation.theme.ColorSurfaceCard
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextSecondary
import com.ecosmart.presentation.theme.ColorTrack
import com.ecosmart.presentation.theme.CremaTexto
import com.ecosmart.presentation.theme.ElevacionTarjeta
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.FormaTarjeta
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.SpaceSection
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeCardTitle
import com.ecosmart.presentation.theme.TypeGreeting
import com.ecosmart.presentation.theme.TypeMeta
import com.ecosmart.presentation.theme.TypeTitle

/**
 * Pantalla Perfil reconstruida según S-06 (especificacion-ui-app-eco.md §9):
 * Avatar de 56 pt, nombre de usuario, "Nivel N · puntos", tarjeta de estadísticas
 * de 3 columnas (Actividades / Recompensas / Racha) y 5 filas de menú con icono, texto y chevron.
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
                modifier = Modifier.fillMaxSize().padding(padding),
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
            onEliminarCuenta = viewModel::solicitarEliminarCuenta,
            onConfirmarEliminarCuenta = viewModel::confirmarEliminarCuenta,
            onCancelarEliminarCuenta = viewModel::cancelarEliminarCuenta,
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
    onEliminarCuenta: () -> Unit,
    onConfirmarEliminarCuenta: () -> Unit,
    onCancelarEliminarCuenta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val usuario = uiState.usuario ?: return
    val primerNombre = usuario.nombre.ifBlank { usuario.nombreUsuario }
    val nivelTexto = "Nivel ${nivelNumero(uiState.nivel)} · ${formatearPuntos(uiState.puntosHistoricos)} pts"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SpaceScreenX, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // S06-A: Avatar 56 pt
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ColorGreen900)
                .clickable(onClick = onEditarPerfil),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = primerNombre.take(1).uppercase(),
                style = TypeTitle.copy(fontSize = 24.sp),
                color = CremaTexto,
            )
        }

        // S06-B: Nombre
        Text(
            text = primerNombre,
            style = TypeGreeting,
            color = ColorTextPrimary,
        )

        // S06-C: Nivel y puntos
        Text(
            text = nivelTexto,
            style = TypeBody,
            color = ColorTextSecondary,
        )

        // S06-D: Tarjeta de estadísticas de 3 columnas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColumnaEstadistica(
                    valor = (uiState.puntosHistoricos / 50).coerceAtLeast(1).toString(),
                    etiqueta = "Actividades",
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(ColorTrack))
                ColumnaEstadistica(
                    valor = "2",
                    etiqueta = "Recompensas",
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(ColorTrack))
                ColumnaEstadistica(
                    valor = if (uiState.rachaActual > 0) "${uiState.rachaActual} d" else "0",
                    etiqueta = "Racha",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(modifier = Modifier.height(8.dp))

        // S06-E: 5 filas de menú con icono, texto y chevron
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FormaTarjeta,
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FilaMenuPerfil(
                    icono = Icons.Filled.Person,
                    texto = "Mi perfil",
                    onClick = onEditarPerfil,
                )
                HorizontalDivider(color = ColorTrack, thickness = 1.dp)
                FilaMenuPerfil(
                    icono = Icons.Filled.Check,
                    texto = "Mis actividades",
                    onClick = onVerHistorial,
                )
                HorizontalDivider(color = ColorTrack, thickness = 1.dp)
                FilaMenuPerfil(
                    icono = Icons.Filled.Star,
                    texto = "Mis recompensas",
                    onClick = onVerHistorial,
                )
                HorizontalDivider(color = ColorTrack, thickness = 1.dp)
                FilaMenuPerfil(
                    icono = ImageVector.vectorResource(R.drawable.ic_caminar),
                    texto = "Configuración",
                    onClick = onEditarPerfil,
                )
                HorizontalDivider(color = ColorTrack, thickness = 1.dp)
                FilaMenuPerfil(
                    icono = ImageVector.vectorResource(R.drawable.ic_hoja),
                    texto = "Ayuda",
                    onClick = onEditarPerfil,
                )
            }
        }

        Box(modifier = Modifier.height(8.dp))

        // Acciones de cuenta
        OutlinedButton(
            onClick = onCerrarSesion,
            shape = FormaBotonPildora,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Cerrar sesión", color = ColorGreen700, style = TypeCardTitle)
        }

        OutlinedButton(
            onClick = onEliminarCuenta,
            shape = FormaBotonPildora,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error, style = TypeCardTitle)
        }
    }

    if (uiState.mostrarConfirmacionEliminarCuenta) {
        AlertDialog(
            onDismissRequest = onCancelarEliminarCuenta,
            title = { Text("¿Eliminar tu cuenta?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se borra de inmediato tu perfil, tu historial y tu acceso. No hay forma de deshacerlo.")
                    uiState.mensajeErrorEliminarCuenta?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmarEliminarCuenta, enabled = !uiState.eliminandoCuenta) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarEliminarCuenta, enabled = !uiState.eliminandoCuenta) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ColumnaEstadistica(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = valor,
            style = TypeCardTitle.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
            color = ColorTextPrimary,
        )
        Text(
            text = etiqueta,
            style = TypeMeta,
            color = ColorTextSecondary,
        )
    }
}

@Composable
private fun FilaMenuPerfil(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = texto
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = ColorGreen700,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = texto,
                style = TypeCardTitle,
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

private fun nivelNumero(nivel: NivelUsuario): Int = when (nivel) {
    NivelUsuario.SEMILLA -> 1
    NivelUsuario.BROTE -> 2
    NivelUsuario.PLANTA -> 3
    NivelUsuario.ARBOL -> 4
}

private fun formatearPuntos(puntos: Int): String {
    val texto = puntos.toString()
    if (texto.length <= 3) return texto
    val prefix = texto.substring(0, texto.length - 3)
    val suffix = texto.substring(texto.length - 3)
    return "$prefix.$suffix"
}

private fun etiquetaCategoria(categoria: CategoriaActividad): String = when (categoria) {
    CategoriaActividad.RECICLAR -> "Reciclar"
    CategoriaActividad.REUTILIZAR -> "Reutilizar"
    CategoriaActividad.CAMINAR -> "Caminar"
}
