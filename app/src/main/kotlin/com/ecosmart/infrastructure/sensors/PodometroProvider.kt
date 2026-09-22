package com.ecosmart.infrastructure.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PASOS_POR_SEGUNDO_MAXIMO_PLAUSIBLE = 10
private const val NANOS_POR_SEGUNDO = 1_000_000_000.0

/**
 * Expone el conteo acumulado de `Sensor.TYPE_STEP_COUNTER` (research.md
 * §1.1: más eficiente en batería que TYPE_STEP_DETECTOR). El valor es
 * acumulado desde el último reinicio del dispositivo, no desde la
 * instalación de la app — quien consuma este Flow SIEMPRE debe calcular el
 * delta contra un valor base propio, nunca usar el crudo directamente.
 *
 * Descarta (no emite) lecturas cuyo delta respecto de la anterior implique
 * más de [PASOS_POR_SEGUNDO_MAXIMO_PLAUSIBLE] pasos por segundo sostenidos
 * (Edge Case "Lecturas anómalas del podómetro").
 */
@Singleton
class PodometroProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observarPasosAcumulados(): Flow<Int> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        var ultimoValor: Int? = null
        var ultimoTimestampNanos: Long? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val valorActual = event.values[0].toInt()
                val timestampActual = event.timestamp

                val anterior = ultimoValor
                val timestampAnterior = ultimoTimestampNanos
                if (anterior != null && timestampAnterior != null) {
                    val deltaPasos = valorActual - anterior
                    val deltaSegundos = (timestampActual - timestampAnterior) / NANOS_POR_SEGUNDO
                    val pasosPorSegundo = if (deltaSegundos > 0) deltaPasos / deltaSegundos else 0.0
                    if (pasosPorSegundo > PASOS_POR_SEGUNDO_MAXIMO_PLAUSIBLE) {
                        // Lectura anómala: se descarta sin actualizar el estado base ni emitir.
                        return
                    }
                }

                ultimoValor = valorActual
                ultimoTimestampNanos = timestampActual
                trySend(valorActual)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
