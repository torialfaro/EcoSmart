package com.ecosmart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosmart.application.profile.CalcularMetricasPerfil
import com.ecosmart.domain.model.Usuario
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.NivelUsuario
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.session.SesionUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class DiaSemanaRachaUi(
    val inicialDia: String,
    val fecha: LocalDate,
    val cumplido: Boolean,
    val esHoy: Boolean,
)

data class ActividadRealizadaResumenUi(
    val id: String,
    val titulo: String,
    val categoria: CategoriaActividad,
    val fecha: LocalDate,
    val etiquetaFecha: String,
)

data class PerfilUiState(
    val cargando: Boolean = true,
    val usuario: Usuario? = null,
    val puntosHistoricos: Int = 0,
    val nivel: NivelUsuario = NivelUsuario.SEMILLA,
    val rachaActual: Int = 0,
    val porcentajePorCategoria: Map<CategoriaActividad, Double> = emptyMap(),
    val pasosHoy: Int? = null,
    val diasSemanaRacha: List<DiaSemanaRachaUi> = emptyList(),
    val actividadesRealizadas: List<ActividadRealizadaResumenUi> = emptyList(),
    val totalActividadesRealizadas: Int = 0,
    val totalRecompensas: Int = 0,
    val mostrarConfirmacionEliminarCuenta: Boolean = false,
    val eliminandoCuenta: Boolean = false,
    val cuentaEliminada: Boolean = false,
    val mensajeErrorEliminarCuenta: String? = null,
)

/**
 * US11 + US3 — perfil consolidado (RF-068, corrección post-QA): datos de
 * cuenta completos (nombre, apellido, nombre de usuario, correo, barrio,
 * teléfono, categorías de interés — RF-007) más las métricas de actividad
 * (puntos, % por categoría, racha, nivel — RF-036 a RF-040). La edición en
 * sí vive en una pantalla separada (`PerfilEdicionScreen`), a la que esta
 * pantalla solo enlaza con un botón "Editar perfil".
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val calcularMetricasPerfil: CalcularMetricasPerfil,
    private val sesionUsuario: SesionUsuario,
    private val backendConfianzaClient: BackendConfianzaClient,
    private val registroVerificacionRepository: RegistroVerificacionRepository,
    private val actividadRepository: ActividadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        refrescar()
    }

    /**
     * Corrección post-QA (2026-09-23): esta pantalla queda en el back stack
     * debajo de `PerfilEdicionScreen`, así que al volver de editar se reusa
     * esta misma instancia de ViewModel — sin este refresco explícito, los
     * cambios guardados nunca se reflejaban acá (`init` solo corre una vez
     * por instancia). `PerfilScreen` llama a esto en cada `ON_RESUME`.
     */
    fun refrescar() {
        viewModelScope.launch {
            val usuarioId = sesionUsuario.usuarioActualId.value ?: return@launch
            val usuario = usuarioRepository.buscarPorId(usuarioId) ?: return@launch
            val metricas = calcularMetricasPerfil(usuarioId)

            val registros = registroVerificacionRepository.todos(usuarioId)
            val registrosAprobados = registros.filter { it.resultado == ResultadoVerificacion.APROBADO }

            val diasSemana = calcularDiasSemanaRacha(registrosAprobados.map { it.fecha }.toSet())
            val actividadesResumen = calcularActividadesResumen(registrosAprobados)

            _uiState.value = PerfilUiState(
                cargando = false,
                usuario = usuario,
                puntosHistoricos = metricas?.puntosHistoricos ?: 0,
                nivel = metricas?.nivel ?: usuario.nivel(),
                rachaActual = metricas?.rachaActual ?: 0,
                porcentajePorCategoria = metricas?.porcentajePorCategoria ?: emptyMap(),
                pasosHoy = metricas?.pasosHoy,
                diasSemanaRacha = diasSemana,
                actividadesRealizadas = actividadesResumen,
                totalActividadesRealizadas = registrosAprobados.size,
                totalRecompensas = 0,
            )
        }
    }

    private fun calcularDiasSemanaRacha(fechasAprobadas: Set<LocalDate>): List<DiaSemanaRachaUi> {
        val hoy = LocalDate.now()
        val lunes = hoy.with(DayOfWeek.MONDAY)
        val iniciales = listOf("L", "M", "M", "J", "V", "S", "D")

        return (0..6).map { i ->
            val fechaDia = lunes.plusDays(i.toLong())
            DiaSemanaRachaUi(
                inicialDia = iniciales[i],
                fecha = fechaDia,
                cumplido = fechasAprobadas.contains(fechaDia),
                esHoy = fechaDia == hoy,
            )
        }
    }

    private suspend fun calcularActividadesResumen(
        registrosAprobados: List<com.ecosmart.domain.model.RegistroVerificacion>,
    ): List<ActividadRealizadaResumenUi> {
        val hoy = LocalDate.now()
        val ayer = hoy.minusDays(1)
        val formateadorFecha = DateTimeFormatter.ofPattern("EEE d MMM", Locale("es", "AR"))

        val tresRecientes = registrosAprobados
            .sortedByDescending { it.fecha }
            .take(3)

        return tresRecientes.map { registro ->
            val actividad = actividadRepository.buscarPorId(registro.actividadId)
            val titulo = actividad?.descripcionCorta ?: registro.categoria.name.lowercase().replaceFirstChar { it.uppercase() }

            val etiquetaTiempo = when (registro.fecha) {
                hoy -> "Hoy"
                ayer -> "Ayer"
                else -> registro.fecha.format(formateadorFecha).replaceFirstChar { it.uppercase() }
            }

            val nombreCategoria = when (registro.categoria) {
                CategoriaActividad.RECICLAR -> "Reciclar"
                CategoriaActividad.REUTILIZAR -> "Reutilizar"
                CategoriaActividad.CAMINAR -> "Caminar"
            }

            ActividadRealizadaResumenUi(
                id = registro.id.valor,
                titulo = titulo,
                categoria = registro.categoria,
                fecha = registro.fecha,
                etiquetaFecha = "$nombreCategoria · $etiquetaTiempo",
            )
        }
    }

    /** Corrección post-QA: con la sesión persistida (RF-010), hace falta un botón explícito para salir. */
    fun cerrarSesion() {
        sesionUsuario.cerrarSesion()
    }

    fun solicitarEliminarCuenta() {
        _uiState.value = _uiState.value.copy(mostrarConfirmacionEliminarCuenta = true, mensajeErrorEliminarCuenta = null)
    }

    fun cancelarEliminarCuenta() {
        _uiState.value = _uiState.value.copy(mostrarConfirmacionEliminarCuenta = false, mensajeErrorEliminarCuenta = null)
    }

    /** RF-D012: inmediato y definitivo — la confirmación previa es la única salvaguarda. */
    fun confirmarEliminarCuenta() {
        _uiState.value = _uiState.value.copy(eliminandoCuenta = true, mensajeErrorEliminarCuenta = null)
        viewModelScope.launch {
            try {
                backendConfianzaClient.eliminarCuenta()
                sesionUsuario.cerrarSesion()
                _uiState.value = _uiState.value.copy(eliminandoCuenta = false, cuentaEliminada = true)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    eliminandoCuenta = false,
                    mensajeErrorEliminarCuenta = "No pudimos eliminar tu cuenta. Revisá tu conexión y volvé a intentar.",
                )
            }
        }
    }
}
