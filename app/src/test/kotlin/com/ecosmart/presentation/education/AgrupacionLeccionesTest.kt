package com.ecosmart.presentation.education

import com.ecosmart.domain.valueobject.CategoriaActividad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AgrupacionLeccionesTest {

    private fun leccion(id: String, categoria: CategoriaActividad) = LeccionUi(
        id = id,
        titulo = "Lección $id",
        categoria = categoria,
        duracionMinutos = 3,
        resumen = "Resumen",
        introduccion = "Introducción",
        ilustracionPortada = null,
        descripcionPortada = "Portada",
        pasos = listOf(PasoLeccionUi(titulo = "Paso", texto = "Texto", ilustracion = null, descripcionIlustracion = "")),
        tip = null,
        textoCierre = null,
    )

    @Test
    fun `las secciones salen en orden fijo Reciclar, Reutilizar, Caminar sin importar el orden del catalogo`() {
        val catalogo = listOf(
            leccion("c1", CategoriaActividad.CAMINAR),
            leccion("r1", CategoriaActividad.RECICLAR),
            leccion("u1", CategoriaActividad.REUTILIZAR),
        )

        val secciones = agruparPorCategoria(catalogo)

        assertEquals(
            listOf(CategoriaActividad.RECICLAR, CategoriaActividad.REUTILIZAR, CategoriaActividad.CAMINAR),
            secciones.map { it.categoria },
        )
    }

    @Test
    fun `dentro de cada seccion se respeta el orden del catalogo`() {
        val catalogo = listOf(
            leccion("r2", CategoriaActividad.RECICLAR),
            leccion("u1", CategoriaActividad.REUTILIZAR),
            leccion("r1", CategoriaActividad.RECICLAR),
            leccion("r3", CategoriaActividad.RECICLAR),
        )

        val reciclar = agruparPorCategoria(catalogo).first { it.categoria == CategoriaActividad.RECICLAR }

        assertEquals(listOf("r2", "r1", "r3"), reciclar.lecciones.map { it.id })
    }

    @Test
    fun `las categorias sin lecciones no generan seccion`() {
        val catalogo = listOf(leccion("c1", CategoriaActividad.CAMINAR))

        val secciones = agruparPorCategoria(catalogo)

        assertEquals(listOf(CategoriaActividad.CAMINAR), secciones.map { it.categoria })
    }

    @Test
    fun `un catalogo vacio no genera secciones`() {
        assertTrue(agruparPorCategoria(emptyList()).isEmpty())
    }

    @Test
    fun `Inicio toma las 2 primeras lecciones en el orden del catalogo sin priorizar categorias`() {
        val catalogo = listOf(
            leccion("c1", CategoriaActividad.CAMINAR),
            leccion("r1", CategoriaActividad.RECICLAR),
            leccion("u1", CategoriaActividad.REUTILIZAR),
        )

        val inicio = leccionesParaInicio(catalogo)

        assertEquals(listOf("c1", "r1"), inicio.map { it.id })
    }

    @Test
    fun `Inicio devuelve menos de 2 si el catalogo tiene menos lecciones`() {
        assertEquals(listOf("r1"), leccionesParaInicio(listOf(leccion("r1", CategoriaActividad.RECICLAR))).map { it.id })
        assertTrue(leccionesParaInicio(emptyList()).isEmpty())
    }
}
