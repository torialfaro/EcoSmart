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

/** US3 — edición de perfil y cambio de contraseña (RF-007 a RF-009). */
@Composable
fun PerfilEdicionScreen(viewModel: PerfilEdicionViewModel = hiltViewModel()) {
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

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Tu perfil", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") })
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") })
            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = { nombreUsuario = it },
                label = { Text("Nombre de usuario") },
            )
            SelectorBarrio(
                barrioSeleccionado = barrio,
                onSeleccionar = { barrio = it },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })

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

            Button(
                enabled = !guardando && barrio != null,
                onClick = {
                    val barrioElegido = barrio
                    if (barrioElegido != null) {
                        onGuardar(
                            DatosPerfil(
                                email = email,
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
                enabled = !guardando,
                onClick = { onCambiarContrasena(contrasenaActual, contrasenaNueva) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Actualizar contraseña")
            }
        }
    }
}
