package com.ecosmart.infrastructure.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Test de integración (T019, spec 002-firestore-datos-usuario) de
 * [RegistroVerificacionRepositoryImpl] contra el emulador de Firestore/Authentication.
 * Los documentos de prueba se insertan ya "completos" (con `resultado`/`puntosOtorgados`),
 * simulando lo que en producción escribe el backend de confianza (RF-D014) — este
 * repositorio es de solo lectura, ver `guardar()`.
 */
@RunWith(AndroidJUnit4::class)
class RegistroVerificacionRepositoryImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repositorio: RegistroVerificacionRepositoryImpl
    private lateinit var uid: String

    @Before
    fun prepararEmuladoresYDatosDePrueba() = runBlocking {
        val auth = FirebaseAuth.getInstance().apply { useEmulator("10.0.2.2", 9099) }
        firestore = FirebaseFirestore.getInstance().apply { useEmulator("10.0.2.2", 8080) }
        uid = auth.signInAnonymously().await().user!!.uid
        repositorio = RegistroVerificacionRepositoryImpl(firestore)

        insertarRegistro(id = "r1", categoria = "RECICLAR", fecha = "2026-10-05", resultado = "APROBADO", segundos = 1_000, huella = "hash-r1")
        insertarRegistro(id = "r2", categoria = "RECICLAR", fecha = "2026-10-06", resultado = "APROBADO", segundos = 2_000, huella = "hash-r2")
        insertarRegistro(id = "r3", categoria = "REUTILIZAR", fecha = "2026-10-06", resultado = "RECHAZADO", segundos = 3_000, huella = null)
    }

    @Test
    fun todos_devuelveElHistorialOrdenadoDelMasRecienteAlMasViejo() = runBlocking {
        val historial = repositorio.todos(UsuarioId(uid))

        assertEquals(listOf("r3", "r2", "r1"), historial.map { it.id.valor })
    }

    @Test
    fun ultimosN_respetaElLimite() = runBlocking {
        val ultimos = repositorio.ultimosN(UsuarioId(uid), 2)

        assertEquals(listOf("r3", "r2"), ultimos.map { it.id.valor })
    }

    @Test
    fun contarAprobadosDelDia_cuentaSoloLaCategoriaYFechaPedidas() = runBlocking {
        val cantidad = repositorio.contarAprobadosDelDia(
            UsuarioId(uid),
            CategoriaActividad.RECICLAR,
            LocalDate.parse("2026-10-06"),
        )

        assertEquals(1, cantidad)
    }

    @Test
    fun huellasAprobadas_devuelveSoloLasDeResultadoAprobado() = runBlocking {
        val huellas = repositorio.huellasAprobadas(UsuarioId(uid), CategoriaActividad.RECICLAR)

        assertTrue(huellas.containsAll(listOf("hash-r1", "hash-r2")))
    }

    private suspend fun insertarRegistro(
        id: String,
        categoria: String,
        fecha: String,
        resultado: String,
        segundos: Long,
        huella: String?,
    ) {
        firestore.collection("usuarios").document(uid)
            .collection("registrosVerificacion").document(id)
            .set(
                mapOf(
                    "actividadId" to "actividad-$id",
                    "categoria" to categoria,
                    "fecha" to fecha,
                    "resultado" to resultado,
                    "motivoIA" to null,
                    "puntosOtorgados" to 0,
                    "huellaImagen" to huella,
                    "pasosRegistrados" to null,
                    "creadoEn" to Timestamp(segundos, 0),
                ),
            )
            .await()
    }
}
