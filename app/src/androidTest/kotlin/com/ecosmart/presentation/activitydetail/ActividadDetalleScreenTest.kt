package com.ecosmart.presentation.activitydetail

import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.estadodiario.EstadoActividadUi
import com.ecosmart.presentation.theme.AnilloProgreso
import com.ecosmart.presentation.theme.EcoSmartTheme
import com.ecosmart.presentation.theme.TAG_PASO_NUMERADO
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val MENSAJE_COMPLETADA = "¡Listo por hoy! Volvé mañana para sumar otra vez."
private const val MENSAJE_NO_DISPONIBLE = "Hoy ya sumaste lo máximo de esta categoría. Volvé mañana para sumar otra vez."
private const val BOTON_HACER = "¡Hacela ahora y sumá 50 puntos!"
private const val NOTA_FOTO = "Sacás una foto, la describís y listo"
private const val ROTULO_TIP = "Qué tiene que verse en la foto"

class ActividadDetalleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actividad(categoria: CategoriaActividad = CategoriaActividad.RECICLAR, puntos: Int = 50) = Actividad(
        id = ActividadId.nuevo(),
        categoria = categoria,
        descripcionCorta = "Reciclá envases limpios",
        pasosASeguir = "1. Separá los envases\n2. Llevalos al punto verde",
        resultadoEsperado = "Se ven los envases limpios y separados",
        fotoReferencialUrl = "",
        puntosBase = puntos,
    )

    private fun mostrar(
        actividad: Actividad = actividad(),
        estado: EstadoActividadUi = EstadoActividadUi.PARA_HACER,
        pasos: List<String> = listOf("Separá los envases", "Llevalos al punto verde"),
        tip: String? = "Se ven los envases limpios y separados",
        leccionRelacionadaId: String? = null,
        onVerLeccion: (String) -> Unit = {},
        onRealizarla: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadDetalleContenido(
                    actividad = actividad,
                    onRealizarla = onRealizarla,
                    estado = estado,
                    pasos = pasos,
                    tip = tip,
                    leccionRelacionadaId = leccionRelacionadaId,
                    onVerLeccion = onVerLeccion,
                    modifier = Modifier.wrapContentHeight(Alignment.Top, unbounded = true).requiredHeight(2400.dp),
                )
            }
        }
    }

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

    // ---- Para hacer (RF-IA-013, 015, 016) ----

    @Test
    fun paraHacer_muestraElBotonConLosPuntosRealesYLaNota() {
        mostrar()

        composeTestRule.onNodeWithText(BOTON_HACER).assertIsDisplayed()
        composeTestRule.onNodeWithText(NOTA_FOTO).assertExists()
    }

    @Test
    fun paraHacer_elNumeroDelBotonEsElPuntajeRealDeLaActividad() {
        mostrar(actividad = actividad(puntos = 35))

        composeTestRule.onNodeWithText("¡Hacela ahora y sumá 35 puntos!").assertExists()
        composeTestRule.onNodeWithText(BOTON_HACER).assertDoesNotExist()
    }

    @Test
    fun paraHacer_tocarElBoton_invocaOnRealizarlaUnaVez() {
        var clics = 0
        mostrar(onRealizarla = { clics++ })

        composeTestRule.onNodeWithText(BOTON_HACER).performClick()

        assertEquals(1, clics)
    }

    @Test
    fun paraHacer_muestraComoSeRegistraConUnPasoNumeradoPorLinea() {
        mostrar()

        composeTestRule.onNodeWithText("Cómo se registra").assertExists()
        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO).assertCountEquals(2)
        composeTestRule.onNodeWithText("Separá los envases").assertExists()
        composeTestRule.onNodeWithText("Llevalos al punto verde").assertExists()
    }

    @Test
    fun paraHacer_muestraElTipConSuRotuloYElResultadoEsperado() {
        mostrar()

        composeTestRule.onNodeWithText(ROTULO_TIP).assertExists()
        composeTestRule.onNodeWithText("Se ven los envases limpios y separados").assertExists()
    }

    @Test
    fun paraHacer_sinTip_noMuestraLaTarjetaAmbar() {
        mostrar(tip = null)

        composeTestRule.onNodeWithText(ROTULO_TIP).assertDoesNotExist()
    }

    @Test
    fun paraHacer_sinPasos_omiteLaTarjetaDePasos() {
        mostrar(pasos = emptyList())

        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO).assertCountEquals(0)
    }

    @Test
    fun paraHacer_noMuestraLosElementosEliminados() {
        mostrar()

        composeTestRule.onNodeWithText("Realizarla").assertDoesNotExist()
        composeTestRule.onNodeWithText("0 / 1").assertDoesNotExist()
        composeTestRule.onNodeWithText("completada").assertDoesNotExist()
        composeTestRule.onNodeWithText("+ 50 puntos").assertDoesNotExist()
    }

    // ---- Completada hoy (RF-IA-014) ----

    @Test
    fun completada_muestraChipTildeYMensajeSinElBoton() {
        mostrar(estado = EstadoActividadUi.COMPLETADA_HOY)

        composeTestRule.onNodeWithText("Completada").assertExists()
        composeTestRule.onNodeWithText(MENSAJE_COMPLETADA).assertExists()
        composeTestRule.onNodeWithText(BOTON_HACER).assertDoesNotExist()
        composeTestRule.onNodeWithText(NOTA_FOTO).assertDoesNotExist()
        composeTestRule.onNode(hasContentDescription("completada hoy", substring = true)).assertExists()
    }

    @Test
    fun completada_conservaLosPasosYElTip() {
        mostrar(estado = EstadoActividadUi.COMPLETADA_HOY)

        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO).assertCountEquals(2)
        composeTestRule.onNodeWithText(ROTULO_TIP).assertExists()
    }

    // ---- No disponible hoy ----

    @Test
    fun noDisponible_muestraElMensajeSinBotonNiTilde() {
        mostrar(estado = EstadoActividadUi.NO_DISPONIBLE_HOY)

        composeTestRule.onNodeWithText(MENSAJE_NO_DISPONIBLE).assertExists()
        composeTestRule.onNodeWithText(BOTON_HACER).assertDoesNotExist()
        composeTestRule.onNodeWithText("Completada").assertDoesNotExist()
        composeTestRule.onNodeWithText("Reciclar").assertExists()
    }

    // ---- Aprendé primero (RF-IA-017) ----

    @Test
    fun conLeccionRelacionada_muestraAprendePrimeroYAbreLaLeccionConSuId() {
        var abierta = ""
        mostrar(leccionRelacionadaId = "circuito", onVerLeccion = { abierta = it })

        composeTestRule.onNodeWithText("Aprendé primero").performClick()

        assertEquals("circuito", abierta)
    }

    @Test
    fun sinLeccionRelacionada_noMuestraElBloque() {
        mostrar(leccionRelacionadaId = null)

        composeTestRule.onNodeWithText("Aprendé primero").assertDoesNotExist()
    }

    // ---- Caminar conserva su diseño (D-7) ----

    @Test
    fun caminar_conservaElAnilloYElBotonDeSeguimiento() {
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadDetalleContenido(
                    actividad = actividad(categoria = CategoriaActividad.CAMINAR),
                    onRealizarla = {},
                    progresoActual = 1200,
                    metaPasos = 2000,
                    modifier = Modifier.wrapContentHeight(Alignment.Top, unbounded = true).requiredHeight(2400.dp),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("60 por ciento").assertExists()
        composeTestRule.onNodeWithText("1.200 pasos").assertExists()
        composeTestRule.onNodeWithText("¡Seguimiento en curso!").assertExists()
        composeTestRule.onNodeWithText("+ 50 puntos").assertExists()
        composeTestRule.onNodeWithText("0 / 1").assertDoesNotExist()
    }

    @Test
    fun caminar_conservaSusTarjetasInformativasYNoUsaLosElementosNuevos() {
        composeTestRule.setContent {
            EcoSmartTheme {
                ActividadDetalleContenido(
                    actividad = actividad(categoria = CategoriaActividad.CAMINAR),
                    onRealizarla = {},
                    estado = EstadoActividadUi.COMPLETADA_HOY,
                    pasos = listOf("Un paso"),
                    tip = "Un tip",
                    leccionRelacionadaId = "caminar-1",
                    modifier = Modifier.wrapContentHeight(Alignment.Top, unbounded = true).requiredHeight(2400.dp),
                )
            }
        }

        composeTestRule.onNodeWithText("C\u00f3mo se registra").assertExists()
        composeTestRule.onNodeWithText(
            "Usa los sensores de tu dispositivo para detectar el movimiento y calcular la distancia.",
        ).assertExists()
        composeTestRule.onNodeWithText(
            "Tip: Pod\u00e9s salir a caminar, hacer una caminata o incluso moverte en tu d\u00eda a d\u00eda.",
        ).assertExists()
        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO).assertCountEquals(0)
        composeTestRule.onNodeWithText("Aprend\u00e9 primero").assertDoesNotExist()
        composeTestRule.onNodeWithText("Completada").assertDoesNotExist()
        composeTestRule.onNodeWithText(MENSAJE_COMPLETADA).assertDoesNotExist()
    }
}
