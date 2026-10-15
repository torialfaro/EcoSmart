package com.ecosmart.presentation.activitydetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.ecosmart.presentation.theme.AnilloProgreso
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Rule
import org.junit.Test

class ActividadDetalleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun anilloProgreso_exponePorcentajeEnContentDescription_AC09() {
        composeTestRule.setContent {
            EcoSmartTheme {
                AnilloProgreso(
                    valor = 1200,
                    meta = 2000,
                    unidad = "m",
                )
            }
        }

        // RF-UI-018 y AC-09: "60 por ciento"
        composeTestRule.onNodeWithContentDescription("60 por ciento").assertExists()
        composeTestRule.onNodeWithText("1.200 m").assertExists()
        composeTestRule.onNodeWithText("de 2.000 m").assertExists()
    }
}
