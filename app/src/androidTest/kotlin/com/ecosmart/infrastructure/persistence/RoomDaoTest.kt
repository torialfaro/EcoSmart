package com.ecosmart.infrastructure.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ecosmart.infrastructure.persistence.room.AppDatabase
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadRoomEntity
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioRoomEntity
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionRoomEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Tests de integración con Room in-memory (constitution.md § stack de
 * testing: instrumentados, distintos de los unit tests puros de
 * `src/test`). Cubre `UsuarioDao`, `ActividadDao` y
 * `RegistroVerificacionDao` contra una base de datos real en memoria, no
 * mockeada.
 */
@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBaseEnMemoria() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun usuarioDao_insertaYEncuentraPorEmail() = runBlocking {
        val usuario = usuarioDePrueba()

        db.usuarioDao().insertarOActualizar(usuario)
        val encontrado = db.usuarioDao().findByEmail(usuario.email)

        assertNotNull(encontrado)
        assertEquals(usuario.nombre, encontrado?.nombre)
    }

    @Test
    fun usuarioDao_noEncuentraUnEmailInexistente() = runBlocking {
        val encontrado = db.usuarioDao().findByEmail("no-existe@ejemplo.com")

        assertNull(encontrado)
    }

    @Test
    fun actividadDao_filtraPorCategorias() = runBlocking {
        db.actividadDao().insertarTodas(
            listOf(
                actividadDePrueba(categoria = "RECICLAR"),
                actividadDePrueba(categoria = "REUTILIZAR"),
                actividadDePrueba(categoria = "CAMINAR"),
            ),
        )

        val encontradas = db.actividadDao().findByCategorias(listOf("RECICLAR", "CAMINAR")).first()

        assertEquals(2, encontradas.size)
    }

    @Test
    fun registroVerificacionDao_contarAprobadosDelDia_soloCuentaAprobados() = runBlocking {
        val usuario = usuarioDePrueba()
        val actividad = actividadDePrueba(categoria = "REUTILIZAR")
        db.usuarioDao().insertarOActualizar(usuario)
        db.actividadDao().insertarTodas(listOf(actividad))

        val hoy = "2026-09-21"
        db.registroVerificacionDao().insertar(
            registroDePrueba(usuario.id, actividad.id, categoria = "REUTILIZAR", fecha = hoy, resultado = "APROBADO"),
        )
        db.registroVerificacionDao().insertar(
            registroDePrueba(usuario.id, actividad.id, categoria = "REUTILIZAR", fecha = hoy, resultado = "RECHAZADO"),
        )

        val cantidad = db.registroVerificacionDao().contarAprobadosDelDia(usuario.id, "REUTILIZAR", hoy)

        assertEquals(1, cantidad)
    }

    @Test
    fun registroVerificacionDao_ultimosN_devuelveLosMasRecientesPrimero() = runBlocking {
        val usuario = usuarioDePrueba()
        val actividad = actividadDePrueba(categoria = "RECICLAR")
        db.usuarioDao().insertarOActualizar(usuario)
        db.actividadDao().insertarTodas(listOf(actividad))

        db.registroVerificacionDao().insertar(
            registroDePrueba(usuario.id, actividad.id, categoria = "RECICLAR", fecha = "2026-09-19", resultado = "APROBADO"),
        )
        db.registroVerificacionDao().insertar(
            registroDePrueba(usuario.id, actividad.id, categoria = "RECICLAR", fecha = "2026-09-21", resultado = "APROBADO"),
        )
        db.registroVerificacionDao().insertar(
            registroDePrueba(usuario.id, actividad.id, categoria = "RECICLAR", fecha = "2026-09-20", resultado = "APROBADO"),
        )

        val ultimosDos = db.registroVerificacionDao().ultimosN(usuario.id, 2)

        assertEquals(2, ultimosDos.size)
        assertEquals("2026-09-21", ultimosDos[0].fecha)
        assertEquals("2026-09-20", ultimosDos[1].fecha)
    }

    private fun usuarioDePrueba() = UsuarioRoomEntity(
        id = UUID.randomUUID().toString(),
        email = "persona-${UUID.randomUUID()}@ejemplo.com",
        contrasenaCifradaJwe = "jwe",
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = "PALERMO",
        telefono = "",
        categoriasDeInteres = "RECICLAR",
        puntosHistoricos = 0,
        rachaActual = 0,
        ultimaActividadAprobadaEn = null,
    )

    private fun actividadDePrueba(categoria: String) = ActividadRoomEntity(
        id = UUID.randomUUID().toString(),
        categoria = categoria,
        descripcionCorta = "desc",
        pasosASeguir = "pasos",
        resultadoEsperado = "resultado",
        fotoReferencialUrl = "",
        puntosBase = 50,
    )

    private fun registroDePrueba(
        usuarioId: String,
        actividadId: String,
        categoria: String,
        fecha: String,
        resultado: String,
    ) = RegistroVerificacionRoomEntity(
        id = UUID.randomUUID().toString(),
        usuarioId = usuarioId,
        actividadId = actividadId,
        categoria = categoria,
        fecha = fecha,
        resultado = resultado,
        motivoIA = null,
        puntosOtorgados = if (resultado == "APROBADO") 100 else 0,
        huellaImagen = null,
        pasosRegistrados = null,
    )
}
