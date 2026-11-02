package com.ecosmart.presentation.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.estadodiario.ActividadInicioUi
import com.ecosmart.presentation.estadodiario.EstadoActividadUi
import com.ecosmart.presentation.estadodiario.EstadoCategoriaUi
import com.ecosmart.presentation.estadodiario.EstadoDiarioUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val TAG_LOGO = "logo_categoria"
private const val TAG_BOTON = "boton_llamado_accion"
private const val TAG_TARJETA = "tarjeta_actividad"
private const val TAG_PANEL = "panel_actividad"
private const val TAG_ENLACE = "enlace_leccion"

class ComponentesInicioActividadTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun mostrarLogo(
        categoria: CategoriaActividad,
        tamano: Int = 76,
        atenuado: Boolean = false,
        conTilde: Boolean = false,
    ) {
        composeTestRule.setContent {
            EcoSmartTheme {
                LogoCategoria(
                    categoria = categoria,
                    tamano = tamano.dp,
                    atenuado = atenuado,
                    conTilde = conTilde,
                    modifier = Modifier.testTag(TAG_LOGO),
                )
            }
        }
    }

    private fun mostrarBoton(
        estado: EstadoActividadUi,
        etiquetaParaHacer: String = "Hacerla ahora",
        onClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EcoSmartTheme {
                BotonLlamadoAccion(
                    estado = estado,
                    onClick = onClick,
                    etiquetaParaHacer = etiquetaParaHacer,
                    modifier = Modifier.testTag(TAG_BOTON),
                )
            }
        }
    }

    // ---- C-19 LogoCategoria ----

    @Test
    fun logo_seMuestraParaCadaCategoria() {
        composeTestRule.setContent {
            EcoSmartTheme {
                Column {
                    CategoriaActividad.entries.forEach { categoria ->
                        LogoCategoria(
                            categoria = categoria,
                            tamano = 44.dp,
                            modifier = Modifier.testTag("$TAG_LOGO-${categoria.name}"),
                        )
                    }
                }
            }
        }

        CategoriaActividad.entries.forEach { categoria ->
            composeTestRule.onNodeWithTag("$TAG_LOGO-${categoria.name}").assertIsDisplayed()
        }
    }

    @Test
    fun logo_respetaLosTresTamanos() {
        val tamanos = listOf(44, 76, 120)
        composeTestRule.setContent {
            EcoSmartTheme {
                Column {
                    tamanos.forEach { tamano ->
                        LogoCategoria(
                            categoria = CategoriaActividad.RECICLAR,
                            tamano = tamano.dp,
                            modifier = Modifier.testTag("$TAG_LOGO-$tamano"),
                        )
                    }
                }
            }
        }

        tamanos.forEach { tamano ->
            composeTestRule.onNodeWithTag("$TAG_LOGO-$tamano")
                .assertWidthIsEqualTo(tamano.dp)
                .assertHeightIsEqualTo(tamano.dp)
        }
    }

    @Test
    fun logo_sinTilde_esDecorativoYNoTieneDescripcion() {
        mostrarLogo(CategoriaActividad.REUTILIZAR, atenuado = true)

        composeTestRule.onNodeWithTag(TAG_LOGO).assert(
            SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription),
        )
    }

    @Test
    fun logo_conTilde_exponeCompletadaAlLectorDePantalla() {
        mostrarLogo(CategoriaActividad.CAMINAR, conTilde = true)

        composeTestRule.onNodeWithTag(TAG_LOGO).assertContentDescriptionEquals("Completada")
    }

    // ---- C-23 BotonLlamadoAccion ----

    @Test
    fun boton_paraHacer_muestraLaFraseDeInvitacionYDisparaElClick() {
        var clics = 0
        mostrarBoton(EstadoActividadUi.PARA_HACER, onClick = { clics++ })

        composeTestRule.onNodeWithText("Hacerla ahora").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTON).performClick()

        assertEquals(1, clics)
    }

    @Test
    fun boton_paraHacer_aceptaUnaEtiquetaPropia() {
        mostrarBoton(EstadoActividadUi.PARA_HACER, etiquetaParaHacer = "¡Hacela ahora y sumá 50 puntos!")

        composeTestRule.onNodeWithText("¡Hacela ahora y sumá 50 puntos!").assertIsDisplayed()
    }

    @Test
    fun boton_completadaHoy_muestraSuTextoYNoDisparaElClick() {
        var clics = 0
        mostrarBoton(EstadoActividadUi.COMPLETADA_HOY, etiquetaParaHacer = "Otra etiqueta", onClick = { clics++ })

        composeTestRule.onNodeWithText("Completada hoy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Otra etiqueta").assertDoesNotExist()
        composeTestRule.onNodeWithTag(TAG_BOTON).performClick()

        assertEquals(0, clics)
    }

    @Test
    fun boton_noDisponibleHoy_muestraSuTextoYNoDisparaElClick() {
        var clics = 0
        mostrarBoton(EstadoActividadUi.NO_DISPONIBLE_HOY, onClick = { clics++ })

        composeTestRule.onNodeWithText("Volvé mañana").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTON).performClick()

        assertEquals(0, clics)
    }

    @Test
    fun boton_cumpleLaZonaTactilMinimaEnLosTresEstados() {
        composeTestRule.setContent {
            EcoSmartTheme {
                Column {
                    EstadoActividadUi.entries.forEach { estado ->
                        BotonLlamadoAccion(
                            estado = estado,
                            onClick = {},
                            modifier = Modifier.testTag("$TAG_BOTON-${estado.name}"),
                        )
                    }
                }
            }
        }

        EstadoActividadUi.entries.forEach { estado ->
            composeTestRule.onNodeWithTag("$TAG_BOTON-${estado.name}")
                .assertHeightIsAtLeast(48.dp)
                .assertWidthIsAtLeast(48.dp)
        }
    }

    // ---- C-20 TarjetaProgresoDiario ----

    private fun estadoDiario(vararg categorias: Pair<CategoriaActividad, Boolean>) = EstadoDiarioUi(
        categorias = categorias.map { (categoria, hecha) ->
            EstadoCategoriaUi(categoria = categoria, hecha = hecha, topeAlcanzado = false)
        },
        estadoPorActividad = emptyMap(),
    )

    @Test
    fun progresoDiario_conTresCategoriasUnaHecha_muestraContadorSegmentosYColumnas() {
        composeTestRule.setContent {
            EcoSmartTheme {
                TarjetaProgresoDiario(
                    estado = estadoDiario(
                        CategoriaActividad.RECICLAR to true,
                        CategoriaActividad.REUTILIZAR to false,
                        CategoriaActividad.CAMINAR to false,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("Tu progreso de hoy").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/3").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(TAG_SEGMENTO_PROGRESO).assertCountEquals(3)
        composeTestRule.onAllNodesWithTag(TAG_COLUMNA_PROGRESO).assertCountEquals(3)
        composeTestRule.onAllNodesWithText("Hecha").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Pendiente").assertCountEquals(2)
        composeTestRule.onNodeWithText("Hacé al menos una de cada categoría por día").assertIsDisplayed()
    }

    @Test
    fun progresoDiario_cadaColumnaSeLeeConSuCategoriaYSuEstado() {
        composeTestRule.setContent {
            EcoSmartTheme {
                TarjetaProgresoDiario(
                    estado = estadoDiario(
                        CategoriaActividad.RECICLAR to true,
                        CategoriaActividad.CAMINAR to false,
                    ),
                )
            }
        }

        val columnas = composeTestRule.onAllNodesWithTag(TAG_COLUMNA_PROGRESO)
        columnas[0].assertContentDescriptionEquals("Reciclar: Hecha")
        columnas[1].assertContentDescriptionEquals("Caminar: Pendiente")
    }

    @Test
    fun progresoDiario_seAdaptaALaCantidadDeCategorias() {
        composeTestRule.setContent {
            EcoSmartTheme {
                TarjetaProgresoDiario(
                    estado = estadoDiario(
                        CategoriaActividad.RECICLAR to false,
                        CategoriaActividad.REUTILIZAR to false,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("0/2").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(TAG_SEGMENTO_PROGRESO).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(TAG_COLUMNA_PROGRESO).assertCountEquals(2)
    }

    // ---- C-21 TarjetaActividadConBoton ----

    private fun actividad(
        categoria: CategoriaActividad = CategoriaActividad.RECICLAR,
        titulo: String = "Reciclá envases limpios",
        puntos: Int = 50,
    ) = Actividad(
        id = ActividadId.nuevo(),
        categoria = categoria,
        descripcionCorta = titulo,
        pasosASeguir = "1. Paso",
        resultadoEsperado = "Resultado",
        fotoReferencialUrl = "",
        puntosBase = puntos,
    )

    private fun mostrarTarjeta(
        estado: EstadoActividadUi,
        titulo: String = "Reciclá envases limpios",
        onClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            EcoSmartTheme {
                TarjetaActividadConBoton(
                    actividad = ActividadInicioUi(actividad(titulo = titulo), estado),
                    onClick = onClick,
                    modifier = Modifier.testTag(TAG_TARJETA),
                )
            }
        }
    }

    @Test
    fun tarjetaActividad_muestraCategoriaPuntosTituloYBoton() {
        mostrarTarjeta(EstadoActividadUi.PARA_HACER)

        composeTestRule.onNodeWithText("Reciclar").assertIsDisplayed()
        composeTestRule.onNodeWithText("+50 pts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reciclá envases limpios").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hacerla ahora").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_conTitulosDeUnoDosYTresLineas_midenLoMismo() {
        val titulos = listOf(
            "Reciclá",
            "Llevá envases limpios y secos a un Punto Verde cercano a tu casa",
            "Llevá envases limpios y secos a un Punto Verde cercano a tu casa y contale a tus vecinos " +
                "cómo separar los residuos para que ellos también puedan sumarse a reciclar cada semana",
        )
        composeTestRule.setContent {
            EcoSmartTheme {
                Column {
                    titulos.forEachIndexed { indice, titulo ->
                        TarjetaActividadConBoton(
                            actividad = ActividadInicioUi(actividad(titulo = titulo), EstadoActividadUi.PARA_HACER),
                            onClick = {},
                            modifier = Modifier.testTag("$TAG_TARJETA-$indice"),
                        )
                    }
                }
            }
        }

        val alturas = titulos.indices.map {
            composeTestRule.onNodeWithTag("$TAG_TARJETA-$it").getBoundsInRoot().height
        }
        assertEquals(alturas[0], alturas[1])
        assertEquals(alturas[0], alturas[2])
    }

    @Test
    fun tarjetaActividad_exponeUnaDescripcionConCategoriaTituloPuntosYEstado() {
        mostrarTarjeta(EstadoActividadUi.PARA_HACER)

        composeTestRule.onNodeWithTag(TAG_TARJETA)
            .assertContentDescriptionContains("Reciclar", substring = true)
            .assertContentDescriptionContains("Reciclá envases limpios", substring = true)
            .assertContentDescriptionContains("50 puntos", substring = true)
            .assertContentDescriptionContains("para hacer", substring = true)
    }

    @Test
    fun tarjetaActividad_enCompletada_laDescripcionDiceElEstado() {
        mostrarTarjeta(EstadoActividadUi.COMPLETADA_HOY)
        composeTestRule.onNodeWithTag(TAG_TARJETA).assertContentDescriptionContains("completada hoy", substring = true)
    }

    @Test
    fun tarjetaActividad_clickEnElBotonParaHacer_abreLaActividadUnaSolaVez() {
        var clics = 0
        mostrarTarjeta(EstadoActividadUi.PARA_HACER, onClick = { clics++ })

        composeTestRule.onNodeWithText("Hacerla ahora").performClick()

        assertEquals(1, clics)
    }

    @Test
    fun tarjetaActividad_clickEnLaTarjetaCompletada_siguienteAbriendoElDetalle() {
        var clics = 0
        mostrarTarjeta(EstadoActividadUi.COMPLETADA_HOY, onClick = { clics++ })

        composeTestRule.onNodeWithTag(TAG_TARJETA).performClick()

        assertEquals(1, clics)
    }

    // ---- C-22 PanelActividad ----

    private fun mostrarPanel(categoria: CategoriaActividad, puntos: Int, completada: Boolean) {
        composeTestRule.setContent {
            EcoSmartTheme {
                PanelActividad(
                    categoria = categoria,
                    puntos = puntos,
                    completada = completada,
                    modifier = Modifier.testTag(TAG_PANEL),
                )
            }
        }
    }

    @Test
    fun panel_sinCompletar_muestraChipDeCategoriaYPuntosReales() {
        mostrarPanel(CategoriaActividad.REUTILIZAR, puntos = 35, completada = false)

        composeTestRule.onNodeWithText("Reutilizar").assertIsDisplayed()
        composeTestRule.onNodeWithText("+35 puntos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completada").assertDoesNotExist()
    }

    @Test
    fun panel_completada_cambiaElChipATildeYLoDiceEnSuDescripcion() {
        mostrarPanel(CategoriaActividad.RECICLAR, puntos = 50, completada = true)

        composeTestRule.onNodeWithText("Completada").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reciclar").assertDoesNotExist()
        composeTestRule.onNodeWithText("+50 puntos").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_PANEL)
            .assertContentDescriptionContains("completada hoy", substring = true)
    }

    @Test
    fun panel_sinCompletar_laDescripcionIncluyeCategoriaYPuntos() {
        mostrarPanel(CategoriaActividad.CAMINAR, puntos = 20, completada = false)

        composeTestRule.onNodeWithTag(TAG_PANEL)
            .assertContentDescriptionContains("Caminar", substring = true)
            .assertContentDescriptionContains("20 puntos", substring = true)
    }

    // ---- C-24 PasoNumerado ----

    private fun mostrarPasos(pasos: List<String>) {
        composeTestRule.setContent {
            EcoSmartTheme { TarjetaPasos(pasos = pasos, modifier = Modifier.testTag("tarjeta_pasos")) }
        }
    }

    private fun verificarCantidadDePasos(cantidad: Int) {
        val pasos = (1..cantidad).map { "Texto del paso $it" }
        mostrarPasos(pasos)

        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO).assertCountEquals(cantidad)
        pasos.forEach { composeTestRule.onNodeWithText(it).assertIsDisplayed() }
        (1..cantidad).forEach { composeTestRule.onAllNodesWithText(it.toString()).assertCountEquals(1) }
    }

    @Test
    fun pasos_conUnPaso_muestraSoloUno() = verificarCantidadDePasos(1)

    @Test
    fun pasos_conTresPasos_muestraTres() = verificarCantidadDePasos(3)

    @Test
    fun pasos_conCincoPasos_muestraCinco() = verificarCantidadDePasos(5)

    @Test
    fun paso_seLeeConSuNumeroYSuTexto() {
        mostrarPasos(listOf("Separá los envases", "Llevalos al punto verde"))

        val pasos = composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO)
        pasos[0].assertContentDescriptionEquals("Paso 1: Separá los envases")
        pasos[1].assertContentDescriptionEquals("Paso 2: Llevalos al punto verde")
    }

    @Test
    fun paso_cumpleLaAlturaMinimaDeLecturaConTextoLargo() {
        mostrarPasos(listOf("Un paso cortito", "Un paso muy largo ".repeat(12).trim()))

        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO)[0].assertHeightIsAtLeast(32.dp)
        composeTestRule.onAllNodesWithTag(TAG_PASO_NUMERADO)[1].assertIsDisplayed()
    }

    // ---- C-25 TarjetaEnlaceLeccion ----

    private fun mostrarEnlace(onClick: () -> Unit = {}) {
        composeTestRule.setContent {
            EcoSmartTheme { TarjetaEnlaceLeccion(onClick = onClick, modifier = Modifier.testTag(TAG_ENLACE)) }
        }
    }

    @Test
    fun enlaceLeccion_muestraSuTextoYDisparaElClick() {
        var clics = 0
        mostrarEnlace(onClick = { clics++ })

        composeTestRule.onNodeWithText("Aprendé primero").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_ENLACE).performClick()

        assertEquals(1, clics)
    }

    @Test
    fun enlaceLeccion_cumpleLaZonaTactilMinima() {
        mostrarEnlace()

        composeTestRule.onNodeWithTag(TAG_ENLACE)
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun enlaceLeccion_seExponeComoUnSoloBoton() {
        mostrarEnlace()

        composeTestRule.onNodeWithTag(TAG_ENLACE)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertContentDescriptionContains("Aprendé primero", substring = true)
    }
}
