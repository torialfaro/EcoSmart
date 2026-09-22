package com.ecosmart.infrastructure.security

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jose.crypto.DirectEncrypter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cifra y descifra contraseñas en formato JWE compacto (RNF-006), usando
 * la clave JWK gestionada por [JweGestorClaves] (Android Keystore,
 * RNF-007). El dominio nunca ve la contraseña en claro ni la clave: solo
 * conoce `ContrasenaCifrada` (el JWE serializado), ver data-model.md §3.
 */
@Singleton
class CifradorContrasena @Inject constructor(
    private val gestorClaves: JweGestorClaves,
) {

    /** Devuelve el JWE compacto a persistir en `UsuarioRoomEntity.contrasenaCifradaJwe`. */
    fun cifrar(contrasenaPlana: String): String {
        val clave = gestorClaves.obtenerOCrearClave()
        val jweObject = JWEObject(JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM), Payload(contrasenaPlana))
        jweObject.encrypt(DirectEncrypter(clave.toSecretKey(ALGORITMO_CLAVE)))
        return jweObject.serialize()
    }

    private fun descifrar(jweCompacto: String): String {
        val clave = gestorClaves.obtenerOCrearClave()
        val jweObject = JWEObject.parse(jweCompacto)
        jweObject.decrypt(DirectDecrypter(clave.toSecretKey(ALGORITMO_CLAVE)))
        return requireNotNull(jweObject.payload.toString())
    }

    /**
     * Compara una contraseña ingresada por el usuario (login, RF-001) o el
     * flujo de "contraseña actual" (RF-008/cambio de contraseña) contra la
     * cifrada guardada, sin exponer el valor descifrado fuera de esta
     * clase.
     */
    fun coincide(contrasenaIngresada: String, jweGuardado: String): Boolean =
        descifrar(jweGuardado) == contrasenaIngresada

    private companion object {
        const val ALGORITMO_CLAVE = "AES"
    }
}
