package com.ecosmart.presentation.verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * US9 — pantalla de resultado de una verificación de foto
 * (Aprobado/Rechazado/Indeterminado/Timeout/…), en tono amable y nunca
 * punitivo (RNF-001, RF-028 a RF-030).
 */
@Composable
fun ResultadoVerificacionScreen(
    resultado: ResultadoUiFoto,
    onVolverAIntentar: () -> Unit,
    onAtras: () -> Unit,
    onVerProgreso: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (resultado) {
                is ResultadoUiFoto.Aprobado -> {
                    Titulo("¡Aprobado! Sumaste ${resultado.puntosOtorgados} puntos.")
                    Button(onClick = onVerProgreso, modifier = Modifier.fillMaxWidth()) { Text("Ver progreso") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                is ResultadoUiFoto.Rechazado -> {
                    Titulo("No pudimos aprobar esta foto")
                    Text(resultado.motivo)
                    Button(onClick = onVolverAIntentar, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                is ResultadoUiFoto.Indeterminado -> {
                    Titulo("Necesitamos una foto más clara")
                    Text(resultado.motivo)
                    Button(onClick = onVolverAIntentar, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                ResultadoUiFoto.TopeDiarioAlcanzado -> {
                    Titulo("Ya alcanzaste el límite de hoy para esta actividad")
                    Text("Mañana se renueva automáticamente. ¡Volvé pronto!")
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                ResultadoUiFoto.DuplicadaLocalmente -> {
                    Titulo("Esta foto ya la usaste antes")
                    Text("Probá con una foto nueva de esta actividad.")
                    Button(onClick = onVolverAIntentar, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                ResultadoUiFoto.Timeout -> {
                    Titulo("EcoGPT tardó más de lo esperado")
                    Text("Tu foto y descripción se conservaron. Podés reintentar cuando quieras.")
                    Button(onClick = onVolverAIntentar, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                ResultadoUiFoto.SinConexion -> {
                    Titulo("Sin conexión a internet")
                    Text("Tu foto y descripción se conservaron. Reintentá cuando tengas señal.")
                    Button(onClick = onVolverAIntentar, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
                is ResultadoUiFoto.Error -> {
                    Titulo("Algo no salió como esperábamos")
                    Text(resultado.mensaje)
                    Button(onClick = onVolverAIntentar, modifier = Modifier.fillMaxWidth()) { Text("Volver a intentar") }
                    TextButton(onClick = onAtras, modifier = Modifier.fillMaxWidth()) { Text("Atrás") }
                }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(text = texto, style = MaterialTheme.typography.titleLarge)
}
