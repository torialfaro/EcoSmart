package com.ecosmart.infrastructure.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ecosmart.domain.model.PuntoVerde
import com.ecosmart.domain.repository.PuntoVerdeRepository
import com.ecosmart.domain.valueobject.PuntoVerdeId
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

/**
 * Sincroniza el dataset de Puntos Verdes en segundo plano cuando hay
 * conexión (RF-052, research.md §5). Si falla, WorkManager reintenta según
 * su política de backoff por defecto; los Puntos Verdes ya sincronizados
 * siguen disponibles offline mientras tanto (el dataset empaquetado o la
 * última sincronización exitosa nunca se borran salvo que la nueva
 * respuesta llegue completa).
 */
@HiltWorker
class PuntosVerdesSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val puntosVerdesSyncClient: PuntosVerdesSyncClient,
    private val puntoVerdeRepository: PuntoVerdeRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        val respuesta = puntosVerdesSyncClient.sincronizarPuntosVerdes()
        val cuerpo = respuesta.body()
        if (respuesta.isSuccessful && cuerpo != null) {
            puntoVerdeRepository.reemplazarTodos(cuerpo.puntos.map { it.aDominio() })
        }
        // 304 (sin cambios) u otra respuesta exitosa sin cuerpo: se conserva la copia local.
        Result.success()
    } catch (error: IOException) {
        Result.retry()
    } catch (error: HttpException) {
        Result.retry()
    }
}

private fun PuntoVerdeDto.aDominio(): PuntoVerde = PuntoVerde(
    id = PuntoVerdeId(id),
    nombre = nombre,
    direccion = direccion,
    barrio = barrio,
    latitud = latitud,
    longitud = longitud,
    categoriasQueAcepta = categoriasQueAcepta?.toSet() ?: emptySet(),
)
