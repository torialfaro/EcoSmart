package com.ecosmart.infrastructure.security

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val ANCHO_REDUCIDO = 9
private const val ALTO_REDUCIDO = 8

/**
 * Hash perceptual dHash de 64 bits (research.md §3): reduce la imagen a
 * 9x8 en escala de grises y codifica 1 bit por cada par de píxeles
 * horizontales adyacentes (izquierda > derecha). Tolera variaciones
 * triviales de compresión/recorte, a diferencia de un hash exacto
 * (SHA-256), sin necesitar red — es el pre-filtro local antes de EcoGPT
 * (RF-058/RF-059).
 *
 * Toda decodificación de `Bitmap` queda encapsulada acá (no en
 * `VerificarFotoConIA`, capa de aplicación) para que esa capa siga siendo
 * testeable con JUnit5 puro, sin Android (Principio VIII / constitution.md
 * § stack de testing).
 */
@Singleton
class CalculadorHuellaPerceptual @Inject constructor() {

    /** Devuelve `null` si el archivo no se pudo decodificar como imagen (Edge Case "Imagen corrupta"). */
    fun calcularDesdeArchivo(archivo: File): String? {
        val bitmap = BitmapFactory.decodeFile(archivo.absolutePath) ?: return null
        return calcular(bitmap)
    }

    fun calcular(bitmap: Bitmap): String {
        val reducido = Bitmap.createScaledBitmap(bitmap, ANCHO_REDUCIDO, ALTO_REDUCIDO, true)
        var hash = 0UL
        var bit = 0
        for (fila in 0 until ALTO_REDUCIDO) {
            for (columna in 0 until ANCHO_REDUCIDO - 1) {
                val grisIzquierda = luminancia(reducido.getPixel(columna, fila))
                val grisDerecha = luminancia(reducido.getPixel(columna + 1, fila))
                if (grisIzquierda > grisDerecha) {
                    hash = hash or (1UL shl bit)
                }
                bit++
            }
        }
        return hash.toString(radix = 16)
    }

    private fun luminancia(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (r + g + b) / 3
    }
}
