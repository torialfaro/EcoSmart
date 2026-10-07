package com.ecosmart.infrastructure.session

import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Expone el `uid` del usuario autenticado (spec 002-firestore-datos-usuario, RF-D005),
 * reemplazando a las `SharedPreferences` propias de spec 001: `FirebaseAuth` ya persiste
 * la sesión entre reinicios de la app por sí solo (RF-010/RF-061 quedan satisfechos sin
 * código adicional acá).
 */
@Singleton
class SesionUsuario @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    private val _usuarioActualId = MutableStateFlow(firebaseAuth.currentUser?.uid?.let(::UsuarioId))
    val usuarioActualId: StateFlow<UsuarioId?> = _usuarioActualId.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _usuarioActualId.value = auth.currentUser?.uid?.let(::UsuarioId)
        }
    }

    @Deprecated(
        "FirebaseAuth ya actualiza el uid actual automáticamente tras un login exitoso " +
            "(ver T027/T028); este método queda como no-op hasta que esos callers se actualicen.",
    )
    fun iniciarSesion(usuarioId: UsuarioId) {
        // No-op intencional — ver @Deprecated.
    }

    fun cerrarSesion() {
        firebaseAuth.signOut()
    }
}
