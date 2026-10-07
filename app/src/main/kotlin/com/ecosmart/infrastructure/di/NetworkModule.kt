package com.ecosmart.infrastructure.di

import com.ecosmart.app.BuildConfig
import com.ecosmart.infrastructure.firebase.FirebaseIdTokenInterceptor
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.BackendConfianzaClientConReintento
import com.ecosmart.infrastructure.network.EcoGptClient
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EcoGptApiKey

/**
 * Cliente HTTP y Retrofit de EcoGPT (research.md §2). El timeout de 30s
 * exacto de RNF-008/SC-008 se fija acá en las 4 dimensiones de timeout de
 * OkHttp (call/connect/read/write), no solo en una, para que ninguna
 * etapa de la request pueda superar el plazo.
 *
 * La sincronización de Puntos Verdes (`PuntosVerdesSyncClient`, T058) usa
 * una URL base distinta (`PUNTOS_VERDES_BASE_URL`) y no tiene el mismo
 * requisito de timeout estricto; se cablea en su propio módulo separado
 * (`PuntosVerdesNetworkModule`), para no acoplar ambos clientes a una
 * única configuración de OkHttp.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val ECOGPT_TIMEOUT: Duration = Duration.ofSeconds(30)

    /**
     * Se expone como binding calificado (en vez de que `VerificarFotoConIA`,
     * T075, lea `BuildConfig` directamente) para que el caso de uso reciba
     * la API key por constructor y sea trivial de testear con una clave
     * falsa (ver `VerificarFotoConIATest`).
     */
    @Provides
    @EcoGptApiKey
    fun provideEcoGptApiKey(): String = BuildConfig.ECOGPT_API_KEY

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideEcoGptOkHttpClient(firebaseIdTokenInterceptor: FirebaseIdTokenInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .callTimeout(ECOGPT_TIMEOUT)
            .connectTimeout(ECOGPT_TIMEOUT)
            .readTimeout(ECOGPT_TIMEOUT)
            .writeTimeout(ECOGPT_TIMEOUT)
            // T012/T014, spec 002-firestore-datos-usuario — agrega Authorization: Bearer
            // <idToken> para los endpoints nuevos del mismo backend (BackendConfianzaClient);
            // no afecta a EcoGptClient, que sigue autenticando con X-EcoGPT-Api-Key.
            .addInterceptor(firebaseIdTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideEcoGptRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.ECOGPT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideEcoGptClient(retrofit: Retrofit): EcoGptClient =
        retrofit.create(EcoGptClient::class.java)

    /**
     * T014 — mismo backend/base URL que EcoGPT (research.md §1 de spec
     * 002-firestore-datos-usuario: se extiende el servicio ya existente, no uno nuevo),
     * por eso reutiliza el mismo [Retrofit] en vez de un módulo/qualifier separado como
     * [PuntosVerdesNetworkModule].
     */
    @Provides
    @Singleton
    fun provideBackendConfianzaClient(retrofit: Retrofit): BackendConfianzaClient =
        BackendConfianzaClientConReintento(retrofit.create(BackendConfianzaClient::class.java))
}
