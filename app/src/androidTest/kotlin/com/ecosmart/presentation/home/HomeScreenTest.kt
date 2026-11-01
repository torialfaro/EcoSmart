package com.ecosmart.presentation.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.education.LeccionUi
import com.ecosmart.presentation.education.PasoLeccionUi
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

    private fun leccion(id: String, titulo: String) = LeccionUi(
        id = id,
        titulo = titulo,
        categoria = CategoriaActividad.RECICLAR,
        duracionMinutos = 4,
        resumen = "Resumen de $titulo",
        introduccion = "Introducción",
        ilustracionPortada = null,
        descripcionPortada = "Portada",
        pasos = List(3) { PasoLeccionUi("Paso ${it + 1}", "Texto", null, "") },
        tip = null,
        textoCierre = null,
    )

    @Test
    fun homeScreen_muestraTarjetaPuntosYTarjetasDeLeccion() {
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                    leccionesInicio = listOf(
                        leccion("l1", "El circuito del reciclaje en CABA"),
                        leccion("l2", "Cómo separar tus residuos"),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("Tus puntos").assertExists()
        composeTestRule.onNodeWithText("1.250").assertExists()
        composeTestRule.onNodeWithText("Actividades de hoy").assertExists()
        composeTestRule.onNodeWithText("Contenido para vos").assertExists()

        // Tarjetas de lección y ninguna de video
        composeTestRule.onNodeWithText("El circuito del reciclaje en CABA").assertExists()
        composeTestRule.onNodeWithText("Cómo separar tus residuos").assertExists()
        composeTestRule.onNodeWithText("¿Cómo afecta la contaminación?").assertDoesNotExist()
        composeTestRule.onNodeWithText("Tu guía de reciclaje").assertDoesNotExist()
    }

    @Test
    fun homeScreen_muestraComoMaximoDosTarjetasDeLeccion() {
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                    leccionesInicio = listOf(
                        leccion("l1", "Primera"),
                        leccion("l2", "Segunda"),
                        leccion("l3", "Tercera"),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("Primera").assertExists()
        composeTestRule.onNodeWithText("Segunda").assertExists()
        composeTestRule.onNodeWithText("Tercera").assertDoesNotExist()
    }

    @Test
    fun homeScreen_sinLecciones_omiteLaSeccionContenidoParaVos() {
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                    leccionesInicio = emptyList(),
                )
            }
        }

        composeTestRule.onNodeWithText("Actividades de hoy").assertExists()
        composeTestRule.onNodeWithText("Contenido para vos").assertDoesNotExist()
    }

    @Test
    fun homeScreen_clickEnTarjetaDeLeccion_invocaOnVerLeccionConElId() {
        var abierta = ""
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                    leccionesInicio = listOf(leccion("circuito", "El circuito del reciclaje en CABA")),
                    onVerLeccion = { abierta = it },
                )
            }
        }

        composeTestRule.onNodeWithText("El circuito del reciclaje en CABA").performClick()
        assertEquals("circuito", abierta)
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

    @Test
    fun homeScreen_botonFlotantePuntosVerdes_disparaNavegacion() {
        var puntosVerdesClickeado = false
        composeTestRule.setContent {
            EcoSmartTheme {
                HomeContenido(
                    actividades = cuatroActividadesTresCompletadas(),
                    puntosTotales = 1250,
                    onSeleccionarActividad = {},
                    onVerPuntosVerdes = { puntosVerdesClickeado = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Puntos Verdes").assertExists()
        composeTestRule.onNodeWithContentDescription("Puntos Verdes").performClick()
        assertTrue(puntosVerdesClickeado)
    }
}
