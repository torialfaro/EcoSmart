package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.persistence.firestore.RegistroVerificacionFirestoreMapper
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val COLECCION_USUARIOS = "usuarios"
private const val SUBCOLECCION_REGISTROS = "registrosVerificacion"

/**
 * Implementación Firestore, de solo lectura, de [RegistroVerificacionRepository] (spec
 * 002-firestore-datos-usuario, RF-D003). El otorgamiento de puntos (escritura de
 * `resultado`/`puntosOtorgados`) pasa exclusivamente por el backend de confianza vía
 * `BackendConfianzaClient` (RF-D014) — nunca por `guardar()` de este repositorio.
 */
@Singleton
class RegistroVerificacionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RegistroVerificacionRepository {

    private fun registros(usuarioId: UsuarioId) = firestore.collection(COLECCION_USUARIOS)
        .document(usuarioId.valor)
        .collection(SUBCOLECCION_REGISTROS)

    override suspend fun guardar(registro: RegistroVerificacion) {
        throw UnsupportedOperationException(
            "RegistroVerificacionRepository.guardar quedó obsoleto por RF-D014 — el " +
                "registro se crea vía BackendConfianzaClient (ver T024a/T037), nunca " +
                "escribiendo Firestore directamente desde el cliente.",
        )
    }

    override suspend fun contarAprobadosDelDia(
        usuarioId: UsuarioId,
        categoria: CategoriaActividad,
        fecha: LocalDate,
    ): Int {
        val conteo = registros(usuarioId)
            .whereEqualTo("categoria", categoria.name)
            .whereEqualTo("fecha", fecha.toString())
            .whereEqualTo("resultado", "APROBADO")
            .count()
            .get(AggregateSource.SERVER)
            .await()
        return conteo.count.toInt()
    }

    override suspend fun ultimosN(usuarioId: UsuarioId, n: Int): List<RegistroVerificacion> =
        registros(usuarioId)
            .orderBy("creadoEn", Query.Direction.DESCENDING)
            .limit(n.toLong())
            .get()
            .await()
            .documents
            .map { RegistroVerificacionFirestoreMapper.toDomain(usuarioId, it) }

    override suspend fun todos(usuarioId: UsuarioId): List<RegistroVerificacion> =
        registros(usuarioId)
            .orderBy("creadoEn", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { RegistroVerificacionFirestoreMapper.toDomain(usuarioId, it) }

    override suspend fun huellasAprobadas(usuarioId: UsuarioId, categoria: CategoriaActividad): List<String> =
        registros(usuarioId)
            .whereEqualTo("categoria", categoria.name)
            .whereEqualTo("resultado", "APROBADO")
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("huellaImagen") }
}
