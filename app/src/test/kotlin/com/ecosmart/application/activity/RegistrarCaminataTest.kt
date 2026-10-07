package com.ecosmart.application.activity

import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.firebase.DispositivoIdProvider
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.EcoGptClient
import com.ecosmart.infrastructure.network.RegistroVerificacionOtorgadoDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegistrarCaminataTest {

    private val backendConfianzaClient = mockk<BackendConfianzaClient>()
    private val dispositivoIdProvider = mockk<DispositivoIdProvider> { every { dispositivoId } returns "dispositivo-test" }
    private val ecoGptClient = mockk<EcoGptClient>(relaxed = true)
    private val registrarCaminata = RegistrarCaminata(backendConfianzaClient, dispositivoIdProvider, ecoGptClient)

    private val usuarioId = UsuarioId.nuevo()
    private val actividadId = ActividadId.nuevo()

    private fun stubBackend(puntosOtorgados: Int) {
        coEvery { backendConfianzaClient.otorgarPuntosCaminar(any()) } returns RegistroVerificacionOtorgadoDto(
            id = "registro-1",
            resultado = "APROBADO",
            puntosOtorgados = puntosOtorgados,
            puntosHistoricosActualizados = puntosOtorgados,
            rachaActual = 1,
            nivel = "SEMILLA",
        )
    }

    @Test
    fun `otorga 50 puntos por cada bloque completo de 133 pasos, sin fracciones`() = runTest {
        // 300 pasos caminados = 2 bloques completos de 133 (266) + 34 pasos remanentes sin puntos.
        stubBackend(puntosOtorgados = 100)

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
        stubBackend(puntosOtorgados = 0)

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
