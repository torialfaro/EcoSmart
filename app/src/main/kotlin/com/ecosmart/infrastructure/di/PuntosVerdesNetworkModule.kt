package com.ecosmart.infrastructure.di

import com.ecosmart.app.BuildConfig
import com.ecosmart.infrastructure.network.PuntosVerdesSyncClient
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PuntosVerdesHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PuntosVerdesRetrofit

/**
 * Cliente HTTP y Retrofit de la sincronización de Puntos Verdes (RF-052,
 * research.md §5). Usa su propia URL base (`PUNTOS_VERDES_BASE_URL`) y sus
 * propios timeouts (los de OkHttp por defecto), distintos de los 30s
 * estrictos de EcoGPT (RNF-008) que configura [NetworkModule] — de ahí que
 * viva en un módulo separado con calificadores propios (`@PuntosVerdesHttpClient`/
 * `@PuntosVerdesRetrofit`), para no chocar con los bindings sin calificar
 * de `NetworkModule`. El [Moshi] sin calificar se reutiliza tal cual, ya
 * que no depende de timeout ni de URL base.
 */
@Module
@InstallIn(SingletonComponent::class)
object PuntosVerdesNetworkModule {

    @Provides
    @Singleton
    @PuntosVerdesHttpClient
    fun providePuntosVerdesOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    @PuntosVerdesRetrofit
    fun providePuntosVerdesRetrofit(
        @PuntosVerdesHttpClient okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.PUNTOS_VERDES_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun providePuntosVerdesSyncClient(@PuntosVerdesRetrofit retrofit: Retrofit): PuntosVerdesSyncClient =
        retrofit.create(PuntosVerdesSyncClient::class.java)
}
