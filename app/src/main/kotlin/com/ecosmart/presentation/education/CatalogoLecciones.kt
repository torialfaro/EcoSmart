package com.ecosmart.presentation.education

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class CatalogoLecciones @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    private var cache: ResultadoCatalogo? = null

    suspend fun obtener(): ResultadoCatalogo = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) { leerAsset() }.also { cache = it }
    }

    suspend fun recargar(): ResultadoCatalogo = mutex.withLock {
        cache = null
        withContext(Dispatchers.IO) { leerAsset() }.also { cache = it }
    }

    private fun leerAsset(): ResultadoCatalogo = try {
        val json = context.assets.open(RUTA_ASSET).bufferedReader().use { it.readText() }
        CatalogoLeccionesParser.parsear(json)
    } catch (_: IOException) {
        ResultadoCatalogo.Error
    }

    private companion object {
        const val RUTA_ASSET = "educacion/lecciones.json"
    }
}
