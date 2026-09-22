package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import javax.inject.Inject

sealed class ResultadoInicioSesion {
    data class Exitoso(val usuario: Usuario) : ResultadoInicioSesion()
    data object CredencialesInvalidas : ResultadoInicioSesion()
}

/**
 * Login con correo/contraseña de una cuenta ya existente (RF-001, RF-010).
 * No es una tarea explícita de tasks.md, pero el propio "Independent Test"
 * del Módulo 1 ("...cerrar sesión → volver a iniciar sesión...") lo
 * requiere; se agrega junto al resto del módulo de autenticación.
 */
class IniciarSesion @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(email: String, contrasenaPlana: String): ResultadoInicioSesion {
        val usuario = usuarioRepository.autenticar(email, contrasenaPlana)
            ?: return ResultadoInicioSesion.CredencialesInvalidas
        return ResultadoInicioSesion.Exitoso(usuario)
    }
}
