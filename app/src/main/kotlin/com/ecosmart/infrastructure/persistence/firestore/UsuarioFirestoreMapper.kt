package com.ecosmart.infrastructure.persistence.firestore

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate

/**
 * Mapea `usuarios/{uid}` (data-model.md §2, spec 002-firestore-datos-usuario) ↔ `Usuario`.
 * La contraseña NUNCA viaja por acá (RF-D002): se delega íntegramente a Firebase
 * Authentication. `ContrasenaCifrada` queda con un valor centinela vacío hasta que US3
 * (T027-T032) termine de desacoplar `Usuario` del esquema JWE heredado de spec 001.
 */
object UsuarioFirestoreMapper {

    private val SIN_CONTRASENA_LOCAL = ContrasenaCifrada("")

    fun toDomain(uid: String, snapshot: DocumentSnapshot): Usuario {
        val datos = snapshot.data.orEmpty()
        return Usuario(
            id = UsuarioId(uid),
            email = datos["email"] as? String ?: "",
            contrasenaCifrada = SIN_CONTRASENA_LOCAL,
            nombre = datos["nombre"] as? String ?: "",
            apellido = datos["apellido"] as? String ?: "",
            nombreUsuario = datos["nombreUsuario"] as? String ?: "",
            barrio = (datos["barrio"] as? String)?.let { Barrio.valueOf(it) },
            telefono = datos["telefono"] as? String ?: "",
            categoriasDeInteres = (datos["categoriasDeInteres"] as? List<*>)
                .orEmpty()
                .filterIsInstance<String>()
                .map { CategoriaActividad.valueOf(it) }
                .toSet(),
            puntosHistoricos = (datos["puntosHistoricos"] as? Long)?.toInt() ?: 0,
            rachaActual = (datos["rachaActual"] as? Long)?.toInt() ?: 0,
            ultimaActividadAprobadaEn = (datos["ultimaActividadAprobadaEn"] as? String)?.let(LocalDate::parse),
        )
    }

    /**
     * Solo los campos que el cliente tiene permitido escribir (RF-D004). Los
     * restringidos por RF-D014 (`puntosHistoricos`, `rachaActual`, `nivel`,
     * `ultimaActividadAprobadaEn`, `pasosHoy`, `pasosHoyFecha`) se omiten siempre — las
     * Reglas de Seguridad (`firestore.rules`) los rechazarían de todas formas.
     */
    fun toFirestoreMap(usuario: Usuario): Map<String, Any?> = mapOf(
        "email" to usuario.email,
        "nombre" to usuario.nombre,
        "apellido" to usuario.apellido,
        "nombreUsuario" to usuario.nombreUsuario,
        "barrio" to usuario.barrio?.name,
        "telefono" to usuario.telefono,
        "categoriasDeInteres" to usuario.categoriasDeInteres.map { it.name },
    )
}
