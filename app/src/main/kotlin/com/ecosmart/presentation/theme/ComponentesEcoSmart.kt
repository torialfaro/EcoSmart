package com.ecosmart.presentation.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosmart.app.R

/**
 * Consulta la preferencia del sistema de reducir movimiento (animator_duration_scale == 0).
 * RF-UI-019: omitir animaciones si el usuario tiene activa esta preferencia.
 */
@Composable
fun reducirMovimientoActivo(): Boolean {
    val context = LocalContext.current
    return try {
        val escala = android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f,
        )
        escala == 0f
    } catch (_: Exception) {
        false
    }
}

/**
 * Encabezado de pantalla (C-03 / especificacion-ui-app-eco.md §3):
 * Bloque alineado a la izquierda con margen space.screen-x (16dp).
 * - Título en type.title (o saludo en type.greeting)
 * - Subtítulo en type.body + icono de hoja color.leaf (12dp)
 * - Acción a la derecha opcional: avatar circular de 36dp
 * - Flecha de volver opcional (24pt con zona táctil mínima 48x48)
 */
@Composable
fun EncabezadoEcoSmart(
    modifier: Modifier = Modifier,
    titulo: String? = null,
    subtitulo: String? = null,
    saludo: String? = null,
    inicialUsuario: String = "U",
    mostrarAvatar: Boolean = true,
    mostrarHojaSubtitulo: Boolean = true,
    onAvatar: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpaceScreenX, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            onVolver?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(24.dp),
                        tint = ColorTextPrimary,
                    )
                }
                Box(modifier = Modifier.width(4.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (saludo != null) {
                    Text(
                        text = saludo,
                        style = TypeGreeting,
                        color = ColorTextPrimary,
                    )
                } else if (titulo != null) {
                    Text(
                        text = titulo,
                        style = TypeTitle,
                        color = ColorTextPrimary,
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_ecosmart_icono),
                            contentDescription = "EcoSmart",
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "ecosmart",
                            style = TypeTitle.copy(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
                            color = VerdeLogo,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                if (subtitulo != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = subtitulo,
                            style = TypeBody,
                            color = ColorTextSecondary,
                        )
                        if (mostrarHojaSubtitulo) {
                            Icon(
                                iconoHoja(),
                                contentDescription = null,
                                tint = ColorLeaf,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
            }
        }

        if (mostrarAvatar) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorGreen900)
                    .then(if (onAvatar != null) Modifier.clickable(onClick = onAvatar) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = inicialUsuario.take(1).uppercase(),
                    style = TypeCardTitle,
                    color = CremaTexto,
                )
            }
        }
    }
}

enum class PestanaInferior { INICIO, ACTIVIDADES, RECOMPENSAS, EDUCACION, PUNTOS_VERDES, PERFIL }

@Composable
private fun iconoHoja(): ImageVector = ImageVector.vectorResource(R.drawable.ic_hoja)

@Composable
private fun iconoCaminar(): ImageVector = ImageVector.vectorResource(R.drawable.ic_caminar)

/** Barra inferior Inicio / Actividades / Recompensas / Educación / Puntos Verdes / Perfil del mockup (RF-UI-002b, RF-UI-002c). Solo iconos, sin etiqueta de texto (feedback post-QA). */
@Composable
fun BarraInferiorEcoSmart(
    seleccion: PestanaInferior,
    onInicio: () -> Unit,
    onActividades: () -> Unit,
    onRecompensas: () -> Unit,
    onEducacion: () -> Unit,
    onPuntosVerdes: () -> Unit,
    onPerfil: () -> Unit,
) {
    NavigationBar(containerColor = FondoPagina, tonalElevation = 0.dp) {
        val colores = NavigationBarItemDefaults.colors(
            selectedIconColor = VerdeBoton,
            selectedTextColor = VerdeBoton,
            unselectedIconColor = VerdeMedio,
            unselectedTextColor = VerdeMedio,
            indicatorColor = Color.Transparent,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.INICIO,
            onClick = onInicio,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.ACTIVIDADES,
            onClick = onActividades,
            icon = { Icon(iconoCaminar(), contentDescription = "Actividades") },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.RECOMPENSAS,
            onClick = onRecompensas,
            icon = { Icon(Icons.Filled.Star, contentDescription = "Recompensas") },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.EDUCACION,
            onClick = onEducacion,
            icon = { Icon(iconoHoja(), contentDescription = "Educación") },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.PUNTOS_VERDES,
            onClick = onPuntosVerdes,
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Puntos Verdes") },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.PERFIL,
            onClick = onPerfil,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            colors = colores,
        )
    }
}

