package com.ecosmart.infrastructure.di

import android.content.Context
import androidx.room.Room
import com.ecosmart.infrastructure.persistence.room.AppDatabase
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadDao
import com.ecosmart.infrastructure.persistence.room.permiso.PermisoDao
import com.ecosmart.infrastructure.persistence.room.puntoverde.PuntoVerdeDao
import com.ecosmart.infrastructure.persistence.room.usuario.UsuarioDao
import com.ecosmart.infrastructure.persistence.room.verificacion.RegistroVerificacionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provee la instancia única de [AppDatabase]. Los providers de cada DAO
 * (`UsuarioDao` ✅, `PermisoDao` ✅, `ActividadDao` ✅, `PuntoVerdeDao` ✅,
 * `RegistroVerificacionDao` ✅) se agregan a este mismo módulo a medida que
 * cada Módulo los crea (ver tasks.md T024, T038, T047, T057, T067).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "ecosmart.db"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .build()

    @Provides
    fun provideUsuarioDao(appDatabase: AppDatabase): UsuarioDao = appDatabase.usuarioDao()

    @Provides
    fun providePermisoDao(appDatabase: AppDatabase): PermisoDao = appDatabase.permisoDao()

    @Provides
    fun provideActividadDao(appDatabase: AppDatabase): ActividadDao = appDatabase.actividadDao()

    @Provides
    fun providePuntoVerdeDao(appDatabase: AppDatabase): PuntoVerdeDao = appDatabase.puntoVerdeDao()

    @Provides
    fun provideRegistroVerificacionDao(appDatabase: AppDatabase): RegistroVerificacionDao =
        appDatabase.registroVerificacionDao()
}
