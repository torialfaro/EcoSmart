package com.ecosmart.presentation.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.colorFondoChipCategoria
import com.ecosmart.presentation.comun.colorTextoChipCategoria
import com.ecosmart.presentation.comun.degradePanelCategoria
import com.ecosmart.presentation.comun.etiquetaCategoria
import com.ecosmart.presentation.estadodiario.ActividadInicioUi
import com.ecosmart.presentation.estadodiario.EstadoActividadUi
import com.ecosmart.presentation.estadodiario.EstadoCategoriaUi
import com.ecosmart.presentation.estadodiario.EstadoDiarioUi

const val TAG_SEGMENTO_PROGRESO = "SegmentoProgreso"
const val TAG_COLUMNA_PROGRESO = "ColumnaProgreso"

private const val NOTA_PROGRESO_DIARIO = "Hacé al menos una de cada categoría por día"
private const val LINEAS_TITULO_TARJETA = 3
private val AnchoTarjetaActividad = 260.dp
private val AlturaPanelTarjeta = 96.dp
private val AlturaSegmentoProgreso = 8.dp
private val SuperposicionEncabezado = 56.dp
private val FormaEncabezadoInicio = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
private val EstiloTituloActividad = TypeSection.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp)
private val EstiloTituloSeccion = TypeSection.copy(fontWeight = FontWeight.Bold)
private val EstiloSaludoInicio = TypeTitle.copy(fontSize = 24.sp, lineHeight = 30.sp)

@Composable
private fun suavizada() = CardDefaults.cardElevation(defaultElevation = ElevacionTarjetaSuave)

/** C-20: avance del día con un segmento y una columna por categoría del dominio (RF-IA-005). */
@Composable
fun TarjetaProgresoDiario(estado: EstadoDiarioUi, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = FormaPanelActividad,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = suavizada(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Tu progreso de hoy", style = EstiloTituloSeccion, color = ColorTextPrimary)
                Text(text = "${estado.hechas}/${estado.total}", style = EstiloTituloSeccion, color = ColorGreen700)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                estado.categorias.forEach { categoria ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(AlturaSegmentoProgreso)
                            .clip(FormaPill)
                            .background(if (categoria.hecha) ColorGreen700 else ColorTrack)
                            .testTag(TAG_SEGMENTO_PROGRESO),
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                estado.categorias.forEach { categoria -> ColumnaCategoria(categoria, Modifier.weight(1f)) }
            }
            Text(text = NOTA_PROGRESO_DIARIO, style = TypeMeta, color = ColorTextSecondary)
        }
    }
}

