package com.ecosmart.application.activity

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import javax.inject.Inject

/**
 * `pasosBase`/`pasosActuales` en vez de un `PodometroProvider` inyectado:
 * el sensor emite un `Flow` de lectura continua que la ViewModel colecciona
 * durante la sesión de caminata (research.md §1.1 — "el caso de uso guarda
 * el valor base al iniciar y compara contra el actual"); este caso de uso
 * solo recibe los dos valores ya resueltos, lo que lo mantiene puro y
 * testeable sin mockear un sensor.
 */
data class DatosCaminata(
    val usuarioId: UsuarioId,
    val actividadId: ActividadId,
    val metaPasos: Int,
    val pasosBase: Int,
    val pasosActuales: Int,
)

sealed class ResultadoCaminata {
    data class Aprobada(val registro: RegistroVerificacion, val pasosCaminados: Int) : ResultadoCaminata()
    data class MetaNoAlcanzada(val pasosCaminados: Int, val metaPasos: Int) : ResultadoCaminata()
}

/** US8 — verificación de caminata vía podómetro (RF-021 a RF-024, RF-031, RF-048, RF-049). */
class RegistrarCaminata @Inject constructor(
    private val aplicarTopeDiario: AplicarTopeDiario,
) {
    suspend operator fun invoke(datos: DatosCaminata): ResultadoCaminata {
        val pasosCaminados = (datos.pasosActuales - datos.pasosBase).coerceAtLeast(0)
        if (pasosCaminados < datos.metaPasos) {
            return ResultadoCaminata.MetaNoAlcanzada(pasosCaminados, datos.metaPasos)
        }

        // Caminar no tiene tope diario (EstrategiaDePuntaje.CaminataPuntajeStrategy.topeDiario == null),
        // por eso no se consulta AplicarTopeDiario.verificarDisponibilidad acá.
        val registro = aplicarTopeDiario.registrarResultado(
            usuarioId = datos.usuarioId,
            actividadId = datos.actividadId,
            categoria = CategoriaActividad.CAMINAR,
            resultado = ResultadoVerificacion.APROBADO,
            motivoIA = null,
            huellaImagen = null,
            pasosRegistrados = pasosCaminados,
            cantidadParaPuntaje = pasosCaminados,
        )
        return ResultadoCaminata.Aprobada(registro, pasosCaminados)
    }
}
