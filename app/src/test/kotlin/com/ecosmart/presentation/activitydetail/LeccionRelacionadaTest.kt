package com.ecosmart.presentation.activitydetail

import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.education.LeccionUi
import com.ecosmart.presentation.education.PasoLeccionUi
import com.ecosmart.presentation.education.ResultadoCatalogo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LeccionRelacionadaTest {

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
    fun `devuelve la primera leccion de la categoria segun el orden del catalogo`() {
        val catalogo = ResultadoCatalogo.Cargado(
            listOf(
                leccion("c1", CategoriaActividad.CAMINAR),
                leccion("r1", CategoriaActividad.RECICLAR),
                leccion("r2", CategoriaActividad.RECICLAR),
            ),
        )

        assertEquals("r1", leccionRelacionadaId(catalogo, CategoriaActividad.RECICLAR))
    }

    @Test
    fun `ignora las lecciones de otras categorias aunque esten primero`() {
        val catalogo = ResultadoCatalogo.Cargado(
            listOf(
                leccion("r1", CategoriaActividad.RECICLAR),
                leccion("u1", CategoriaActividad.REUTILIZAR),
            ),
        )

        assertEquals("u1", leccionRelacionadaId(catalogo, CategoriaActividad.REUTILIZAR))
    }

    @Test
    fun `devuelve null si la categoria no tiene lecciones`() {
        val catalogo = ResultadoCatalogo.Cargado(listOf(leccion("r1", CategoriaActividad.RECICLAR)))

        assertNull(leccionRelacionadaId(catalogo, CategoriaActividad.CAMINAR))
    }

    @Test
    fun `devuelve null si el catalogo esta vacio`() {
        assertNull(leccionRelacionadaId(ResultadoCatalogo.Vacio, CategoriaActividad.RECICLAR))
        assertNull(leccionRelacionadaId(ResultadoCatalogo.Cargado(emptyList()), CategoriaActividad.RECICLAR))
    }

    @Test
    fun `devuelve null si el catalogo no se pudo leer`() {
        assertNull(leccionRelacionadaId(ResultadoCatalogo.Error, CategoriaActividad.RECICLAR))
    }
}
