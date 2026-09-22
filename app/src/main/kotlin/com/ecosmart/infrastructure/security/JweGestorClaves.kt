package com.ecosmart.infrastructure.security

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Genera y recupera la clave JWK simétrica (A256GCM) que cifra las
 * contraseñas de los usuarios (RNF-006). La clave se guarda en un archivo
 * separado del archivo de base de datos de Room, protegido por Android
 * Keystore vía Jetpack Security (`EncryptedFile` + [MasterKey]) — nunca en
 * una tabla de Room ni en `SharedPreferences` sin cifrar (RNF-007). Ver
 * research.md §4 y `[AMBIGÜEDAD-005]`/`[AMBIGÜEDAD-006]` en spec.md.
 */
@Singleton
class JweGestorClaves @Inject constructor(
    @ApplicationContext private val context: Context,
    private val masterKey: MasterKey,
) {

    private val archivoClave: File
        get() = File(context.filesDir, NOMBRE_ARCHIVO_CLAVE)

    /**
     * Devuelve la clave JWK existente o, la primera vez que se llama en
     * esta instalación, genera una nueva y la persiste.
     */
    fun obtenerOCrearClave(): OctetSequenceKey =
        if (archivoClave.exists()) leerClave() else generarYGuardarClave()

    private fun generarYGuardarClave(): OctetSequenceKey {
        val nuevaClave = OctetSequenceKeyGenerator(TAMANIO_CLAVE_BITS)
            .keyID(ID_CLAVE)
            .generate()
        escribirClave(nuevaClave)
        return nuevaClave
    }

    private fun leerClave(): OctetSequenceKey {
        val json = crearEncryptedFile().openFileInput().use { it.readBytes().decodeToString() }
        return OctetSequenceKey.parse(json)
    }

    private fun escribirClave(clave: OctetSequenceKey) {
        crearEncryptedFile().openFileOutput().use {
            it.write(clave.toJSONString().encodeToByteArray())
        }
    }

    private fun crearEncryptedFile(): EncryptedFile =
        EncryptedFile.Builder(
            context,
            archivoClave,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()

    private companion object {
        const val NOMBRE_ARCHIVO_CLAVE = "ecosmart_password_jwk.bin"
        const val ID_CLAVE = "ecosmart-password-key"
        const val TAMANIO_CLAVE_BITS = 256
    }
}
