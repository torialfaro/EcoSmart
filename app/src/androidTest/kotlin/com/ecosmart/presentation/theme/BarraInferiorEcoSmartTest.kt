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
    fun barraInferior_exponeLosSeisItems() {
        composeTestRule.setContent {
            EcoSmartTheme {
                BarraInferiorEcoSmart(
                    seleccion = PestanaInferior.INICIO,
                    onInicio = {},
                    onActividades = {},
                    onRecompensas = {},
                    onEducacion = {},
                    onPuntosVerdes = {},
                    onPerfil = {},
                )
            }
        }

        // Solo iconos sin etiqueta de texto (feedback post-QA); se identifican por contentDescription.
        composeTestRule.onNodeWithContentDescription("Inicio").assertExists()
        composeTestRule.onNodeWithContentDescription("Actividades").assertExists()
        composeTestRule.onNodeWithContentDescription("Recompensas").assertExists()
        composeTestRule.onNodeWithContentDescription("Educación").assertExists()
        composeTestRule.onNodeWithContentDescription("Puntos Verdes").assertExists()
        composeTestRule.onNodeWithContentDescription("Perfil").assertExists()
    }

    @Test
    fun barraInferior_marcaItemSeleccionadoCorrectamente() {
        PestanaInferior.entries.forEach { pestana ->
            composeTestRule.setContent {
                EcoSmartTheme {
                    BarraInferiorEcoSmart(
                        seleccion = pestana,
                        onInicio = {},
                        onActividades = {},
                        onRecompensas = {},
                        onEducacion = {},
                        onPuntosVerdes = {},
                        onPerfil = {},
                    )
                }
            }

            val descripcionEsperada = when (pestana) {
                PestanaInferior.INICIO -> "Inicio"
                PestanaInferior.ACTIVIDADES -> "Actividades"
                PestanaInferior.RECOMPENSAS -> "Recompensas"
                PestanaInferior.EDUCACION -> "Educación"
                PestanaInferior.PUNTOS_VERDES -> "Puntos Verdes"
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
                    onPuntosVerdes = { clickeado = "PUNTOS_VERDES" },
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

        composeTestRule.onNodeWithContentDescription("Puntos Verdes").performClick()
        assertEquals("PUNTOS_VERDES", clickeado)

        composeTestRule.onNodeWithContentDescription("Perfil").performClick()
        assertEquals("PERFIL", clickeado)

        composeTestRule.onNodeWithContentDescription("Inicio").performClick()
        assertEquals("INICIO", clickeado)
    }
}
