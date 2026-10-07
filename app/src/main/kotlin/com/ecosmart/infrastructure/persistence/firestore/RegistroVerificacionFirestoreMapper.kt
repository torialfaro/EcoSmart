package com.ecosmart.infrastructure.persistence.firestore

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate

/**
 * Mapea `usuarios/{uid}/registrosVerificacion/{id}` (data-model.md §3) → `RegistroVerificacion`.
 * Solo lectura (`toDomain`): el cliente nunca escribe este documento directamente (ver
 * corrección I2 de `/speckit.analyze`) — envía la evidencia al backend de confianza vía
 * `BackendConfianzaClient`, que es quien lo crea completo (RF-D014).
 */
object RegistroVerificacionFirestoreMapper {

    fun toDomain(usuarioId: UsuarioId, snapshot: DocumentSnapshot): RegistroVerificacion {
        val datos = snapshot.data.orEmpty()
        return RegistroVerificacion(
            id = RegistroVerificacionId(snapshot.id),
            usuarioId = usuarioId,
            actividadId = ActividadId(datos["actividadId"] as? String ?: ""),
            categoria = CategoriaActividad.valueOf(datos["categoria"] as String),
            fecha = LocalDate.parse(datos["fecha"] as String),
            resultado = ResultadoVerificacion.valueOf(datos["resultado"] as String),
            motivoIA = datos["motivoIA"] as? String,
            puntosOtorgados = (datos["puntosOtorgados"] as? Long)?.toInt() ?: 0,
            huellaImagen = datos["huellaImagen"] as? String,
            pasosRegistrados = (datos["pasosRegistrados"] as? Long)?.toInt(),
        )
    }
}
