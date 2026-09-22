package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RegistroVerificacionTest {

    private fun registroConHuella(huella: String?) = RegistroVerificacion(
        id = RegistroVerificacionId.nuevo(),
        usuarioId = UsuarioId.nuevo(),
        actividadId = ActividadId.nuevo(),
        categoria = CategoriaActividad.RECICLAR,
        fecha = LocalDate.now(),
        resultado = ResultadoVerificacion.APROBADO,
        huellaImagen = huella,
    )

    @Test
    fun `es duplicado si la distancia de Hamming esta dentro del umbral`() {
        // "ff00" vs "ff01": difieren en 1 bit.
        val nuevo = registroConHuella("ff00")
        val previo = registroConHuella("ff01")

        assertTrue(nuevo.esDuplicadoDe(previo))
    }

    @Test
    fun `no es duplicado si la distancia de Hamming supera el umbral`() {
        // "0000" vs "ffff": difieren en los 16 bits.
        val nuevo = registroConHuella("0000")
        val previo = registroConHuella("ffff")

        assertFalse(nuevo.esDuplicadoDe(previo))
    }

    @Test
    fun `nunca es duplicado si a alguno le falta la huella`() {
        val sinFoto = registroConHuella(null)
        val conFoto = registroConHuella("ff00")

        assertFalse(sinFoto.esDuplicadoDe(conFoto))
    }

    @Test
    fun `el mismo hash exacto siempre es duplicado`() {
        val a = registroConHuella("a1b2c3d4")
        val b = registroConHuella("a1b2c3d4")

        assertTrue(a.esDuplicadoDe(b))
    }
}
