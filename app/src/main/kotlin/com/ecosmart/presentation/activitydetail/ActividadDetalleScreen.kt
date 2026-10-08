package com.ecosmart.presentation.activitydetail

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.EncabezadoConUsuario
import com.ecosmart.presentation.theme.CremaTexto
import com.ecosmart.presentation.theme.FormaBotonPildora
import com.ecosmart.presentation.theme.TarjetaEcoSmart
import com.ecosmart.presentation.theme.VerdeBoton

/**
 * US5 — pasos a seguir y resultado esperado, ausentes en la tarjeta de Home
 * (RF-014/RF-015), con la distribución del mockup (RF-083): tarjeta de
 * título + puntos, pasos numerados en círculos, tarjeta de "Resultado
 * esperado" con check y botón "Realizarla" en píldora.
 */
@Composable
fun ActividadDetalleScreen(
    viewModel: ActividadDetalleViewModel = hiltViewModel(),
    onRealizarla: (ActividadId, CategoriaActividad) -> Unit,
    onVolver: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val actividad = uiState.actividad

    if (uiState.cargando || actividad == null) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoConUsuario(onVolver = onVolver) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TarjetaEcoSmart {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(R.drawable.logo_ecosmart_icono),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                    )
                    Column {
                        Text(text = actividad.descripcionCorta, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "+${actividad.puntosBase} pts",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }

            Text(text = "Pasos a seguir", style = MaterialTheme.typography.titleMedium)
            actividad.pasosASeguir.lines().filter { it.isNotBlank() }.forEachIndexed { indice, paso ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(VerdeBoton),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${indice + 1}", style = MaterialTheme.typography.labelMedium, color = CremaTexto)
                    }
                    Text(text = paso.replace(Regex("^\\d+\\.\\s*"), ""), style = MaterialTheme.typography.bodyMedium)
                }
            }

            TarjetaEcoSmart {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(VerdeBoton),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = CremaTexto, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Resultado esperado", style = MaterialTheme.typography.titleSmall)
                        Text(text = actividad.resultadoEsperado, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = { onRealizarla(actividad.id, actividad.categoria) },
                shape = FormaBotonPildora,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Realizarla")
            }
            Box(modifier = Modifier.size(8.dp))
        }
    }
}
