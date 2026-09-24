package com.ecosmart.domain.valueobject

private const val LONGITUD_MINIMA_CONTRASENA = 8
private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private val TELEFONO_REGEX = Regex("^\\+549\\d{10}$")

/** RF-003: formato válido de correo electrónico. */
fun esEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email)

/**
 * Corrección post-QA (2026-09-23): el teléfono es obligatorio y DEBE tener
 * el formato de celular argentino "+549" + código de área + número (10
 * dígitos en total después del "+549", p. ej. "+54911XXXXXXXX").
 */
fun esTelefonoValido(telefono: String): Boolean = TELEFONO_REGEX.matches(telefono)

/**
 * RF-004: la contraseña debe tener al menos 8 caracteres y combinar letras
 * y números; se rechaza si es solo numérica o solo alfabética. Se comparte
 * entre `RegistrarUsuario` y `CambiarContrasena` para no duplicar esta
 * regla crítica en dos lugares.
 */
fun esContrasenaValida(contrasena: String): Boolean {
    if (contrasena.length < LONGITUD_MINIMA_CONTRASENA) return false
    val tieneLetra = contrasena.any { it.isLetter() }
    val tieneNumero = contrasena.any { it.isDigit() }
    return tieneLetra && tieneNumero
}
