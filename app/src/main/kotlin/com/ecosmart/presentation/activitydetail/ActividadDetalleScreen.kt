package com.ecosmart.presentation.activitydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ecosmart.app.R
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.education.TipLeccionUi
import com.ecosmart.presentation.estadodiario.EstadoActividadUi
import com.ecosmart.presentation.theme.AnilloProgreso
import com.ecosmart.presentation.theme.BotonLlamadoAccion
import com.ecosmart.presentation.theme.BotonPrincipal
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen100
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorGreen900
import com.ecosmart.presentation.theme.ColorLeaf
import com.ecosmart.presentation.theme.ColorSurfaceCard
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextSecondary
import com.ecosmart.presentation.theme.ColorTrack
import com.ecosmart.presentation.theme.ElevacionTarjeta
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaPill
import com.ecosmart.presentation.theme.FormaTarjeta
import com.ecosmart.presentation.theme.PanelActividad
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TarjetaEnlaceLeccion
import com.ecosmart.presentation.theme.TarjetaPasos
import com.ecosmart.presentation.theme.TarjetaTip
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeChip
import com.ecosmart.presentation.theme.TypeMeta
import com.ecosmart.presentation.theme.TypeSection
import com.ecosmart.presentation.theme.TypeTitle

private const val ROTULO_TIP_FOTO = "Qué tiene que verse en la foto"
private const val NOTA_FOTO = "Sacás una foto, la describís y listo"
private const val MENSAJE_COMPLETADA = "¡Listo por hoy! Volvé mañana para sumar otra vez."
private const val MENSAJE_NO_DISPONIBLE = "Hoy ya sumaste lo máximo de esta categoría. Volvé mañana para sumar otra vez."
private val EstiloTituloDetalle = TypeTitle.copy(fontSize = 22.sp, lineHeight = 28.sp)

/**
 * Detalle de actividad. Reciclar/Reutilizar siguen el diseño de spec 005 (RF-IA-011 a 018): encabezado, panel con
 * logo, botón principal o mensaje del día, "Cómo se registra" con pasos numerados, tip y "Aprendé primero".
 * Caminar (medible) conserva su diseño con anillo de progreso y botón de seguimiento (S-03).
 */
@Composable
fun ActividadDetalleScreen(
    viewModel: ActividadDetalleViewModel = hiltViewModel(),
    onRealizarla: (ActividadId, CategoriaActividad) -> Unit,
    onVolver: () -> Unit,
    onVerLeccion: (String) -> Unit = {},
    barraInferior: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val actividad = uiState.actividad

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) viewModel.refrescar()
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    if (uiState.cargando || actividad == null) {
        Scaffold(containerColor = ColorBgScreen) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGreen700)
            }
        }
        return
    }

    val esCaminar = actividad.categoria == CategoriaActividad.CAMINAR
    val subtitulo = when (actividad.categoria) {
        CategoriaActividad.CAMINAR -> "Sumá distancia, cuidá el planeta"
        CategoriaActividad.RECICLAR -> "Separá tus residuos, generá impacto"
        CategoriaActividad.REUTILIZAR -> "Dale nueva vida a tus objetos"
    }

    Scaffold(
        containerColor = ColorBgScreen,
        topBar = {
            EncabezadoEcoSmart(
                titulo = actividad.descripcionCorta,
                subtitulo = subtitulo,
                onVolver = onVolver,
                mostrarAvatar = false,
                mostrarHojaSubtitulo = true,
                estiloTitulo = if (esCaminar) TypeTitle else EstiloTituloDetalle,
                hojaInline = !esCaminar,
            )
        },
        bottomBar = barraInferior,
    ) { padding ->
        ActividadDetalleContenido(
            actividad = actividad,
            onRealizarla = { onRealizarla(actividad.id, actividad.categoria) },
            progresoActual = uiState.pasosHoy,
            estado = uiState.estado,
            pasos = uiState.pasos,
            tip = uiState.tip,
            leccionRelacionadaId = uiState.leccionRelacionadaId,
            onVerLeccion = onVerLeccion,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun ActividadDetalleContenido(
    actividad: Actividad,
    onRealizarla: () -> Unit,
    progresoActual: Int = 0,
    metaPasos: Int = com.ecosmart.presentation.home.META_PASOS_POR_DEFECTO_DISPLAY,
    estado: EstadoActividadUi = EstadoActividadUi.PARA_HACER,
    pasos: List<String> = emptyList(),
    tip: String? = null,
    leccionRelacionadaId: String? = null,
    onVerLeccion: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (actividad.categoria == CategoriaActividad.CAMINAR) {
        ContenidoMedible(actividad, onRealizarla, progresoActual, metaPasos, modifier)
    } else {
        ContenidoNoMedible(
            actividad = actividad,
            onRealizarla = onRealizarla,
            estado = estado,
            pasos = pasos,
            tip = tip,
            leccionRelacionadaId = leccionRelacionadaId,
            onVerLeccion = onVerLeccion,
            modifier = modifier,
        )
    }
}

/** Reciclar y Reutilizar (RF-IA-018): el orden de los bloques es el del spec 005. */
@Composable
private fun ContenidoNoMedible(
    actividad: Actividad,
    onRealizarla: () -> Unit,
    estado: EstadoActividadUi,
    pasos: List<String>,
    tip: String?,
    leccionRelacionadaId: String?,
    onVerLeccion: (String) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = SpaceScreenX, end = SpaceScreenX, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PanelActividad(
                categoria = actividad.categoria,
                puntos = actividad.puntosBase,
                completada = estado == EstadoActividadUi.COMPLETADA_HOY,
            )
        }
        item { BloquePrincipal(estado, actividad.puntosBase, onRealizarla) }
        item { Text(text = "Cómo se registra", style = TypeSection, color = ColorTextPrimary) }
        if (pasos.isNotEmpty()) item { TarjetaPasos(pasos = pasos) }
        if (tip != null) item { TarjetaTip(tip = TipLeccionUi(titulo = ROTULO_TIP_FOTO, texto = tip)) }
        if (leccionRelacionadaId != null) {
            item { TarjetaEnlaceLeccion(onClick = { onVerLeccion(leccionRelacionadaId) }) }
        }
    }
}

