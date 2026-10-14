package com.ecosmart.presentation.theme

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BotonPrincipalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun botonPrincipal_habilitado_disparaOnClick() {
        var clickeado = false
        composeTestRule.setContent {
            EcoSmartTheme {
                BotonPrincipal(
                    texto = "Ver todas las actividades",
                    onClick = { clickeado = true },
                    habilitado = true,
                )
            }
        }

        composeTestRule.onNodeWithText("Ver todas las actividades").assertIsEnabled()
        composeTestRule.onNodeWithText("Ver todas las actividades").performClick()
        assertTrue(clickeado)
    }

    @Test
    fun botonPrincipal_deshabilitado_noDisparaOnClick_yEstaDeshabilitado() {
        var clickeado = false
        composeTestRule.setContent {
            EcoSmartTheme {
                BotonPrincipal(
                    texto = "Guardar cambios",
                    onClick = { clickeado = true },
                    habilitado = false,
                )
            }
        }

        composeTestRule.onNodeWithText("Guardar cambios").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Guardar cambios").performClick()
        assertFalse(clickeado)
    }

    @Test
    fun botonPrincipal_conFlecha_muestraTextoConFlecha() {
        var clickeado = false
        composeTestRule.setContent {
            EcoSmartTheme {
                BotonPrincipal(
                    texto = "Ver todas las actividades",
                    onClick = { clickeado = true },
                    conFlecha = true,
                )
            }
        }

        composeTestRule.onNodeWithText("Ver todas las actividades →").assertExists()
        composeTestRule.onNodeWithText("Ver todas las actividades →").performClick()
        assertTrue(clickeado)
    }
}
