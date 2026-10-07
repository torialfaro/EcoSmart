package com.ecosmart.infrastructure.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de integración (T018, spec 002-firestore-datos-usuario) de
 * [UsuarioRepositoryImpl] contra el emulador de Firestore/Authentication
 * (quickstart.md §4) — NUNCA contra la nube real, para no consumir la cuota gratuita
 * compartida (research.md §5). Requiere `firebase emulators:start --only firestore,auth`
 * corriendo antes de ejecutar este test.
 */
@RunWith(AndroidJUnit4::class)
class UsuarioRepositoryImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repositorio: UsuarioRepositoryImpl
    private lateinit var uid: String

    @Before
    fun prepararEmuladoresYSesion() = runBlocking {
        val auth = FirebaseAuth.getInstance().apply { useEmulator("10.0.2.2", 9099) }
        firestore = FirebaseFirestore.getInstance().apply { useEmulator("10.0.2.2", 8080) }
        // Las Reglas de Seguridad (RF-D010) exigen un usuario autenticado cuyo uid
        // coincida con el documento que se intenta leer/escribir.
        uid = auth.signInAnonymously().await().user!!.uid
        repositorio = UsuarioRepositoryImpl(firestore)
    }

    @Test
    fun guardarYBuscarPorId_persisteElPerfilEnFirestore() = runBlocking {
        val usuario = usuarioDePrueba(uid)

        repositorio.guardar(usuario)
        val encontrado = repositorio.buscarPorId(UsuarioId(uid))

        assertEquals(usuario.nombre, encontrado?.nombre)
        assertEquals(usuario.nombreUsuario, encontrado?.nombreUsuario)
    }

    @Test
    // T026: caso ya cubierto desde T018 — SC-D003 (ningún campo de contraseña en Firestore).
    fun documentoRecienCreado_noContieneNingunCampoDeContrasena() = runBlocking {
        repositorio.guardar(usuarioDePrueba(uid))

        val snapshot = firestore.collection("usuarios").document(uid).get().await()

        assertNull(snapshot.get("contrasenaCifradaJwe"))
        assertNull(snapshot.get("contrasenaCifrada"))
    }

    @Test
    fun buscarPorId_devuelveNullSiElDocumentoNoExiste() = runBlocking {
        val encontrado = repositorio.buscarPorId(UsuarioId("no-existe-${System.nanoTime()}"))

        assertNull(encontrado)
    }

    /**
     * T033, spec 002-firestore-datos-usuario (RF-D017/SC-D002): una segunda instancia de
     * `UsuarioRepositoryImpl` (misma cuenta, mismo cliente de Firestore) ve el cambio
     * escrito por la primera con una simple recarga, sin listener en vivo — simula el
     * "volver a primer plano" de un segundo dispositivo. La simulación de dos
     * dispositivos/procesos totalmente independientes queda para la validación manual de
     * quickstart.md §6 (el login anónimo de este test no permite compartir un mismo uid
     * entre dos instancias de FirebaseApp).
     */
    @Test
    fun dosInstanciasDelRepositorio_reflejanElMismoEstadoTrasUnaRecarga() = runBlocking {
        val repositorioOtraInstancia = UsuarioRepositoryImpl(firestore)

        repositorio.guardar(usuarioDePrueba(uid).copy(nombre = "Ana Editada Desde Otro Dispositivo"))
        val vistoDesdeOtraInstancia = repositorioOtraInstancia.buscarPorId(UsuarioId(uid))

        assertEquals("Ana Editada Desde Otro Dispositivo", vistoDesdeOtraInstancia?.nombre)
    }

    private fun usuarioDePrueba(uid: String) = Usuario(
        id = UsuarioId(uid),
        email = "persona-$uid@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada(""),
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = null,
        telefono = "+5491155555555",
        categoriasDeInteres = emptySet(),
    )
}
