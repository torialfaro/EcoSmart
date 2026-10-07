package com.ecosmart.infrastructure.firebase

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provee la instancia única de [FirebaseAuth] (spec 002-firestore-datos-usuario,
 * RF-D002/RF-D005). El estado de sesión que antes vivía en `SesionUsuario`
 * (SharedPreferences propias) pasa a delegarse en `FirebaseAuth.currentUser` (T022).
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseAuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
