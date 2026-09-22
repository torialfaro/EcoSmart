package com.ecosmart.domain.valueobject

/**
 * Estado de un permiso de dispositivo para un
 * [com.ecosmart.domain.model.PermisoDispositivo] (RF-043 a RF-045).
 * Valor cerrado — Principio VI de constitution.md. Se resolvió en
 * `/speckit.clarify` que 3 estados alcanzan (la denegación permanente no
 * necesita un 4º estado, la cubre el Edge Case correspondiente).
 */
enum class EstadoPermiso {
    NO_SOLICITADO,
    OTORGADO,
    DENEGADO,
}

/** Los 4 permisos de dispositivo que EcoSmart solicita post-login (RF-043). */
enum class TipoPermiso {
    GALERIA,
    CAMARA,
    GPS,
    PODOMETRO,
}
