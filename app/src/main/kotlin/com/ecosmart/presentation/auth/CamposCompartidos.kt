package com.ecosmart.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.ecosmart.domain.valueobject.Barrio

/**
 * Campo de contraseña reutilizable: oculto por defecto (nunca en texto
 * plano a primera vista, corrección post-QA), con un botón para
 * mostrarla/ocultarla a pedido explícito del usuario. Se usa tanto en
 * Registro/Login (`RegistroScreen`) como en cambio de contraseña
 * (`PerfilEdicionScreen`).
 */
@Composable
fun CampoContrasena(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var mostrar by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (mostrar) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = { mostrar = !mostrar }) {
                Text(if (mostrar) "Ocultar" else "Mostrar")
            }
        },
        modifier = modifier,
    )
}

/**
 * Desplegable con los 48 barrios de CABA (corrección post-QA: reemplaza el
 * campo de dirección de texto libre). El valor elegido es el que usa
 * después la búsqueda de Puntos Verdes por barrio (RF-018/RF-053).
 */
@Composable
fun SelectorBarrio(
    barrioSeleccionado: Barrio?,
    onSeleccionar: (Barrio) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expandido = true }, modifier = Modifier.fillMaxWidth()) {
            Text(barrioSeleccionado?.nombreVisible ?: "Elegí tu barrio")
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            Barrio.entries.forEach { barrio ->
                DropdownMenuItem(
                    text = { Text(barrio.nombreVisible) },
                    onClick = {
                        onSeleccionar(barrio)
                        expandido = false
                    },
                )
            }
        }
    }
}
