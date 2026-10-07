package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** T025, spec 002-firestore-datos-usuario: confirma que IniciarSesion delega en
 * FirebaseAuth y nunca compara contra un JWE/`UsuarioRepository.autenticar` (RF-D002). */
class IniciarSesionTest {

    private val firebaseAuth = mockk<FirebaseAuth>()
    private val usuarioRepository = mockk<UsuarioRepository>()
    private val iniciarSesion = IniciarSesion(firebaseAuth, usuarioRepository)

    @Test
    fun `login exitoso usa signInWithEmailAndPassword y recupera el perfil por uid`() = runTest {
        val firebaseUser = mockk<FirebaseUser> { every { uid } returns "uid-456" }
        val authResult = mockk<AuthResult> { every { user } returns firebaseUser }
        val usuario = usuarioDePrueba("uid-456")
        every {
            firebaseAuth.signInWithEmailAndPassword("persona@ejemplo.com", "contrasena123")
        } returns tareaExitosa(authResult)
        coEvery { usuarioRepository.buscarPorId(UsuarioId("uid-456")) } returns usuario

        val resultado = iniciarSesion("persona@ejemplo.com", "contrasena123")

        assertTrue(resultado is ResultadoInicioSesion.Exitoso)
    }

    @Test
    fun `credenciales invalidas se reportan sin exponer el motivo exacto de Firebase`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword(any(), any())
        } returns tareaFallida(mockk<FirebaseAuthInvalidCredentialsException>())

        val resultado = iniciarSesion("persona@ejemplo.com", "mal")

        assertTrue(resultado is ResultadoInicioSesion.CredencialesInvalidas)
    }

    private fun usuarioDePrueba(uid: String) = Usuario(
        id = UsuarioId(uid),
        email = "persona@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada(""),
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = Barrio.PALERMO,
        telefono = "+5491155555555",
        categoriasDeInteres = setOf(CategoriaActividad.RECICLAR),
    )
}
