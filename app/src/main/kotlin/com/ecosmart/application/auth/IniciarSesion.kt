package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class ResultadoInicioSesion {
    data class Exitoso(val usuario: Usuario) : ResultadoInicioSesion()
    data object CredencialesInvalidas : ResultadoInicioSesion()
}

/**
 * Login con correo/contraseña vía Firebase Authentication (RF-D002, spec
 * 002-firestore-datos-usuario) — reemplaza a `UsuarioRepository.autenticar`/
 * `CifradorContrasena`, estructuralmente imposibles ahora que Firestore no guarda
 * ninguna forma de la contraseña.
 */
class IniciarSesion @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(email: String, contrasenaPlana: String): ResultadoInicioSesion {
        val credencial = try {
            firebaseAuth.signInWithEmailAndPassword(email, contrasenaPlana).await()
        } catch (credencialesInvalidas: FirebaseAuthInvalidCredentialsException) {
            return ResultadoInicioSesion.CredencialesInvalidas
        } catch (usuarioInvalido: FirebaseAuthInvalidUserException) {
            return ResultadoInicioSesion.CredencialesInvalidas
        }
        val uid = credencial.user?.uid ?: return ResultadoInicioSesion.CredencialesInvalidas
        val usuario = usuarioRepository.buscarPorId(UsuarioId(uid)) ?: return ResultadoInicioSesion.CredencialesInvalidas
        return ResultadoInicioSesion.Exitoso(usuario)
    }
}

