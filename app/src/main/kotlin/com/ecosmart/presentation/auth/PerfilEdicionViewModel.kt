package com.ecosmart.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.auth.CambiarContrasena
import com.ecosmart.application.auth.DatosPerfil
import com.ecosmart.application.auth.EditarPerfil
import com.ecosmart.application.auth.ResultadoCambioContrasena
import com.ecosmart.application.auth.ResultadoEdicionPerfil
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilEdicionUiState(
    val usuario: Usuario? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
)

/** US3 — edición de perfil, correo y contraseña (RF-007 a RF-009). */
@HiltViewModel
class PerfilEdicionViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val editarPerfil: EditarPerfil,
    private val cambiarContrasenaUseCase: CambiarContrasena,
    sesionUsuario: SesionUsuario,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilEdicionUiState())
    val uiState: StateFlow<PerfilEdicionUiState> = _uiState.asStateFlow()

    init {
        val usuarioId = sesionUsuario.usuarioActualId.value
        if (usuarioId != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(usuario = usuarioRepository.buscarPorId(usuarioId))
            }
        }
    }

    fun guardarPerfil(datos: DatosPerfil) {
        val usuarioId = _uiState.value.usuario?.id ?: return
        _uiState.value = _uiState.value.copy(guardando = true, mensajeError = null, mensajeExito = null)
        viewModelScope.launch {
            when (val resultado = editarPerfil(usuarioId, datos)) {
                is ResultadoEdicionPerfil.Exitoso ->
                    _uiState.value = _uiState.value.copy(
                        usuario = resultado.usuario,
                        guardando = false,
                        mensajeExito = "Perfil actualizado.",
                    )
                ResultadoEdicionPerfil.EmailInvalido ->
                    _uiState.value = _uiState.value.copy(guardando = false, mensajeError = "Ingresá un correo válido.")
                ResultadoEdicionPerfil.EmailYaRegistrado ->
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        mensajeError = "Ese correo ya tiene una cuenta.",
                    )
            }
        }
    }

    fun actualizarContrasena(contrasenaActual: String, contrasenaNueva: String) {
        val usuarioId = _uiState.value.usuario?.id ?: return
        _uiState.value = _uiState.value.copy(guardando = true, mensajeError = null, mensajeExito = null)
        viewModelScope.launch {
            when (cambiarContrasenaUseCase(usuarioId, contrasenaActual, contrasenaNueva)) {
                ResultadoCambioContrasena.Exitoso ->
                    _uiState.value = _uiState.value.copy(guardando = false, mensajeExito = "Contraseña actualizada.")
                ResultadoCambioContrasena.ContrasenaActualIncorrecta ->
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        mensajeError = "La contraseña actual no coincide.",
                    )
                ResultadoCambioContrasena.NuevaContrasenaInvalida ->
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        mensajeError = "La nueva contraseña necesita al menos 8 caracteres, con letras y números.",
                    )
            }
        }
    }
}
