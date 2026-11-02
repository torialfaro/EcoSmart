package com.ecosmart.presentation.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.degradePanelCategoria
import com.ecosmart.presentation.comun.etiquetaCategoria
import com.ecosmart.presentation.comun.logoCategoria
import com.ecosmart.presentation.estadodiario.EstadoActividadUi

const val TAG_PASO_NUMERADO = "PasoNumerado"

private const val OPACIDAD_LOGO_ATENUADO = 0.4f
private const val PROPORCION_INSIGNIA = 0.32f
private const val PROPORCION_RELLENO_TILDE = 0.2f
private val TamanoCirculoPaso = 28.dp
private val AlturaMinimaPaso = 32.dp
private val EstiloBotonLlamadoAccion = TypeChip.copy(fontWeight = FontWeight.SemiBold)

/**
 * C-19: logo propio de cada categoría. Es decorativo (el texto vecino ya lo describe) salvo con [conTilde],
 * que lo expone como "Completada". [atenuado] baja su opacidad para las categorías pendientes.
 */
@Composable
fun LogoCategoria(
    categoria: CategoriaActividad,
    tamano: Dp,
    modifier: Modifier = Modifier,
    atenuado: Boolean = false,
    conTilde: Boolean = false,
) {
    val semantica = if (conTilde) Modifier.semantics { contentDescription = "Completada" } else Modifier
    Box(modifier = modifier.size(tamano).then(semantica)) {
        Image(
            painter = painterResource(logoCategoria(categoria)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(if (atenuado) OPACIDAD_LOGO_ATENUADO else 1f),
        )
        if (conTilde) InsigniaTilde(tamano * PROPORCION_INSIGNIA, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun InsigniaTilde(tamano: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .background(ColorGreen700, CircleShape)
            .border(2.dp, Color.White, CircleShape),
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize().padding(tamano * PROPORCION_RELLENO_TILDE),
        )
    }
}

/**
 * C-23: botón con frase de invitación. Solo en [EstadoActividadUi.PARA_HACER] es accionable; en los otros
 * dos estados es una etiqueta informativa que no dispara [onClick].
 */
@Composable
fun BotonLlamadoAccion(
    estado: EstadoActividadUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etiquetaParaHacer: String = "Hacerla ahora",
    icono: Painter? = null,
) {
    when (estado) {
        EstadoActividadUi.PARA_HACER -> BotonParaHacer(etiquetaParaHacer, onClick, icono, modifier)
        EstadoActividadUi.COMPLETADA_HOY ->
            EtiquetaDeEstado("Completada hoy", ColorGreen100, ColorGreen700, modifier)
        EstadoActividadUi.NO_DISPONIBLE_HOY ->
            EtiquetaDeEstado("Volvé mañana", ColorStateIdle, ColorTextoSecundarioFuerte, modifier)
    }
}

@Composable
private fun BotonParaHacer(etiqueta: String, onClick: () -> Unit, icono: Painter?, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = ZonaTactilMinima, minHeight = ZonaTactilMinima),
        shape = FormaPill,
        colors = ButtonDefaults.buttonColors(containerColor = ColorGreen700, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icono != null) {
                Icon(painter = icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text = etiqueta, style = EstiloBotonLlamadoAccion, color = Color.White)
        }
    }
}

@Composable
private fun EtiquetaDeEstado(texto: String, fondo: Color, colorTexto: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = ZonaTactilMinima, minHeight = ZonaTactilMinima)
            .clip(FormaPill)
            .background(fondo)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = texto, style = EstiloBotonLlamadoAccion, color = colorTexto)
    }
}

/**
 * C-22: panel superior del detalle con el logo grande, el chip y la píldora de puntos sobre el degradé de la
 * categoría. Si [completada], el logo lleva tilde y el chip pasa a decir "Completada" (RF-IA-012, 014).
 */
@Composable
fun PanelActividad(
    categoria: CategoriaActividad,
    puntos: Int,
    completada: Boolean,
    modifier: Modifier = Modifier,
) {
    val estado = if (completada) ", completada hoy" else ""
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaPanelActividad)
            .background(degradePanelCategoria(categoria))
            .padding(vertical = 20.dp, horizontal = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${etiquetaCategoria(categoria)}, $puntos puntos$estado"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LogoCategoria(
            categoria = categoria,
            tamano = TamanoLogoGrande,
            conTilde = completada,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (completada) {
                PildoraTexto("Completada", ColorGreen100, ColorGreen700)
            } else {
                ChipCategoria(categoria)
            }
            PildoraTexto("+$puntos puntos", Color.White, ColorGreen900)
        }
    }
}

@Composable
private fun PildoraTexto(texto: String, fondo: Color, colorTexto: Color) {
    Box(
        modifier = Modifier
            .heightIn(min = AlturaChip)
            .clip(FormaPill)
            .background(fondo)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = texto, style = TypeChip.copy(fontWeight = FontWeight.Bold), color = colorTexto)
    }
}

/** C-24: un paso de "Cómo se registra": círculo con su número y el texto, leído como "Paso N: texto" (RF-IA-015). */
@Composable
fun PasoNumerado(numero: Int, texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AlturaMinimaPaso)
            .testTag(TAG_PASO_NUMERADO)
            .semantics(mergeDescendants = true) { contentDescription = "Paso $numero: $texto" },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(TamanoCirculoPaso).background(ColorGreen700, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = numero.toString(), style = TypeChip.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Text(
            text = texto,
            style = TypeBody,
            color = ColorTextPrimary,
            modifier = Modifier.weight(1f).padding(top = 5.dp),
        )
    }
}

/** Tarjeta blanca que agrupa los [PasoNumerado] de una actividad, tantos como haya. */
@Composable
fun TarjetaPasos(pasos: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = FormaPanelActividad,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjetaSuave),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pasos.forEachIndexed { indice, texto -> PasoNumerado(numero = indice + 1, texto = texto) }
        }
    }
}

/** C-25: enlace a la primera lección de la categoría de la actividad (RF-IA-017). */
@Composable
fun TarjetaEnlaceLeccion(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaPanelActividad)
            .background(ColorGreen100)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = ZonaTactilMinima)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Aprendé primero. Abre la primera lección de la categoría"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(painter = painterResource(R.drawable.ic_bombilla), contentDescription = null, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "Aprendé primero", style = TypeSection.copy(fontWeight = FontWeight.Bold), color = ColorGreen900)
            Text(text = "Mirá la lección antes de hacerla", style = TypeResumen, color = ColorTextoSecundarioFuerte)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ColorGreen700,
        )
    }
}
