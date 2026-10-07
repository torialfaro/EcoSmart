package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.domain.valueobject.esContrasenaValida
import com.ecosmart.domain.valueobject.esEmailValido
import com.ecosmart.domain.valueobject.esTelefonoValido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DatosRegistro(
    val email: String,
    val contrasenaPlana: String,
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: Barrio,
    val telefono: String,
    val categoriasDeInteres: Set<CategoriaActividad>,
)

sealed class ResultadoRegistro {
    data class Exitoso(val usuario: Usuario) : ResultadoRegistro()
    data object EmailInvalido : ResultadoRegistro()
    data object EmailYaRegistrado : ResultadoRegistro()
    data object ContrasenaInvalida : ResultadoRegistro()
    /** Corrección post-QA (2026-09-23): nombre, apellido y nombre de usuario ahora son obligatorios. */
    data object CamposObligatoriosIncompletos : ResultadoRegistro()
    /** Corrección post-QA (2026-09-23): teléfono obligatorio, formato "+549" + 10 dígitos. */
    data object TelefonoInvalido : ResultadoRegistro()
    data object SinCategoriasSeleccionadas : ResultadoRegistro()
}

/**
 * US1/US2 — crea una cuenta con correo/contraseña (RF-001 a RF-006). RF-D002 (spec
 * 002-firestore-datos-usuario): la contraseña se delega íntegramente a Firebase
 * Authentication — nunca se cifra ni se guarda acá, ni en Firestore (reemplaza al
 * cifrado JWE de spec 001).
 */
class RegistrarUsuario @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(datos: DatosRegistro): ResultadoRegistro {
        if (!esEmailValido(datos.email)) return ResultadoRegistro.EmailInvalido
        if (!esContrasenaValida(datos.contrasenaPlana)) return ResultadoRegistro.ContrasenaInvalida
        if (datos.nombre.isBlank() || datos.apellido.isBlank() || datos.nombreUsuario.isBlank()) {
            return ResultadoRegistro.CamposObligatoriosIncompletos
        }
        if (!esTelefonoValido(datos.telefono)) return ResultadoRegistro.TelefonoInvalido
        if (datos.categoriasDeInteres.isEmpty()) return ResultadoRegistro.SinCategoriasSeleccionadas

        val credencial = try {
            firebaseAuth.createUserWithEmailAndPassword(datos.email, datos.contrasenaPlana).await()
        } catch (yaRegistrado: FirebaseAuthUserCollisionException) {
            return ResultadoRegistro.EmailYaRegistrado
        } catch (contrasenaDebil: FirebaseAuthWeakPasswordException) {
            return ResultadoRegistro.ContrasenaInvalida
        } catch (emailInvalido: FirebaseAuthInvalidCredentialsException) {
            return ResultadoRegistro.EmailInvalido
        }
        val uid = credencial.user?.uid ?: return ResultadoRegistro.EmailInvalido

        val usuario = Usuario(
            id = UsuarioId(uid),
            email = datos.email,
            // RF-D002: nunca una forma real de la contraseña — Firebase Authentication
            // ya la guarda hasheada por su cuenta, fuera de Firestore.
            contrasenaCifrada = ContrasenaCifrada(""),
            nombre = datos.nombre,
            apellido = datos.apellido,
            nombreUsuario = datos.nombreUsuario,
            barrio = datos.barrio,
            telefono = datos.telefono,
            categoriasDeInteres = datos.categoriasDeInteres,
        )
        usuarioRepository.guardar(usuario)
        return ResultadoRegistro.Exitoso(usuario)
    }
}

