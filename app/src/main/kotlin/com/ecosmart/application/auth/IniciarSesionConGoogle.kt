package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import javax.inject.Inject

data class DatosCuentaGoogle(
    val email: String,
    val nombre: String,
    val apellido: String,
)

/**
 * RF-002 — crea (o recupera, si ya existía) la cuenta asociada al correo
 * de Google. El flujo real de Credential Manager/Google Sign-In que
 * produce [DatosCuentaGoogle] se cablea en la capa de presentación cuando
 * exista una `Activity` (gap señalado desde la Fase 1/2).
 */
class IniciarSesionConGoogle @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(datos: DatosCuentaGoogle): Usuario {
        usuarioRepository.buscarPorEmail(datos.email)?.let { return it }

        val nuevo = Usuario(
            id = UsuarioId.nuevo(),
            email = datos.email,
            // Sin contraseña local: el login de esta cuenta siempre pasa por
            // Google, nunca por UsuarioRepository.autenticar (que trata un JWE
            // vacío como "sin contraseña local" y rechaza el intento).
            contrasenaCifrada = ContrasenaCifrada(jweCompacto = ""),
            nombre = datos.nombre,
            apellido = datos.apellido,
            nombreUsuario = datos.email.substringBefore("@"),
            // Google no pide barrio; el usuario lo completa después desde su perfil (RF-007/RF-063).
            barrio = null,
            telefono = "",
            categoriasDeInteres = emptySet(),
        )
        usuarioRepository.guardar(nuevo)
        return nuevo
    }
}
