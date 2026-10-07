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
 * Base de datos Room de EcoSmart. Nacida como arquitectura local-first (spec 001,
 * research.md §0), pero **superada por spec 002-firestore-datos-usuario**: Firestore es
 * ahora la fuente de verdad de los datos de usuario (perfil, historial, puntaje) y Room
 * nunca llegó a reimplementarse como caché activo de ese módulo (RF-D006). Quedan:
 *   - `UsuarioDao`/`RegistroVerificacionDao` (`@Deprecated`): sin uso de negocio, solo
 *     los lee `MigracionDatosLocales` (T043) para la subida única de cuentas de spec 001.
 *   - `ActividadDao`/`PuntoVerdeDao`/`PermisoDao`: siguen vigentes (catálogos de solo
 *     lectura y gating local de permisos, fuera de alcance de spec 002).
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
    @Suppress("DEPRECATION")
    abstract fun usuarioDao(): UsuarioDao
    abstract fun permisoDao(): PermisoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun puntoVerdeDao(): PuntoVerdeDao
    @Suppress("DEPRECATION")
    abstract fun registroVerificacionDao(): RegistroVerificacionDao
}
