package com.ecosmart.presentation.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ecosmart.app.R
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.comun.colorFondoChipCategoria
import com.ecosmart.presentation.comun.colorFondoIlustracionCategoria
import com.ecosmart.presentation.comun.colorTextoChipCategoria
import com.ecosmart.presentation.comun.etiquetaCategoria
import com.ecosmart.presentation.education.LeccionUi
import com.ecosmart.presentation.education.PasoLeccionUi
import com.ecosmart.presentation.education.TipLeccionUi

const val TAG_ILUSTRACION_LECCION = "IlustracionLeccion"
const val TAG_ILUSTRACION_FALLBACK = "IlustracionFallback"
const val TAG_ILUSTRACION_IMAGEN = "IlustracionImagen"

/** Dibuja el drawable `nombre`; si falta o no existe, muestra el fondo pastel de la categoría. */
@Composable
fun IlustracionLeccion(
    nombre: String?,
    descripcion: String?,
    categoria: CategoriaActividad,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val recurso = remember(nombre) { resolverDrawable(context, nombre) }
    val semantica = if (descripcion.isNullOrBlank()) {
        Modifier
    } else {
        Modifier.semantics { contentDescription = descripcion }
    }

    Box(modifier = modifier.then(semantica).testTag(TAG_ILUSTRACION_LECCION)) {
        if (recurso != 0) {
            Image(
                painter = painterResource(recurso),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().testTag(TAG_ILUSTRACION_IMAGEN),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorFondoIlustracionCategoria(categoria))
                    .testTag(TAG_ILUSTRACION_FALLBACK),
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
private fun resolverDrawable(context: android.content.Context, nombre: String?): Int {
    if (nombre.isNullOrBlank()) return 0
    return context.resources.getIdentifier(nombre, "drawable", context.packageName)
}

/** Tarjeta `C-13`: un único elemento tocable que abre la lección; `compacta` es la variante de Inicio. */
@Composable
fun TarjetaLeccion(
    leccion: LeccionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compacta: Boolean = false,
) {
    Column(
        modifier = modifier
            .then(if (compacta) Modifier.width(280.dp) else Modifier.fillMaxWidth())
            .shadow(ElevacionTarjeta, FormaTarjetaLeccion)
            .clip(FormaTarjetaLeccion)
            .background(ColorSurfaceCard)
            .border(BorderStroke(1.dp, ColorTrack), FormaTarjetaLeccion)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button },
    ) {
        IlustracionLeccion(
            nombre = leccion.ilustracionPortada,
            descripcion = leccion.descripcionPortada,
            categoria = leccion.categoria,
            modifier = Modifier.fillMaxWidth().height(AlturaIlustracionTarjeta),
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChipCategoria(categoria = leccion.categoria)
                Text(
                    text = "${leccion.duracionMinutos} min · ${leccion.cantidadPasos} pasos",
                    style = TypeResumen,
                    color = ColorTextoSecundarioFuerte,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(text = leccion.titulo, style = TypeTituloTarjeta, color = ColorTextPrimary)
            Text(
                text = leccion.resumen,
                style = TypeResumen,
                color = ColorTextoLectura,
                maxLines = if (compacta) 2 else Int.MAX_VALUE,
            )
            Text(text = "Ver lección →", style = TypeLink, color = ColorGreen700)
        }
    }
}

/** Paso `C-15`: ilustración, píldora "Paso N de M", título y texto. */
@Composable
fun PasoIlustrado(
    paso: PasoLeccionUi,
    numero: Int,
    total: Int,
    categoria: CategoriaActividad,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjetaLeccion)
            .background(ColorSurfaceCard)
            .border(BorderStroke(1.dp, ColorTrack), FormaTarjetaLeccion),
    ) {
        IlustracionLeccion(
            nombre = paso.ilustracion,
            descripcion = paso.descripcionIlustracion,
            categoria = categoria,
            modifier = Modifier.fillMaxWidth().height(AlturaIlustracionPaso),
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .heightIn(min = AlturaChip)
                    .clip(FormaPill)
                    .background(ColorGreen100)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Paso $numero de $total", style = TypeChip, color = ColorGreen700)
            }
            Text(text = paso.titulo, style = TypeTituloTarjeta, color = ColorTextPrimary)
            Text(text = paso.texto, style = TypeTextoPaso, color = ColorTextoLectura)
        }
    }
}

/** Separador decorativo entre pasos; no se anuncia. */
@Composable
fun ConectorPaso(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(AlturaConectorPaso),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = ColorLeaf,
            modifier = Modifier.size(AlturaConectorPaso),
        )
    }
}

/** Tip `C-16`. */
@Composable
fun TarjetaTip(tip: TipLeccionUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjetaLeccion)
            .background(ColorTipFondo)
            .border(BorderStroke(1.dp, ColorTipBorde), FormaTarjetaLeccion)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_bombilla),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = tip.titulo, style = TypeTituloTarjeta, color = ColorTipTitulo)
            Text(text = tip.texto, style = TypeTextoPaso, color = ColorTipTexto)
        }
    }
}

/** Encabezado `C-18`: banda con la portada, botón de volver y hoja crema superpuesta. */
@Composable
fun EncabezadoLeccion(
    leccion: LeccionUi,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(AlturaBandaLeccion)) {
        IlustracionLeccion(
            nombre = leccion.ilustracionPortada,
            descripcion = leccion.descripcionPortada,
            categoria = leccion.categoria,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(SuperposicionHoja)
                .clip(FormaHojaLeccion)
                .background(ColorBgScreen),
        )
        IconButton(
            onClick = onVolver,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(ZonaTactilMinima),
            colors = IconButtonDefaults.iconButtonColors(containerColor = ColorSurfaceCard),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = ColorTextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Tarjeta de cierre `C-17`: invita a pasar a la pestaña Actividades. */
@Composable
fun TarjetaLlamadoAccion(
    texto: String,
    onVerActividades: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjetaLeccion)
            .background(Brush.verticalGradient(listOf(ColorGreen700, ColorGreen900)))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Ahora te toca a vos", style = TypeChip, color = Color.White.copy(alpha = 0.9f))
        Text(text = texto, style = TypeTituloTarjeta, color = Color.White)
        Button(
            onClick = onVerActividades,
            modifier = Modifier.fillMaxWidth().height(ZonaTactilMinima),
            shape = FormaPill,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ColorGreen700),
        ) {
            Text(text = "Ver actividades →", style = TypeLink, color = ColorGreen700)
        }
    }
}

/** Chip `C-14`: píldora de 28 dp con el nombre de la categoría. */
@Composable
fun ChipCategoria(
    categoria: CategoriaActividad,
    modifier: Modifier = Modifier,
    translucido: Boolean = false,
) {
    val fondo = colorFondoChipCategoria(categoria)
    Box(
        modifier = modifier
            .heightIn(min = AlturaChip)
            .clip(FormaPill)
            .background(if (translucido) fondo.copy(alpha = 0.88f) else fondo)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = etiquetaCategoria(categoria),
            style = TypeChip,
            color = colorTextoChipCategoria(categoria),
        )
    }
}
