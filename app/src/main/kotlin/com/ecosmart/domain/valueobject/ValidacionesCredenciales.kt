package com.ecosmart.domain.valueobject

private const val LONGITUD_MINIMA_CONTRASENA = 8
private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/** RF-003: formato válido de correo electrónico. */
fun esEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email)

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
