package com.ecosmart.presentation.theme

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

        composeTestRule.onNodeWithText("Inicio").assertExists()
        composeTestRule.onNodeWithText("Actividades").assertExists()
        composeTestRule.onNodeWithText("Recompensas").assertExists()
        composeTestRule.onNodeWithText("Educación").assertExists()
        composeTestRule.onNodeWithText("Perfil").assertExists()
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
                        onPerfil = {},
                    )
                }
            }

            val textoEsperado = when (pestana) {
                PestanaInferior.INICIO -> "Inicio"
                PestanaInferior.ACTIVIDADES -> "Actividades"
                PestanaInferior.RECOMPENSAS -> "Recompensas"
                PestanaInferior.EDUCACION -> "Educación"
                PestanaInferior.PERFIL -> "Perfil"
            }

            composeTestRule.onNodeWithText(textoEsperado).assertIsSelected()
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

        composeTestRule.onNodeWithText("Actividades").performClick()
        assertEquals("ACTIVIDADES", clickeado)

        composeTestRule.onNodeWithText("Recompensas").performClick()
        assertEquals("RECOMPENSAS", clickeado)

        composeTestRule.onNodeWithText("Educación").performClick()
        assertEquals("EDUCACION", clickeado)

        composeTestRule.onNodeWithText("Perfil").performClick()
        assertEquals("PERFIL", clickeado)

        composeTestRule.onNodeWithText("Inicio").performClick()
        assertEquals("INICIO", clickeado)
    }
}
