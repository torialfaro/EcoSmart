@file:Suppress("MatchingDeclarationName")

package com.ecosmart.presentation.education

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.presentation.theme.TarjetaLeccion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LeccionesInicioViewModel @Inject constructor(
    private val catalogo: CatalogoLecciones,
) : ViewModel() {

    private val _lecciones = MutableStateFlow<List<LeccionUi>>(emptyList())
    val lecciones: StateFlow<List<LeccionUi>> = _lecciones.asStateFlow()

    init {
        viewModelScope.launch {
            _lecciones.value = (catalogo.obtener() as? ResultadoCatalogo.Cargado)
                ?.lecciones
                ?.let(::leccionesParaInicio)
                .orEmpty()
        }
    }
}

/** Fila horizontal de tarjetas compactas; no dibuja nada si no hay lecciones. */
@Composable
fun TarjetasEducativasInicio(
    lecciones: List<LeccionUi>,
    onVerLeccion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (lecciones.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        lecciones.forEach { leccion ->
            TarjetaLeccion(
                leccion = leccion,
                onClick = { onVerLeccion(leccion.id) },
                compacta = true,
            )
        }
    }
}
