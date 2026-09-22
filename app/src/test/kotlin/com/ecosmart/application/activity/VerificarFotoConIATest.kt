package com.ecosmart.application.activity

import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.network.EcoGptClient
import com.ecosmart.infrastructure.security.CalculadorHuellaPerceptual
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.net.SocketTimeoutException

class VerificarFotoConIATest {

    private val ecoGptClient = mockk<EcoGptClient>()
    private val registroVerificacionRepository = mockk<RegistroVerificacionRepository>(relaxed = true)
    private val usuarioRepository = mockk<UsuarioRepository>(relaxed = true)
    private val aplicarTopeDiario = AplicarTopeDiario(registroVerificacionRepository, usuarioRepository)
    private val calculadorHuellaPerceptual = mockk<CalculadorHuellaPerceptual>()
    private val verificarFotoConIA = VerificarFotoConIA(
        ecoGptClient = ecoGptClient,
        aplicarTopeDiario = aplicarTopeDiario,
        calculadorHuellaPerceptual = calculadorHuellaPerceptual,
        registroVerificacionRepository = registroVerificacionRepository,
        apiKey = "api-key-de-prueba",
    )

    private val datos = DatosVerificacionFoto(
        usuarioId = UsuarioId.nuevo(),
        actividadId = ActividadId.nuevo(),
        categoria = CategoriaActividad.RECICLAR,
        resultadoEsperado = "Materiales reciclables en un contenedor",
        descripcionUsuario = "Llevé botellas al punto verde",
        archivoFoto = File.createTempFile("foto", ".jpg"),
    )

    @Test
    fun `un timeout de EcoGPT se reporta como Timeout, no como Rechazado o Indeterminado`() = runTest {
        coEvery { registroVerificacionRepository.contarAprobadosDelDia(any(), any(), any()) } returns 0
        coEvery { registroVerificacionRepository.huellasAprobadas(any(), any()) } returns emptyList()
        coEvery { calculadorHuellaPerceptual.calcularDesdeArchivo(any()) } returns "a1b2c3d4"
        coEvery {
            ecoGptClient.verificarFotoActividad(any(), any(), any(), any(), any(), any())
        } throws SocketTimeoutException()

        val resultado = verificarFotoConIA(datos)

        assertTrue(resultado is ResultadoVerificarFotoConIA.Timeout)
    }

    @Test
    fun `no llama a EcoGPT si el tope diario ya fue alcanzado`() = runTest {
        coEvery { registroVerificacionRepository.contarAprobadosDelDia(any(), any(), any()) } returns 1

        val resultado = verificarFotoConIA(datos)

        assertTrue(resultado is ResultadoVerificarFotoConIA.TopeDiarioAlcanzado)
    }

    @Test
    fun `rechaza localmente una foto casi identica a una ya aprobada, sin llamar a EcoGPT`() = runTest {
        coEvery { registroVerificacionRepository.contarAprobadosDelDia(any(), any(), any()) } returns 0
        coEvery { registroVerificacionRepository.huellasAprobadas(any(), any()) } returns listOf("a1b2c3d4")
        coEvery { calculadorHuellaPerceptual.calcularDesdeArchivo(any()) } returns "a1b2c3d4"

        val resultado = verificarFotoConIA(datos)

        assertTrue(resultado is ResultadoVerificarFotoConIA.DuplicadaLocalmente)
    }
}
