package com.ecosmart.presentation.comun

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.infrastructure.session.SesionUsuario
import com.ecosmart.presentation.theme.EncabezadoEcoSmart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EncabezadoViewModel @Inject constructor(
    usuarioRepository: UsuarioRepository,
    sesionUsuario: SesionUsuario,
) : ViewModel() {
    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario.asStateFlow()

    init {
        viewModelScope.launch {
            val id = sesionUsuario.usuarioActualId.value ?: return@launch
            _nombreUsuario.value = usuarioRepository.buscarPorId(id)?.nombreUsuario.orEmpty()
        }
    }
}

/** Encabezado del mockup (RF-083) con la inicial del usuario en sesión (RF-062) y, opcionalmente, el saludo (RF-069). */
@Composable
fun EncabezadoConUsuario(
    modifier: Modifier = Modifier,
    conSaludo: Boolean = false,
    onAvatar: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null,
    viewModel: EncabezadoViewModel = hiltViewModel(),
) {
    val nombre by viewModel.nombreUsuario.collectAsState()
    EncabezadoEcoSmart(
        inicialUsuario = nombre.ifBlank { "U" },
        modifier = modifier,
        saludo = if (conSaludo && nombre.isNotBlank()) "Hola, $nombre" else null,
        onAvatar = onAvatar,
        onVolver = onVolver,
    )
}
