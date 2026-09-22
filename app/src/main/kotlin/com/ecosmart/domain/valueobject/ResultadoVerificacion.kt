package com.ecosmart.domain.valueobject

/**
 * Resultado de un [com.ecosmart.domain.model.RegistroVerificacion]
 * (RF-023, RF-028, RF-029). Valor cerrado — Principio VI de
 * constitution.md. El timeout de EcoGPT (RNF-008) NO es un valor de este
 * enum: se trata como un error de infraestructura, nunca como
 * RECHAZADO/INDETERMINADO (ver spec.md, Edge Case "Timeout de EcoGPT").
 */
enum class ResultadoVerificacion {
    APROBADO,
    RECHAZADO,
    INDETERMINADO,
}
