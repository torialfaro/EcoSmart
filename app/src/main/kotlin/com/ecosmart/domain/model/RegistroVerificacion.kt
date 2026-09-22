package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate

private const val UMBRAL_HAMMING_POR_DEFECTO = 5

/**
 * Entidad de dominio que representa un intento de completar una actividad
 * (data-model.md §2.3, US8/US9/US10).
 */
data class RegistroVerificacion(
    val id: RegistroVerificacionId,
    val usuarioId: UsuarioId,
    val actividadId: ActividadId,
    val categoria: CategoriaActividad,
    val fecha: LocalDate,
    val resultado: ResultadoVerificacion,
    val motivoIA: String? = null,
    val puntosOtorgados: Int = 0,
    val huellaImagen: String? = null,
    val pasosRegistrados: Int? = null,
) {
    /** RF-050 — solo lo Aprobado descuenta el tope diario; Rechazado/Indeterminado no. */
    fun consumeTopeDiario(): Boolean = resultado == ResultadoVerificacion.APROBADO

    /**
     * RF-058/RF-059 — compara la huella perceptual (dHash de 64 bits,
     * research.md §3) con la de otro registro ya Aprobado, vía distancia
     * de Hamming. Si a cualquiera de los dos le falta la huella (p. ej.
     * una actividad de Caminar, sin foto), nunca son duplicados.
     */
    fun esDuplicadoDe(otro: RegistroVerificacion, umbralHamming: Int = UMBRAL_HAMMING_POR_DEFECTO): Boolean {
        val huellaPropia = huellaImagen ?: return false
        val huellaOtra = otro.huellaImagen ?: return false
        return distanciaHamming(huellaPropia, huellaOtra) <= umbralHamming
    }

    private fun distanciaHamming(hashHexA: String, hashHexB: String): Int {
        val a = hashHexA.toULong(radix = 16)
        val b = hashHexB.toULong(radix = 16)
        return (a xor b).countOneBits()
    }
}
