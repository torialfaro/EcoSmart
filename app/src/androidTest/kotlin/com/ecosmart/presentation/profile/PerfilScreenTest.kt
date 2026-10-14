package com.ecosmart.presentation.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PerfilScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun usuarioDePrueba() = Usuario(
        id = UsuarioId("usuario-123"),
        email = "martina@ejemplo.com",
        contrasenaCifrada = ContrasenaCifrada("hash"),
        nombre = "Martina",
        apellido = "Gómez",
        nombreUsuario = "martina",
        barrio = null,
        telefono = "+5491100000000",
        categoriasDeInteres = emptySet(),
        puntosHistoricos = 1250,
        rachaActual = 3,
    )

    @Test
    fun perfilScreen_exponeAvatarNombreNivelYPuntos() {
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 3,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = {},
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                    onEliminarCuenta = {},
                    onConfirmarEliminarCuenta = {},
                    onCancelarEliminarCuenta = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Martina").assertExists()
        composeTestRule.onNodeWithText("Nivel 1 · 1.250 pts").assertExists()
    }

    @Test
    fun perfilScreen_exponeTarjetaEstadisticasTresColumnas() {
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 3,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = {},
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                    onEliminarCuenta = {},
                    onConfirmarEliminarCuenta = {},
                    onCancelarEliminarCuenta = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Actividades").assertExists()
        composeTestRule.onNodeWithText("Recompensas").assertExists()
        composeTestRule.onNodeWithText("Racha").assertExists()
    }

    @Test
    fun perfilScreen_exponeCincoFilasDeMenuYEjecutaCallbacks() {
        var editarPerfilClickeado = false
        var verHistorialClickeado = false

        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 3,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = { verHistorialClickeado = true },
                    onEditarPerfil = { editarPerfilClickeado = true },
                    onCerrarSesion = {},
                    onEliminarCuenta = {},
                    onConfirmarEliminarCuenta = {},
                    onCancelarEliminarCuenta = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Mi perfil").assertExists()
        composeTestRule.onNodeWithText("Mis actividades").assertExists()
        composeTestRule.onNodeWithText("Mis recompensas").assertExists()
        composeTestRule.onNodeWithText("Configuración").assertExists()
        composeTestRule.onNodeWithText("Ayuda").assertExists()

        composeTestRule.onNodeWithText("Mi perfil").performClick()
        assertTrue(editarPerfilClickeado)

        composeTestRule.onNodeWithText("Mis actividades").performClick()
        assertTrue(verHistorialClickeado)
    }
}
