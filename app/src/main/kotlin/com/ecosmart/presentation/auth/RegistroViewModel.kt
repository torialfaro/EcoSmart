package com.ecosmart.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.auth.DatosCuentaGoogle
import com.ecosmart.application.auth.DatosRegistro
import com.ecosmart.application.auth.IniciarSesion
import com.ecosmart.application.auth.IniciarSesionConGoogle
import com.ecosmart.application.auth.RegistrarUsuario
import com.ecosmart.application.auth.ResultadoInicioSesion
import com.ecosmart.application.auth.ResultadoRegistro
import com.ecosmart.domain.model.Usuario
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistroUiState(
    val modoLogin: Boolean = false,
    val enviando: Boolean = false,
    val usuarioAutenticado: Usuario? = null,
    val mensajeError: String? = null,
)

/**
 * Cubre tanto el registro (US1/US2, RF-001 a RF-006) como el login de una
 * cuenta existente (RF-001/RF-010) en un solo ViewModel/Screen con un
 * toggle `modoLogin` — tasks.md solo define una pantalla de entrada para
 * el Módulo 1 (T031), y el "Independent Test" del módulo exige poder
 * volver a iniciar sesión desde ahí tras cerrar sesión.
 */
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registrarUsuario: RegistrarUsuario,
    private val iniciarSesionUseCase: IniciarSesion,
    private val iniciarSesionConGoogle: IniciarSesionConGoogle,
    private val sesionUsuario: SesionUsuario,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun alternarModo() {
        _uiState.value = _uiState.value.copy(modoLogin = !_uiState.value.modoLogin, mensajeError = null)
    }

    fun registrar(datos: DatosRegistro) {
        enviar {
            when (val resultado = registrarUsuario(datos)) {
                is ResultadoRegistro.Exitoso -> autenticarEnSesion(resultado.usuario)
                ResultadoRegistro.EmailInvalido -> mostrarError("Ingresá un correo válido.")
                ResultadoRegistro.EmailYaRegistrado -> mostrarError("Ese correo ya tiene una cuenta.")
                ResultadoRegistro.ContrasenaInvalida ->
                    mostrarError("La contraseña necesita al menos 8 caracteres, con letras y números.")
                ResultadoRegistro.SinCategoriasSeleccionadas -> mostrarError("Elegí al menos una categoría.")
            }
        }
    }

    fun iniciarSesionConCredenciales(email: String, contrasena: String) {
        enviar {
            when (val resultado = iniciarSesionUseCase(email, contrasena)) {
                is ResultadoInicioSesion.Exitoso -> autenticarEnSesion(resultado.usuario)
                ResultadoInicioSesion.CredencialesInvalidas -> mostrarError("Correo o contraseña incorrectos.")
            }
        }
    }

    fun continuarConGoogle(datos: DatosCuentaGoogle) {
        enviar { autenticarEnSesion(iniciarSesionConGoogle(datos)) }
    }

    private fun enviar(accion: suspend () -> Unit) {
        _uiState.value = _uiState.value.copy(enviando = true, mensajeError = null)
        viewModelScope.launch { accion() }
    }

    private fun autenticarEnSesion(usuario: Usuario) {
        sesionUsuario.iniciarSesion(usuario.id)
        _uiState.value = _uiState.value.copy(enviando = false, usuarioAutenticado = usuario)
    }

    private fun mostrarError(mensaje: String) {
        _uiState.value = _uiState.value.copy(enviando = false, mensajeError = mensaje)
    }
}
