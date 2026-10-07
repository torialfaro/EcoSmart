package com.ecosmart.application.auth

import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * T025, spec 002-firestore-datos-usuario: confirma que `RegistrarUsuario` delega en
 * `FirebaseAuth.createUserWithEmailAndPassword` y nunca construye un
 * `ContrasenaCifrada`/JWE real (RF-D002) — reemplaza la versión de spec 001 basada en
 * `CifradorContrasena`.
 */
class RegistrarUsuarioTest {

    private val firebaseAuth = mockk<FirebaseAuth>()
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)
    private val registrarUsuario = RegistrarUsuario(firebaseAuth, usuarioRepository)

    private fun datosValidos(email: String = "persona@ejemplo.com") = DatosRegistro(
        email = email,
        contrasenaPlana = "abc12345",
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = Barrio.PALERMO,
        telefono = "+5491155554444",
        categoriasDeInteres = setOf(CategoriaActividad.RECICLAR),
    )

    @Test
    fun `registro exitoso crea la cuenta via FirebaseAuth y nunca construye un JWE`() = runTest {
        val datos = datosValidos()
        val firebaseUser = mockk<FirebaseUser> { every { uid } returns "uid-123" }
        val authResult = mockk<AuthResult> { every { user } returns firebaseUser }
        every {
            firebaseAuth.createUserWithEmailAndPassword(datos.email, datos.contrasenaPlana)
        } returns tareaExitosa(authResult)

        val resultado = registrarUsuario(datos)

        assertTrue(resultado is ResultadoRegistro.Exitoso)
        val usuario = (resultado as ResultadoRegistro.Exitoso).usuario
        assertEquals("uid-123", usuario.id.valor)
        assertEquals("", usuario.contrasenaCifrada.jweCompacto)
        coVerify { usuarioRepository.guardar(usuario) }
    }

    @Test
    fun `un email ya registrado en Firebase Authentication se reporta como EmailYaRegistrado`() = runTest {
        val datos = datosValidos()
        every {
            firebaseAuth.createUserWithEmailAndPassword(datos.email, datos.contrasenaPlana)
        } returns tareaFallida(mockk<FirebaseAuthUserCollisionException>())

        val resultado = registrarUsuario(datos)

        assertEquals(ResultadoRegistro.EmailYaRegistrado, resultado)
        coVerify(exactly = 0) { usuarioRepository.guardar(any()) }
    }

    @Test
    fun `rechaza un correo con formato invalido sin llamar a FirebaseAuth`() = runTest {
        val resultado = registrarUsuario(datosValidos(email = "no-es-un-correo"))

        assertEquals(ResultadoRegistro.EmailInvalido, resultado)
    }

    @Test
    fun `rechaza una contrasena compuesta solo por numeros`() = runTest {
        val resultado = registrarUsuario(datosValidos().copy(contrasenaPlana = "12345678"))

        assertEquals(ResultadoRegistro.ContrasenaInvalida, resultado)
    }

    @Test
    fun `rechaza una contrasena compuesta solo por letras`() = runTest {
        val resultado = registrarUsuario(datosValidos().copy(contrasenaPlana = "abcdefgh"))

        assertEquals(ResultadoRegistro.ContrasenaInvalida, resultado)
    }

    @Test
    fun `rechaza un telefono sin el formato +549 mas 10 digitos`() = runTest {
        val resultado = registrarUsuario(datosValidos().copy(telefono = "11-5555-5555"))

        assertEquals(ResultadoRegistro.TelefonoInvalido, resultado)
    }

    @Test
    fun `rechaza el registro si falta el nombre, apellido o nombre de usuario`() = runTest {
        val resultado = registrarUsuario(datosValidos().copy(nombre = ""))

        assertEquals(ResultadoRegistro.CamposObligatoriosIncompletos, resultado)
    }

    @Test
    fun `rechaza el registro sin ninguna categoria seleccionada`() = runTest {
        val resultado = registrarUsuario(datosValidos().copy(categoriasDeInteres = emptySet()))

        assertEquals(ResultadoRegistro.SinCategoriasSeleccionadas, resultado)
    }
}

