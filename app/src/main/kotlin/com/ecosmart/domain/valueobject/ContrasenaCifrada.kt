package com.ecosmart.domain.valueobject

/**
 * Envuelve el JWE compacto de la contraseña cifrada (RNF-006). El dominio
 * nunca maneja la contraseña en texto plano ni la clave JWK que la cifra o
 * descifra — eso es responsabilidad exclusiva de `infrastructure.security`
 * (ver research.md §4, data-model.md §3).
 */
@JvmInline
value class ContrasenaCifrada(val jweCompacto: String)
