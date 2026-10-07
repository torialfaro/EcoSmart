package com.ecosmart.infrastructure.firebase

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provee la instancia única de [FirebaseFirestore] (spec 002-firestore-datos-usuario):
 * fuente de verdad remota y autoritativa de los datos de usuario (RF-D001/RF-D006,
 * constitution.md v2.0.0), reemplazando a Room para decisiones de negocio.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
