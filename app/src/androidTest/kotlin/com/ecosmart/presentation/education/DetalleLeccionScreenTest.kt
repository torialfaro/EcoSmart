package com.ecosmart.presentation.education

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetalleLeccionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun leccion(pasos: Int = 5, conTip: Boolean = true, textoCierre: String? = null) = LeccionUi(
        id = "circuito",
        titulo = "El circuito del reciclaje en CABA",
        categoria = CategoriaActividad.RECICLAR,
        duracionMinutos = 4,
        resumen = "Resumen",
        introduccion = "Cada envase que separás hace un recorrido largo antes de volver a ser útil.",
        ilustracionPortada = "il_leccion_circuito_portada",
        descripcionPortada = "Portada",
        pasos = List(pasos) { PasoLeccionUi("Título del paso ${it + 1}", "Texto del paso ${it + 1}", null, "Dibujo ${it + 1}") },
        tip = if (conTip) TipLeccionUi("Tip para que cuente", "Enjuagalo antes.") else null,
        textoCierre = textoCierre,
    )

    private fun mostrar(
        estado: EstadoDetalleLeccion,
        fontScale: Float = 1f,
        onVolver: () -> Unit = {},
        onVerActividades: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            val densidad = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(densidad.density, fontScale)) {
                EcoSmartTheme {
                    DetalleLeccionContenido(estado = estado, onVolver = onVolver, onVerActividades = onVerActividades)
                }
            }
        }
    }

    @Test
    fun detalle_tarjetaDeCierreSeMuestraSiempreConTextoGeneralSiFaltaElPropio() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(textoCierre = null)))

        composeTestRule.onNodeWithText("Ahora te toca a vos").assertExists()
        composeTestRule.onNodeWithText("Poné en práctica lo que aprendiste").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun detalle_tarjetaDeCierreUsaElTextoPropioDeLaLeccion() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(textoCierre = "Llevá tus envases limpios a un Punto Verde")))

        composeTestRule.onNodeWithText("Llevá tus envases limpios a un Punto Verde").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Poné en práctica lo que aprendiste").assertDoesNotExist()
    }

    @Test
    fun detalle_verActividadesDisparaOnVerActividades() {
        var clicks = 0
        mostrar(EstadoDetalleLeccion.Cargada(leccion()), onVerActividades = { clicks++ })

        composeTestRule.onNodeWithText("Ver actividades", substring = true).performScrollTo().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun detalle_muestraChipMetadatoTituloEIntroduccion() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion()))

        composeTestRule.onAllNodesWithText("Reciclar").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("4 min · 5 pasos", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("El circuito del reciclaje en CABA").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Cada envase que separás", substring = true).assertIsDisplayed()
    }

    private fun verificarPasosNumerados(total: Int) {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(pasos = total)))

        for (n in 1..total) {
            composeTestRule.onNodeWithText("Paso $n de $total").assertExists()
        }
        composeTestRule.onNodeWithText("Paso ${total + 1} de $total").assertDoesNotExist()
    }

    @Test
    fun detalle_pasosNumerados_con3Pasos() = verificarPasosNumerados(3)

    @Test
    fun detalle_pasosNumerados_con5Pasos() = verificarPasosNumerados(5)

    @Test
    fun detalle_pasosNumerados_con7Pasos() = verificarPasosNumerados(7)

    @Test
    fun detalle_conTip_muestraElTip() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(conTip = true)))

        composeTestRule.onNodeWithText("Tip para que cuente").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun detalle_sinTip_noMuestraTarjetaDeTip() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(conTip = false)))

        composeTestRule.onNodeWithText("Tip para que cuente").assertDoesNotExist()
        composeTestRule.onNodeWithText("Texto del paso 5").assertExists()
    }

    @Test
    fun detalle_leccionInexistente_ejecutaOnVolverSinMensaje() {
        var volvio = 0
        mostrar(EstadoDetalleLeccion.NoEncontrada, onVolver = { volvio++ })

        composeTestRule.waitForIdle()
        assertEquals(1, volvio)
        composeTestRule.onAllNodesWithText("No pudimos", substring = true).assertCountEquals(0)
    }

    @Test
    fun detalle_pasosExpuestosComoColeccionOrdenada() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(pasos = 5)))

        composeTestRule.onNodeWithTag("ListaPasos").assert(
            SemanticsMatcher("colección de 5 elementos") {
                it.config.getOrNull(SemanticsProperties.CollectionInfo)?.rowCount == 5
            },
        )
    }

    @Test
    fun detalle_conTextoAl130_muestraPasosYTitulos() {
        mostrar(EstadoDetalleLeccion.Cargada(leccion(pasos = 5)), fontScale = 1.3f)

        composeTestRule.onNodeWithText("Título del paso 1").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Título del paso 5").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tip para que cuente").performScrollTo().assertIsDisplayed()
    }
}
