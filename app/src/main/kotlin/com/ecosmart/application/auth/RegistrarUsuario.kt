package com.ecosmart.application.auth

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.domain.valueobject.esContrasenaValida
import com.ecosmart.domain.valueobject.esEmailValido
import com.ecosmart.infrastructure.security.CifradorContrasena
import javax.inject.Inject

data class DatosRegistro(
    val email: String,
    val contrasenaPlana: String,
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: Barrio,
    val telefono: String,
    val categoriasDeInteres: Set<CategoriaActividad>,
)

sealed class ResultadoRegistro {
    data class Exitoso(val usuario: Usuario) : ResultadoRegistro()
    data object EmailInvalido : ResultadoRegistro()
    data object EmailYaRegistrado : ResultadoRegistro()
    data object ContrasenaInvalida : ResultadoRegistro()
    data object SinCategoriasSeleccionadas : ResultadoRegistro()
}

/**
 * US1/US2 — crea una cuenta con correo/contraseña (RF-001 a RF-006). La
 * contraseña se cifra acá; nunca llega en claro a [UsuarioRepository].
 */
class RegistrarUsuario @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cifradorContrasena: CifradorContrasena,
) {
    suspend operator fun invoke(datos: DatosRegistro): ResultadoRegistro {
        if (!esEmailValido(datos.email)) return ResultadoRegistro.EmailInvalido
        if (usuarioRepository.buscarPorEmail(datos.email) != null) return ResultadoRegistro.EmailYaRegistrado
        if (!esContrasenaValida(datos.contrasenaPlana)) return ResultadoRegistro.ContrasenaInvalida
        if (datos.categoriasDeInteres.isEmpty()) return ResultadoRegistro.SinCategoriasSeleccionadas

        val usuario = Usuario(
            id = UsuarioId.nuevo(),
            email = datos.email,
            contrasenaCifrada = ContrasenaCifrada(cifradorContrasena.cifrar(datos.contrasenaPlana)),
            nombre = datos.nombre,
            apellido = datos.apellido,
            nombreUsuario = datos.nombreUsuario,
            barrio = datos.barrio,
            telefono = datos.telefono,
            categoriasDeInteres = datos.categoriasDeInteres,
        )
        usuarioRepository.guardar(usuario)
        return ResultadoRegistro.Exitoso(usuario)
    }
}
