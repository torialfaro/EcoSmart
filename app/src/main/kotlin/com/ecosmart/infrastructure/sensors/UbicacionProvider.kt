package com.ecosmart.infrastructure.sensors

import android.annotation.SuppressLint
import android.content.Context
import com.ecosmart.domain.valueobject.Coordenada
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Obtiene una única posición reciente del dispositivo (research.md §1.2:
 * sin tracking continuo — RF-053 solo necesita un punto de referencia). El
 * permiso de GPS (RF-043) se valida en el llamador antes de invocar esta
 * clase; si el permiso no fue otorgado, `getCurrentLocation` lanza
 * `SecurityException`, que el llamador (`PuntosVerdesViewModel`) trata
 * igual que "sin ubicación disponible".
 */
@Singleton
class UbicacionProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacionActual(): Coordenada? {
        val cliente = LocationServices.getFusedLocationProviderClient(context)
        val solicitud = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        val ubicacion = cliente.getCurrentLocation(solicitud, null).await() ?: return null
        return Coordenada(latitud = ubicacion.latitude, longitud = ubicacion.longitude)
    }
}