@Composable
private fun ColumnaCategoria(estado: EstadoCategoriaUi, modifier: Modifier = Modifier) {
    val textoEstado = if (estado.hecha) "Hecha" else "Pendiente"
    Column(
        modifier = modifier
            .testTag(TAG_COLUMNA_PROGRESO)
            .semantics(mergeDescendants = true) {
                contentDescription = "${etiquetaCategoria(estado.categoria)}: $textoEstado"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LogoCategoria(
            categoria = estado.categoria,
            tamano = TamanoLogoChico,
            atenuado = !estado.hecha,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = textoEstado,
            style = TypeMeta.copy(fontWeight = FontWeight.SemiBold),
            color = if (estado.hecha) ColorGreen700 else ColorTextoSecundarioFuerte,
        )
    }
}

/** C-21: tarjeta del carrusel de Inicio con panel de categoría, datos de la actividad y botón de acción. */
@Composable
fun TarjetaActividadConBoton(actividad: ActividadInicioUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dato = actividad.actividad
    val estadoTexto = when (actividad.estado) {
        EstadoActividadUi.PARA_HACER -> "para hacer"
        EstadoActividadUi.COMPLETADA_HOY -> "completada hoy"
        EstadoActividadUi.NO_DISPONIBLE_HOY -> "no disponible hoy"
    }
    val descripcion = "${etiquetaCategoria(dato.categoria)}, ${dato.descripcionCorta}, ${dato.puntosBase} puntos, $estadoTexto"
    Card(
        modifier = modifier
            .width(AnchoTarjetaActividad)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = descripcion },
        shape = FormaTarjetaActividad,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = suavizada(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AlturaPanelTarjeta)
                .background(degradePanelCategoria(dato.categoria)),
            contentAlignment = Alignment.Center,
        ) {
            LogoCategoria(
                categoria = dato.categoria,
                tamano = TamanoLogoMedio,
                conTilde = actividad.estado == EstadoActividadUi.COMPLETADA_HOY,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChipCategoria(dato.categoria)
                Text(text = "+${dato.puntosBase} pts", style = TypeChip, color = ColorGreen700)
            }
            Text(
                text = dato.descripcionCorta,
                style = EstiloTituloActividad,
                color = ColorTextPrimary,
                minLines = LINEAS_TITULO_TARJETA,
                maxLines = LINEAS_TITULO_TARJETA,
                overflow = TextOverflow.Ellipsis,
            )
            BotonLlamadoAccion(estado = actividad.estado, onClick = onClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ChipCategoria(categoria: CategoriaActividad) {
    Box(
        modifier = Modifier
            .height(AlturaChip)
            .clip(FormaPill)
            .background(colorFondoChipCategoria(categoria))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = etiquetaCategoria(categoria), style = TypeChip, color = colorTextoChipCategoria(categoria))
    }
}

/**
 * C-02b: bloque verde con saludo, subtítulo de marca y avatar; [tarjeta] (los puntos) se superpone a su borde inferior.
 */
@Composable
fun EncabezadoInicio(
    saludo: String,
    subtitulo: String,
    inicialUsuario: String,
    onAvatar: () -> Unit,
    modifier: Modifier = Modifier,
    tarjeta: @Composable () -> Unit,
) {
    Layout(
        content = {
            BloqueEncabezadoInicio(saludo, subtitulo, inicialUsuario, onAvatar)
            Box(modifier = Modifier.padding(horizontal = SpaceScreenX)) { tarjeta() }
        },
        modifier = modifier.fillMaxWidth(),
    ) { medibles, restricciones ->
        val libres = restricciones.copy(minWidth = 0, minHeight = 0)
        val bloque = medibles[0].measure(restricciones.copy(minHeight = 0))
        val puntos = medibles[1].measure(libres)
        val superposicion = puntos.height / 2
        layout(bloque.width, bloque.height - superposicion + puntos.height) {
            bloque.placeRelative(0, 0)
            puntos.placeRelative(0, bloque.height - superposicion)
        }
    }
}

@Composable
private fun BloqueEncabezadoInicio(saludo: String, subtitulo: String, inicial: String, onAvatar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaEncabezadoInicio)
            .background(Brush.verticalGradient(listOf(ColorGreen700, ColorGreen900)))
            .statusBarsPadding()
            .padding(start = SpaceScreenX, end = 8.dp, top = 12.dp, bottom = SuperposicionEncabezado),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = saludo, style = EstiloSaludoInicio, color = Color.White)
            Text(text = subtitulo, style = TypeResumen, color = CremaTexto)
        }
        Box(
            modifier = Modifier
                .size(ZonaTactilMinima)
                .clickable(onClick = onAvatar)
                .semantics { contentDescription = "Abrir perfil" },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CremaTexto),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = inicial.take(1).uppercase(), style = TypeCardTitle, color = ColorGreen900)
            }
        }
    }
}

/** Tarjeta blanca de puntos de Inicio (RF-IA-004): logo de la app, etiqueta, total real y chevron. */
@Composable
fun TarjetaPuntosInicio(puntos: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hoja: ImageVector = ImageVector.vectorResource(R.drawable.ic_hoja)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "Tus puntos, $puntos puntos" },
        shape = FormaTarjetaActividad,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = suavizada(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Image(
                    painter = painterResource(R.drawable.logo_ecosmart_icono),
                    contentDescription = null,
                    modifier = Modifier.size(TamanoLogoChico),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "Tus puntos", style = TypeMeta, color = ColorTextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = formatearPuntos(puntos), style = TypePoints, color = ColorGreen900)
                        Icon(imageVector = hoja, contentDescription = null, tint = ColorLeaf, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ColorGreen900,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
