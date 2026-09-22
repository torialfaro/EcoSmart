package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.domain.valueobject.esEmailValido
import javax.inject.Inject

data class DatosPerfil(
    val email: String,
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: Barrio,
    val telefono: String,
    val categoriasDeInteres: Set<CategoriaActividad>,
)

sealed class ResultadoEdicionPerfil {
    data class Exitoso(val usuario: Usuario) : ResultadoEdicionPerfil()
    data object EmailInvalido : ResultadoEdicionPerfil()
    data object EmailYaRegistrado : ResultadoEdicionPerfil()
}

/** US3 — edición de datos de perfil, correo y categorías (RF-007 a RF-009). */
class EditarPerfil @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
) {
    suspend operator fun invoke(usuarioId: UsuarioId, datos: DatosPerfil): ResultadoEdicionPerfil {
        val usuario = requireNotNull(usuarioRepository.buscarPorId(usuarioId)) {
            "Usuario $usuarioId no encontrado"
        }

        if (datos.email != usuario.email) {
            if (!esEmailValido(datos.email)) return ResultadoEdicionPerfil.EmailInvalido
            if (usuarioRepository.buscarPorEmail(datos.email) != null) {
                return ResultadoEdicionPerfil.EmailYaRegistrado
            }
        }

        val actualizado = usuario.copy(
            email = datos.email,
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
