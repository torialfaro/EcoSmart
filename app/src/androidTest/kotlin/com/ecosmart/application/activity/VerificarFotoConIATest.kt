package com.ecosmart.application.activity

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.EcoGptClient
import com.ecosmart.infrastructure.persistence.RegistroVerificacionRepositoryImpl
import com.ecosmart.infrastructure.persistence.UsuarioRepositoryImpl
import com.ecosmart.infrastructure.security.CalculadorHuellaPerceptual
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

/**
 * Test de integración (T024b, spec 002-firestore-datos-usuario) de
 * [VerificarFotoConIA]: un veredicto APROBADO llama a
 * `BackendConfianzaClient.otorgarPuntosReciclarReutilizar` (RF-D014/RF-D015) y NUNCA
 * escribe `puntosHistoricos`/`rachaActual` directamente. Usa la implementación REAL de
 * `RegistroVerificacionRepositoryImpl` (no un mock relajado) contra el emulador de
 * Firestore: su `guardar()` lanza `UnsupportedOperationException` si algún código
 * remanente la llamara, así que este test fallaría ruidosamente en vez de pasar en
 * silencio si esa garantía se rompiera.
 *
 * Requiere `firebase emulators:start --only firestore,auth` corriendo (quickstart.md §4).
 */
@RunWith(AndroidJUnit4::class)
class VerificarFotoConIATest {

    private lateinit var servidor: MockWebServer
    private lateinit var verificarFotoConIA: VerificarFotoConIA
    private lateinit var archivoFoto: File
    private lateinit var uid: String

    @Before
    fun preparar() = runBlocking {
        servidor = MockWebServer()
        servidor.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
        val ecoGptClient = retrofit.create(EcoGptClient::class.java)
        val backendConfianzaClient = retrofit.create(BackendConfianzaClient::class.java)

        val firestore = FirebaseFirestore.getInstance().apply { useEmulator("10.0.2.2", 8080) }
        val auth = FirebaseAuth.getInstance().apply { useEmulator("10.0.2.2", 9099) }
        uid = auth.signInAnonymously().await().user!!.uid

        val registroVerificacionRepository = RegistroVerificacionRepositoryImpl(firestore)
        val usuarioRepository = UsuarioRepositoryImpl(firestore)
        val aplicarTopeDiario = AplicarTopeDiario(registroVerificacionRepository, usuarioRepository)

        // Imagen real mínima (requiere runtime de Android de verdad, por eso este test
        // vive en androidTest y no en src/test): evita mockear CalculadorHuellaPerceptual.
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        archivoFoto = File.createTempFile("foto", ".png")
        archivoFoto.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        verificarFotoConIA = VerificarFotoConIA(
            ecoGptClient = ecoGptClient,
            backendConfianzaClient = backendConfianzaClient,
            aplicarTopeDiario = aplicarTopeDiario,
            calculadorHuellaPerceptual = CalculadorHuellaPerceptual(),
            registroVerificacionRepository = registroVerificacionRepository,
            apiKey = "api-key-de-prueba",
        )
    }

    @After
    fun detenerServidor() {
        servidor.shutdown()
    }

    @Test
    fun veredictoAprobado_llamaAlBackendDeConfianzaYNuncaEscribeFirestoreDirectamente() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(200).setBody("""{"veredicto":"APROBADO","motivo":null}"""))
        servidor.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"id":"registro-1","resultado":"APROBADO","puntosOtorgados":50,""" +
                    """"puntosHistoricosActualizados":150,"rachaActual":2,"nivel":"SEMILLA"}""",
            ),
        )

        val datos = DatosVerificacionFoto(
            usuarioId = UsuarioId(uid),
            actividadId = ActividadId.nuevo(),
            categoria = CategoriaActividad.RECICLAR,
            resultadoEsperado = "Materiales reciclables en un contenedor",
            descripcionUsuario = "Llevé botellas al punto verde",
            archivoFoto = archivoFoto,
        )

        // Si VerificarFotoConIA llamara a registroVerificacionRepository.guardar() en vez
        // de BackendConfianzaClient, la UnsupportedOperationException de RF-D014 haría
        // fallar este `invoke` y por lo tanto este test, en vez de pasar en silencio.
        val resultado = verificarFotoConIA(datos)

        assertTrue(resultado is ResultadoVerificarFotoConIA.Completado)
        assertTrue((resultado as ResultadoVerificarFotoConIA.Completado).registro.puntosOtorgados == 50)

        servidor.takeRequest() // la llamada a EcoGPT — no es el foco de este test.
        val requestBackendConfianza = servidor.takeRequest()
        assertTrue(requestBackendConfianza.path.orEmpty().contains("registros-verificacion/reciclar-reutilizar"))
    }
}
