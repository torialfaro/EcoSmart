package com.ecosmart.infrastructure.session

import android.content.Context
import androidx.core.content.edit
import com.ecosmart.domain.valueobject.UsuarioId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCIAS_SESION = "ecosmart_sesion"
private const val CLAVE_USUARIO_ID = "usuario_id"

/**
 * Mantiene el ID del usuario autenticado, persistido en SharedPreferences
 * (constitution.md § Stack Tecnológico: "Persistencia Local: Room /
 * SharedPreferences") para que la sesión sobreviva a reinicios de la app —
 * corrección post-QA: antes era solo en memoria y se perdía cada vez que
 * el proceso moría, obligando a un login en cada apertura pese a que
 * RF-010 pide "autenticado hasta que cierre sesión explícitamente", no
 * hasta que el proceso muera.
 */
@Singleton
class SesionUsuario @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferencias = context.getSharedPreferences(PREFERENCIAS_SESION, Context.MODE_PRIVATE)

    private val _usuarioActualId = MutableStateFlow(cargarUsuarioIdGuardado())
    val usuarioActualId: StateFlow<UsuarioId?> = _usuarioActualId.asStateFlow()

    fun iniciarSesion(usuarioId: UsuarioId) {
        _usuarioActualId.value = usuarioId
        preferencias.edit { putString(CLAVE_USUARIO_ID, usuarioId.valor) }
    }

    fun cerrarSesion() {
        _usuarioActualId.value = null
        preferencias.edit { remove(CLAVE_USUARIO_ID) }
    }

    private fun cargarUsuarioIdGuardado(): UsuarioId? =
        preferencias.getString(CLAVE_USUARIO_ID, null)?.let(::UsuarioId)
}