@Composable
private fun BloquePrincipal(estado: EstadoActividadUi, puntos: Int, onRealizarla: () -> Unit) {
    when (estado) {
        EstadoActividadUi.PARA_HACER -> Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BotonLlamadoAccion(
                estado = estado,
                onClick = onRealizarla,
                modifier = Modifier.fillMaxWidth(),
                etiquetaParaHacer = "¡Hacela ahora y sumá $puntos puntos!",
                icono = painterResource(R.drawable.ic_camara),
            )
            Text(text = NOTA_FOTO, style = TypeMeta, color = ColorTextSecondary, textAlign = TextAlign.Center)
        }
        EstadoActividadUi.COMPLETADA_HOY -> MensajeDeEstado(MENSAJE_COMPLETADA)
        EstadoActividadUi.NO_DISPONIBLE_HOY -> MensajeDeEstado(MENSAJE_NO_DISPONIBLE)
    }
}

@Composable
private fun MensajeDeEstado(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(ColorGreen100)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            style = TypeSection.copy(fontWeight = FontWeight.SemiBold),
            color = ColorGreen900,
            textAlign = TextAlign.Center,
        )
    }
}

/** Caminar (S-03): se conserva el diseño anterior a spec 005 (D-7). */
@Composable
private fun ContenidoMedible(
    actividad: Actividad,
    onRealizarla: () -> Unit,
    progresoActual: Int,
    metaPasos: Int,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = SpaceScreenX, end = SpaceScreenX, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnilloProgreso(valor = progresoActual, meta = metaPasos, unidad = "pasos")
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(FormaPill)
                    .background(ColorGreen100),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_hoja),
                        contentDescription = null,
                        tint = ColorLeaf,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "+ ${actividad.puntosBase} puntos",
                        style = TypeChip.copy(fontWeight = FontWeight.Bold),
                        color = ColorGreen900,
                    )
                }
            }
        }

        item { BotonPrincipal(texto = "¡Seguimiento en curso!", onClick = onRealizarla) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(text = "Cómo se registra", style = TypeSection, color = ColorTextPrimary)
            }
        }

        item {
            TarjetaInformativaDetalle(
                icono = Icons.Filled.Info,
                texto = "Usa los sensores de tu dispositivo para detectar el movimiento y calcular la distancia.",
            )
        }

        item {
            TarjetaTipDetalle(
                textoTip = "Podés salir a caminar, hacer una caminata o incluso moverte en tu día a día.",
            )
        }

        // Ilustración decorativa: el lector de pantalla la ignora (RF-UI-018).
        item {
            Image(
                painter = painterResource(R.drawable.il_paisaje_colinas),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clearAndSetSemantics {},
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

@Composable
private fun TarjetaInformativaDetalle(
    icono: ImageVector,
    texto: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
        border = BorderStroke(1.dp, ColorTrack),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorGreen100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = ColorGreen700,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(text = texto, style = TypeBody, color = ColorTextPrimary)
        }
    }
}

@Composable
private fun TarjetaTipDetalle(
    textoTip: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevacionTarjeta),
        border = BorderStroke(1.dp, ColorTrack),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorGreen100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_hoja),
                    contentDescription = null,
                    tint = ColorLeaf,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Tip: ")
                    }
                    append(textoTip)
                },
                style = TypeBody,
                color = ColorTextPrimary,
            )
        }
    }
}