/** Tarjeta verde claro de esquinas muy redondeadas usada en todo el mockup. */
@Composable
fun TarjetaEcoSmart(
    modifier: Modifier = Modifier,
    color: Color = VerdeTarjeta,
    contenido: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { contenido() }
    }
}

/** Botón principal del mockup (C-07): ancho completo, alto 48 pt, píldora, fondo ColorGreen700, opacidad 50% al deshabilitarse. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    conFlecha: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .alpha(if (habilitado) 1f else 0.5f),
        enabled = habilitado,
        shape = FormaPill,
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorGreen700,
            contentColor = Color.White,
            disabledContainerColor = ColorGreen700,
            disabledContentColor = Color.White,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (conFlecha) "$texto →" else texto,
                style = TypeChip.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
        }
    }
}

/** Chip individual reutilizable de selección única (C-04). Zona táctil mínima 48x48 (RF-UI-016). */
@Composable
fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    val fondo = when {
        !habilitado -> ColorStateIdle
        seleccionado -> ColorGreen700
        else -> ColorGreen100
    }
    val colorTexto = when {
        !habilitado -> ColorTextSecondary
        seleccionado -> Color.White
        else -> ColorGreen700
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (habilitado) Modifier.clickable(onClick = onClick) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(FormaPill)
                .background(fondo)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = texto,
                style = TypeChip,
                color = colorTexto,
            )
        }
    }
}

/** Fila de chips sueltos con scroll horizontal si desborda (C-04). */
@Composable
fun <T> FilaChipsFiltro(
    opciones: List<T>,
    seleccionado: T,
    onSeleccionar: (T) -> Unit,
    etiqueta: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        opciones.forEach { opcion ->
            ChipFiltro(
                texto = etiqueta(opcion),
                seleccionado = opcion == seleccionado,
                onClick = { onSeleccionar(opcion) },
            )
        }
    }
}

/** Segmented control de 2 o 3 opciones a ancho completo (C-04). Zona táctil mínima 48x48 por opción (RF-UI-016). */
@Composable
fun <T> SegmentoEcoSmart(
    opciones: List<T>,
    seleccionado: T,
    onSeleccionar: (T) -> Unit,
    etiqueta: (T) -> String,
    modifier: Modifier = Modifier,
    estaHabilitada: (T) -> Boolean = { true },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(FormaPill)
            .background(ColorGreen100)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        opciones.forEach { opcion ->
            val esActivo = opcion == seleccionado
            val habilitada = estaHabilitada(opcion)
            val fondo = when {
                esActivo -> ColorGreen700
                !habilitada -> Color.Transparent
                else -> Color.Transparent
            }
            val colorTexto = when {
                esActivo -> Color.White
                !habilitada -> ColorTextSecondary
                else -> ColorGreen700
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(FormaPill)
                    .background(fondo)
                    .then(
                        if (habilitada) Modifier.clickable { onSeleccionar(opcion) } else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = etiqueta(opcion),
                    style = TypeChip,
                    color = colorTexto,
                )
            }
        }
    }
}

/** Barra de progreso lineal (C-05): alto 6 pt, riel color.track, relleno color.green.500. */
@Composable
fun BarraProgresoLineal(
    progreso: Float,
    modifier: Modifier = Modifier,
    alto: Dp = 6.dp,
    colorRiel: Color = ColorTrack,
    colorRelleno: Color = ColorGreen500,
    animada: Boolean = true,
) {
    val reducirMovimiento = reducirMovimientoActivo()
    val fraccionClampeada = progreso.coerceIn(0f, 1f)
    val fraccionAnimada by animateFloatAsState(
        targetValue = fraccionClampeada,
        animationSpec = if (animada && !reducirMovimiento) {
            tween(durationMillis = 600, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 0)
        },
        label = "BarraProgresoLinealAnimacion",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(FormaPill)
            .background(colorRiel),
    ) {
        if (fraccionAnimada > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccionAnimada)
                    .clip(FormaPill)
                    .background(colorRelleno),
            )
        }
    }
}

/** Estado visual de progreso para tarjetas de actividad (C-06 / data-model.md §4). */
enum class EstadoTarjetaActividad { IDLE, IN_PROGRESS, COMPLETED }

/**
 * Tarjeta de actividad (C-06): soporte para variante compacta (Inicio) y completa (Actividades)
 * en sus tres estados (idle / in_progress / completed), con fusión de accesibilidad (RF-UI-018).
 */
