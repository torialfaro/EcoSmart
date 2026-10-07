package com.ecosmart.infrastructure.migration

import android.content.SharedPreferences
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.SolicitudMigracionDto
import com.ecosmart.infrastructure.persistence.room.AppDatabase
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioDao
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioRoomEntity
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private val USUARIO_LOCAL = UsuarioRoomEntity(
    id = "local-1",
    email = "ada@ejemplo.com",
    contrasenaCifradaJwe = "",
    nombre = "Ada",
    apellido = "Lovelace",
    nombreUsuario = "ada",
    barrio = null,
    telefono = "+5491112345678",
    categoriasDeInteres = "RECICLAR",
    puntosHistoricos = 100,
    rachaActual = 2,
    ultimaActividadAprobadaEn = null,
)

class MigracionDatosLocalesTest {

    private val prefs = mockk<SharedPreferences>(relaxed = true)
    private val firebaseAuth = mockk<FirebaseAuth> {
        every { currentUser } returns mockk<FirebaseUser> { every { email } returns "ada@ejemplo.com" }
    }
    private val usuarioDao = mockk<UsuarioDao> { coEvery { findByEmail("ada@ejemplo.com") } returns USUARIO_LOCAL }
    private val registroVerificacionDao = mockk<RegistroVerificacionDao> {
        coEvery { todos("local-1") } returns emptyList()
    }
    private val appDatabase = mockk<AppDatabase>(relaxed = true)
    private val backendConfianzaClient = mockk<BackendConfianzaClient>()

    private val migracionDatosLocales = MigracionDatosLocales(
        prefs, firebaseAuth, usuarioDao, registroVerificacionDao, appDatabase, backendConfianzaClient,
    )

    @Test
    fun `borra las tablas de Room solo despues de que el backend confirme la subida`() = runTest {
        coEvery { backendConfianzaClient.migrarDatosLocales(any<SolicitudMigracionDto>()) } returns Unit

        migracionDatosLocales.ejecutarSiCorresponde()

        coVerify(exactly = 1) { backendConfianzaClient.migrarDatosLocales(any()) }
        coVerify(exactly = 1) { appDatabase.clearAllTables() }
    }

    @Test
    fun `no borra las tablas de Room si el backend falla`() = runTest {
        coEvery { backendConfianzaClient.migrarDatosLocales(any<SolicitudMigracionDto>()) } throws RuntimeException("sin red")

        val error = runCatching { migracionDatosLocales.ejecutarSiCorresponde() }.exceptionOrNull()

        assertTrue(error is RuntimeException)
        coVerify(exactly = 0) { appDatabase.clearAllTables() }
    }
}
