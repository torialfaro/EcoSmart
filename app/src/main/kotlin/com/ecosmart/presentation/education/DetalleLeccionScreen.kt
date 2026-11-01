package com.ecosmart.presentation.education

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.presentation.theme.ChipCategoria
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextoLectura
import com.ecosmart.presentation.theme.ColorTextoSecundarioFuerte
import com.ecosmart.presentation.theme.ConectorPaso
import com.ecosmart.presentation.theme.EncabezadoLeccion
import com.ecosmart.presentation.theme.PasoIlustrado
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.TarjetaLlamadoAccion
import com.ecosmart.presentation.theme.TarjetaTip
import com.ecosmart.presentation.theme.TypeIntroLeccion
import com.ecosmart.presentation.theme.TypeResumen
import com.ecosmart.presentation.theme.TypeTituloLeccion

const val TAG_LISTA_PASOS = "ListaPasos"
private const val TEXTO_CIERRE_GENERAL = "Poné en práctica lo que aprendiste"

@Composable
fun DetalleLeccionScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleLeccionViewModel = hiltViewModel(),
    barraInferior: @Composable () -> Unit = {},
    onVerActividades: () -> Unit = {},
) {
    val estado by viewModel.estado.collectAsState()
    DetalleLeccionContenido(
        estado = estado,
        onVolver = onVolver,
        modifier = modifier,
        barraInferior = barraInferior,
        onVerActividades = onVerActividades,
    )
}

@Composable
fun DetalleLeccionContenido(
    estado: EstadoDetalleLeccion,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    barraInferior: @Composable () -> Unit = {},
    onVerActividades: () -> Unit = {},
) {
    if (estado is EstadoDetalleLeccion.NoEncontrada) {
        LaunchedEffect(Unit) { onVolver() }
    }

    Scaffold(
        modifier = modifier,
        containerColor = ColorBgScreen,
        bottomBar = barraInferior,
    ) { padding ->
        if (estado is EstadoDetalleLeccion.Cargada) {
            val leccion = estado.leccion
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                EncabezadoLeccion(leccion = leccion, onVolver = onVolver)
                Column(
                    modifier = Modifier.padding(horizontal = SpaceScreenX),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        )
                    }
                    Text(text = leccion.titulo, style = TypeTituloLeccion, color = ColorTextPrimary)
                    Text(text = leccion.introduccion, style = TypeIntroLeccion, color = ColorTextoLectura)
                    ListaPasos(leccion)
                    leccion.tip?.let { TarjetaTip(tip = it) }
                    TarjetaLlamadoAccion(
                        texto = leccion.textoCierre ?: TEXTO_CIERRE_GENERAL,
                        onVerActividades = onVerActividades,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ListaPasos(leccion: LeccionUi) {
    val total = leccion.cantidadPasos
    Column(
        modifier = Modifier
            .testTag(TAG_LISTA_PASOS)
            .semantics { collectionInfo = CollectionInfo(rowCount = total, columnCount = 1) },
    ) {
        leccion.pasos.forEachIndexed { indice, paso ->
            if (indice > 0) ConectorPaso()
            PasoIlustrado(
                paso = paso,
                numero = indice + 1,
                total = total,
                categoria = leccion.categoria,
                modifier = Modifier.semantics {
                    collectionItemInfo = CollectionItemInfo(indice, 1, 0, 1)
                },
            )
        }
    }
}
