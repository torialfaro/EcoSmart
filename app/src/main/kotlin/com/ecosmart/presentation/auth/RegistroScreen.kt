package com.ecosmart.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.application.auth.DatosRegistro
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.FormaBotonPildora

/**
 * Pantalla de registro/login (US1/US2/US3, RF-001 a RF-006, RF-010).
 * Rediseño post-QA 2026-09-22 siguiendo la referencia visual del usuario
 * (campos con etiqueta, botón de Google destacado, separador "o continuá
 * con", botón principal en forma de píldora), con la paleta verde/ámbar de
 * la app en vez de los colores literales del ejemplo. El botón "Continuar
 * con Google" delega en [RegistroViewModel.continuarConGoogle]; el flujo
 * real de Credential Manager/Google Sign-In se cablea cuando exista una
 * `Activity` dedicada (gap señalado desde la Fase 1/2).
 */
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = hiltViewModel(),
    onAutenticado: (Usuario) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.usuarioAutenticado) {
        uiState.usuarioAutenticado?.let(onAutenticado)
    }

    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var barrio by remember { mutableStateOf<Barrio?>(null) }
    var telefono by remember { mutableStateOf("") }
    var categoriasSeleccionadas by remember { mutableStateOf(setOf<CategoriaActividad>()) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (uiState.modoLogin) "Iniciá sesión" else "Creá tu cuenta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            BotonGoogle(
                enabled = !uiState.enviando,
                texto = if (uiState.modoLogin) "Iniciar sesión con Google" else "Registrarme con Google",
                onClick = { /* Google Sign-In se integra junto con MainActivity/Credential Manager */ },
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = if (uiState.modoLogin) "o iniciá sesión con" else "o registrate con",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
            )
            CampoContrasena(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = "Contraseña",
                modifier = Modifier.fillMaxWidth(),
            )

            if (!uiState.modoLogin) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                )
                SelectorBarrio(
                    barrioSeleccionado = barrio,
                    onSeleccionar = { barrio = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(text = "¿Qué te interesa?", style = MaterialTheme.typography.labelLarge)
                CategoriaActividad.entries.forEach { categoria ->
                    Row {
                        Checkbox(
                            checked = categoria in categoriasSeleccionadas,
                            onCheckedChange = { marcado ->
                                categoriasSeleccionadas = if (marcado) {
                                    categoriasSeleccionadas + categoria
                                } else {
                                    categoriasSeleccionadas - categoria
                                }
                            },
                        )
                        Text(text = categoria.name)
                    }
                }
            }

            uiState.mensajeError?.let { mensaje ->
                Text(text = mensaje, color = MaterialTheme.colorScheme.tertiary)
            }

            Button(
                enabled = !uiState.enviando && (uiState.modoLogin || barrio != null),
                shape = FormaBotonPildora,
                onClick = {
                    if (uiState.modoLogin) {
                        viewModel.iniciarSesionConCredenciales(email, contrasena)
                    } else {
                        val barrioElegido = barrio
                        if (barrioElegido != null) {
                            viewModel.registrar(
                                DatosRegistro(
                                    email = email,
                                    contrasenaPlana = contrasena,
                                    nombre = nombre,
                                    apellido = apellido,
                                    nombreUsuario = nombreUsuario,
                                    barrio = barrioElegido,
                                    telefono = telefono,
                                    categoriasDeInteres = categoriasSeleccionadas,
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.modoLogin) "Iniciar sesión" else "Registrarme")
            }

            TextButton(onClick = { viewModel.alternarModo() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (uiState.modoLogin) "¿No tenés cuenta? Registrate" else "¿Ya tenés cuenta? Iniciá sesión")
            }
        }
    }
}

/** Botón de Google destacado (forma píldora + insignia circular "G"), como en la referencia visual. */
@Composable
private fun BotonGoogle(enabled: Boolean, texto: String, onClick: () -> Unit) {
    OutlinedButton(
        enabled = enabled,
        onClick = onClick,
        shape = FormaBotonPildora,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "G", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiary)
            }
            Text(texto)
        }
    }
}
