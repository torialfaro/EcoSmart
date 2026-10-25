package com.ecosmart.presentation.verification

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class VerificacionFotoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun crearArchivoImagenTemporal(): File {
        return File.createTempFile("test_foto_", ".jpg").apply {
            deleteOnExit()
            val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }
    }

    @Test
    fun paso1_muestraEncabezadoPaso1_insigniaDePuntos_yControlesDeCamara() {
        composeTestRule.setContent {
            EcoSmartTheme {
                VerificacionFotoContenido(
                    uiState = VerificacionFotoUiState(
                        nombreActividad = "Llevá tus envases limpios a un Punto Verde",
                        puntosBase = 100,
                        archivoFoto = null,
                    ),
                    modoCamara = true,
                    flashEncendido = false,
                    panelExpandido = false,
                    onAlternarPanel = {},
                    onSeleccionarCamara = {},
                    onSeleccionarGaleria = {},
                    onCambiarCamara = {},
                    onAlternarFlash = {},
                    onDisparar = {},
                    onDescripcionCambio = {},
                    onRepetirFoto = {},
                    onEnviar = {},
                    onAtras = {},
                )
            }
        }

        // RF-UI-002d: Paso 1 de 2, insignia de puntos y controles
        composeTestRule.onNodeWithText("Paso 1 de 2").assertExists()
        composeTestRule.onNodeWithText("+100 pts").assertExists()
        composeTestRule.onNodeWithText("Actividad de hoy").assertExists()
        composeTestRule.onNodeWithText("Llevá tus envases limpios a un Punto Verde").assertExists()
        composeTestRule.onNodeWithText("Cámara").assertExists()
        composeTestRule.onNodeWithText("Galería").assertExists()
        composeTestRule.onNodeWithText("Tomá una foto del material que reciclaste o reutilizaste").assertExists()
        composeTestRule.onNodeWithContentDescription("Tomar foto").assertExists()
    }

    @Test
    fun paso2_conFoto_muestraPaso2_yFotoReal() {
        val archivo = crearArchivoImagenTemporal()

        composeTestRule.setContent {
            EcoSmartTheme {
                VerificacionFotoContenido(
                    uiState = VerificacionFotoUiState(
                        nombreActividad = "Separar residuos",
                        puntosBase = 80,
                        archivoFoto = archivo,
                        descripcion = "",
                    ),
                    modoCamara = true,
                    flashEncendido = false,
                    panelExpandido = true,
                    onAlternarPanel = {},
                    onSeleccionarCamara = {},
                    onSeleccionarGaleria = {},
                    onCambiarCamara = {},
                    onAlternarFlash = {},
                    onDisparar = {},
                    onDescripcionCambio = {},
                    onRepetirFoto = {},
                    onEnviar = {},
                    onAtras = {},
                )
            }
        }

        // RF-UI-002d: Paso 2 de 2, foto real capturada a pantalla completa y panel inferior
        composeTestRule.onNodeWithText("Paso 2 de 2").assertExists()
        composeTestRule.onNodeWithText("+80 pts").assertExists()
        composeTestRule.onNodeWithContentDescription("Foto capturada").assertExists()
        composeTestRule.onNodeWithText("Foto lista").assertExists()
        composeTestRule.onNodeWithText("Repetir foto").assertExists()
        composeTestRule.onNodeWithText("Describí lo que hiciste").assertExists()
    }

    @Test
    fun panelInferior_colapsaYExpandeAlTocarEncabezado() {
        val archivo = crearArchivoImagenTemporal()

        composeTestRule.setContent {
            var expandido by remember { mutableStateOf(true) }
            EcoSmartTheme {
                VerificacionFotoContenido(
                    uiState = VerificacionFotoUiState(
                        nombreActividad = "Separar residuos",
                        puntosBase = 80,
                        archivoFoto = archivo,
                        descripcion = "",
                    ),
                    modoCamara = true,
                    flashEncendido = false,
                    panelExpandido = expandido,
                    onAlternarPanel = { expandido = !expandido },
                    onSeleccionarCamara = {},
                    onSeleccionarGaleria = {},
                    onCambiarCamara = {},
                    onAlternarFlash = {},
                    onDisparar = {},
                    onDescripcionCambio = {},
                    onRepetirFoto = {},
                    onEnviar = {},
                    onAtras = {},
                )
            }
        }

        // Inicialmente expandido: se ve el campo de descripción
        composeTestRule.onNodeWithText("Describí lo que hiciste").assertExists()

        // Tocar "Foto lista" para colapsar
        composeTestRule.onNodeWithText("Foto lista").performClick()
        composeTestRule.waitForIdle()

        // Ahora colapsado: el campo de descripción ya no se muestra
        composeTestRule.onNodeWithText("Describí lo que hiciste").assertDoesNotExist()

        // Tocar nuevamente para volver a expandir
        composeTestRule.onNodeWithText("Foto lista").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Describí lo que hiciste").assertExists()
    }

    @Test
    fun botonEnviarActividad_estaDeshabilitadoSinDescripcion_ySeHabilitaConDescripcion() {
        val archivo = crearArchivoImagenTemporal()
        var envioDisparado = false

        composeTestRule.setContent {
            var textoDescripcion by remember { mutableStateOf("") }
            EcoSmartTheme {
                VerificacionFotoContenido(
                    uiState = VerificacionFotoUiState(
                        nombreActividad = "Llevá tus envases limpios",
                        puntosBase = 100,
                        archivoFoto = archivo,
                        descripcion = textoDescripcion,
                    ),
                    modoCamara = true,
                    flashEncendido = false,
                    panelExpandido = true,
                    onAlternarPanel = {},
                    onSeleccionarCamara = {},
                    onSeleccionarGaleria = {},
                    onCambiarCamara = {},
                    onAlternarFlash = {},
                    onDisparar = {},
                    onDescripcionCambio = { textoDescripcion = it },
                    onRepetirFoto = {},
                    onEnviar = { envioDisparado = true },
                    onAtras = {},
                )
            }
        }

        // Estado inicial sin descripción:
        composeTestRule.onNodeWithText("Describí lo que hiciste para continuar").assertExists()
        composeTestRule.onNodeWithText("Enviar actividad").assertIsNotEnabled()

        // Escribir descripción:
        composeTestRule.onNodeWithText("Ej.: Llevé 6 botellas y 3 latas limpias al Punto Verde de mi barrio")
            .performTextInput("Llevé 6 botellas limpias")

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Listo para enviar").assertExists()
        composeTestRule.onNodeWithText("Enviar actividad").assertIsEnabled()

        // Tocar enviar actividad:
        composeTestRule.onNodeWithText("Enviar actividad").performClick()
        assertTrue(envioDisparado)
    }

    @Test
    fun repetirFoto_invocaCallbackAlHacerClick() {
        val archivo = crearArchivoImagenTemporal()
        var repetirClickeado = false

        composeTestRule.setContent {
            EcoSmartTheme {
                VerificacionFotoContenido(
                    uiState = VerificacionFotoUiState(
                        nombreActividad = "Llevá tus envases limpios",
                        puntosBase = 100,
                        archivoFoto = archivo,
                        descripcion = "",
                    ),
                    modoCamara = true,
                    flashEncendido = false,
                    panelExpandido = true,
                    onAlternarPanel = {},
                    onSeleccionarCamara = {},
                    onSeleccionarGaleria = {},
                    onCambiarCamara = {},
                    onAlternarFlash = {},
                    onDisparar = {},
                    onDescripcionCambio = {},
                    onRepetirFoto = { repetirClickeado = true },
                    onEnviar = {},
                    onAtras = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Repetir foto").performClick()
        assertTrue(repetirClickeado)
    }
}
