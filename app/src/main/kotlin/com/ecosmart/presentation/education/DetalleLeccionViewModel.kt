package com.ecosmart.presentation.education

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EstadoDetalleLeccion {
    data object Cargando : EstadoDetalleLeccion()
    data class Cargada(val leccion: LeccionUi) : EstadoDetalleLeccion()
    data object NoEncontrada : EstadoDetalleLeccion()
}

@HiltViewModel
class DetalleLeccionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogo: CatalogoLecciones,
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoDetalleLeccion>(EstadoDetalleLeccion.Cargando)
    val estado: StateFlow<EstadoDetalleLeccion> = _estado.asStateFlow()

    init {
        val id = savedStateHandle.get<String>("id")
        viewModelScope.launch {
            val leccion = (catalogo.obtener() as? ResultadoCatalogo.Cargado)
                ?.lecciones
                ?.firstOrNull { it.id == id }
            _estado.value = if (leccion != null) EstadoDetalleLeccion.Cargada(leccion) else EstadoDetalleLeccion.NoEncontrada
        }
    }
}
