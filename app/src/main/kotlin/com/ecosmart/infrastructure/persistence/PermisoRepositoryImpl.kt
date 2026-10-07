package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.PermisoDispositivo
import com.ecosmart.domain.repository.PermisoRepository
import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import com.ecosmart.infrastructure.firebase.DispositivoIdProvider
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoDao
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoMapper
import com.ecosmart.infrastructure.session.SesionUsuario
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermisoRepositoryImpl @Inject constructor(
    private val permisoDao: PermisoDao,
    private val firestore: FirebaseFirestore,
    private val sesionUsuario: SesionUsuario,
    private val dispositivoIdProvider: DispositivoIdProvider,
) : PermisoRepository {

    override suspend fun obtenerTodos(): List<PermisoDispositivo> =
        permisoDao.obtenerTodos().map(PermisoMapper::toDomain)

    override suspend fun guardarEstado(tipo: TipoPermiso, estado: EstadoPermiso) {
        permisoDao.upsert(PermisoMapper.toRoomEntity(PermisoDispositivo(tipo, estado)))
        escribirEnFirestoreSinBloquear(tipo, estado)
    }

    override fun observar(tipo: TipoPermiso): Flow<PermisoDispositivo?> =
        permisoDao.observar(tipo.name).map { it?.let(PermisoMapper::toDomain) }

    /**
     * RF-D009: informativo, el cliente escribe directamente a Firestore (no pasa por el
     * backend de confianza). Room sigue siendo la fuente de verdad del gating local
     * (`bloqueaFormulario`), por eso una falla acá se ignora en silencio — nunca debe
     * bloquear el flujo de permisos del dispositivo.
     */
    private suspend fun escribirEnFirestoreSinBloquear(tipo: TipoPermiso, estado: EstadoPermiso) {
        val uid = sesionUsuario.usuarioActualId.value?.valor ?: return
        val dispositivoId = dispositivoIdProvider.dispositivoId
        try {
            firestore.collection("usuarios").document(uid)
                .collection("permisosDispositivo")
                .document("${dispositivoId}_${tipo.name}")
                .set(
                    mapOf(
                        "dispositivoId" to dispositivoId,
                        "tipo" to tipo.name,
                        "estado" to estado.name,
                        "actualizadoEn" to FieldValue.serverTimestamp(),
                    ),
                )
                .await()
        } catch (_: Exception) {
            // best-effort, ver KDoc.
        }
    }
}
