package com.ecosmart.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.activity.ObtenerPuntosVerdesDelBarrio
import com.ecosmart.domain.model.PuntoVerde
import com.ecosmart.domain.repository.PuntoVerdeRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PuntosVerdesUiState(
    val cargando: Boolean = true,
    val puntos: List<PuntoVerde> = emptyList(),
    val barrioNoConfigurado: Boolean = false,
)

/**
 * US7 — Puntos Verdes del barrio del usuario (RF-018, RF-020, RF-053
 * revisadas post-QA: ya no busca por GPS/radio, sino por el barrio elegido
 * en el perfil).
 */
@HiltViewModel
class PuntosVerdesViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val puntoVerdeRepository: PuntoVerdeRepository,
    private val sesionUsuario: SesionUsuario,
    private val obtenerPuntosVerdesDelBarrio: ObtenerPuntosVerdesDelBarrio,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PuntosVerdesUiState())
    val uiState: StateFlow<PuntosVerdesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            puntoVerdeRepository.sembrarSiEstaVacio()
            val usuarioId = sesionUsuario.usuarioActualId.value ?: return@launch
            val usuario = usuarioRepository.buscarPorId(usuarioId) ?: return@launch
            val barrio = usuario.barrio
            if (barrio == null) {
                _uiState.value = PuntosVerdesUiState(cargando = false, barrioNoConfigurado = true)
                return@launch
            }
            _uiState.value = PuntosVerdesUiState(cargando = false, puntos = obtenerPuntosVerdesDelBarrio(barrio))
        }
    }
}

/** US7 — listado de Puntos Verdes del barrio del usuario (RF-018, RF-020). */
@Composable
fun PuntosVerdesScreen(viewModel: PuntosVerdesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        when {
            uiState.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.barrioNoConfigurado -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Configurá tu barrio en el perfil para ver los Puntos Verdes cercanos a vos.")
            }

            uiState.puntos.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                // RF-020: informar explícitamente que no hay puntos en el barrio.
                Text("No encontramos Puntos Verdes en tu barrio todavía.")
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.puntos, key = { it.id.valor }) { punto ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = punto.nombre, style = MaterialTheme.typography.titleLarge)
                            Text(text = punto.direccion, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
