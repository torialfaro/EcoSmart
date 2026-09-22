package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.security.CifradorContrasena
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegistrarUsuarioTest {

    private val usuarioRepository = mockk<UsuarioRepository>()
    private val cifradorContrasena = mockk<CifradorContrasena>()
    private val registrarUsuario = RegistrarUsuario(usuarioRepository, cifradorContrasena)

    private fun datosValidos(email: String = "persona@ejemplo.com") = DatosRegistro(
        email = email,
        contrasenaPlana = "abc12345",
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = Barrio.PALERMO,
        telefono = "11-5555-5555",
        categoriasDeInteres = setOf(CategoriaActividad.RECICLAR),
    )

    private fun usuarioExistente() = Usuario(
        id = UsuarioId.nuevo(),
        email = "persona@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada("jwe-existente"),
        nombre = "Otra",
        apellido = "Persona",
        nombreUsuario = "otrap",
        barrio = Barrio.CABALLITO,
        telefono = "",
        categoriasDeInteres = setOf(CategoriaActividad.CAMINAR),
    )

    @Test
    fun `rechaza el registro si el correo ya esta en uso`() = runTest {
        coEvery { usuarioRepository.buscarPorEmail(any()) } returns usuarioExistente()

        val resultado = registrarUsuario(datosValidos())

        assertEquals(ResultadoRegistro.EmailYaRegistrado, resultado)
        coVerify(exactly = 0) { usuarioRepository.guardar(any()) }
    }

    @Test
    fun `rechaza un correo con formato invalido`() = runTest {
        val resultado = registrarUsuario(datosValidos(email = "no-es-un-correo"))

        assertEquals(ResultadoRegistro.EmailInvalido, resultado)
    }

    @Test
    fun `rechaza una contrasena compuesta solo por numeros`() = runTest {
        coEvery { usuarioRepository.buscarPorEmail(any()) } returns null

        val resultado = registrarUsuario(datosValidos().copy(contrasenaPlana = "12345678"))

        assertEquals(ResultadoRegistro.ContrasenaInvalida, resultado)
    }

    @Test
    fun `rechaza una contrasena compuesta solo por letras`() = runTest {
        coEvery { usuarioRepository.buscarPorEmail(any()) } returns null

        val resultado = registrarUsuario(datosValidos().copy(contrasenaPlana = "abcdefgh"))

        assertEquals(ResultadoRegistro.ContrasenaInvalida, resultado)
    }

    @Test
    fun `rechaza el registro sin ninguna categoria seleccionada`() = runTest {
        coEvery { usuarioRepository.buscarPorEmail(any()) } returns null

        val resultado = registrarUsuario(datosValidos().copy(categoriasDeInteres = emptySet()))

        assertEquals(ResultadoRegistro.SinCategoriasSeleccionadas, resultado)
    }

    @Test
    fun `registra correctamente cifrando la contrasena antes de persistir`() = runTest {
        coEvery { usuarioRepository.buscarPorEmail(any()) } returns null
        every { cifradorContrasena.cifrar("abc12345") } returns "jwe-cifrado"
        coEvery { usuarioRepository.guardar(any()) } returns Unit

        val resultado = registrarUsuario(datosValidos())

        assertTrue(resultado is ResultadoRegistro.Exitoso)
        val usuario = (resultado as ResultadoRegistro.Exitoso).usuario
        assertEquals("jwe-cifrado", usuario.contrasenaCifrada.jweCompacto)
        coVerify { usuarioRepository.guardar(usuario) }
    }
}
