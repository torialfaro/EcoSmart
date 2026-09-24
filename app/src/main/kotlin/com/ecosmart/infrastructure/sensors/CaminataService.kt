package com.ecosmart.infrastructure.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.ecosmart.application.activity.DatosCaminata
import com.ecosmart.application.activity.RegistrarCaminata
import com.ecosmart.application.activity.ResultadoCaminata
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.UsuarioId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CANAL_ID = "caminata_en_curso"
private const val NOTIFICACION_ID = 4001

/**
 * Servicio en primer plano que sigue contando pasos con la app en segundo
 * plano (RF-082). Se detiene solo al llegar a la meta (aprueba y suma
 * puntos vía [RegistrarCaminata]) o al vencer el día (RF-057).
 */
@AndroidEntryPoint
class CaminataService : Service() {

    @Inject lateinit var podometroProvider: PodometroProvider

    @Inject lateinit var registrarCaminata: RegistrarCaminata

    @Inject lateinit var store: CaminataEnCursoStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var observacion: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanal()
        val enCurso = store.estado.value as? EstadoCaminata.EnCurso
        if (enCurso == null || store.descartarSiVencio()) {
            detener()
            return START_NOT_STICKY
        }
        iniciarEnPrimerPlano(notificacion(enCurso.pasosLogrados, enCurso.metaPasos))
        if (observacion?.isActive != true) observacion = scope.launch { seguirPasos() }
        return START_STICKY
    }

    private suspend fun seguirPasos() {
        podometroProvider.observarPasosAcumulados().collect { valor ->
            val actual = store.estado.value as? EstadoCaminata.EnCurso ?: return@collect
            if (store.descartarSiVencio()) {
                detener()
                return@collect
            }
            val previo = actual.ultimoValorSensor
            // Tras un reinicio del celular el sensor vuelve a 0: se cuenta el valor entero como delta.
            val delta = if (previo == null) 0 else if (valor >= previo) valor - previo else valor
            val logrados = actual.pasosLogrados + delta
            store.actualizarProgreso(logrados, valor)
            notificar(notificacion(logrados, actual.metaPasos))
            if (logrados >= actual.metaPasos) completar(actual, logrados)
        }
    }

    private suspend fun completar(actual: EstadoCaminata.EnCurso, logrados: Int) {
        val resultado = registrarCaminata(
            DatosCaminata(
                usuarioId = UsuarioId(actual.usuarioId),
                actividadId = ActividadId(actual.actividadId),
                metaPasos = actual.metaPasos,
                pasosBase = 0,
                pasosActuales = logrados,
            ),
        )
        if (resultado is ResultadoCaminata.Aprobada) {
            val puntos = resultado.registro.puntosOtorgados
            store.completar(puntos, resultado.pasosCaminados, actual.metaPasos)
            notificar(notificacionFinal(puntos))
        }
        detener(conservarNotificacion = resultado is ResultadoCaminata.Aprobada)
    }

    private fun detener(conservarNotificacion: Boolean = false) {
        observacion?.cancel()
        ServiceCompat.stopForeground(
            this,
            if (conservarNotificacion) ServiceCompat.STOP_FOREGROUND_DETACH else ServiceCompat.STOP_FOREGROUND_REMOVE,
        )
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun iniciarEnPrimerPlano(notificacion: Notification) {
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH else 0
        ServiceCompat.startForeground(this, NOTIFICACION_ID, notificacion, tipo)
    }

    private fun notificar(notificacion: Notification) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICACION_ID, notificacion)
    }

    private fun notificacion(logrados: Int, meta: Int): Notification =
        NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Caminata en curso")
            .setContentText("$logrados / $meta pasos")
            .setProgress(meta, logrados.coerceAtMost(meta), false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    private fun notificacionFinal(puntos: Int): Notification =
        NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("¡Meta alcanzada!")
            .setContentText("Sumaste $puntos puntos. Abrí EcoSmart para verlo.")
            .setAutoCancel(true)
            .build()

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(CANAL_ID, "Caminata en curso", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    companion object {
        fun iniciar(context: Context) {
            androidx.core.content.ContextCompat.startForegroundService(context, Intent(context, CaminataService::class.java))
        }
    }
}
