package com.ecosmart.infrastructure.di

import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.repository.PermisoRepository
import com.ecosmart.domain.repository.PuntoVerdeRepository
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.infrastructure.persistence.ActividadRepositoryImpl
import com.ecosmart.infrastructure.persistence.PermisoRepositoryImpl
import com.ecosmart.infrastructure.persistence.PuntoVerdeRepositoryImpl
import com.ecosmart.infrastructure.persistence.RegistroVerificacionRepositoryImpl
import com.ecosmart.infrastructure.persistence.UsuarioRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings de interfaz de dominio → implementación de infraestructura para
 * los repositorios (Principio IV). Cada Módulo agrega su propio `@Binds`
 * acá a medida que implementa su `*RepositoryImpl` (ver tasks.md T025,
 * T039, T048, T060, T068).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUsuarioRepository(impl: UsuarioRepositoryImpl): UsuarioRepository

    @Binds
    @Singleton
    abstract fun bindPermisoRepository(impl: PermisoRepositoryImpl): PermisoRepository

    @Binds
    @Singleton
    abstract fun bindActividadRepository(impl: ActividadRepositoryImpl): ActividadRepository

    @Binds
    @Singleton
    abstract fun bindPuntoVerdeRepository(impl: PuntoVerdeRepositoryImpl): PuntoVerdeRepository

    @Binds
    @Singleton
    abstract fun bindRegistroVerificacionRepository(
        impl: RegistroVerificacionRepositoryImpl,
    ): RegistroVerificacionRepository
}
