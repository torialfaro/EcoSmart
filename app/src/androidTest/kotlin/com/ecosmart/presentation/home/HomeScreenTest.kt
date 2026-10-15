package com.ecosmart.presentation.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun cuatroActividadesTresCompletadas() = listOf(
        ActividadItemUi(
            id = "act-1",
            nombre = "Caminar",
            categoria = CategoriaActividad.CAMINAR,
            iconoRes = R.drawable.ic_caminar,
            progresoActual = 2000,
            meta = 2000,
            puntosBase = 50,
            esMedible = true,
        ),
        ActividadItemUi(
            id = "act-2",
            nombre = "Reciclar",
            categoria = CategoriaActividad.RECICLAR,
            iconoRes = R.drawable.ic_categoria_reciclar,
            progresoActual = 1,
            meta = 1,
            puntosBase = 100,
            esMedible = false,
        ),
        ActividadItemUi(
            id = "act-3",
            nombre = "Separar residuos",
            categoria = CategoriaActividad.RECICLAR,
            iconoRes = R.drawable.ic_categoria_reciclar,
            progresoActual = 1,
            meta = 1,
            puntosBase = 80,
            esMedible = false,
        ),
        ActividadItemUi(
            id = "act-4",
            nombre = "Usar menos plástico",
            categoria = CategoriaActividad.REUTILIZAR,
            iconoRes = R.drawable.ic_categoria_reutilizar,
            progresoActual = 0,
            meta = 1,
            puntosBase = 70,
            esMedible = false,
        ),
    )

    @Test
    fun homeScreen_conTresDeCuatroCompletadas_muestraTextoProgresoYBarra_AC02() {
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                )
            }
        }

        // AC-02: Se ve "Tu progreso general" y "3/4 actividades"
        composeTestRule.onNodeWithText("Tu progreso general").assertExists()
        composeTestRule.onNodeWithText("3/4 actividades").assertExists()
    }

    @Test
    fun homeScreen_muestraTarjetaPuntosYCarruseles() {
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                )
            }
        }

        // Puntos
        composeTestRule.onNodeWithText("Tus puntos").assertExists()
        composeTestRule.onNodeWithText("1.250").assertExists()

        // Títulos de secciones
        composeTestRule.onNodeWithText("Actividades de hoy").assertExists()
        composeTestRule.onNodeWithText("Contenido para vos").assertExists()

        // Videos en carrusel
        composeTestRule.onNodeWithText("¿Cómo afecta la contaminación?").assertExists()
        composeTestRule.onNodeWithText("Tu guía de reciclaje").assertExists()
    }

    @Test
    fun homeScreen_clickEnVerTodasLasActividades_disparaNavegacion_AC04() {
        var verTodasClickeado = false
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                    onVerTodasLasActividades = { verTodasClickeado = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Ver todas las actividades →").performClick()
        assertTrue(verTodasClickeado)
    }

    @Test
    fun homeScreen_clickEnTarjetaActividad_disparaSeleccion_AC05() {
        var seleccionada = ""
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = { seleccionada = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Caminar").performClick()
        assertEquals("act-1", seleccionada)
    }
}
