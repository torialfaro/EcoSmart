package com.ecosmart.infrastructure.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adjunta `Authorization: Bearer <idToken>` a cada request hacia el backend de
 * confianza (research.md §3, spec 002-firestore-datos-usuario) — el backend extrae el
 * `uid` verificado de ese token, nunca de un campo enviado en el body (RF-D014).
 */
class FirebaseIdTokenInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val usuarioActual = firebaseAuth.currentUser
            ?: return chain.proceed(chain.request())

        // getIdToken() es async (Task); se bloquea acá porque OkHttp ya ejecuta los
        // interceptors en un hilo de red dedicado, nunca en el hilo principal.
        val idToken = Tasks.await(usuarioActual.getIdToken(false)).token

        val requestConToken = chain.request().newBuilder()
            .apply { idToken?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        return chain.proceed(requestConToken)
    }
}
