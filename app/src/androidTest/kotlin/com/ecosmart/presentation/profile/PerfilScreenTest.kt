package com.ecosmart.presentation.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ContrasenaCifrada
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

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
        rachaActual = 5,
    )

    private fun diasSemanaDePrueba() = listOf(
        DiaSemanaRachaUi("L", LocalDate.now().minusDays(3), true, false),
        DiaSemanaRachaUi("M", LocalDate.now().minusDays(2), true, false),
        DiaSemanaRachaUi("M", LocalDate.now().minusDays(1), true, false),
        DiaSemanaRachaUi("J", LocalDate.now(), true, true),
        DiaSemanaRachaUi("V", LocalDate.now().plusDays(1), false, false),
        DiaSemanaRachaUi("S", LocalDate.now().plusDays(2), false, false),
        DiaSemanaRachaUi("D", LocalDate.now().plusDays(3), false, false),
    )

    private fun actividadesResumenDePrueba() = listOf(
        ActividadRealizadaResumenUi(
            id = "reg-1",
            titulo = "Llevá tus envases limpios a un Punto Verde",
            categoria = CategoriaActividad.RECICLAR,
            fecha = LocalDate.now(),
            etiquetaFecha = "Reciclar · Hoy",
        ),
        ActividadRealizadaResumenUi(
            id = "reg-2",
            titulo = "Caminá 6.000 pasos",
            categoria = CategoriaActividad.CAMINAR,
            fecha = LocalDate.now().minusDays(1),
            etiquetaFecha = "Caminar · Ayer",
        ),
    )

    @Test
    fun perfilScreen_exponeAvatarNombreNivelYPuntos() {
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 5,
            diasSemanaRacha = diasSemanaDePrueba(),
            actividadesRealizadas = actividadesResumenDePrueba(),
            totalActividadesRealizadas = 7,
            totalRecompensas = 2,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = {},
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Martina").assertExists()
        composeTestRule.onNodeWithText("Semilla · 1.250 pts").assertExists()
    }

    @Test
    fun perfilScreen_exponeTarjetaRachaDestacadaYTiraSemanal() {
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 5,
            diasSemanaRacha = diasSemanaDePrueba(),
            actividadesRealizadas = actividadesResumenDePrueba(),
            totalActividadesRealizadas = 7,
            totalRecompensas = 2,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = {},
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                )
            }
        }

        composeTestRule.onNodeWithText("5").assertExists()
        composeTestRule.onNodeWithText("días de racha").assertExists()
        composeTestRule.onNodeWithText("¡Hoy ya sumaste! Mañana llegás a 6.").assertExists()
        composeTestRule.onNodeWithContentDescription("J: actividad completada").assertExists()
    }

    @Test
    fun perfilScreen_exponeEstadisticasDosColumnas() {
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 5,
            diasSemanaRacha = diasSemanaDePrueba(),
            actividadesRealizadas = actividadesResumenDePrueba(),
            totalActividadesRealizadas = 7,
            totalRecompensas = 0,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = {},
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                )
            }
        }

        composeTestRule.onNodeWithText("7").assertExists()
        composeTestRule.onNodeWithText("Actividades").assertExists()
        composeTestRule.onNodeWithText("0").assertExists()
        composeTestRule.onNodeWithText("Recompensas").assertExists()
    }

    @Test
    fun perfilScreen_exponeSeccionActividadesRealizadasYVerTodas() {
        var verHistorialClickeado = false
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 5,
            diasSemanaRacha = diasSemanaDePrueba(),
            actividadesRealizadas = actividadesResumenDePrueba(),
            totalActividadesRealizadas = 7,
            totalRecompensas = 2,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = { verHistorialClickeado = true },
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Actividades realizadas").assertExists()
        composeTestRule.onNodeWithText("Ver todas").assertExists()
        composeTestRule.onNodeWithText("Llevá tus envases limpios a un Punto Verde").assertExists()
        composeTestRule.onNodeWithText("Reciclar · Hoy").assertExists()
        composeTestRule.onNodeWithText("Caminá 6.000 pasos").assertExists()
        composeTestRule.onNodeWithText("Caminar · Ayer").assertExists()

        composeTestRule.onNodeWithText("Ver todas").performClick()
        assertTrue(verHistorialClickeado)
    }

    @Test
    fun perfilScreen_estadoVacioActividadesRealizadas() {
        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 0,
            diasSemanaRacha = emptyList(),
            actividadesRealizadas = emptyList(),
            totalActividadesRealizadas = 0,
            totalRecompensas = 2,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = {},
                    onEditarPerfil = {},
                    onCerrarSesion = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Actividades realizadas").assertExists()
        composeTestRule.onNodeWithText("Comenzá a realizar actividades").assertExists()
    }

    @Test
    fun perfilScreen_exponeCuatroFilasDeMenuYEjecutaCallbacks() {
        var editarPerfilClickeado = false
        var verHistorialClickeado = false
        var cerrarSesionClickeado = false

        val uiState = PerfilUiState(
            cargando = false,
            usuario = usuarioDePrueba(),
            puntosHistoricos = 1250,
            nivel = NivelUsuario.SEMILLA,
            rachaActual = 5,
            diasSemanaRacha = diasSemanaDePrueba(),
            actividadesRealizadas = actividadesResumenDePrueba(),
            totalActividadesRealizadas = 7,
            totalRecompensas = 0,
        )

        composeTestRule.setContent {
            EcoSmartTheme {
                PerfilContenido(
                    uiState = uiState,
                    onVerHistorial = { verHistorialClickeado = true },
                    onEditarPerfil = { editarPerfilClickeado = true },
                    onCerrarSesion = { cerrarSesionClickeado = true },
                )
            }
        }

        // Se eliminó "Mi perfil" por redundante; quedan 4 filas
        composeTestRule.onNodeWithText("Mi perfil").assertDoesNotExist()
        composeTestRule.onNodeWithText("Mis actividades").assertExists()
        composeTestRule.onNodeWithText("Mis recompensas").assertExists()
        composeTestRule.onNodeWithText("Configuración").assertExists()
        composeTestRule.onNodeWithText("Ayuda").assertExists()
        composeTestRule.onNodeWithText("Cerrar sesión").assertExists()

        composeTestRule.onNodeWithText("Configuración").performScrollTo().performClick()
        assertTrue(editarPerfilClickeado)

        composeTestRule.onNodeWithText("Mis actividades").performScrollTo().performClick()
        assertTrue(verHistorialClickeado)

        composeTestRule.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        assertTrue(cerrarSesionClickeado)
    }
}
