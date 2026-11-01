package com.ecosmart.presentation.education

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecosmart.presentation.comun.EncabezadoViewModel
import com.ecosmart.presentation.comun.etiquetaCategoria
import com.ecosmart.presentation.theme.BotonPrincipal
import com.ecosmart.presentation.theme.ColorBgScreen
import com.ecosmart.presentation.theme.ColorGreen900
import com.ecosmart.presentation.theme.ColorTextPrimary
import com.ecosmart.presentation.theme.ColorTextoLectura
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import com.ecosmart.presentation.theme.SpaceGapTarjetaLeccion
import com.ecosmart.presentation.theme.SpaceScreenX
import com.ecosmart.presentation.theme.SpaceSeccionEducacion
import com.ecosmart.presentation.theme.TarjetaLeccion
import com.ecosmart.presentation.theme.TypeBody
import com.ecosmart.presentation.theme.TypeSection
import com.ecosmart.presentation.theme.TypeTituloEducacion

@Composable
fun EducacionScreen(
    onSeleccionarLeccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EducacionViewModel = hiltViewModel(),
    encabezadoViewModel: EncabezadoViewModel = hiltViewModel(),
    onVerPerfil: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null,
    barraInferior: @Composable () -> Unit = {},
) {
    val estado by viewModel.estado.collectAsState()
    val nombreUsuario by encabezadoViewModel.nombreUsuario.collectAsState()

    EducacionContenido(
        estado = estado,
        nombreUsuario = nombreUsuario,
        onSeleccionarLeccion = onSeleccionarLeccion,
        onReintentar = viewModel::reintentar,
        onVolver = onVolver,
        modifier = modifier,
        onVerPerfil = onVerPerfil,
        barraInferior = barraInferior,
    )
}

@Composable
fun EducacionContenido(
    estado: EstadoEducacion,
    nombreUsuario: String,
    onSeleccionarLeccion: (String) -> Unit,
    onReintentar: () -> Unit,
    onVolver: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onVerPerfil: (() -> Unit)? = null,
    barraInferior: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = ColorBgScreen,
        topBar = {
            EncabezadoEcoSmart(
                titulo = "Educación",
                subtitulo = "Aprendé, informate, hacé la diferencia",
                inicialUsuario = nombreUsuario.ifBlank { "U" },
                onAvatar = onVerPerfil,
                onVolver = onVolver,
                estiloTitulo = TypeTituloEducacion,
                colorTitulo = ColorGreen900,
                hojaInline = true,
            )
        },
        bottomBar = barraInferior,
    ) { padding ->
        when (estado) {
            EstadoEducacion.Cargando -> Unit
            is EstadoEducacion.Cargado -> CatalogoPorCategoria(
                lecciones = estado.lecciones,
                onSeleccionarLeccion = onSeleccionarLeccion,
                modifier = Modifier.padding(padding),
            )
            EstadoEducacion.Vacio -> MensajeEstado(
                mensaje = "Todavía no hay lecciones disponibles",
                modifier = Modifier.padding(padding),
            )
            EstadoEducacion.Error -> MensajeEstado(
                mensaje = "No pudimos cargar las lecciones",
                accion = "Reintentar",
                onAccion = onReintentar,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun CatalogoPorCategoria(
    lecciones: List<LeccionUi>,
    onSeleccionarLeccion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val secciones = agruparPorCategoria(lecciones)
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = SpaceScreenX, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(SpaceGapTarjetaLeccion),
    ) {
        secciones.forEachIndexed { indice, seccion ->
            item(key = "titulo-${seccion.categoria.name}") {
                Text(
                    text = etiquetaCategoria(seccion.categoria),
                    style = TypeSection,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(top = if (indice == 0) 0.dp else SpaceSeccionEducacion - SpaceGapTarjetaLeccion),
                )
            }
            items(seccion.lecciones, key = { it.id }) { leccion ->
                TarjetaLeccion(leccion = leccion, onClick = { onSeleccionarLeccion(leccion.id) })
            }
        }
    }
}

@Composable
private fun MensajeEstado(
    mensaje: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    onAccion: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpaceScreenX),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = mensaje, style = TypeBody, color = ColorTextoLectura, textAlign = TextAlign.Center)
        if (accion != null) {
            BotonPrincipal(texto = accion, onClick = onAccion, modifier = Modifier.fillMaxWidth())
        }
    }
}
