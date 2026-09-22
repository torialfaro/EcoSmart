package com.ecosmart.application.activity

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AplicarTopeDiarioTest {

    private val registroVerificacionRepository = mockk<RegistroVerificacionRepository>()
    private val usuarioRepository = mockk<UsuarioRepository>()
    private val aplicarTopeDiario = AplicarTopeDiario(registroVerificacionRepository, usuarioRepository)

    private val usuarioId = UsuarioId.nuevo()

    private fun usuarioDePrueba() = Usuario(
        id = usuarioId,
        email = "persona@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada("jwe"),
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = Barrio.PALERMO,
        telefono = "",
        categoriasDeInteres = setOf(CategoriaActividad.REUTILIZAR),
    )

    @Test
    fun `bloquea Reutilizar al alcanzar el tope de 5 fotos aprobadas`() = runTest {
        coEvery {
            registroVerificacionRepository.contarAprobadosDelDia(usuarioId, CategoriaActividad.REUTILIZAR, any())
        } returns 5

        val disponibilidad = aplicarTopeDiario.verificarDisponibilidad(usuarioId, CategoriaActividad.REUTILIZAR)

        assertTrue(disponibilidad.bloqueado)
        assertEquals(5, disponibilidad.tope)
    }

    @Test
    fun `bloquea Reciclar al alcanzar el tope de 1 foto aprobada`() = runTest {
        coEvery {
            registroVerificacionRepository.contarAprobadosDelDia(usuarioId, CategoriaActividad.RECICLAR, any())
        } returns 1

        val disponibilidad = aplicarTopeDiario.verificarDisponibilidad(usuarioId, CategoriaActividad.RECICLAR)

        assertTrue(disponibilidad.bloqueado)
        assertEquals(1, disponibilidad.tope)
    }

    @Test
    fun `no bloquea Caminar porque no tiene tope diario`() = runTest {
        val disponibilidad = aplicarTopeDiario.verificarDisponibilidad(usuarioId, CategoriaActividad.CAMINAR)

        assertFalse(disponibilidad.bloqueado)
        assertNull(disponibilidad.tope)
    }

    @Test
    fun `un resultado Indeterminado no otorga puntos ni actualiza al usuario`() = runTest {
        coEvery { registroVerificacionRepository.guardar(any()) } returns Unit

        val registro = aplicarTopeDiario.registrarResultado(
            usuarioId = usuarioId,
            actividadId = ActividadId.nuevo(),
            categoria = CategoriaActividad.REUTILIZAR,
            resultado = ResultadoVerificacion.INDETERMINADO,
            motivoIA = "No se pudo determinar el contenido",
            huellaImagen = "abc123",
            pasosRegistrados = null,
            cantidadParaPuntaje = 1,
        )

        assertEquals(0, registro.puntosOtorgados)
        assertFalse(registro.consumeTopeDiario())
        coVerify(exactly = 0) { usuarioRepository.buscarPorId(any()) }
    }

    @Test
    fun `un resultado Aprobado de Reutilizar otorga 100 puntos y actualiza al usuario`() = runTest {
        coEvery { registroVerificacionRepository.guardar(any()) } returns Unit
        coEvery { usuarioRepository.buscarPorId(usuarioId) } returns usuarioDePrueba()
        coEvery { usuarioRepository.guardar(any()) } returns Unit

        val registro = aplicarTopeDiario.registrarResultado(
            usuarioId = usuarioId,
            actividadId = ActividadId.nuevo(),
            categoria = CategoriaActividad.REUTILIZAR,
            resultado = ResultadoVerificacion.APROBADO,
            motivoIA = null,
            huellaImagen = "abc123",
            pasosRegistrados = null,
            cantidadParaPuntaje = 1,
        )

        assertEquals(100, registro.puntosOtorgados)
        coVerify { usuarioRepository.guardar(match { it.puntosHistoricos == 100 }) }
    }
}
