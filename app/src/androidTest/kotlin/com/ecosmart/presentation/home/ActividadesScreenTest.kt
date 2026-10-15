package com.ecosmart.presentation.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ActividadesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actividadesDePrueba() = listOf(
        ActividadItemUi(
            id = "act-1",
            nombre = "Caminar",
            categoria = CategoriaActividad.CAMINAR,
            iconoRes = R.drawable.ic_caminar,
            progresoActual = 1200,
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
            progresoActual = 0,
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
        ActividadItemUi(
            id = "act-5",
            nombre = "Ahorrar agua",
            categoria = CategoriaActividad.REUTILIZAR,
            iconoRes = R.drawable.ic_categoria_reutilizar,
            progresoActual = 0,
            meta = 1,
            puntosBase = 60,
            esMedible = false,
        ),
        ActividadItemUi(
            id = "act-6",
            nombre = "Elegí tu actividad",
            categoria = CategoriaActividad.RECICLAR,
            iconoRes = R.drawable.ic_categoria_reciclar,
            progresoActual = 0,
            meta = 1,
            puntosBase = 50,
            esMedible = false,
        ),
    )

    @Test
    fun actividadesScreen_muestraTodasLasTarjetasPorDefecto() {
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadesContenido(
                    actividades = actividadesDePrueba(),
                    onSeleccionarActividad = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Todas").assertExists()
        composeTestRule.onNodeWithText("En progreso").assertExists()
        composeTestRule.onNodeWithText("Completadas").assertExists()

        composeTestRule.onNodeWithText("Caminar").assertExists()
        composeTestRule.onNodeWithText("Reciclar").assertExists()
        composeTestRule.onNodeWithText("Separar residuos").assertExists()
        composeTestRule.onNodeWithText("Usar menos plástico").assertExists()
        composeTestRule.onNodeWithText("Ahorrar agua").assertExists()
        composeTestRule.onNodeWithText("Elegí tu actividad").assertExists()
    }

    @Test
    fun actividadesScreen_filtraPorEnProgreso() {
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadesContenido(
                    actividades = actividadesDePrueba(),
                    onSeleccionarActividad = {},
                )
            }
        }

        composeTestRule.onNodeWithText("En progreso").performClick()

        // Solo "Caminar" está en progreso (1200 / 2000)
        composeTestRule.onNodeWithText("Caminar").assertExists()
        composeTestRule.onNodeWithText("Reciclar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Separar residuos").assertDoesNotExist()
    }

    @Test
    fun actividadesScreen_filtraPorCompletadas() {
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadesContenido(
                    actividades = actividadesDePrueba(),
                    onSeleccionarActividad = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Completadas").performClick()

        // Solo "Reciclar" está completada (1 / 1)
        composeTestRule.onNodeWithText("Reciclar").assertExists()
        composeTestRule.onNodeWithText("Caminar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Separar residuos").assertDoesNotExist()
    }

    @Test
    fun actividadesScreen_clickEnTarjeta_disparaOnSeleccionarActividad() {
        var seleccionada = ""
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadesContenido(
                    actividades = actividadesDePrueba(),
                    onSeleccionarActividad = { seleccionada = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Caminar").performClick()
        assertEquals("act-1", seleccionada)
    }
}
