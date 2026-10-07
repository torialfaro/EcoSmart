package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DatosCuentaGoogle(
    /** ID Token de Google (`GoogleSignInAccount.idToken`, `play-services-auth`) obtenido
     * en la capa de presentación — ver research.md §2. */
    val idTokenGoogle: String,
)

/**
 * RF-002 — canjea el ID Token de Google por una credencial de Firebase Authentication
 * (RF-D002, spec 002-firestore-datos-usuario) y crea (o recupera, si ya existía) el
 * documento `usuarios/{uid}` asociado. Nombre, apellido y email salen del `FirebaseUser`
 * ya verificado, no de un valor pasado a mano como en spec 001.
 */
class IniciarSesionConGoogle @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(datos: DatosCuentaGoogle): Usuario {
        val credencialGoogle = GoogleAuthProvider.getCredential(datos.idTokenGoogle, null)
        val resultado = firebaseAuth.signInWithCredential(credencialGoogle).await()
        val firebaseUser = requireNotNull(resultado.user) { "Firebase no devolvió un usuario tras signInWithCredential" }

        usuarioRepository.buscarPorId(UsuarioId(firebaseUser.uid))?.let { return it }

        val nombreCompleto = firebaseUser.displayName.orEmpty()
        val nuevo = Usuario(
            id = UsuarioId(firebaseUser.uid),
            email = firebaseUser.email.orEmpty(),
            // Sin contraseña local: el login de esta cuenta siempre pasa por Google (RF-D002).
            contrasenaCifrada = ContrasenaCifrada(""),
            nombre = nombreCompleto.substringBefore(" ").ifBlank { "Usuario" },
            apellido = nombreCompleto.substringAfter(" ", missingDelimiterValue = ""),
            nombreUsuario = firebaseUser.email.orEmpty().substringBefore("@"),
            // Google no pide barrio; el usuario lo completa después desde su perfil (RF-007/RF-063).
            barrio = null,
            telefono = "",
            categoriasDeInteres = emptySet(),
        )
        usuarioRepository.guardar(nuevo)
        return nuevo
    }
}

