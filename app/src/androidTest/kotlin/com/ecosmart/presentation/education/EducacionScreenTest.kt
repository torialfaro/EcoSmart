package com.ecosmart.presentation.education

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EducacionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun leccion(id: String, titulo: String, categoria: CategoriaActividad) = LeccionUi(
        id = id,
        titulo = titulo,
        categoria = categoria,
        duracionMinutos = 4,
        resumen = "Resumen de $titulo",
        introduccion = "Introducción",
        ilustracionPortada = null,
        descripcionPortada = "Portada",
        pasos = List(5) { PasoLeccionUi("Paso ${it + 1}", "Texto", null, "") },
        tip = null,
        textoCierre = null,
    )

    private fun mostrar(
        estado: EstadoEducacion,
        nombreUsuario: String = "Mica",
        onSeleccionarLeccion: (String) -> Unit = {},
        onReintentar: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EcoSmartTheme {
                EducacionContenido(
                    estado = estado,
                    nombreUsuario = nombreUsuario,
                    onSeleccionarLeccion = onSeleccionarLeccion,
                    onReintentar = onReintentar,
                    onVolver = {},
                )
            }
        }
    }

    private val catalogo = EstadoEducacion.Cargado(
        listOf(leccion("circuito", "El circuito del reciclaje en CABA", CategoriaActividad.RECICLAR)),
    )

    @Test
    fun educacion_noMuestraSegmentoNiVideosDestacados() {
        mostrar(catalogo)

        composeTestRule.onNodeWithText("Videos").assertDoesNotExist()
        composeTestRule.onNodeWithText("Artículos").assertDoesNotExist()
        composeTestRule.onNodeWithText("IA").assertDoesNotExist()
        composeTestRule.onNodeWithText("Videos destacados").assertDoesNotExist()
        composeTestRule.onNodeWithText("Ver todos").assertDoesNotExist()
    }

    @Test
    fun educacion_muestraSeccionReciclarConLaLeccion() {
        mostrar(catalogo)

        composeTestRule.onAllNodesWithText("Reciclar").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("El circuito del reciclaje en CABA").assertIsDisplayed()
    }

    @Test
    fun educacion_omiteCategoriasSinLecciones() {
        mostrar(catalogo)

        composeTestRule.onNodeWithText("Reutilizar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Caminar").assertDoesNotExist()
    }

    @Test
    fun educacion_estadoVacio_muestraMensaje() {
        mostrar(EstadoEducacion.Vacio)

        composeTestRule.onNodeWithText("Todavía no hay lecciones disponibles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertDoesNotExist()
    }

    @Test
    fun educacion_estadoError_muestraMensajeYReintentar() {
        var reintentos = 0
        mostrar(EstadoEducacion.Error, onReintentar = { reintentos++ })

        composeTestRule.onNodeWithText("No pudimos cargar las lecciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()
        assertEquals(1, reintentos)
    }

    @Test
    fun educacion_avatarMuestraLaInicialDelUsuario() {
        mostrar(catalogo, nombreUsuario = "mica")

        composeTestRule.onNodeWithText("M").assertIsDisplayed()
    }

    @Test
    fun educacion_avatarSinNombreMuestraU() {
        mostrar(catalogo, nombreUsuario = "")

        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }

    @Test
    fun educacion_tarjetaEsUnicoElementoTocableYSeleccionaLaLeccion() {
        var seleccionada: String? = null
        mostrar(catalogo, onSeleccionarLeccion = { seleccionada = it })

        composeTestRule.onNodeWithText("El circuito del reciclaje en CABA").assertHasClickAction()
        composeTestRule.onAllNodesWithText("Ver lección", substring = true).assertCountEquals(1)
        composeTestRule.onNode(hasText("El circuito del reciclaje en CABA")).performClick()
        assertEquals("circuito", seleccionada)
    }
}
