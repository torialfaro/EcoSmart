package com.ecosmart.infrastructure.migration

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.PerfilMigracionDto
import com.ecosmart.infrastructure.network.RegistroMigracionDto
import com.ecosmart.infrastructure.network.SolicitudMigracionDto
import com.ecosmart.infrastructure.persistence.room.AppDatabase
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioDao
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionDao
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val PREFS = "migracion_datos_locales"
private const val CLAVE_COMPLETADA = "migracion_firestore_completada"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MigracionPrefs

@Module
@InstallIn(SingletonComponent::class)
object MigracionModule {
    @Provides
    @Singleton
    @MigracionPrefs
    fun provideMigracionPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/**
 * RF-D011 — sube una única vez, en el primer login posterior a esta actualización, el
 * perfil e historial de una cuenta creada bajo spec 001 (datos solo en Room). Borra las
 * tablas de Room únicamente después de que el backend confirme la subida (research.md
 * §6): si `backendConfianzaClient.migrarDatosLocales` falla, Room queda intacto para
 * reintentar en el próximo login, sin duplicar nada (idempotencia del lado del backend).
 */
@Singleton
@Suppress("DEPRECATION") // único uso legítimo restante de UsuarioDao/RegistroVerificacionDao, ver AppDatabase.kt.
class MigracionDatosLocales @Inject constructor(
    @MigracionPrefs private val prefs: SharedPreferences,
    private val firebaseAuth: FirebaseAuth,
    private val usuarioDao: UsuarioDao,
    private val registroVerificacionDao: RegistroVerificacionDao,
    private val appDatabase: AppDatabase,
    private val backendConfianzaClient: BackendConfianzaClient,
) {
    suspend fun ejecutarSiCorresponde() {
        if (prefs.getBoolean(CLAVE_COMPLETADA, false)) return

        val email = firebaseAuth.currentUser?.email
        val usuarioLocal = email?.let { usuarioDao.findByEmail(it) }
        if (usuarioLocal == null) {
            marcarCompletada()
            return
        }

        val historialLocal = registroVerificacionDao.todos(usuarioLocal.id)
        backendConfianzaClient.migrarDatosLocales(
            SolicitudMigracionDto(
                perfil = PerfilMigracionDto(
                    nombre = usuarioLocal.nombre,
                    apellido = usuarioLocal.apellido,
                    nombreUsuario = usuarioLocal.nombreUsuario,
                    barrio = usuarioLocal.barrio,
                    telefono = usuarioLocal.telefono,
                    categoriasDeInteres = usuarioLocal.categoriasDeInteres
                        .split(",")
                        .filter { it.isNotBlank() },
                    puntosHistoricos = usuarioLocal.puntosHistoricos,
                    rachaActual = usuarioLocal.rachaActual,
                    ultimaActividadAprobadaEn = usuarioLocal.ultimaActividadAprobadaEn,
                ),
                historial = historialLocal.map { registro ->
                    RegistroMigracionDto(
                        id = registro.id,
                        actividadId = registro.actividadId,
                        categoria = registro.categoria,
                        fecha = registro.fecha,
                        resultado = registro.resultado,
                        motivoIA = registro.motivoIA,
                        puntosOtorgados = registro.puntosOtorgados,
                        huellaImagen = registro.huellaImagen,
                        pasosRegistrados = registro.pasosRegistrados,
                    )
                },
            ),
        )

        // Solo se llega acá si la llamada de arriba no lanzó: ver KDoc de la clase.
        appDatabase.clearAllTables()
        marcarCompletada()
    }

    private fun marcarCompletada() = prefs.edit { putBoolean(CLAVE_COMPLETADA, true) }
}
