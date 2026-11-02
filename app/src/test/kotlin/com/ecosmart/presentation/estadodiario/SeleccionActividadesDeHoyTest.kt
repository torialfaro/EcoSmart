package com.ecosmart.presentation.estadodiario

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SeleccionActividadesDeHoyTest {

    private fun actividad(categoria: CategoriaActividad) = Actividad(
        id = ActividadId.nuevo(),
        categoria = categoria,
        descripcionCorta = "Actividad de ${categoria.name}",
        pasosASeguir = "1. Primero",
        resultadoEsperado = "Se ve el resultado",
        fotoReferencialUrl = "",
        puntosBase = 50,
    )

    private fun estadoDiario(vararg estados: Pair<Actividad, EstadoActividadUi>) = EstadoDiarioUi(
        categorias = listOf(
            CategoriaActividad.RECICLAR,
            CategoriaActividad.REUTILIZAR,
            CategoriaActividad.CAMINAR,
        ).map { EstadoCategoriaUi(categoria = it, hecha = false, topeAlcanzado = false) },
        estadoPorActividad = estados.associate { (actividad, estado) -> actividad.id to estado },
    )

    @Test
    fun `devuelve una tarjeta por categoria en el orden Reciclar, Reutilizar, Caminar`() {
        val caminar = actividad(CategoriaActividad.CAMINAR)
        val reciclar = actividad(CategoriaActividad.RECICLAR)
        val reutilizar = actividad(CategoriaActividad.REUTILIZAR)
        val reciclarExtra = actividad(CategoriaActividad.RECICLAR)
        val catalogo = listOf(caminar, reciclar, reutilizar, reciclarExtra)
        val estado = estadoDiario(*catalogo.map { it to EstadoActividadUi.PARA_HACER }.toTypedArray())

        val tarjetas = seleccionarActividadesDeHoy(catalogo, estado)

        assertEquals(listOf(reciclar, reutilizar, caminar), tarjetas.map { it.actividad })
    }

    @Test
    fun `elige la primera actividad para hacer de la categoria y salta las completadas`() {
        val completada = actividad(CategoriaActividad.REUTILIZAR)
        val paraHacer = actividad(CategoriaActividad.REUTILIZAR)
        val otraParaHacer = actividad(CategoriaActividad.REUTILIZAR)
        val estado = estadoDiario(
            completada to EstadoActividadUi.COMPLETADA_HOY,
            paraHacer to EstadoActividadUi.PARA_HACER,
            otraParaHacer to EstadoActividadUi.PARA_HACER,
        )

        val tarjetas = seleccionarActividadesDeHoy(listOf(completada, paraHacer, otraParaHacer), estado)

        assertEquals(1, tarjetas.size)
        assertEquals(paraHacer, tarjetas.single().actividad)
        assertEquals(EstadoActividadUi.PARA_HACER, tarjetas.single().estado)
    }

    @Test
    fun `sin actividades para hacer elige la primera completada hoy`() {
        val noDisponible = actividad(CategoriaActividad.RECICLAR)
        val completada = actividad(CategoriaActividad.RECICLAR)
        val estado = estadoDiario(
            noDisponible to EstadoActividadUi.NO_DISPONIBLE_HOY,
            completada to EstadoActividadUi.COMPLETADA_HOY,
        )

        val tarjetas = seleccionarActividadesDeHoy(listOf(noDisponible, completada), estado)

        assertEquals(completada, tarjetas.single().actividad)
        assertEquals(EstadoActividadUi.COMPLETADA_HOY, tarjetas.single().estado)
    }

    @Test
    fun `sin para hacer ni completadas elige la primera del catalogo`() {
        val primera = actividad(CategoriaActividad.RECICLAR)
        val segunda = actividad(CategoriaActividad.RECICLAR)
        val estado = estadoDiario(
            primera to EstadoActividadUi.NO_DISPONIBLE_HOY,
            segunda to EstadoActividadUi.NO_DISPONIBLE_HOY,
        )

        val tarjetas = seleccionarActividadesDeHoy(listOf(primera, segunda), estado)

        assertEquals(primera, tarjetas.single().actividad)
        assertEquals(EstadoActividadUi.NO_DISPONIBLE_HOY, tarjetas.single().estado)
    }

    @Test
    fun `una categoria sin actividades en el catalogo no muestra tarjeta`() {
        val reciclar = actividad(CategoriaActividad.RECICLAR)
        val estado = estadoDiario(reciclar to EstadoActividadUi.PARA_HACER)

        val tarjetas = seleccionarActividadesDeHoy(listOf(reciclar), estado)

        assertEquals(listOf(CategoriaActividad.RECICLAR), tarjetas.map { it.actividad.categoria })
    }

    @Test
    fun `un catalogo vacio no produce tarjetas`() {
        val tarjetas = seleccionarActividadesDeHoy(emptyList(), estadoDiario())

        assertTrue(tarjetas.isEmpty())
    }
}