@Composable
fun TarjetaActividad(
    nombre: String,
    iconoRes: Int,
    progresoActual: Int,
    meta: Int,
    puntosBase: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    esCompacta: Boolean = false,
    esMedible: Boolean = meta > 1,
    unidad: String = "m",
) {
    val estado = when {
        progresoActual >= meta && meta > 0 -> EstadoTarjetaActividad.COMPLETED
        progresoActual > 0 -> EstadoTarjetaActividad.IN_PROGRESS
        else -> EstadoTarjetaActividad.IDLE
    }

    val fondo = when (estado) {
        EstadoTarjetaActividad.IDLE -> ColorStateIdle
        EstadoTarjetaActividad.IN_PROGRESS -> ColorStateProgress
        EstadoTarjetaActividad.COMPLETED -> ColorGreen100
    }

    val borde = if (estado == EstadoTarjetaActividad.IN_PROGRESS) {
        BorderStroke(1.dp, ColorStateProgressBorder)
    } else {
        null
    }

    val estadoTexto = when (estado) {
        EstadoTarjetaActividad.IDLE -> "sin iniciar"
        EstadoTarjetaActividad.IN_PROGRESS -> "en curso"
        EstadoTarjetaActividad.COMPLETED -> "completada"
    }

    val progresoTexto = if (esMedible) "$progresoActual / $meta $unidad" else "$progresoActual / $meta"
    val descripcionAccesibilidad = "$nombre, $progresoTexto, $puntosBase puntos, $estadoTexto"

    // Alto fijo por variante (feedback post-QA: las tarjetas no deben variar de alto entre sí).
    val anchoModifier = if (esCompacta) Modifier.width(150.dp).height(176.dp) else Modifier.fillMaxWidth().height(186.dp)

    Card(
        modifier = modifier
            .then(anchoModifier)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = descripcionAccesibilidad
            },
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = borde,
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxSize(),
            horizontalAlignment = if (esCompacta) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorSurfaceCard),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(iconoRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = nombre,
                style = TypeCardTitle,
                color = ColorTextPrimary,
                textAlign = if (esCompacta) TextAlign.Center else TextAlign.Start,
            )

            Text(
                text = progresoTexto,
                style = TypeMeta,
                color = ColorTextSecondary,
                textAlign = if (esCompacta) TextAlign.Center else TextAlign.Start,
            )

            if (esMedible && estado == EstadoTarjetaActividad.IN_PROGRESS) {
                val ratio = if (meta > 0) progresoActual.toFloat() / meta.toFloat() else 0f
                BarraProgresoLineal(
                    progreso = ratio,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            // Empuja el costo/estado al pie de la tarjeta para que todas midan igual (feedback post-QA).
            Box(modifier = Modifier.weight(1f))

            if (!esCompacta) {
                Text(
                    text = "+ $puntosBase pts",
                    style = TypeMeta.copy(fontWeight = FontWeight.SemiBold),
                    color = ColorGreen700,
                )
            }

            if (estado == EstadoTarjetaActividad.COMPLETED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = if (esCompacta) Alignment.Center else Alignment.BottomEnd,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ColorGreen700),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Enlace de sección (C-08): texto «Ver todas» o «Ver todos» alineado a la derecha, con área táctil ≥48x48. */
@Composable
fun EnlaceSeccion(
    texto: String = "Ver todas",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = texto,
            style = TypeLink,
            color = ColorGreen700,
        )
    }
}

/**
 * Tarjeta de contenido de video (C-09): esquinas radius.media (20dp), degradé oscuro inferior,
 * botón de reproducir circular de 32dp, título truncado con puntos suspensivos y metadato opcional.
 * Variantes: compacta (150 x 100 pt) y ancha (fillMaxWidth x 100 pt).
 */
