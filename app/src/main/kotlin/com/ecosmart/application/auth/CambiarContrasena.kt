package com.ecosmart.application.auth

import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.domain.valueobject.esContrasenaValida
import com.ecosmart.infrastructure.security.CifradorContrasena
import javax.inject.Inject

sealed class ResultadoCambioContrasena {
    data object Exitoso : ResultadoCambioContrasena()
    data object ContrasenaActualIncorrecta : ResultadoCambioContrasena()
    data object NuevaContrasenaInvalida : ResultadoCambioContrasena()
}

/** US3 — cambio de contraseña exigiendo la actual (RF-008, RNF-006). */
class CambiarContrasena @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cifradorContrasena: CifradorContrasena,
) {
    suspend operator fun invoke(
        usuarioId: UsuarioId,
        contrasenaActual: String,
        contrasenaNueva: String,
    ): ResultadoCambioContrasena {
        val usuario = requireNotNull(usuarioRepository.buscarPorId(usuarioId)) {
            "Usuario $usuarioId no encontrado"
        }

        val coincide = cifradorContrasena.coincide(contrasenaActual, usuario.contrasenaCifrada.jweCompacto)
        if (!coincide) return ResultadoCambioContrasena.ContrasenaActualIncorrecta
        if (!esContrasenaValida(contrasenaNueva)) return ResultadoCambioContrasena.NuevaContrasenaInvalida

        val actualizado = usuario.copy(
            contrasenaCifrada = ContrasenaCifrada(cifradorContrasena.cifrar(contrasenaNueva)),
        )
        usuarioRepository.guardar(actualizado)
        return ResultadoCambioContrasena.Exitoso
    }
}
