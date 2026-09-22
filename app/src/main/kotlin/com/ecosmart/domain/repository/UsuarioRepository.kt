package com.ecosmart.domain.repository

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.UsuarioId
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de dominio hacia la persistencia de [Usuario] (Principio IV: el
 * dominio no conoce Room ni ningún detalle de infraestructura). La
 * implementación vive en `infrastructure/persistence/UsuarioRepositoryImpl`.
 */
interface UsuarioRepository {

    suspend fun buscarPorEmail(email: String): Usuario?

    suspend fun buscarPorId(id: UsuarioId): Usuario?

    /** Inserta o actualiza (upsert) — se usa tanto para crear como para editar. */
    suspend fun guardar(usuario: Usuario)

    /**
     * Verifica las credenciales de login sin exponer la contraseña
     * descifrada fuera de la capa de infraestructura (RNF-006).
     */
    suspend fun autenticar(email: String, contrasenaPlana: String): Usuario?

    fun observarPorId(id: UsuarioId): Flow<Usuario?>
}
