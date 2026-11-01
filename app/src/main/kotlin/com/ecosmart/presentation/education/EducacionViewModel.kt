package com.ecosmart.presentation.education

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EstadoEducacion {
    data object Cargando : EstadoEducacion()
    data class Cargado(val lecciones: List<LeccionUi>) : EstadoEducacion()
    data object Vacio : EstadoEducacion()
    data object Error : EstadoEducacion()
}

@HiltViewModel
class EducacionViewModel @Inject constructor(
    private val catalogo: CatalogoLecciones,
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoEducacion>(EstadoEducacion.Cargando)
    val estado: StateFlow<EstadoEducacion> = _estado.asStateFlow()

    init {
        viewModelScope.launch { _estado.value = catalogo.obtener().aEstado() }
    }

    fun reintentar() {
        _estado.value = EstadoEducacion.Cargando
        viewModelScope.launch { _estado.value = catalogo.recargar().aEstado() }
    }

    private fun ResultadoCatalogo.aEstado(): EstadoEducacion = when (this) {
        is ResultadoCatalogo.Cargado -> EstadoEducacion.Cargado(lecciones)
        ResultadoCatalogo.Vacio -> EstadoEducacion.Vacio
        ResultadoCatalogo.Error -> EstadoEducacion.Error
    }
}
