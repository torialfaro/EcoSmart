package com.ecosmart.presentation.education

import com.ecosmart.domain.valueobject.CategoriaActividad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CatalogoLeccionesParserTest {

    private fun pasoJson(titulo: String = "Paso", texto: String = "Texto del paso", ilustracion: String? = "il_leccion_x") =
        buildString {
            append("""{"titulo": "$titulo", "texto": "$texto"""")
            if (ilustracion != null) append(""", "ilustracion": "$ilustracion", "descripcionIlustracion": "Dibujo del paso"""")
            append("}")
        }

    @Suppress("LongParameterList")
    private fun leccionJson(
        id: String = "leccion-1",
        titulo: String = "El circuito del reciclaje",
        categoria: String = "RECICLAR",
        duracion: Int = 4,
        pasos: List<String> = listOf(pasoJson()),
        extra: String = "",
    ) = """
        {
          "id": "$id",
          "titulo": "$titulo",
          "categoria": "$categoria",
          "duracionMinutos": $duracion,
          "resumen": "Resumen",
          "introduccion": "Introducción",
          "ilustracionPortada": "il_leccion_portada",
          "descripcionPortada": "Portada",
          "pasos": [${pasos.joinToString(",")}]
          $extra
        }
    """.trimIndent()

    private fun catalogoJson(vararg lecciones: String) = """{"lecciones": [${lecciones.joinToString(",")}]}"""

    private fun cargadas(resultado: ResultadoCatalogo): List<LeccionUi> {
        assertTrue(resultado is ResultadoCatalogo.Cargado, "Se esperaba Cargado pero fue $resultado")
        return (resultado as ResultadoCatalogo.Cargado).lecciones
    }

    @Test
    fun `un archivo valido produce Cargado con todos los campos mapeados`() {
        val json = catalogoJson(
            leccionJson(
                pasos = listOf(pasoJson("Separá en casa"), pasoJson("Llevalos al contenedor")),
                extra = """, "tip": {"titulo": "Tip para que cuente", "texto": "Enjuagalo antes."},
                    "textoCierre": "Llevá tus envases limpios a un Punto Verde"""",
            ),
        )

        val lecciones = cargadas(CatalogoLeccionesParser.parsear(json))

        assertEquals(1, lecciones.size)
        val leccion = lecciones.first()
        assertEquals("leccion-1", leccion.id)
        assertEquals(CategoriaActividad.RECICLAR, leccion.categoria)
        assertEquals(4, leccion.duracionMinutos)
        assertEquals("il_leccion_portada", leccion.ilustracionPortada)
        assertEquals("Portada", leccion.descripcionPortada)
        assertEquals(listOf("Separá en casa", "Llevalos al contenedor"), leccion.pasos.map { it.titulo })
        assertEquals("Dibujo del paso", leccion.pasos.first().descripcionIlustracion)
        assertEquals("Tip para que cuente", leccion.tip?.titulo)
        assertEquals("Llevá tus envases limpios a un Punto Verde", leccion.textoCierre)
    }

    @Test
    fun `un archivo valido sin lecciones produce Vacio`() {
        assertEquals(ResultadoCatalogo.Vacio, CatalogoLeccionesParser.parsear("""{"lecciones": []}"""))
    }

    @Test
    fun `un JSON truncado produce Error`() {
        val truncado = catalogoJson(leccionJson()).dropLast(15)

        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear(truncado))
    }

    @Test
    fun `un texto vacio o sin la clave lecciones produce Error`() {
        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear(""))
        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear("   "))
        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear("{}"))
        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear("""{"otraCosa": []}"""))
        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear("[]"))
    }

    @Test
    fun `una leccion invalida se descarta y las demas se conservan`() {
        val json = catalogoJson(
            leccionJson(id = "ok"),
            leccionJson(id = "sin-titulo", titulo = ""),
            leccionJson(id = "", titulo = "Sin id"),
            leccionJson(id = "categoria-desconocida", categoria = "VOLAR"),
            leccionJson(id = "sin-pasos", pasos = emptyList()),
            leccionJson(id = "duracion-cero", duracion = 0),
            leccionJson(id = "duracion-negativa", duracion = -3),
            leccionJson(id = "paso-sin-titulo", pasos = listOf(pasoJson(titulo = ""))),
        )

        val lecciones = cargadas(CatalogoLeccionesParser.parsear(json))

        assertEquals(listOf("ok"), lecciones.map { it.id })
    }

    @Test
    fun `si todas las lecciones son invalidas el resultado es Error`() {
        val json = catalogoJson(
            leccionJson(id = "a", titulo = ""),
            leccionJson(id = "b", categoria = "VOLAR"),
        )

        assertEquals(ResultadoCatalogo.Error, CatalogoLeccionesParser.parsear(json))
    }

    @Test
    fun `un id duplicado conserva la primera ocurrencia`() {
        val json = catalogoJson(
            leccionJson(id = "repetida", titulo = "Primera"),
            leccionJson(id = "repetida", titulo = "Segunda"),
            leccionJson(id = "otra", titulo = "Tercera"),
        )

        val lecciones = cargadas(CatalogoLeccionesParser.parsear(json))

        assertEquals(listOf("Primera", "Tercera"), lecciones.map { it.titulo })
    }

    @Test
    fun `la cantidad de pasos se deriva de la lista y se ignora el campo del archivo`() {
        val json = catalogoJson(
            leccionJson(
                pasos = listOf(pasoJson("Uno"), pasoJson("Dos"), pasoJson("Tres")),
                extra = ""","cantidadPasos": 99""",
            ),
        )

        val leccion = cargadas(CatalogoLeccionesParser.parsear(json)).first()

        assertEquals(3, leccion.cantidadPasos)
    }

    @Test
    fun `un texto de cierre ausente o vacio queda como null`() {
        val json = catalogoJson(
            leccionJson(id = "ausente"),
            leccionJson(id = "vacio", extra = """, "textoCierre": "   """"),
        )

        val lecciones = cargadas(CatalogoLeccionesParser.parsear(json))

        assertNull(lecciones.first { it.id == "ausente" }.textoCierre)
        assertNull(lecciones.first { it.id == "vacio" }.textoCierre)
    }

    @Test
    fun `un tip incompleto se ignora sin descartar la leccion`() {
        val json = catalogoJson(
            leccionJson(extra = """, "tip": {"titulo": "", "texto": "Sin título"}"""),
        )

        val leccion = cargadas(CatalogoLeccionesParser.parsear(json)).first()

        assertNull(leccion.tip)
    }

    @Test
    fun `una portada sin descripcion usa un texto por defecto derivado del titulo`() {
        val sinDescripcion = """
            {"id": "x", "titulo": "Mi lección", "categoria": "CAMINAR", "duracionMinutos": 2,
             "resumen": "r", "introduccion": "i", "pasos": [${pasoJson()}]}
        """.trimIndent()

        val leccion = cargadas(CatalogoLeccionesParser.parsear(catalogoJson(sinDescripcion))).first()

        assertTrue(leccion.descripcionPortada.contains("Mi lección"))
        assertNull(leccion.ilustracionPortada)
        assertNotNull(leccion.pasos.first())
    }
}
