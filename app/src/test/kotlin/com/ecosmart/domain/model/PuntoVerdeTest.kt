package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.Coordenada
import com.ecosmart.domain.valueobject.PuntoVerdeId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PuntoVerdeTest {

    // Obelisco (CABA) como origen de referencia.
    private val origen = Coordenada(latitud = -34.6037, longitud = -58.3816)

    private fun puntoEn(latitud: Double, longitud: Double) = PuntoVerde(
        id = PuntoVerdeId.nuevo(),
        nombre = "Punto de prueba",
        direccion = "",
        barrio = "",
        latitud = latitud,
        longitud = longitud,
        categoriasQueAcepta = emptySet(),
    )

    @Test
    fun `esta dentro del radio si la distancia es menor a 3 km`() {
        // ~1.1 km al norte del Obelisco.
        val puntoCercano = puntoEn(-34.594, -58.3816)

        assertTrue(puntoCercano.estaDentroDelRadio(origen))
    }

    @Test
    fun `no esta dentro del radio si la distancia supera los 3 km`() {
        // ~11 km al norte del Obelisco.
        val puntoLejano = puntoEn(-34.505, -58.3816)

        assertFalse(puntoLejano.estaDentroDelRadio(origen))
    }

    @Test
    fun `el mismo punto que el origen esta siempre dentro del radio`() {
        val mismoPunto = puntoEn(origen.latitud, origen.longitud)

        assertTrue(mismoPunto.estaDentroDelRadio(origen))
    }
}
