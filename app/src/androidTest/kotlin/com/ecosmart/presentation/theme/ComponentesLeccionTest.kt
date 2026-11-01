package com.ecosmart.presentation.theme

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.education.LeccionUi
import com.ecosmart.presentation.education.PasoLeccionUi
import com.ecosmart.presentation.education.TipLeccionUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComponentesLeccionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun mostrarIlustracion(nombre: String?, descripcion: String?) {
        composeTestRule.setContent {
            EcoSmartTheme {
                IlustracionLeccion(
                    nombre = nombre,
                    descripcion = descripcion,
                    categoria = CategoriaActividad.REUTILIZAR,
                    modifier = Modifier.width(200.dp).height(120.dp),
                )
            }
        }
    }

    @Test
    fun ilustracion_conNombreInexistente_muestraFondoPastel() {
        mostrarIlustracion("il_que_no_existe", "Dibujo")

        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_FALLBACK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_IMAGEN).assertDoesNotExist()
    }

    @Test
    fun ilustracion_sinNombre_muestraFondoPastel() {
        mostrarIlustracion(null, "Dibujo")

        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_FALLBACK).assertIsDisplayed()
    }

    @Test
    fun ilustracion_conNombreExistente_muestraLaImagen() {
        mostrarIlustracion("il_leccion_circuito_portada", "Portada")

        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_IMAGEN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_FALLBACK).assertDoesNotExist()
    }

    @Test
    fun ilustracion_conDescripcion_exponeDescripcionAccesible() {
        mostrarIlustracion("il_leccion_circuito_portada", "Botella junto a un contenedor verde")

        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_LECCION)
            .assertContentDescriptionEquals("Botella junto a un contenedor verde")
    }

    @Test
    fun ilustracion_sinDescripcion_esAdornoSinDescripcion() {
        mostrarIlustracion("il_leccion_circuito_portada", "")

        composeTestRule.onNodeWithTag(TAG_ILUSTRACION_LECCION)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun chipCategoria_muestraLasTresCategorias() {
        composeTestRule.setContent {
            EcoSmartTheme {
                androidx.compose.foundation.layout.Column {
                    CategoriaActividad.entries.forEach { ChipCategoria(categoria = it) }
                }
            }
        }

        composeTestRule.onNodeWithText("Reciclar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reutilizar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Caminar").assertIsDisplayed()
    }

    @Test
    fun chipCategoria_translucido_muestraElTexto() {
        composeTestRule.setContent {
            EcoSmartTheme {
                ChipCategoria(categoria = CategoriaActividad.RECICLAR, translucido = true)
            }
        }

        composeTestRule.onNodeWithText("Reciclar").assertIsDisplayed()
    }

    private val tituloLargo = "El circuito completo del reciclaje de envases en la Ciudad Autónoma de Buenos Aires"

    private fun leccionDePrueba(titulo: String = tituloLargo) = LeccionUi(
        id = "circuito",
        titulo = titulo,
        categoria = CategoriaActividad.REUTILIZAR,
        duracionMinutos = 4,
        resumen = "Seguí el recorrido de una botella en 5 pasos.",
        introduccion = "Introducción",
        ilustracionPortada = "il_leccion_circuito_portada",
        descripcionPortada = "Portada",
        pasos = List(5) { PasoLeccionUi("Paso ${it + 1}", "Texto", null, "") },
        tip = null,
        textoCierre = null,
    )

    private fun mostrarTarjeta(fontScale: Float, onClick: () -> Unit = {}, compacta: Boolean = false) {
        composeTestRule.setContent {
            val densidad = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(densidad.density, fontScale)) {
                EcoSmartTheme {
                    TarjetaLeccion(leccion = leccionDePrueba(), onClick = onClick, compacta = compacta)
                }
            }
        }
    }

    @Test
    fun tarjetaLeccion_esUnUnicoElementoTocableConMetadato() {
        var clicks = 0
        mostrarTarjeta(fontScale = 1f, onClick = { clicks++ })

        composeTestRule.onNodeWithText(tituloLargo).assertHasClickAction().performClick()
        composeTestRule.onNodeWithText("4 min · 5 pasos", substring = true, useUnmergedTree = true).assertIsDisplayed()
        assertEquals(1, clicks)
    }

    @Test
    fun tarjetaLeccion_conFontScale13_tituloSinRecorteNiSuperposicionConChip() {
        mostrarTarjeta(fontScale = 1.3f)

        val titulo = composeTestRule.onNodeWithText(tituloLargo, useUnmergedTree = true)
        titulo.assertIsDisplayed()
        val resultados = mutableListOf<TextLayoutResult>()
        titulo.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(resultados)
        assertTrue(resultados.isNotEmpty())
        assertFalse("El título no debe recortarse", resultados.first().hasVisualOverflow)

        val chip = composeTestRule.onNodeWithText("Reutilizar", useUnmergedTree = true).getBoundsInRoot()
        val tituloBounds = titulo.getBoundsInRoot()
        val sinSuperposicion = tituloBounds.top >= chip.bottom ||
            tituloBounds.bottom <= chip.top ||
            tituloBounds.left >= chip.right ||
            tituloBounds.right <= chip.left
        assertTrue("El título se superpone con el chip", sinSuperposicion)
    }

    @Test
    fun tarjetaLeccion_zonaTactilMinima48() {
        mostrarTarjeta(fontScale = 1f)

        composeTestRule.onNodeWithText(tituloLargo)
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun tarjetaLeccion_compacta_muestraTituloYEsTocable() {
        mostrarTarjeta(fontScale = 1f, compacta = true)

        composeTestRule.onNodeWithText(tituloLargo).assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun tarjetaLlamadoAccion_muestraEtiquetaTextoYBotonDe48() {
        var clicks = 0
        composeTestRule.setContent {
            EcoSmartTheme {
                TarjetaLlamadoAccion(texto = "Poné en práctica lo que aprendiste", onVerActividades = { clicks++ })
            }
        }

        composeTestRule.onNodeWithText("Ahora te toca a vos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Poné en práctica lo que aprendiste").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ver actividades", substring = true)
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun tarjetaLlamadoAccion_textoBlancoTieneContrasteSuficienteSobreElDegrade() {
        listOf(ColorGreen700, ColorGreen900).forEach { fondo ->
            val l1 = androidx.compose.ui.graphics.Color.White.luminance() + 0.05f
            val l2 = fondo.luminance() + 0.05f
            assertTrue("Contraste insuficiente: ${l1 / l2}", l1 / l2 >= 4.5f)
        }
    }

    @Test
    fun pasoIlustrado_muestraPildoraTituloTextoYDescripcionDeLaIlustracion() {
        composeTestRule.setContent {
            EcoSmartTheme {
                PasoIlustrado(
                    paso = PasoLeccionUi(
                        titulo = "Separá en casa",
                        texto = "Vaciá y enjuagá tus envases.",
                        ilustracion = "il_leccion_circuito_paso1",
                        descripcionIlustracion = "Envases en cuatro contenedores",
                    ),
                    numero = 2,
                    total = 5,
                    categoria = CategoriaActividad.RECICLAR,
                )
            }
        }

        composeTestRule.onNodeWithText("Paso 2 de 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Separá en casa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vaciá y enjuagá tus envases.").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Envases en cuatro contenedores").assertIsDisplayed()
    }

    @Test
    fun tarjetaTip_muestraTituloYTexto() {
        composeTestRule.setContent {
            EcoSmartTheme {
                TarjetaTip(tip = TipLeccionUi("Tip para que cuente", "Enjuagalo antes."))
            }
        }

        composeTestRule.onNodeWithText("Tip para que cuente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enjuagalo antes.").assertIsDisplayed()
    }

    @Test
    fun encabezadoLeccion_botonVolverDe48x48YDisparaOnVolver() {
        var volvio = 0
        composeTestRule.setContent {
            EcoSmartTheme {
                EncabezadoLeccion(leccion = leccionDePrueba(), onVolver = { volvio++ })
            }
        }

        composeTestRule.onNodeWithContentDescription("Volver")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertEquals(1, volvio)
    }
}
