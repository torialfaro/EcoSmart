package com.ecosmart.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.app.R
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.TarjetaEcoSmart
import com.ecosmart.presentation.theme.VerdeBarra
import com.ecosmart.presentation.theme.VerdeBoton

/**
 * US4 — Home con actividades agrupadas por categoría de interés (RF-011 a
 * RF-013), con la distribución del mockup (RF-083): encabezado con logo y
 * saludo, pestañas "Puntos Verdes"/"Aprender más" y tarjetas verdes con
 * botón "Realizarla" en píldora.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSeleccionarActividad: (ActividadId) -> Unit,
    onVerPuntosVerdes: () -> Unit,
    onVerContenidoEducativo: () -> Unit,
    onVerPerfil: () -> Unit,
    barraInferior: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EncabezadoEcoSmart(
                inicialUsuario = uiState.nombreUsuario.ifBlank { "U" },
                saludo = if (uiState.nombreUsuario.isNotBlank()) "Hola, ${uiState.nombreUsuario}" else null,
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
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onVerPuntosVerdes,
                        shape = FormaBotonPildora,
                        modifier = Modifier.weight(1f),
                    ) { Text("Puntos Verdes") }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(FormaBotonPildora)
                            .border(1.dp, VerdeBarra, FormaBotonPildora)
                            .clickable(onClick = onVerContenidoEducativo)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Aprender más", style = MaterialTheme.typography.labelLarge, color = VerdeBoton)
                    }
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
private fun TarjetaActividad(actividad: Actividad, onClick: () -> Unit) {
    TarjetaEcoSmart(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_ecosmart_icono),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
            }
            Column {
                Text(text = actividad.descripcionCorta, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "+${actividad.puntosBase} pts",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
        Button(
            onClick = onClick,
            shape = FormaBotonPildora,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Realizarla")
        }
    }
}

private fun etiquetaCategoria(categoria: CategoriaActividad): String = when (categoria) {
    CategoriaActividad.RECICLAR -> "Reciclar"
    CategoriaActividad.REUTILIZAR -> "Reutilizar"
    CategoriaActividad.CAMINAR -> "Caminar"
}
