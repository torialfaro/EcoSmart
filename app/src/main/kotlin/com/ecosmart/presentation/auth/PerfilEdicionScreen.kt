package com.ecosmart.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.application.auth.DatosPerfil
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.esTelefonoValido
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaBotonPildora

/**
 * US3 — edición de perfil y cambio de contraseña (RF-007 a RF-009).
 * Corrección post-QA (RF-068): recién ahora se conecta a la navegación,
 * desde el botón "Editar perfil" de `PerfilScreen`.
 */
@Composable
fun PerfilEdicionScreen(
    viewModel: PerfilEdicionViewModel = hiltViewModel(),
    onVolver: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuario = uiState.usuario

    if (usuario == null) {
        Scaffold { padding ->
            Text(text = "Cargando perfil…", modifier = Modifier.padding(padding).padding(24.dp))
        }
        return
    }

    CamposDePerfil(
        usuario = usuario,
        guardando = uiState.guardando,
        mensajeError = uiState.mensajeError,
        mensajeExito = uiState.mensajeExito,
        onGuardar = viewModel::guardarPerfil,
        onCambiarContrasena = viewModel::actualizarContrasena,
        onVolver = onVolver,
    )
}

@Composable
private fun CamposDePerfil(
    usuario: Usuario,
    guardando: Boolean,
    mensajeError: String?,
    mensajeExito: String?,
    onGuardar: (DatosPerfil) -> Unit,
    onCambiarContrasena: (String, String) -> Unit,
    onVolver: () -> Unit,
) {
    var email by remember(usuario.id) { mutableStateOf(usuario.email) }
    var nombre by remember(usuario.id) { mutableStateOf(usuario.nombre) }
    var apellido by remember(usuario.id) { mutableStateOf(usuario.apellido) }
    var nombreUsuario by remember(usuario.id) { mutableStateOf(usuario.nombreUsuario) }
    var barrio by remember(usuario.id) { mutableStateOf(usuario.barrio) }
    var telefono by remember(usuario.id) { mutableStateOf(usuario.telefono) }
    var categorias by remember(usuario.id) { mutableStateOf(usuario.categoriasDeInteres) }

    var contrasenaActual by remember { mutableStateOf("") }
    var contrasenaNueva by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoEcoSmart(inicialUsuario = usuario.nombreUsuario.ifBlank { "U" }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = onVolver) { Text("← Volver") }

            Text(text = "Editar perfil", style = MaterialTheme.typography.titleLarge)

            // Corrección post-QA (2026-09-23): el correo NUNCA se edita desde acá (única excepción).
            OutlinedTextField(
                value = email,
                onValueChange = {},
                readOnly = true,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                isError = nombre.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                isError = apellido.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = { nombreUsuario = it },
                label = { Text("Nombre de usuario") },
                isError = nombreUsuario.isBlank(),
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
                placeholder = { Text("+54911XXXXXXXX") },
                isError = telefono.isNotBlank() && !esTelefonoValido(telefono),
                supportingText = { Text("Formato: +54911XXXXXXXX") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(text = "Categorías de interés", style = MaterialTheme.typography.labelLarge)
            CategoriaActividad.entries.forEach { categoria ->
                Row {
                    Checkbox(
                        checked = categoria in categorias,
                        onCheckedChange = { marcado ->
                            categorias = if (marcado) categorias + categoria else categorias - categoria
                        },
                    )
                    Text(text = categoria.name)
                }
            }

            mensajeError?.let { Text(text = it, color = MaterialTheme.colorScheme.tertiary) }
            mensajeExito?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }

            val formularioValido = barrio != null &&
                nombre.isNotBlank() &&
                apellido.isNotBlank() &&
                nombreUsuario.isNotBlank() &&
                esTelefonoValido(telefono)

            Button(
                shape = FormaBotonPildora,
                enabled = !guardando && formularioValido,
                onClick = {
                    val barrioElegido = barrio
                    if (barrioElegido != null) {
                        onGuardar(
                            DatosPerfil(
                                nombre = nombre,
                                apellido = apellido,
                                nombreUsuario = nombreUsuario,
                                barrio = barrioElegido,
                                telefono = telefono,
                                categoriasDeInteres = categorias,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Guardar cambios")
            }

            HorizontalDivider()

            Text(text = "Cambiar contraseña", style = MaterialTheme.typography.labelLarge)
            CampoContrasena(
                value = contrasenaActual,
                onValueChange = { contrasenaActual = it },
                label = "Contraseña actual",
                modifier = Modifier.fillMaxWidth(),
            )
            CampoContrasena(
                value = contrasenaNueva,
                onValueChange = { contrasenaNueva = it },
                label = "Contraseña nueva",
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                shape = FormaBotonPildora,
                enabled = !guardando,
                onClick = { onCambiarContrasena(contrasenaActual, contrasenaNueva) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Actualizar contraseña")
            }
        }
    }
}
