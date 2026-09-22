package com.ecosmart.infrastructure.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadDao
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadRoomEntity
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoDao
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoDispositivoRoomEntity
import com.ecosmart.infrastructure.persistence.room.puntoverde.PuntoVerdeDao
import com.ecosmart.infrastructure.persistence.room.puntoverde.PuntoVerdeRoomEntity
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioDao
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioRoomEntity
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionDao
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionRoomEntity

/**
 * Base de datos Room de EcoSmart (arquitectura local-first — ver
 * research.md §0: no hay backend propio, Room es la única base de datos).
 *
 * El `entities` se completa incrementalmente a medida que cada Módulo
 * implementa su propia entidad y DAO, per tasks.md:
 *   - Módulo 1 (Login):        UsuarioRoomEntity + UsuarioDao               (T023/T024) ✅
 *   - Módulo 2 (Permisos):     PermisoDispositivoRoomEntity + PermisoDao    (T038) ✅
 *   - Módulo 3 (Home):         ActividadRoomEntity + ActividadDao,
 *                              PuntoVerdeRoomEntity + PuntoVerdeDao         (T047/T057) ✅
 *   - Módulo 4 (Verificación): RegistroVerificacionRoomEntity + RegistroVerificacionDao (T067) ✅
 *
 * Ver data-model.md §4 para el esquema completo de cada entidad. Versión 1
 * sin migraciones todavía (sin release previa publicada — ver
 * data-model.md § Migraciones: cualquier cambio de esquema posterior a la
 * primera release DEBE ir acompañado de una `Migration` explícita, nunca
 * `fallbackToDestructiveMigration` en producción).
 */
@Database(
    entities = [
        UsuarioRoomEntity::class,
        PermisoDispositivoRoomEntity::class,
        ActividadRoomEntity::class,
        PuntoVerdeRoomEntity::class,
        RegistroVerificacionRoomEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun permisoDao(): PermisoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun puntoVerdeDao(): PuntoVerdeDao
    abstract fun registroVerificacionDao(): RegistroVerificacionDao
}
