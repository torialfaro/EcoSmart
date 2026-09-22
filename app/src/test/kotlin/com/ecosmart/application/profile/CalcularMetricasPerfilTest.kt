package com.ecosmart.application.profile

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CalcularMetricasPerfilTest {

    private val usuarioRepository = mockk<UsuarioRepository>()
    private val registroVerificacionRepository = mockk<RegistroVerificacionRepository>()
    private val calcularMetricasPerfil = CalcularMetricasPerfil(usuarioRepository, registroVerificacionRepository)

    private val usuarioId = UsuarioId.nuevo()

    private fun usuarioDePrueba(
        puntosHistoricos: Int = 0,
        rachaActual: Int = 0,
        ultimaActividadAprobadaEn: LocalDate? = null,
    ) = Usuario(
        id = usuarioId,
        email = "persona@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada("jwe"),
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = Barrio.PALERMO,
        telefono = "",
        categoriasDeInteres = setOf(CategoriaActividad.RECICLAR),
        puntosHistoricos = puntosHistoricos,
        rachaActual = rachaActual,
        ultimaActividadAprobadaEn = ultimaActividadAprobadaEn,
    )

    private fun registro(categoria: CategoriaActividad) = RegistroVerificacion(
        id = RegistroVerificacionId.nuevo(),
        usuarioId = usuarioId,
        actividadId = ActividadId.nuevo(),
        categoria = categoria,
        fecha = LocalDate.now(),
        resultado = ResultadoVerificacion.APROBADO,
    )

    @Test
    fun `la racha se muestra rota tras un dia calendario completo sin actividad`() = runTest {
        val haceTresDias = LocalDate.now().minusDays(3)
        coEvery { usuarioRepository.buscarPorId(usuarioId) } returns
            usuarioDePrueba(rachaActual = 5, ultimaActividadAprobadaEn = haceTresDias)
        coEvery { registroVerificacionRepository.todos(usuarioId) } returns emptyList()

        val metricas = calcularMetricasPerfil(usuarioId)

        assertEquals(0, metricas?.rachaActual)
    }

    @Test
    fun `la racha se mantiene si la ultima actividad aprobada fue ayer`() = runTest {
        val ayer = LocalDate.now().minusDays(1)
        coEvery { usuarioRepository.buscarPorId(usuarioId) } returns
            usuarioDePrueba(rachaActual = 3, ultimaActividadAprobadaEn = ayer)
        coEvery { registroVerificacionRepository.todos(usuarioId) } returns emptyList()

        val metricas = calcularMetricasPerfil(usuarioId)

        assertEquals(3, metricas?.rachaActual)
    }

    @Test
    fun `el nivel se deriva del puntaje historico acumulado, que nunca desciende`() = runTest {
        coEvery { usuarioRepository.buscarPorId(usuarioId) } returns usuarioDePrueba(puntosHistoricos = 3500)
        coEvery { registroVerificacionRepository.todos(usuarioId) } returns emptyList()

        val metricas = calcularMetricasPerfil(usuarioId)

        assertEquals(NivelUsuario.ARBOL, metricas?.nivel)
    }

    @Test
    fun `calcula el porcentaje de participacion por categoria sobre el total de actividades realizadas`() = runTest {
        coEvery { usuarioRepository.buscarPorId(usuarioId) } returns usuarioDePrueba()
        coEvery { registroVerificacionRepository.todos(usuarioId) } returns listOf(
            registro(CategoriaActividad.RECICLAR),
            registro(CategoriaActividad.RECICLAR),
            registro(CategoriaActividad.RECICLAR),
            registro(CategoriaActividad.REUTILIZAR),
        )

        val metricas = calcularMetricasPerfil(usuarioId)

        assertEquals(75.0, metricas?.porcentajePorCategoria?.get(CategoriaActividad.RECICLAR))
        assertEquals(25.0, metricas?.porcentajePorCategoria?.get(CategoriaActividad.REUTILIZAR))
    }
}
