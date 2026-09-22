package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioDao
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioMapper
import com.ecosmart.infrastructure.security.CifradorContrasena
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación Room de [UsuarioRepository]. El cifrado/descifrado de la
 * contraseña (RNF-006/RNF-007) se concentra acá: `autenticar` es el único
 * punto del sistema que compara una contraseña en claro contra el JWE
 * guardado, usando [CifradorContrasena] sin exponer el valor descifrado
 * fuera de este método.
 */
@Singleton
class UsuarioRepositoryImpl @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val cifradorContrasena: CifradorContrasena,
) : UsuarioRepository {

    override suspend fun buscarPorEmail(email: String): Usuario? =
        usuarioDao.findByEmail(email)?.let(UsuarioMapper::toDomain)

    override suspend fun buscarPorId(id: UsuarioId): Usuario? =
        usuarioDao.findById(id.valor)?.let(UsuarioMapper::toDomain)

    override suspend fun guardar(usuario: Usuario) {
        usuarioDao.insertarOActualizar(UsuarioMapper.toRoomEntity(usuario))
    }

    override suspend fun autenticar(email: String, contrasenaPlana: String): Usuario? {
        val entity = usuarioDao.findByEmail(email) ?: return null
        // Las cuentas creadas con Google (RF-002) no tienen contraseña local: el JWE queda vacío.
        if (entity.contrasenaCifradaJwe.isEmpty()) return null
        if (!cifradorContrasena.coincide(contrasenaPlana, entity.contrasenaCifradaJwe)) return null
        return UsuarioMapper.toDomain(entity)
    }

    override fun observarPorId(id: UsuarioId): Flow<Usuario?> =
        usuarioDao.observarPorId(id.valor).map { it?.let(UsuarioMapper::toDomain) }
}
