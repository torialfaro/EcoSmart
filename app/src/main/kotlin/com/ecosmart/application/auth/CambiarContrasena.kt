package com.ecosmart.application.auth

import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.domain.valueobject.esContrasenaValida
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class ResultadoCambioContrasena {
    data object Exitoso : ResultadoCambioContrasena()
    data object ContrasenaActualIncorrecta : ResultadoCambioContrasena()
    data object NuevaContrasenaInvalida : ResultadoCambioContrasena()
}

/**
 * US3 — cambio de contraseña exigiendo la actual (RF-008). RF-D002 (spec
 * 002-firestore-datos-usuario): delega íntegramente en Firebase Authentication —
 * reautentica con la contraseña actual (reemplaza a `CifradorContrasena.coincide`) y
 * actualiza con `FirebaseUser.updatePassword`, sin que Firestore vea ninguna forma de
 * la contraseña en ningún momento.
 */
class CambiarContrasena @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    suspend operator fun invoke(
        usuarioId: UsuarioId,
        contrasenaActual: String,
        contrasenaNueva: String,
    ): ResultadoCambioContrasena {
        if (!esContrasenaValida(contrasenaNueva)) return ResultadoCambioContrasena.NuevaContrasenaInvalida

        val usuarioFirebase = firebaseAuth.currentUser
            ?: return ResultadoCambioContrasena.ContrasenaActualIncorrecta
        val email = usuarioFirebase.email ?: return ResultadoCambioContrasena.ContrasenaActualIncorrecta

        try {
            usuarioFirebase.reauthenticate(EmailAuthProvider.getCredential(email, contrasenaActual)).await()
        } catch (credencialesInvalidas: FirebaseAuthInvalidCredentialsException) {
            return ResultadoCambioContrasena.ContrasenaActualIncorrecta
        }

        usuarioFirebase.updatePassword(contrasenaNueva).await()
        return ResultadoCambioContrasena.Exitoso
    }
}

