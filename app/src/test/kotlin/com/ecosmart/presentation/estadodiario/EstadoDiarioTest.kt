package com.ecosmart.presentation.estadodiario

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EstadoDiarioTest {

    private val hoy = LocalDate.of(2026, 11, 1)
    private val usuario = UsuarioId("usuario-prueba")

    private fun actividad(categoria: CategoriaActividad) = Actividad(
        id = ActividadId.nuevo(),
        categoria = categoria,
        descripcionCorta = "Actividad de ${categoria.name}",
        pasosASeguir = "1. Primero\n2. Segundo\n3. Tercero",
        resultadoEsperado = "Se ve el resultado",
        fotoReferencialUrl = "",
        puntosBase = 50,
    )

    private fun registro(
        actividadId: ActividadId,
        categoria: CategoriaActividad,
        resultado: ResultadoVerificacion = ResultadoVerificacion.APROBADO,
        fecha: LocalDate = hoy,
    ) = RegistroVerificacion(
        id = RegistroVerificacionId.nuevo(),
        usuarioId = usuario,
        actividadId = actividadId,
        categoria = categoria,
        fecha = fecha,
        resultado = resultado,
    )

    private fun EstadoDiarioUi.categoria(categoria: CategoriaActividad) = categorias.first { it.categoria == categoria }

    @Test
    fun `sin registros todas las actividades estan para hacer y ninguna categoria esta hecha`() {
        val actividades = CategoriaActividad.entries.map { actividad(it) }

        val estado = calcularEstadoDiario(actividades, emptyList(), hoy)

        assertTrue(actividades.all { estado.estadoPorActividad[it.id] == EstadoActividadUi.PARA_HACER })
        assertEquals(0, estado.hechas)
        assertEquals(CategoriaActividad.entries.size, estado.total)
        assertTrue(estado.categorias.none { it.hecha || it.topeAlcanzado })
    }

    @Test
    fun `una aprobada de Reciclar deja la categoria hecha, la completada y bloquea las demas de Reciclar`() {
        val reciclarA = actividad(CategoriaActividad.RECICLAR)
        val reciclarB = actividad(CategoriaActividad.RECICLAR)
        val reutilizar = actividad(CategoriaActividad.REUTILIZAR)
        val caminar = actividad(CategoriaActividad.CAMINAR)
        val registros = listOf(registro(reciclarA.id, CategoriaActividad.RECICLAR))

        val estado = calcularEstadoDiario(listOf(reciclarA, reciclarB, reutilizar, caminar), registros, hoy)

        assertTrue(estado.categoria(CategoriaActividad.RECICLAR).hecha)
        assertTrue(estado.categoria(CategoriaActividad.RECICLAR).topeAlcanzado)
        assertEquals(EstadoActividadUi.COMPLETADA_HOY, estado.estadoPorActividad[reciclarA.id])
        assertEquals(EstadoActividadUi.NO_DISPONIBLE_HOY, estado.estadoPorActividad[reciclarB.id])
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[reutilizar.id])
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[caminar.id])
        assertEquals(1, estado.hechas)
    }

    @Test
    fun `dos aprobadas de Reutilizar dejan la categoria hecha y las demas siguen disponibles porque el tope es 5`() {
        val actividades = List(4) { actividad(CategoriaActividad.REUTILIZAR) }
        val registros = actividades.take(2).map { registro(it.id, CategoriaActividad.REUTILIZAR) }

        val estado = calcularEstadoDiario(actividades, registros, hoy)

        assertTrue(estado.categoria(CategoriaActividad.REUTILIZAR).hecha)
        assertFalse(estado.categoria(CategoriaActividad.REUTILIZAR).topeAlcanzado)
        assertEquals(EstadoActividadUi.COMPLETADA_HOY, estado.estadoPorActividad[actividades[0].id])
        assertEquals(EstadoActividadUi.COMPLETADA_HOY, estado.estadoPorActividad[actividades[1].id])
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[actividades[2].id])
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[actividades[3].id])
    }

    @Test
    fun `cinco aprobadas de Reutilizar alcanzan el tope y la actividad restante queda no disponible hoy`() {
        val actividades = List(6) { actividad(CategoriaActividad.REUTILIZAR) }
        val registros = actividades.take(5).map { registro(it.id, CategoriaActividad.REUTILIZAR) }

        val estado = calcularEstadoDiario(actividades, registros, hoy)

        assertTrue(estado.categoria(CategoriaActividad.REUTILIZAR).topeAlcanzado)
        assertEquals(EstadoActividadUi.NO_DISPONIBLE_HOY, estado.estadoPorActividad[actividades[5].id])
    }

    @Test
    fun `Caminar no tiene tope por lo que nunca queda no disponible hoy`() {
        val caminarA = actividad(CategoriaActividad.CAMINAR)
        val caminarB = actividad(CategoriaActividad.CAMINAR)
        val registros = List(3) { registro(caminarA.id, CategoriaActividad.CAMINAR) }

        val estado = calcularEstadoDiario(listOf(caminarA, caminarB), registros, hoy)

        assertTrue(estado.categoria(CategoriaActividad.CAMINAR).hecha)
        assertFalse(estado.categoria(CategoriaActividad.CAMINAR).topeAlcanzado)
        assertEquals(EstadoActividadUi.COMPLETADA_HOY, estado.estadoPorActividad[caminarA.id])
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[caminarB.id])
    }

    @Test
    fun `los registros rechazados e indeterminados no cuentan`() {
        val reciclar = actividad(CategoriaActividad.RECICLAR)
        val registros = listOf(
            registro(reciclar.id, CategoriaActividad.RECICLAR, ResultadoVerificacion.RECHAZADO),
            registro(reciclar.id, CategoriaActividad.RECICLAR, ResultadoVerificacion.INDETERMINADO),
        )

        val estado = calcularEstadoDiario(listOf(reciclar), registros, hoy)

        assertFalse(estado.categoria(CategoriaActividad.RECICLAR).hecha)
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[reciclar.id])
    }

    @Test
    fun `los registros de otro dia no cuentan`() {
        val reciclar = actividad(CategoriaActividad.RECICLAR)
        val registros = listOf(registro(reciclar.id, CategoriaActividad.RECICLAR, fecha = hoy.minusDays(1)))

        val estado = calcularEstadoDiario(listOf(reciclar), registros, hoy)

        assertFalse(estado.categoria(CategoriaActividad.RECICLAR).hecha)
        assertEquals(EstadoActividadUi.PARA_HACER, estado.estadoPorActividad[reciclar.id])
    }

    @Test
    fun `un registro con actividad inexistente cuenta para la categoria pero no marca ninguna actividad`() {
        val reciclarA = actividad(CategoriaActividad.RECICLAR)
        val reciclarB = actividad(CategoriaActividad.RECICLAR)
        val registros = listOf(registro(ActividadId.nuevo(), CategoriaActividad.RECICLAR))

        val estado = calcularEstadoDiario(listOf(reciclarA, reciclarB), registros, hoy)

        assertTrue(estado.categoria(CategoriaActividad.RECICLAR).hecha)
        assertTrue(estado.categoria(CategoriaActividad.RECICLAR).topeAlcanzado)
        assertEquals(EstadoActividadUi.NO_DISPONIBLE_HOY, estado.estadoPorActividad[reciclarA.id])
        assertEquals(EstadoActividadUi.NO_DISPONIBLE_HOY, estado.estadoPorActividad[reciclarB.id])
    }

    @Test
    fun `el total y el orden salen de las categorias del dominio aunque el catalogo este vacio`() {
        val estado = calcularEstadoDiario(emptyList(), emptyList(), hoy)

        assertEquals(CategoriaActividad.entries.size, estado.total)
        assertEquals(
            listOf(CategoriaActividad.RECICLAR, CategoriaActividad.REUTILIZAR, CategoriaActividad.CAMINAR),
            estado.categorias.map { it.categoria },
        )
    }
}
