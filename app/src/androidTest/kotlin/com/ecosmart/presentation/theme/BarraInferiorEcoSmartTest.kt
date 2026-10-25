package com.ecosmart.presentation.theme

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BarraInferiorEcoSmartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun barraInferior_exponeLosCincoItems() {
        composeTestRule.setContent {
            EcoSmartTheme {
                BarraInferiorEcoSmart(
                    seleccion = PestanaInferior.INICIO,
                    onInicio = {},
                    onActividades = {},
                    onRecompensas = {},
                    onEducacion = {},
                    onPerfil = {},
                )
            }
        }

        // Solo iconos sin etiqueta de texto: se identifican por contentDescription (5 items).
        composeTestRule.onNodeWithContentDescription("Inicio").assertExists()
        composeTestRule.onNodeWithContentDescription("Actividades").assertExists()
        composeTestRule.onNodeWithContentDescription("Recompensas").assertExists()
        composeTestRule.onNodeWithContentDescription("Educación").assertExists()
        composeTestRule.onNodeWithContentDescription("Perfil").assertExists()
        composeTestRule.onNodeWithContentDescription("Puntos Verdes").assertDoesNotExist()
    }

    @Test
    fun barraInferior_marcaItemSeleccionadoCorrectamente() {
        val seleccionActual = androidx.compose.runtime.mutableStateOf(PestanaInferior.INICIO)
        composeTestRule.setContent {
            EcoSmartTheme {
                BarraInferiorEcoSmart(
                    seleccion = seleccionActual.value,
                    onInicio = {},
                    onActividades = {},
                    onRecompensas = {},
                    onEducacion = {},
                    onPerfil = {},
                )
            }
        }

        PestanaInferior.entries.forEach { pestana ->
            seleccionActual.value = pestana
            composeTestRule.waitForIdle()

            val descripcionEsperada = when (pestana) {
                PestanaInferior.INICIO -> "Inicio"
                PestanaInferior.ACTIVIDADES -> "Actividades"
                PestanaInferior.RECOMPENSAS -> "Recompensas"
                PestanaInferior.EDUCACION -> "Educación"
                PestanaInferior.PERFIL -> "Perfil"
            }

            composeTestRule.onNodeWithContentDescription(descripcionEsperada).assertIsSelected()
        }
    }

    @Test
    fun barraInferior_disparaCallbacksAlHacerClick() {
        var clickeado = ""
        composeTestRule.setContent {
            EcoSmartTheme {
                BarraInferiorEcoSmart(
                    seleccion = PestanaInferior.INICIO,
                    onInicio = { clickeado = "INICIO" },
                    onActividades = { clickeado = "ACTIVIDADES" },
                    onRecompensas = { clickeado = "RECOMPENSAS" },
                    onEducacion = { clickeado = "EDUCACION" },
                    onPerfil = { clickeado = "PERFIL" },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Actividades").performClick()
        assertEquals("ACTIVIDADES", clickeado)

        composeTestRule.onNodeWithContentDescription("Recompensas").performClick()
        assertEquals("RECOMPENSAS", clickeado)

        composeTestRule.onNodeWithContentDescription("Educación").performClick()
        assertEquals("EDUCACION", clickeado)

        composeTestRule.onNodeWithContentDescription("Perfil").performClick()
        assertEquals("PERFIL", clickeado)

        composeTestRule.onNodeWithContentDescription("Inicio").performClick()
        assertEquals("INICIO", clickeado)
    }
}
