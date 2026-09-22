package com.ecosmart.presentation.activitydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad

/** US5 — pasos a seguir y resultado esperado, ausentes en la tarjeta de Home (RF-014/RF-015). */
@Composable
fun ActividadDetalleScreen(
    viewModel: ActividadDetalleViewModel = hiltViewModel(),
    onRealizarla: (ActividadId, CategoriaActividad) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val actividad = uiState.actividad

    if (uiState.cargando || actividad == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = actividad.descripcionCorta, style = MaterialTheme.typography.titleLarge)
            Text(text = "+${actividad.puntosBase} pts", style = MaterialTheme.typography.labelLarge)

            Text(text = "Pasos a seguir", style = MaterialTheme.typography.labelLarge)
            Text(text = actividad.pasosASeguir)

            Text(text = "Resultado esperado", style = MaterialTheme.typography.labelLarge)
            Text(text = actividad.resultadoEsperado)

            Button(
                onClick = { onRealizarla(actividad.id, actividad.categoria) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Realizarla")
            }
        }
    }
}
