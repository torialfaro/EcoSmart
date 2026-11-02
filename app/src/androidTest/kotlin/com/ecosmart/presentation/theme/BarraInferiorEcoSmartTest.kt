package com.ecosmart.presentation.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BarraInferiorEcoSmartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val descripciones = mapOf(
        PestanaInferior.INICIO to "Inicio",
        PestanaInferior.ACTIVIDADES to "Actividades",
        PestanaInferior.RECOMPENSAS to "Recompensas",
        PestanaInferior.EDUCACION to "Educación",
        PestanaInferior.PERFIL to "Perfil",
        PestanaInferior.PUNTOS_VERDES to "Puntos Verdes",
    )

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
                    onPerfil = {},
                    onPuntosVerdes = {},
                )
            }
        }

        // Solo iconos sin etiqueta de texto: se identifican por contentDescription (6 items).
        assertEquals(6, PestanaInferior.entries.size)
        descripciones.values.forEach { composeTestRule.onNodeWithContentDescription(it).assertExists() }
    }

    @Test
    fun barraInferior_marcaItemSeleccionadoCorrectamente() {
        val seleccionActual = mutableStateOf(PestanaInferior.INICIO)
        composeTestRule.setContent {
            EcoSmartTheme {
                BarraInferiorEcoSmart(
                    seleccion = seleccionActual.value,
                    onInicio = {},
                    onActividades = {},
                    onRecompensas = {},
                    onEducacion = {},
                    onPerfil = {},
                    onPuntosVerdes = {},
                )
            }
        }

        PestanaInferior.entries.forEach { pestana ->
            seleccionActual.value = pestana
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithContentDescription(descripciones.getValue(pestana)).assertIsSelected()
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
                    onPuntosVerdes = { clickeado = "PUNTOS_VERDES" },
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

        composeTestRule.onNodeWithContentDescription("Puntos Verdes").performClick()
        assertEquals("PUNTOS_VERDES", clickeado)

        composeTestRule.onNodeWithContentDescription("Inicio").performClick()
        assertEquals("INICIO", clickeado)
    }

    @Test
    fun barraInferior_cadaItemCumpleLaZonaTactilMinima() {
        composeTestRule.setContent {
            EcoSmartTheme {
                BarraInferiorEcoSmart(
                    seleccion = PestanaInferior.INICIO,
                    onInicio = {},
                    onActividades = {},
                    onRecompensas = {},
                    onEducacion = {},
                    onPerfil = {},
                    onPuntosVerdes = {},
                )
            }
        }

        descripciones.values.forEach {
            composeTestRule.onNodeWithContentDescription(it).assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
        }
    }
}
