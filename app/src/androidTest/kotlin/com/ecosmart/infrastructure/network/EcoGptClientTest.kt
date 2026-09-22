package com.ecosmart.infrastructure.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.SocketTimeoutException
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Test de integración de [EcoGptClient] contra un `MockWebServer` embebido
 * (quickstart.md §7), sin depender de la disponibilidad real de EcoGPT.
 * Reconstruye la MISMA configuración de timeout que `NetworkModule` (30s en
 * las 4 dimensiones de OkHttp) para validar el comportamiento real de
 * RNF-008, no una simulación con mocks.
 */
@RunWith(AndroidJUnit4::class)
class EcoGptClientTest {

    private lateinit var servidor: MockWebServer
    private lateinit var client: EcoGptClient

    @Before
    fun iniciarServidor() {
        servidor = MockWebServer()
        servidor.start()

        val timeout = Duration.ofSeconds(30)
        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(timeout)
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
        client = retrofit.create(EcoGptClient::class.java)
    }

    @After
    fun detenerServidor() {
        servidor.shutdown()
    }

    @Test
    fun verificarFotoActividad_parseaUnVeredictoAprobado() = runBlocking {
        servidor.enqueue(
            MockResponse()
                .setBody("""{"veredicto":"APROBADO","motivo":null}""")
                .setHeader("Content-Type", "application/json"),
        )

        val respuesta = client.verificarFotoActividad(
            apiKey = "clave-de-prueba",
            imagen = parteDeImagenDePrueba(),
            categoria = "RECICLAR".toRequestBody("text/plain".toMediaType()),
            resultadoEsperado = "Materiales reciclables".toRequestBody("text/plain".toMediaType()),
            descripcionUsuario = "Descripción de prueba".toRequestBody("text/plain".toMediaType()),
            huellasImagenesAprobadasPrevias = "".toRequestBody("text/plain".toMediaType()),
        )

        assertEquals("APROBADO", respuesta.veredicto)
    }

    @Test
    fun verificarFotoActividad_parseaUnVeredictoRechazadoConMotivo() = runBlocking {
        servidor.enqueue(
            MockResponse()
                .setBody("""{"veredicto":"RECHAZADO","motivo":"La imagen no coincide con la actividad"}""")
                .setHeader("Content-Type", "application/json"),
        )

        val respuesta = client.verificarFotoActividad(
            apiKey = "clave-de-prueba",
            imagen = parteDeImagenDePrueba(),
            categoria = "REUTILIZAR".toRequestBody("text/plain".toMediaType()),
            resultadoEsperado = "Objeto reutilizado".toRequestBody("text/plain".toMediaType()),
            descripcionUsuario = "Descripción de prueba".toRequestBody("text/plain".toMediaType()),
            huellasImagenesAprobadasPrevias = "".toRequestBody("text/plain".toMediaType()),
        )

        assertEquals("RECHAZADO", respuesta.veredicto)
        assertEquals("La imagen no coincide con la actividad", respuesta.motivo)
    }

    /**
     * quickstart.md §7 — respuesta deliberadamente más lenta que los 30s de
     * RNF-008. Este test tarda intencionalmente >30s (solo corre en
     * `connectedDebugAndroidTest`, con emulador/dispositivo): valida el
     * timeout real de OkHttp, no una simulación con mocks.
     */
    @Test(timeout = 40_000)
    fun verificarFotoActividad_lanzaTimeoutSiEcoGptTardaMasDe30s() {
        servidor.enqueue(
            MockResponse()
                .setBody("""{"veredicto":"APROBADO","motivo":null}""")
                .setBodyDelay(35, TimeUnit.SECONDS),
        )

        var lanzoTimeout = false
        try {
            runBlocking {
                client.verificarFotoActividad(
                    apiKey = "clave-de-prueba",
                    imagen = parteDeImagenDePrueba(),
                    categoria = "RECICLAR".toRequestBody("text/plain".toMediaType()),
                    resultadoEsperado = "Materiales reciclables".toRequestBody("text/plain".toMediaType()),
                    descripcionUsuario = "Descripción de prueba".toRequestBody("text/plain".toMediaType()),
                    huellasImagenesAprobadasPrevias = "".toRequestBody("text/plain".toMediaType()),
                )
            }
        } catch (timeout: SocketTimeoutException) {
            lanzoTimeout = true
        }

        assertTrue("Se esperaba SocketTimeoutException al superar los 30s de RNF-008", lanzoTimeout)
    }

    private fun parteDeImagenDePrueba(): MultipartBody.Part {
        val bytes = ByteArray(10) { it.toByte() }
        return MultipartBody.Part.createFormData("imagen", "foto.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
    }
}
