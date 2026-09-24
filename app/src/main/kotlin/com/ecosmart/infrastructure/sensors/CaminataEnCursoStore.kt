package com.ecosmart.infrastructure.sensors

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS = "caminata_en_curso"

sealed class EstadoCaminata {
    data object Ninguna : EstadoCaminata()

    data class EnCurso(
        val usuarioId: String,
        val actividadId: String,
        val metaPasos: Int,
        val pasosLogrados: Int,
        val ultimoValorSensor: Int?,
        val fecha: String,
    ) : EstadoCaminata()

    data class Completada(val puntosOtorgados: Int, val pasosCaminados: Int, val metaPasos: Int) : EstadoCaminata()
}

/**
 * Estado persistente de la única caminata en segundo plano (RF-082): sobrevive
 * a que el usuario salga de la pantalla o se cierre el proceso. Vence a
 * medianoche (RF-057) y no admite una segunda caminata en paralelo.
 */
@Singleton
class CaminataEnCursoStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _estado = MutableStateFlow(leer())
    val estado: StateFlow<EstadoCaminata> = _estado.asStateFlow()

    /** Devuelve `false` si ya hay una caminata en curso (RF-082: una sola a la vez). */
    fun iniciar(usuarioId: String, actividadId: String, metaPasos: Int): Boolean {
        descartarSiVencio()
        if (_estado.value is EstadoCaminata.EnCurso) return false
        guardar(EstadoCaminata.EnCurso(usuarioId, actividadId, metaPasos, 0, null, LocalDate.now().toString()))
        return true
    }

    fun actualizarProgreso(pasosLogrados: Int, ultimoValorSensor: Int) {
        val actual = _estado.value as? EstadoCaminata.EnCurso ?: return
        guardar(actual.copy(pasosLogrados = pasosLogrados, ultimoValorSensor = ultimoValorSensor))
    }

    fun completar(puntosOtorgados: Int, pasosCaminados: Int, metaPasos: Int) =
        guardar(EstadoCaminata.Completada(puntosOtorgados, pasosCaminados, metaPasos))

    fun limpiar() = guardar(EstadoCaminata.Ninguna)

    /** Descarta una caminata iniciada un día anterior; `true` si la descartó. */
    fun descartarSiVencio(): Boolean {
        val actual = _estado.value as? EstadoCaminata.EnCurso ?: return false
        if (actual.fecha == LocalDate.now().toString()) return false
        limpiar()
        return true
    }

    private fun guardar(estado: EstadoCaminata) {
        val editor = prefs.edit().clear()
        when (estado) {
            EstadoCaminata.Ninguna -> Unit
            is EstadoCaminata.EnCurso -> {
                editor.putString("tipo", "EN_CURSO")
                    .putString("usuarioId", estado.usuarioId)
                    .putString("actividadId", estado.actividadId)
                    .putInt("meta", estado.metaPasos)
                    .putInt("logrados", estado.pasosLogrados)
                    .putString("fecha", estado.fecha)
                estado.ultimoValorSensor?.let { editor.putInt("ultimoValor", it) }
            }
            is EstadoCaminata.Completada -> editor.putString("tipo", "COMPLETADA")
                .putInt("puntos", estado.puntosOtorgados)
                .putInt("logrados", estado.pasosCaminados)
                .putInt("meta", estado.metaPasos)
        }
        editor.apply()
        _estado.value = estado
    }

    private fun leer(): EstadoCaminata = when (prefs.getString("tipo", null)) {
        "EN_CURSO" -> EstadoCaminata.EnCurso(
            usuarioId = prefs.getString("usuarioId", "").orEmpty(),
            actividadId = prefs.getString("actividadId", "").orEmpty(),
            metaPasos = prefs.getInt("meta", 0),
            pasosLogrados = prefs.getInt("logrados", 0),
            ultimoValorSensor = if (prefs.contains("ultimoValor")) prefs.getInt("ultimoValor", 0) else null,
            fecha = prefs.getString("fecha", "").orEmpty(),
        )
        "COMPLETADA" -> EstadoCaminata.Completada(
            puntosOtorgados = prefs.getInt("puntos", 0),
            pasosCaminados = prefs.getInt("logrados", 0),
            metaPasos = prefs.getInt("meta", 0),
        )
        else -> EstadoCaminata.Ninguna
    }
}
