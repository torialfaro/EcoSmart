package com.ecosmart.infrastructure.persistence.firestore

import com.ecosmart.infrastructure.sensors.EstadoCaminata
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Mapea `usuarios/{uid}/caminataEnCurso/actual` (data-model.md §4) → [EstadoCaminata], para
 * que el dispositivo que no inició la caminata pueda reflejar su progreso al refrescar
 * (RF-D017). Solo lectura: toda la escritura de este documento pasa por la transacción
 * del backend de confianza (RF-D008).
 *
 * El documento no incluye `puntosOtorgados` (no otorga puntos por sí solo, ver data-model.md
 * §4), por eso `"COMPLETADA"` se mapea a [EstadoCaminata.Ninguna] acá: ese resultado ya le
 * llega al dispositivo que completó la caminata directamente en la respuesta del endpoint
 * `/registros-verificacion/caminar`, no por este mapper.
 */
object CaminataEnCursoFirestoreMapper {

    fun toDomain(snapshot: DocumentSnapshot): EstadoCaminata {
        val datos = snapshot.data.orEmpty()
        if (datos["estado"] as? String != "EN_CURSO") return EstadoCaminata.Ninguna
        return EstadoCaminata.EnCurso(
            usuarioId = "",
            actividadId = datos["actividadId"] as? String ?: "",
            metaPasos = (datos["metaPasos"] as? Long)?.toInt() ?: 0,
            pasosLogrados = (datos["pasosLogrados"] as? Long)?.toInt() ?: 0,
            ultimoValorSensor = null,
            fecha = datos["fecha"] as? String ?: "",
        )
    }
}
