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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.app.R
import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.theme.AnilloProgreso
import com.ecosmart.presentation.theme.BotonPrincipal
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen100
import com.ecosmart.presentation.theme.ColorGreen700
import com.ecosmart.presentation.theme.ColorGreen900
import com.ecosmart.presentation.theme.ColorLeaf
import com.ecosmart.presentation.theme.ColorStateIdle
import com.ecosmart.presentation.theme.ColorSurfaceCard
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextSecondary
import com.ecosmart.presentation.theme.ColorTrack
import com.ecosmart.presentation.theme.ElevacionTarjeta
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.FormaPill
import com.ecosmart.presentation.theme.FormaTarjeta
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeCardTitle
import com.ecosmart.presentation.theme.TypeChip
import com.ecosmart.presentation.theme.TypeDisplay
import com.ecosmart.presentation.theme.TypeSection

/**
 * Detalle de actividad reconstruido según S-03 (especificacion-ui-app-eco.md §6):
 * Encabezado con flecha de volver (C-03), título y subtítulo con hoja,
 * Anillo de progreso animado (para CAMINAR) o indicador "0/1" (para RECICLAR/REUTILIZAR),
 * píldora de recompensa "+ N puntos", botón principal ("Realizarla" o "Iniciar caminata"),
 * sección "Cómo se registra" con tarjetas de información y tip, e ilustración inferior.
 */
@Composable
fun ActividadDetalleScreen(
    viewModel: ActividadDetalleViewModel = hiltViewModel(),
    onRealizarla: (ActividadId, CategoriaActividad) -> Unit,
    onVolver: () -> Unit,
    barraInferior: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val actividad = uiState.actividad

    if (uiState.cargando || actividad == null) {
        Scaffold(containerColor = ColorBgScreen) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGreen700)
            }
        }
        return
    }

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
            )
        },
        bottomBar = barraInferior,
    ) { padding ->
        ActividadDetalleContenido(
            actividad = actividad,
            onRealizarla = { onRealizarla(actividad.id, actividad.categoria) },
            progresoActual = uiState.pasosHoy,
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
    modifier: Modifier = Modifier,
) {
    val esCaminar = actividad.categoria == CategoriaActividad.CAMINAR

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = SpaceScreenX, end = SpaceScreenX, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // S03-D: Anillo de progreso para Caminar o Indicador 0/1
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (esCaminar) {
                    AnilloProgreso(
                        valor = progresoActual,
                        meta = metaPasos,
                        unidad = "pasos",
                    )
                } else {
                    IndicadorNoMedible()
                }
            }
        }

        // S03-E: Píldora de recompensa (+ N puntos)
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

        // S03-F: Botón de acción
        item {
            BotonPrincipal(
                texto = if (esCaminar) "¡Seguimiento en curso!" else "Realizarla",
                onClick = onRealizarla,
            )
        }

        // S03-G: Título "Cómo se registra"
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "Cómo se registra",
                    style = TypeSection,
                    color = ColorTextPrimary,
                )
            }
        }

        // S03-H: Fila informativa 1 (Sensores o Pasos a seguir)
        item {
            val textoInfo = if (esCaminar) {
                "Usa los sensores de tu dispositivo para detectar el movimiento y calcular la distancia."
            } else {
                actividad.pasosASeguir.ifBlank { "Seguí las instrucciones y verificá la actividad con una foto." }
            }

            TarjetaInformativaDetalle(
                icono = Icons.Filled.Info,
                texto = textoInfo,
            )
        }

        // S03-I: Fila informativa 2 (Tip o Resultado esperado)
        item {
            val textoTip = if (esCaminar) {
                "Podés salir a caminar, hacer una caminata o incluso moverte en tu día a día."
            } else {
                actividad.resultadoEsperado.ifBlank { "Completá la actividad para sumar puntos a tu perfil." }
            }

            TarjetaTipDetalle(textoTip = textoTip)
        }

        // S03-J: Ilustración de paisaje pegada abajo, ignorada por lector de pantalla (RF-UI-018)
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
private fun IndicadorNoMedible() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(ColorStateIdle),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "0 / 1",
                style = TypeDisplay.copy(fontSize = 28.sp),
                color = ColorTextPrimary,
            )
            Text(
                text = "completada",
                style = TypeBody,
                color = ColorTextSecondary,
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
            Text(
                text = texto,
                style = TypeBody,
                color = ColorTextPrimary,
            )
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
