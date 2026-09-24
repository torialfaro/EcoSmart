package com.ecosmart.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.presentation.comun.EncabezadoConUsuario
import com.ecosmart.presentation.theme.TarjetaEcoSmart

/** US12 — 3 actividades recientes con botón "Ver más" que despliega el historial completo (RF-041/RF-042). */
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = hiltViewModel(),
    barraInferior: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoConUsuario() },
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

        val registros = if (uiState.mostrandoCompleto) uiState.completo else uiState.recientes

        if (registros.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Todavía no hay actividades en tu historial.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Text(text = "Tus misiones", style = MaterialTheme.typography.titleLarge) }
            items(registros, key = { it.id.valor }) { registro ->
                TarjetaHistorial(registro)
            }
            if (!uiState.mostrandoCompleto) {
                item {
                    TextButton(onClick = viewModel::verMas, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver más")
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaHistorial(registro: RegistroVerificacion) {
    TarjetaEcoSmart {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = etiquetaCategoria(registro.categoria), style = MaterialTheme.typography.titleSmall)
            Text(text = registro.fecha.toString(), style = MaterialTheme.typography.bodySmall)
        }
        Text(text = etiquetaResultado(registro.resultado), style = MaterialTheme.typography.bodyMedium)
        if (registro.puntosOtorgados > 0) {
            Text(text = "+${registro.puntosOtorgados} pts", style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun etiquetaCategoria(categoria: CategoriaActividad): String = when (categoria) {
    CategoriaActividad.RECICLAR -> "Reciclar"
    CategoriaActividad.REUTILIZAR -> "Reutilizar"
    CategoriaActividad.CAMINAR -> "Caminar"
}

private fun etiquetaResultado(resultado: ResultadoVerificacion): String = when (resultado) {
    ResultadoVerificacion.APROBADO -> "Aprobado"
    ResultadoVerificacion.RECHAZADO -> "No aprobado"
    ResultadoVerificacion.INDETERMINADO -> "Indeterminado"
}
