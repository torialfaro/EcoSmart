package com.ecosmart.infrastructure.sensors

import android.content.Context
import com.ecosmart.domain.repository.PasosDelDiaRepository
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.SolicitudPasosDelDiaDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS = "pasos_del_dia"
private const val CLAVE_FECHA = "fecha"
private const val CLAVE_BASE = "base"
private const val CLAVE_ULTIMOS_PASOS = "ultimosPasos"

/**
 * Pasos de hoy = conteo acumulado actual de `TYPE_STEP_COUNTER` menos el
 * valor base del día (RF-070, corrección post-QA 2026-09-24). El sensor
 * cuenta desde el último reinicio del dispositivo, así que la base se
 * guarda por fecha; si el conteo actual es menor que la base (el celular se
 * reinició), se toma como nueva base. La base del día es la primera
 * lectura que la app logra ese día, por eso `HomeViewModel` dispara una
 * lectura apenas abre: los pasos previos a esa primera apertura no se
 * pueden recuperar.
 */
@Singleton
class PasosDelDiaRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val podometroProvider: PodometroProvider,
    private val backendConfianzaClient: BackendConfianzaClient,
) : PasosDelDiaRepository {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val scopeSincronizacion = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun pasosDeHoy(): Int? {
        val hoy = LocalDate.now().toString()
        val actual = podometroProvider.leerPasosAcumuladosActual()
            ?: return if (prefs.getString(CLAVE_FECHA, null) == hoy) prefs.getInt(CLAVE_ULTIMOS_PASOS, 0) else null

        var base = prefs.getInt(CLAVE_BASE, actual)
        if (prefs.getString(CLAVE_FECHA, null) != hoy || actual < base) base = actual

        val pasos = actual - base
        prefs.edit()
            .putString(CLAVE_FECHA, hoy)
            .putInt(CLAVE_BASE, base)
            .putInt(CLAVE_ULTIMOS_PASOS, pasos)
            .apply()
        sincronizarConFirestoreSinBloquear(pasos, hoy)
        return pasos
    }

    /**
     * RF-D007: sincroniza `pasosHoy` a Firestore en cada actualización — informativo,
     * nunca otorga puntos (eso es RF-D015, vía `BackendConfianzaClient.otorgarPuntosCaminar`
     * al completar una meta). La línea base del sensor sigue siendo 100% local (arriba);
     * fire-and-forget porque una falla de red acá no debe demorar ni romper la lectura
     * local del podómetro.
     */
    private fun sincronizarConFirestoreSinBloquear(pasosHoy: Int, fecha: String) {
        scopeSincronizacion.launch {
            try {
                backendConfianzaClient.actualizarPasosDelDia(SolicitudPasosDelDiaDto(pasosHoy = pasosHoy, fecha = fecha))
            } catch (_: Exception) {
                // best-effort, ver KDoc.
            }
        }
    }
}
