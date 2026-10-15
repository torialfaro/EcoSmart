package com.ecosmart.presentation.rewards

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecompensasScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recompensasScreen_muestraTodoElCatalogoPorDefecto() {
        composeTestRule.setContent {
            EcoSmartTheme {
                RecompensasContenido(
                    recompensas = RECOMPENSAS_EJEMPLO,
                )
            }
        }

        // Chips de categoría visibles (sin segmento "Para adultos"/"Para Kids" — feedback post-QA)
        composeTestRule.onNodeWithText("Todos").assertExists()
        composeTestRule.onNodeWithText("Comida").assertExists()
        composeTestRule.onNodeWithText("Descuentos").assertExists()
        composeTestRule.onNodeWithText("Productos").assertExists()

        // Catálogo único: todas las recompensas son visibles
        composeTestRule.onNodeWithText("Combo café + medialuna").assertExists()
        composeTestRule.onNodeWithText("15% de descuento en tiendas eco").assertExists()
        composeTestRule.onNodeWithText("Cine").assertExists()
        composeTestRule.onNodeWithText("Producto sustentable").assertExists()
    }

    @Test
    fun recompensasScreen_filtraPorChipCategoriaComida_AC14() {
        composeTestRule.setContent {
            EcoSmartTheme {
                RecompensasContenido(
                    recompensas = RECOMPENSAS_EJEMPLO,
                )
            }
        }

        // Tocar chip "Comida"
        composeTestRule.onNodeWithText("Comida").performClick()

        // Se ve "Combo café + medialuna"
        composeTestRule.onNodeWithText("Combo café + medialuna").assertExists()

        // Las de descuentos y productos no se ven
        composeTestRule.onNodeWithText("15% de descuento en tiendas eco").assertDoesNotExist()
        composeTestRule.onNodeWithText("Cine").assertDoesNotExist()
        composeTestRule.onNodeWithText("Producto sustentable").assertDoesNotExist()
    }

    @Test
    fun recompensasScreen_clickEnRecompensa_disparaCallback() {
        var seleccionada: RecompensaEjemplo? = null
        composeTestRule.setContent {
            EcoSmartTheme {
                RecompensasContenido(
                    recompensas = RECOMPENSAS_EJEMPLO,
                    onSeleccionarRecompensa = { seleccionada = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Combo café + medialuna").performClick()
        assertEquals("cafe-medialuna", seleccionada?.id)
    }
}
