package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.domain.valueobject.esTelefonoValido
import javax.inject.Inject

/**
 * Corrección post-QA (2026-09-23): `email` se saca de [DatosPerfil] — el
 * correo NUNCA se edita desde esta pantalla (es la única excepción; el
 * resto de los campos son editables y obligatorios).
 */
data class DatosPerfil(
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: Barrio,
    val telefono: String,
    val categoriasDeInteres: Set<CategoriaActividad>,
)

sealed class ResultadoEdicionPerfil {
    data class Exitoso(val usuario: Usuario) : ResultadoEdicionPerfil()
    /** Corrección post-QA (2026-09-23): nombre, apellido y nombre de usuario ahora son obligatorios. */
    data object CamposObligatoriosIncompletos : ResultadoEdicionPerfil()
    /** Corrección post-QA (2026-09-23): teléfono obligatorio, formato "+549" + 10 dígitos. */
    data object TelefonoInvalido : ResultadoEdicionPerfil()
}

/** US3 — edición de datos de perfil y categorías (RF-007 a RF-009). El correo NUNCA se edita acá. */
class EditarPerfil @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(usuarioId: UsuarioId, datos: DatosPerfil): ResultadoEdicionPerfil {
        val usuario = requireNotNull(usuarioRepository.buscarPorId(usuarioId)) {
            "Usuario $usuarioId no encontrado"
        }

        if (datos.nombre.isBlank() || datos.apellido.isBlank() || datos.nombreUsuario.isBlank()) {
            return ResultadoEdicionPerfil.CamposObligatoriosIncompletos
        }
        if (!esTelefonoValido(datos.telefono)) return ResultadoEdicionPerfil.TelefonoInvalido

        val actualizado = usuario.copy(
            nombre = datos.nombre,
            apellido = datos.apellido,
            nombreUsuario = datos.nombreUsuario,
            barrio = datos.barrio,
            telefono = datos.telefono,
            categoriasDeInteres = datos.categoriasDeInteres,
        )
        usuarioRepository.guardar(actualizado)
        return ResultadoEdicionPerfil.Exitoso(actualizado)
    }
}
