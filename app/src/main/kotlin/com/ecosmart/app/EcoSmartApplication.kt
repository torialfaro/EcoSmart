package com.ecosmart.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ecosmart.infrastructure.network.PuntosVerdesSyncWorker
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration as OsmdroidConfiguration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val NOMBRE_TRABAJO_SYNC_PUNTOS_VERDES = "sincronizar-puntos-verdes"
private const val INTERVALO_SYNC_PUNTOS_VERDES_HORAS = 12L

/**
 * Punto de entrada de Hilt para todo el grafo de dependencias de la app
 * (ver plan.md § Project Structure → infrastructure/di).
 *
 * Implementa `Configuration.Provider` para que WorkManager use
 * [HiltWorkerFactory] al instanciar `@HiltWorker`s como
 * [PuntosVerdesSyncWorker] (T059). El inicializador automático de
 * WorkManager se deshabilita en el Manifest (ver `AndroidManifest.xml`) y
 * se llama a `WorkManager.initialize(...)` manualmente en [onCreate], ya
 * que la inyección de Hilt (`workerFactory`) recién está garantizada
 * disponible después de `super.onCreate()` — el patrón recomendado para
 * evitar el orden ambiguo entre el `ContentProvider` de auto-init de
 * WorkManager y la inyección de campos de Hilt.
 */
@HiltAndroidApp
class EcoSmartApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(applicationContext, workManagerConfiguration)
        programarSincronizacionPuntosVerdes()
        configurarOsmdroid()
    }

    /**
     * RF-065 (research.md §6) — osmdroid exige un user-agent propio para no
     * quedar bloqueado por la política de uso de tiles de OpenStreetMap, y
     * usa el almacenamiento privado de la app para el caché (sin pedir
     * ningún permiso de almacenamiento adicional).
     */
    private fun configurarOsmdroid() {
        val configuracion = OsmdroidConfiguration.getInstance()
        configuracion.userAgentValue = packageName
        configuracion.osmdroidBasePath = getDir("osmdroid", MODE_PRIVATE)
        configuracion.osmdroidTileCache = java.io.File(configuracion.osmdroidBasePath, "tiles")
    }

    /** RF-052 — sincronización en segundo plano de Puntos Verdes cuando hay conexión (research.md §5). */
    private fun programarSincronizacionPuntosVerdes() {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val solicitud = PeriodicWorkRequestBuilder<PuntosVerdesSyncWorker>(
            INTERVALO_SYNC_PUNTOS_VERDES_HORAS,
            TimeUnit.HOURS,
        )
            .setConstraints(restricciones)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NOMBRE_TRABAJO_SYNC_PUNTOS_VERDES,
            ExistingPeriodicWorkPolicy.KEEP,
            solicitud,
        )
    }
}
