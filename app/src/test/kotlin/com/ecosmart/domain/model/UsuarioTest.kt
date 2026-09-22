package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.Barrio
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.UsuarioId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UsuarioTest {

    private fun usuarioBase(
        puntosHistoricos: Int = 0,
        rachaActual: Int = 0,
        ultimaActividadAprobadaEn: LocalDate? = null,
    ) = Usuario(
        id = UsuarioId.nuevo(),
        email = "persona@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada("jwe"),
        nombre = "Ana",
        apellido = "Pérez",
        nombreUsuario = "anap",
        barrio = Barrio.PALERMO,
        telefono = "11-5555-5555",
        categoriasDeInteres = setOf(CategoriaActividad.RECICLAR),
        puntosHistoricos = puntosHistoricos,
        rachaActual = rachaActual,
        ultimaActividadAprobadaEn = ultimaActividadAprobadaEn,
    )

    @Test
    fun `sumarPuntos incrementa el puntaje historico`() {
        val usuario = usuarioBase(puntosHistoricos = 100)

        val actualizado = usuario.sumarPuntos(50)

        assertEquals(150, actualizado.puntosHistoricos)
    }

    @Test
    fun `sumarPuntos rechaza cantidades negativas`() {
        val usuario = usuarioBase()

        assertThrows(IllegalArgumentException::class.java) { usuario.sumarPuntos(-10) }
    }

    @Test
    fun `nivel se deriva del puntaje historico segun los umbrales`() {
        assertEquals(NivelUsuario.SEMILLA, usuarioBase(puntosHistoricos = 0).nivel())
        assertEquals(NivelUsuario.BROTE, usuarioBase(puntosHistoricos = 500).nivel())
        assertEquals(NivelUsuario.PLANTA, usuarioBase(puntosHistoricos = 1500).nivel())
        assertEquals(NivelUsuario.ARBOL, usuarioBase(puntosHistoricos = 3500).nivel())
    }

    @Test
    fun `registrarActividadAprobadaHoy incrementa la racha en dias consecutivos`() {
        val ayer = LocalDate.of(2026, 9, 20)
        val hoy = LocalDate.of(2026, 9, 21)
        val usuario = usuarioBase(rachaActual = 3, ultimaActividadAprobadaEn = ayer)

        val actualizado = usuario.registrarActividadAprobadaHoy(hoy)

        assertEquals(4, actualizado.rachaActual)
        assertEquals(hoy, actualizado.ultimaActividadAprobadaEn)
    }

    @Test
    fun `registrarActividadAprobadaHoy no duplica la racha si ya se registro hoy`() {
        val hoy = LocalDate.of(2026, 9, 21)
        val usuario = usuarioBase(rachaActual = 2, ultimaActividadAprobadaEn = hoy)

        val actualizado = usuario.registrarActividadAprobadaHoy(hoy)

        assertEquals(2, actualizado.rachaActual)
    }

    @Test
    fun `registrarActividadAprobadaHoy reinicia la racha tras un dia sin actividad`() {
        val haceTresDias = LocalDate.of(2026, 9, 18)
        val hoy = LocalDate.of(2026, 9, 21)
        val usuario = usuarioBase(rachaActual = 5, ultimaActividadAprobadaEn = haceTresDias)

        val actualizado = usuario.registrarActividadAprobadaHoy(hoy)

        assertEquals(1, actualizado.rachaActual)
    }

    @Test
    fun `rachaVigente muestra la racha rota si paso un dia calendario completo sin actividad`() {
        val hoy = LocalDate.of(2026, 9, 21)
        val haceTresDias = LocalDate.of(2026, 9, 18)
        val usuario = usuarioBase(rachaActual = 5, ultimaActividadAprobadaEn = haceTresDias)

        assertEquals(0, usuario.rachaVigente(hoy))
    }

    @Test
    fun `rachaVigente mantiene la racha si la ultima actividad fue ayer`() {
        val hoy = LocalDate.of(2026, 9, 21)
        val ayer = LocalDate.of(2026, 9, 20)
        val usuario = usuarioBase(rachaActual = 3, ultimaActividadAprobadaEn = ayer)

        assertEquals(3, usuario.rachaVigente(hoy))
    }

    @Test
    fun `rachaVigente es 0 si nunca hubo una actividad aprobada`() {
        val usuario = usuarioBase(rachaActual = 0, ultimaActividadAprobadaEn = null)

        assertEquals(0, usuario.rachaVigente(LocalDate.of(2026, 9, 21)))
    }
}
