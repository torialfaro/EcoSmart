package com.ecosmart.infrastructure.network

import com.ecosmart.domain.valueobject.ResultadoVerificacion

/**
 * Factory simple (plan.md § Patrones de Diseño Aplicados): centraliza en
 * un solo lugar la interpretación del veredicto crudo de EcoGPT
 * (`contracts/openapi.yaml`), para que la infraestructura no "filtre"
 * strings crudos de la IA hacia el dominio.
 */
object EcoGptResponseMapper {
    fun desde(respuesta: VeredictoEcoGptDto): ResultadoVerificacion = when (respuesta.veredicto) {
        "APROBADO" -> ResultadoVerificacion.APROBADO
        "RECHAZADO" -> ResultadoVerificacion.RECHAZADO
        else -> ResultadoVerificacion.INDETERMINADO
    }
}
