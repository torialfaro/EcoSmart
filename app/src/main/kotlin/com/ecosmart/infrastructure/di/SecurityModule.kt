package com.ecosmart.infrastructure.di

import android.content.Context
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provee el [MasterKey] de Android Keystore que usa `JweGestorClaves`
 * para proteger la clave de cifrado de contraseñas, separada de la base
 * de datos Room (RNF-007, research.md §4). `JweGestorClaves` y
 * `CifradorContrasena` se inyectan directamente vía `@Inject constructor`
 * (no son interfaces, no necesitan un `@Provides` propio); este módulo
 * solo expone la dependencia de Android Keystore que ambas consumen.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
}