@Composable
fun TarjetaVideo(
    titulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imagenDrawableRes: Int? = null,
    colorFondoFallback: Color = VerdeMedio,
    metadato: String? = null,
    esCompacta: Boolean = true,
) {
    val anchoModifier = if (esCompacta) Modifier.width(150.dp) else Modifier.fillMaxWidth()

    Card(
        modifier = modifier
            .then(anchoModifier)
            .height(100.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = if (metadato != null) "$titulo, $metadato" else titulo
            },
        shape = FormaMedia,
        colors = CardDefaults.cardColors(containerColor = colorFondoFallback),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imagenDrawableRes != null) {
                Image(
                    painter = painterResource(imagenDrawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorFondoFallback),
                )
            }

            // Degradé oscuro inferior para contraste mínimo 4.5:1 con texto blanco (RF-UI-015)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000)),
                            startY = 0f,
                        ),
                    ),
            )

            // Botón ▶ circular semitransparente de 32pt abajo a la izquierda
            Box(
                modifier = Modifier
                    .padding(start = 10.dp, bottom = 10.dp)
                    .size(32.dp)
                    .align(Alignment.BottomStart)
                    .clip(CircleShape)
                    .background(Color(0x80000000)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Título y metadatos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, end = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = titulo,
                    style = TypeCardTitle,
                    color = Color.White,
                    maxLines = if (metadato != null) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (metadato != null) {
                    Text(
                        text = metadato,
                        style = TypeMeta,
                        color = CremaTexto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de recompensa (C-10): imagen superior con relación ~4:3, esquinas radius.card,
 * título de hasta 2 líneas truncado con elipsis, fila inferior con costo («800 pts») y chevron ›.
 */
@Composable
fun TarjetaRecompensa(
    titulo: String,
    costoPuntos: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imagenDrawableRes: Int? = null,
    colorFondoFallback: Color = VerdeTarjeta,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "$titulo, $costoPuntos puntos"
            },
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(FormaTarjeta)
                    .background(colorFondoFallback),
            ) {
                if (imagenDrawableRes != null) {
                    Image(
                        painter = painterResource(imagenDrawableRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = titulo,
                    style = TypeCardTitle,
                    color = ColorTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${formatearPuntos(costoPuntos)} pts",
                        style = TypeMeta.copy(fontWeight = FontWeight.Bold),
                        color = ColorGreen700,
                    )

                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ColorTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de puntos (C-11): fondo color.green.100, radius.card (16dp), alto aprox. 80pt,
 * etiqueta «Tus puntos» (type.meta), valor numérico en type.points con icono de hoja color.leaf,
 * y chevron › a la derecha. Soporta ilustración o icono a la izquierda.
 */
@Composable
fun TarjetaPuntos(
    puntos: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Tus puntos",
    iconoIzquierda: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "$etiqueta, $puntos puntos"
            },
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = ColorGreen100),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (iconoIzquierda != null) {
                    iconoIzquierda()
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ColorSurfaceCard),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_ecosmart_icono),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = etiqueta,
                        style = TypeMeta,
                        color = ColorTextSecondary,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = formatearPuntos(puntos),
                            style = TypePoints,
                            color = ColorGreen900,
                        )
                        Icon(
                            iconoHoja(),
                            contentDescription = null,
                            tint = ColorLeaf,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ColorGreen900,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Formatea puntaje con punto como separador de miles (ej: 1.250). */
fun formatearPuntos(puntos: Int): String {
    val texto = puntos.toString()
    if (texto.length <= 3) return texto
    val prefix = texto.substring(0, texto.length - 3)
    val suffix = texto.substring(texto.length - 3)
    return "$prefix.$suffix"
}

/**
 * Anillo de progreso animado (S03-D / especificacion-ui-app-eco.md §6.1):
 * Diámetro ~160dp, trazo de 8dp, riel color.track, arco color.green.700.
 * Inicia a las 12 en punto (-90°) en sentido horario. Centro con valor y meta.
 * Accesibilidad (RF-UI-018): se lee como porcentaje ("X por ciento").
 */
@Composable
fun AnilloProgreso(
    valor: Int,
    meta: Int,
    modifier: Modifier = Modifier,
    unidad: String = "m",
    iconoCentro: @Composable (() -> Unit)? = null,
    animado: Boolean = true,
) {
    val reducirMovimiento = reducirMovimientoActivo()
    val porcentajeCalculado = if (meta > 0) ((valor.toFloat() / meta.toFloat()) * 100).toInt() else 0
    val fraccion = if (meta > 0) (valor.toFloat() / meta.toFloat()).coerceIn(0f, 1f) else 0f

    val fraccionAnimada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = if (animado && !reducirMovimiento) {
            tween(durationMillis = 600, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 0)
        },
        label = "AnilloProgresoAnimacion",
    )

    val descripcionAccesibilidad = "$porcentajeCalculado por ciento"

    Box(
        modifier = modifier
            .size(160.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = descripcionAccesibilidad
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 8.dp.toPx()

            // Riel de fondo
            drawArc(
                color = ColorTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
            )

            // Arco activo desde las 12 en punto (-90°) en sentido horario
            if (fraccionAnimada > 0f) {
                drawArc(
                    color = ColorGreen700,
                    startAngle = -90f,
                    sweepAngle = 360f * fraccionAnimada,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (iconoCentro != null) {
                iconoCentro()
            } else {
                Icon(
                    iconoCaminar(),
                    contentDescription = null,
                    tint = ColorGreen700,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = "${formatearPuntos(valor)} $unidad",
                style = TypeDisplay.copy(fontSize = 24.sp),
                color = ColorTextPrimary,
            )

            Text(
                text = "de ${formatearPuntos(meta)} $unidad",
                style = TypeBody,
                color = ColorTextSecondary,
            )
        }
    }
}
