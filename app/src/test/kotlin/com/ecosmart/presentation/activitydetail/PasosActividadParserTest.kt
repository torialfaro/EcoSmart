package com.ecosmart.presentation.activitydetail

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasosActividadParserTest {

    @Test
    fun `quita el prefijo numerico con punto de cada paso`() {
        val texto = "1. Juntá botellas limpias\n2. Llevalas al contenedor\n3. Sacá la foto"

        assertEquals(
            listOf("Juntá botellas limpias", "Llevalas al contenedor", "Sacá la foto"),
            PasosActividadParser.parsear(texto),
        )
    }

    @Test
    fun `quita el prefijo numerico con parentesis`() {
        assertEquals(listOf("Primero", "Segundo"), PasosActividadParser.parsear("1) Primero\n2) Segundo"))
    }

    @Test
    fun `descarta lineas vacias y espacios sobrantes`() {
        val texto = "\n  1.   Primero  \n\n   \n2. Segundo\n"

        assertEquals(listOf("Primero", "Segundo"), PasosActividadParser.parsear(texto))
    }

    @Test
    fun `un texto sin numeracion devuelve una linea por paso`() {
        assertEquals(listOf("Hacé esto", "Luego aquello"), PasosActividadParser.parsear("Hacé esto\nLuego aquello"))
    }

    @Test
    fun `no fuerza tres pasos con menos o mas lineas`() {
        assertEquals(1, PasosActividadParser.parsear("1. Único paso").size)
        assertEquals(5, PasosActividadParser.parsear("1. a\n2. b\n3. c\n4. d\n5. e").size)
    }

    @Test
    fun `un numero dentro del texto del paso no se recorta`() {
        assertEquals(listOf("Llevá 2 envases"), PasosActividadParser.parsear("1. Llevá 2 envases"))
    }

    @Test
    fun `un texto vacio o solo espacios devuelve lista vacia`() {
        assertTrue(PasosActividadParser.parsear("").isEmpty())
        assertTrue(PasosActividadParser.parsear("  \n \n").isEmpty())
    }
}
