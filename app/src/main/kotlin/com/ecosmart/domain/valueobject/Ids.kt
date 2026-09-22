package com.ecosmart.domain.valueobject

import java.util.UUID

/**
 * IDs tipados (Value Objects, Principio VI) para no mezclar
 * accidentalmente el identificador de una entidad con el de otra en la
 * firma de un caso de uso (ver data-model.md §3).
 */
@JvmInline
value class UsuarioId(val valor: String) {
    companion object {
        fun nuevo(): UsuarioId = UsuarioId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class ActividadId(val valor: String) {
    companion object {
        fun nuevo(): ActividadId = ActividadId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class RegistroVerificacionId(val valor: String) {
    companion object {
        fun nuevo(): RegistroVerificacionId = RegistroVerificacionId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class PuntoVerdeId(val valor: String) {
    companion object {
        fun nuevo(): PuntoVerdeId = PuntoVerdeId(UUID.randomUUID().toString())
    }
}
