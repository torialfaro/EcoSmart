package com.ecosmart.presentation.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.education.LeccionUi
import com.ecosmart.presentation.education.PasoLeccionUi
import com.ecosmart.presentation.estadodiario.ActividadInicioUi
import com.ecosmart.presentation.estadodiario.EstadoActividadUi
import com.ecosmart.presentation.estadodiario.EstadoCategoriaUi
import com.ecosmart.presentation.estadodiario.EstadoDiarioUi
import com.ecosmart.presentation.theme.EcoSmartTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun actividad(categoria: CategoriaActividad, titulo: String, puntos: Int = 50) = Actividad(
        id = ActividadId.nuevo(),
        categoria = categoria,
        descripcionCorta = titulo,
        pasosASeguir = "1. Paso",
        resultadoEsperado = "Resultado",
        fotoReferencialUrl = "",
        puntosBase = puntos,
    )

    private val reciclar = actividad(CategoriaActividad.RECICLAR, "Reciclá envases limpios")
    private val reutilizar = actividad(CategoriaActividad.REUTILIZAR, "Dale segunda vida a un frasco", puntos = 100)
    private val caminar = actividad(CategoriaActividad.CAMINAR, "Salí a caminar por una plaza")

    private fun tarjetas(estadoReciclar: EstadoActividadUi = EstadoActividadUi.PARA_HACER) = listOf(
        ActividadInicioUi(reciclar, estadoReciclar),
        ActividadInicioUi(reutilizar, EstadoActividadUi.PARA_HACER),
        ActividadInicioUi(caminar, EstadoActividadUi.PARA_HACER),
    )

    private fun estadoDiario(hechas: Set<CategoriaActividad> = emptySet()) = EstadoDiarioUi(
        categorias = listOf(CategoriaActividad.RECICLAR, CategoriaActividad.REUTILIZAR, CategoriaActividad.CAMINAR)
            .map { EstadoCategoriaUi(categoria = it, hecha = it in hechas, topeAlcanzado = false) },
        estadoPorActividad = emptyMap(),
    )

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

    private fun mostrar(
        nombreUsuario: String = "Ana",
        puntosTotales: Int = 1250,
        estadoDiario: EstadoDiarioUi? = estadoDiario(),
        actividades: List<ActividadInicioUi> = tarjetas(),
        leccionesInicio: List<LeccionUi> = emptyList(),
        onSeleccionarActividad: (ActividadId) -> Unit = {},
        onVerTodasLasActividades: () -> Unit = {},
        onVerLeccion: (String) -> Unit = {},
        onVerPerfil: () -> Unit = {},
        fontScale: Float = 1f,
    ) {
        composeTestRule.setContent {
            EcoSmartTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                    HomeContenido(
                        nombreUsuario = nombreUsuario,
                        puntosTotales = puntosTotales,
                        estadoDiario = estadoDiario,
                        actividades = actividades,
                        leccionesInicio = leccionesInicio,
                        onSeleccionarActividad = onSeleccionarActividad,
                        onVerTodasLasActividades = onVerTodasLasActividades,
                        onVerLeccion = onVerLeccion,
                        onVerPerfil = onVerPerfil,
                    )
                }
            }
        }
    }

    private fun irA(texto: String) {
        composeTestRule.onNodeWithTag(TAG_LISTA_INICIO).performScrollToNode(hasText(texto))
    }

    @Test
    fun home_muestraSaludoPuntosYProgresoConLosDatosRecibidos() {
        mostrar(nombreUsuario = "Ana", puntosTotales = 1250, estadoDiario = estadoDiario(setOf(CategoriaActividad.RECICLAR)))

        composeTestRule.onNodeWithText("¡Hola, Ana!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tus puntos").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.250").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tu progreso de hoy").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/3").assertIsDisplayed()
    }

    @Test
    fun home_noMuestraValoresDeEjemploDelDiseno() {
        mostrar(nombreUsuario = "Ana", puntosTotales = 1250)

        composeTestRule.onNodeWithText("¡Hola, Viky!").assertDoesNotExist()
        composeTestRule.onNodeWithText("0 / 100").assertDoesNotExist()
    }

    @Test
    fun home_sinEstadoDiario_ocultaElProgresoYNoInventaCifras() {
        mostrar(estadoDiario = null)

        composeTestRule.onNodeWithText("Tu progreso de hoy").assertDoesNotExist()
        composeTestRule.onNodeWithText("0/3").assertDoesNotExist()
        composeTestRule.onNodeWithText("Actividades de hoy").assertIsDisplayed()
    }

    @Test
    fun home_muestraLaPrimeraTarjetaConSuBotonDeInvitacion() {
        mostrar()

        composeTestRule.onNodeWithText("Actividades de hoy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reciclá envases limpios").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Hacerla ahora")[0].assertIsDisplayed()
    }

    @Test
    fun home_noMuestraLosElementosEliminados() {
        mostrar()

        composeTestRule.onNodeWithText("Tu progreso general").assertDoesNotExist()
        composeTestRule.onNodeWithText("3/4 actividades").assertDoesNotExist()
        composeTestRule.onNodeWithText("0 / 1").assertDoesNotExist()
        composeTestRule.onNodeWithText("Ver todas").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Puntos Verdes").assertDoesNotExist()
    }

    @Test
    fun home_botonVerTodasLasActividades_disparaNavegacion() {
        var clickeado = false
        mostrar(onVerTodasLasActividades = { clickeado = true })

        irA("Ver todas las actividades")
        composeTestRule.onNodeWithText("Ver todas las actividades").performClick()

        assertTrue(clickeado)
    }

    @Test
    fun home_clickEnTarjetaDeActividad_seleccionaEsaActividad() {
        var seleccionada: ActividadId? = null
        mostrar(onSeleccionarActividad = { seleccionada = it })

        composeTestRule.onNodeWithText("Reciclá envases limpios").performClick()

        assertEquals(reciclar.id, seleccionada)
    }

    @Test
    fun home_clickEnLaTarjetaDePuntos_abreElPerfil() {
        var perfilAbierto = false
        mostrar(onVerPerfil = { perfilAbierto = true })

        composeTestRule.onNodeWithText("Tus puntos").performClick()

        assertTrue(perfilAbierto)
    }

    @Test
    fun home_muestraLasTarjetasDeLeccionYNingunaDeVideo() {
        mostrar(
            leccionesInicio = listOf(
                leccion("l1", "El circuito del reciclaje en CABA"),
                leccion("l2", "Cómo separar tus residuos"),
            ),
        )

        irA("Contenido para vos")
        composeTestRule.onNodeWithText("Contenido para vos").assertIsDisplayed()
        composeTestRule.onNodeWithText("El circuito del reciclaje en CABA").assertExists()
        composeTestRule.onNodeWithText("¿Cómo afecta la contaminación?").assertDoesNotExist()
    }

    @Test
    fun home_muestraComoMaximoDosTarjetasDeLeccion() {
        mostrar(
            leccionesInicio = listOf(
                leccion("l1", "Primera"),
                leccion("l2", "Segunda"),
                leccion("l3", "Tercera"),
            ),
        )

        irA("Contenido para vos")
        composeTestRule.onNodeWithText("Primera").assertExists()
        composeTestRule.onNodeWithText("Segunda").assertExists()
        composeTestRule.onNodeWithText("Tercera").assertDoesNotExist()
    }

    @Test
    fun home_sinLecciones_omiteLaSeccionContenidoParaVos() {
        mostrar(leccionesInicio = emptyList())

        composeTestRule.onNodeWithText("Actividades de hoy").assertExists()
        composeTestRule.onNodeWithText("Contenido para vos").assertDoesNotExist()
    }

    @Test
    fun home_conTextoAl130_losTextosClaveNoSeRecortan() {
        mostrar(fontScale = 1.3f, estadoDiario = estadoDiario(setOf(CategoriaActividad.RECICLAR)))

        listOf("¡Hola, Ana!", "Tus puntos", "Tu progreso de hoy", "Hacerla ahora").forEach { texto ->
            val resultados = mutableListOf<TextLayoutResult>()
            composeTestRule.onAllNodesWithText(texto, useUnmergedTree = true)[0]
                .fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(resultados)
            val r = resultados.first()
            val recortado = r.didOverflowHeight || (0 until r.lineCount).any { r.isLineEllipsized(it) }
            assertFalse("$texto no debe recortarse", recortado)
        }
    }

    @Test
    fun home_conTextoAl130_losBotonesCumplenLaZonaTactilMinima() {
        mostrar(fontScale = 1.3f)

        composeTestRule.onAllNodesWithText("Hacerla ahora")[0].assertHeightIsAtLeast(48.dp)
        irA("Ver todas las actividades")
        composeTestRule.onNodeWithText("Ver todas las actividades").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun home_clickEnTarjetaDeLeccion_invocaOnVerLeccionConElId() {
        var abierta = ""
        mostrar(
            leccionesInicio = listOf(leccion("circuito", "El circuito del reciclaje en CABA")),
            onVerLeccion = { abierta = it },
        )

        irA("El circuito del reciclaje en CABA")
        composeTestRule.onNodeWithText("El circuito del reciclaje en CABA").performClick()

        assertEquals("circuito", abierta)
    }
}
