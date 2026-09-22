package com.ecosmart.application.activity

import com.ecosmart.domain.model.EstrategiaDePuntaje
import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.repository.RegistroVerificacionRepository
import com.ecosmart.domain.repository.UsuarioRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.RegistroVerificacionId
import com.ecosmart.domain.valueobject.ResultadoVerificacion
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate
import javax.inject.Inject

/** RF-057: `bloqueado`/`usadas`/`tope` de HOY, hora local del dispositivo. `tope = null` significa sin tope. */
data class DisponibilidadTope(val bloqueado: Boolean, val usadas: Int, val tope: Int?)

/**
 * US10 — aplica los topes diarios por categoría (RF-032 a RF-035, RF-056,
 * RF-057) y centraliza cómo se persiste el resultado de una verificación
 * ya resuelta (Caminata o Foto), calculando puntos vía [EstrategiaDePuntaje]
 * y actualizando el puntaje/racha del usuario cuando corresponde. Por eso
 * lo usan tanto `RegistrarCaminata` como `VerificarFotoConIA` (T072/T075).
 */
class AplicarTopeDiario @Inject constructor(
    private val registroVerificacionRepository: RegistroVerificacionRepository,
    private val usuarioRepository: UsuarioRepository,
) {
    /** Edge Case "Tope diario superado" — se consulta ANTES de permitir iniciar el formulario. */
    suspend fun verificarDisponibilidad(usuarioId: UsuarioId, categoria: CategoriaActividad): DisponibilidadTope {
        val tope = EstrategiaDePuntaje.para(categoria).topeDiario
            ?: return DisponibilidadTope(bloqueado = false, usadas = 0, tope = null)
        val usadas = registroVerificacionRepository.contarAprobadosDelDia(usuarioId, categoria, LocalDate.now())
        return DisponibilidadTope(bloqueado = usadas >= tope, usadas = usadas, tope = tope)
    }

    /**
     * RF-034/RF-035/RF-050 — registra el resultado de una verificación ya
     * resuelta (Aprobado/Rechazado/Indeterminado/lo que corresponda),
     * calcula los puntos si `resultado == APROBADO` (vía
     * [EstrategiaDePuntaje]) y, en ese caso, suma el puntaje y actualiza la
     * racha del usuario. Un resultado no-Aprobado nunca otorga puntos ni
     * toca al usuario (RF-050: Rechazado/Indeterminado no consumen tope).
     */
    suspend fun registrarResultado(
        usuarioId: UsuarioId,
        actividadId: ActividadId,
        categoria: CategoriaActividad,
        resultado: ResultadoVerificacion,
        motivoIA: String?,
        huellaImagen: String?,
        pasosRegistrados: Int?,
        cantidadParaPuntaje: Int,
    ): RegistroVerificacion {
        val puntos = if (resultado == ResultadoVerificacion.APROBADO) {
            EstrategiaDePuntaje.para(categoria).calcularPuntos(cantidadParaPuntaje)
        } else {
            0
        }

        val hoy = LocalDate.now()
        val registro = RegistroVerificacion(
            id = RegistroVerificacionId.nuevo(),
            usuarioId = usuarioId,
            actividadId = actividadId,
            categoria = categoria,
            fecha = hoy,
            resultado = resultado,
            motivoIA = motivoIA,
            puntosOtorgados = puntos,
            huellaImagen = huellaImagen,
            pasosRegistrados = pasosRegistrados,
        )
        registroVerificacionRepository.guardar(registro)

        if (resultado == ResultadoVerificacion.APROBADO) {
            val usuario = usuarioRepository.buscarPorId(usuarioId)
            if (usuario != null) {
                val actualizado = usuario.sumarPuntos(puntos).registrarActividadAprobadaHoy(hoy)
                usuarioRepository.guardar(actualizado)
            }
        }

        return registro
    }
}
