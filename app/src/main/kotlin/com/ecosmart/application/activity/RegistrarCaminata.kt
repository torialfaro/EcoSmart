package com.ecosmart.application.activity

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import com.ecosmart.infrastructure.firebase.DispositivoIdProvider
import com.ecosmart.infrastructure.network.BackendConfianzaClient
import com.ecosmart.infrastructure.network.EcoGptClient
import com.ecosmart.infrastructure.network.SolicitudCaminarDto
import javax.inject.Inject
import java.time.LocalDate

/**
 * `pasosBase`/`pasosActuales` en vez de un `PodometroProvider` inyectado:
 * el sensor emite un `Flow` de lectura continua que `CaminataService`
 * (servicio en primer plano, RF-082) colecciona mientras la caminata está en
 * curso (research.md §1.1 — "el caso de uso guarda el valor base al iniciar
 * y compara contra el actual"); este caso de uso
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

/**
 * US8 — verificación de caminata vía podómetro (RF-021 a RF-024, RF-031, RF-048, RF-049).
 * RF-D014/RF-D015 (spec 002-firestore-datos-usuario): al llegar a la meta, el otorgamiento
 * de puntos pasa por `BackendConfianzaClient` (`accion = COMPLETAR`) en vez de escribirse
 * localmente vía `AplicarTopeDiario.registrarResultado` — Caminar no tiene tope diario
 * (`EstrategiaDePuntaje.CaminataPuntajeStrategy.topeDiario == null`), por eso tampoco se
 * consulta `AplicarTopeDiario.verificarDisponibilidad` acá.
 */
class RegistrarCaminata @Inject constructor(
    private val backendConfianzaClient: BackendConfianzaClient,
    private val dispositivoIdProvider: DispositivoIdProvider,
    private val ecoGptClient: EcoGptClient,
) {
    suspend operator fun invoke(datos: DatosCaminata): ResultadoCaminata {
        val pasosCaminados = (datos.pasosActuales - datos.pasosBase).coerceAtLeast(0)
        if (pasosCaminados < datos.metaPasos) {
            return ResultadoCaminata.MetaNoAlcanzada(pasosCaminados, datos.metaPasos)
        }

        val otorgado = backendConfianzaClient.otorgarPuntosCaminar(
            SolicitudCaminarDto(
                accion = "COMPLETAR",
                actividadId = datos.actividadId.valor,
                metaPasos = datos.metaPasos,
                pasosLogrados = pasosCaminados,
                dispositivoId = dispositivoIdProvider.dispositivoId,
            ),
        )

        val registro = RegistroVerificacion(
            id = RegistroVerificacionId(otorgado.id),
            usuarioId = datos.usuarioId,
            actividadId = datos.actividadId,
            categoria = CategoriaActividad.CAMINAR,
            fecha = LocalDate.now(),
            resultado = ResultadoVerificacion.APROBADO,
            motivoIA = null,
            puntosOtorgados = otorgado.puntosOtorgados,
            huellaImagen = null,
            pasosRegistrados = pasosCaminados,
        )
        return ResultadoCaminata.Aprobada(registro, pasosCaminados)
    }

    /**
     * RF-D015/RF-079 — "despertar" best-effort del mismo backend compartido con EcoGPT
     * (research.md §1), apenas se abre la pantalla de Caminar; mismo patrón que
     * `VerificarFotoConIA.precalentarBackend()`, nunca propaga errores.
     */
    suspend fun precalentarBackend() {
        try {
            ecoGptClient.ping()
        } catch (_: Exception) {
            // best-effort, sin acción.
        }
    }
}

