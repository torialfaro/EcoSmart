package com.ecosmart.infrastructure.firebase

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Identificador estable de esta instalación (data-model.md §5, spec 002-firestore-datos-usuario),
 * usado para diferenciar dispositivos en `permisosDispositivo` (RF-D009) y en el arbitraje
 * de `caminataEnCurso`/`pasosHoy` entre dispositivos (RF-D007/RF-D008). `ANDROID_ID` es
 * estable por combinación de dispositivo/usuario/app-signing-key y no requiere permisos.
 */
@Singleton
class DispositivoIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val dispositivoId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "desconocido"
    }
}
