package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.persistence.firestore.UsuarioFirestoreMapper
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLECCION_USUARIOS = "usuarios"

/**
 * Implementación Firestore de [UsuarioRepository] (spec 002-firestore-datos-usuario,
 * RF-D001/RF-D005) — reemplaza a Room como fuente de verdad del perfil (RF-D006).
 */
@Singleton
class UsuarioRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UsuarioRepository {

    private val coleccion get() = firestore.collection(COLECCION_USUARIOS)

    override suspend fun buscarPorEmail(email: String): Usuario? {
        val resultado = coleccion.whereEqualTo("email", email).limit(1).get().await()
        val documento = resultado.documents.firstOrNull() ?: return null
        return UsuarioFirestoreMapper.toDomain(documento.id, documento)
    }

    override suspend fun buscarPorId(id: UsuarioId): Usuario? {
        val snapshot = coleccion.document(id.valor).get().await()
        if (!snapshot.exists()) return null
        return UsuarioFirestoreMapper.toDomain(id.valor, snapshot)
    }

    override suspend fun guardar(usuario: Usuario) {
        // Edge Case de spec.md (ediciones concurrentes desde dos dispositivos): se
        // resuelven por última escritura según el reloj del SERVIDOR, nunca el del
        // dispositivo — de ahí FieldValue.serverTimestamp() y no Instant.now() local.
        val datos = UsuarioFirestoreMapper.toFirestoreMap(usuario) +
            mapOf("actualizadoEn" to FieldValue.serverTimestamp())
        coleccion.document(usuario.id.valor).set(datos, SetOptions.merge()).await()
    }

    /**
     * RF-D002: Firestore nunca conoce la contraseña, ni siquiera cifrada — el login
     * real pasa a hacerse contra Firebase Authentication (`IniciarSesion`, T028), no
     * contra este repositorio. No hay una implementación funcional posible acá; lanza
     * para que un caller remanente de spec 001 falle ruidosamente en vez de en silencio.
     */
    override suspend fun autenticar(email: String, contrasenaPlana: String): Usuario? {
        throw UnsupportedOperationException(
            "UsuarioRepository.autenticar quedó obsoleto por RF-D002 — usar " +
                "FirebaseAuth.signInWithEmailAndPassword (ver T028).",
        )
    }

    /**
     * RF-D017: sin listener permanente de Firestore — emite una única lectura bajo
     * demanda. Quien necesite datos actualizados vuelve a recolectar este `Flow` (p.
     * ej. al volver a primer plano, T024); no hay una suscripción en vivo a cambios.
     */
    override fun observarPorId(id: UsuarioId): Flow<Usuario?> = flow {
        emit(buscarPorId(id))
    }
}
