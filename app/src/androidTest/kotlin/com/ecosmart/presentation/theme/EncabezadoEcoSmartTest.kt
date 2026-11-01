package com.ecosmart.presentation.theme

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EncabezadoEcoSmartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun resultadoDeTexto(texto: String): TextLayoutResult {
        val resultados = mutableListOf<TextLayoutResult>()
        composeTestRule.onNodeWithText(texto, substring = true).fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult].action?.invoke(resultados)
        return resultados.first()
    }

    @Test
    fun conParametrosPorDefecto_tituloUsaTypeTitle() {
        composeTestRule.setContent {
            EcoSmartTheme { EncabezadoEcoSmart(titulo = "Actividades", subtitulo = "Subtítulo") }
        }

        val estilo = resultadoDeTexto("Actividades").layoutInput.style
        assertEquals(TypeTitle.fontSize, estilo.fontSize)
        assertEquals(TypeTitle.fontWeight, estilo.fontWeight)
    }

    @Test
    fun conParametrosPorDefecto_hojitaQuedaALaDerechaDelSubtitulo() {
        composeTestRule.setContent {
            EcoSmartTheme { EncabezadoEcoSmart(titulo = "Actividades", subtitulo = "Subtítulo") }
        }

        val subtitulo = composeTestRule.onNodeWithText("Subtítulo").getBoundsInRoot()
        val hoja = composeTestRule.onNodeWithTag(TAG_HOJA_SUBTITULO).getBoundsInRoot()
        assertTrue("La hoja debe quedar a la derecha del subtítulo", hoja.left >= subtitulo.right)
    }

    @Test
    fun conParametrosPorDefecto_avatarYFlechaSeComportanComoAntes() {
        var volvio = 0
        var avatar = 0
        composeTestRule.setContent {
            EcoSmartTheme {
                EncabezadoEcoSmart(
                    titulo = "Actividades",
                    inicialUsuario = "mica",
                    onAvatar = { avatar++ },
                    onVolver = { volvio++ },
                )
            }
        }

        composeTestRule.onNodeWithText("M").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        assertEquals(1, avatar)
        assertEquals(1, volvio)
    }

    @Test
    fun conHojaInline_elIconoFormaParteDelTextoDelSubtitulo() {
        composeTestRule.setContent {
            EcoSmartTheme {
                EncabezadoEcoSmart(
                    titulo = "Educación",
                    subtitulo = "Aprendé, informate, hacé la diferencia",
                    hojaInline = true,
                )
            }
        }

        composeTestRule.onNodeWithTag(TAG_HOJA_SUBTITULO).assertDoesNotExist()
        val resultado = resultadoDeTexto("Aprendé, informate")
        assertTrue("El ícono debe ser un placeholder dentro del texto", resultado.placeholderRects.orEmpty().isNotEmpty())
    }
}
