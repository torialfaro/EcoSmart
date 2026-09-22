package com.ecosmart.application.activity

import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.UsuarioId
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegistrarCaminataTest {

    private val registroVerificacionRepository = mockk<RegistroVerificacionRepository>(relaxed = true)
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)
    private val aplicarTopeDiario = AplicarTopeDiario(registroVerificacionRepository, usuarioRepository)
    private val registrarCaminata = RegistrarCaminata(aplicarTopeDiario)

    private val usuarioId = UsuarioId.nuevo()
    private val actividadId = ActividadId.nuevo()

    @Test
    fun `otorga 50 puntos por cada bloque completo de 133 pasos, sin fracciones`() = runTest {
        // 300 pasos caminados = 2 bloques completos de 133 (266) + 34 pasos remanentes sin puntos.
        val resultado = registrarCaminata(
            DatosCaminata(
                usuarioId = usuarioId,
                actividadId = actividadId,
                metaPasos = 200,
                pasosBase = 1000,
                pasosActuales = 1300,
            ),
        )

        assertTrue(resultado is ResultadoCaminata.Aprobada)
        assertEquals(100, (resultado as ResultadoCaminata.Aprobada).registro.puntosOtorgados)
    }

    @Test
    fun `no otorga puntos por pasos que no completan un bloque de 133`() = runTest {
        val resultado = registrarCaminata(
            DatosCaminata(
                usuarioId = usuarioId,
                actividadId = actividadId,
                metaPasos = 100,
                pasosBase = 1000,
                pasosActuales = 1132,
            ),
        )

        assertTrue(resultado is ResultadoCaminata.Aprobada)
        assertEquals(0, (resultado as ResultadoCaminata.Aprobada).registro.puntosOtorgados)
    }

    @Test
    fun `informa que la meta no se cumplio sin otorgar puntos`() = runTest {
        val resultado = registrarCaminata(
            DatosCaminata(
                usuarioId = usuarioId,
                actividadId = actividadId,
                metaPasos = 500,
                pasosBase = 1000,
                pasosActuales = 1200,
            ),
        )

        assertTrue(resultado is ResultadoCaminata.MetaNoAlcanzada)
        assertEquals(200, (resultado as ResultadoCaminata.MetaNoAlcanzada).pasosCaminados)
    }
}
