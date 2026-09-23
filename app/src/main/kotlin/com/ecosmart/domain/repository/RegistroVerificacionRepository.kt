package com.ecosmart.domain.repository

import com.ecosmart.domain.model.RegistroVerificacion
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.domain.valueobject.UsuarioId
import java.time.LocalDate

/**
 * Puerto de dominio hacia la persistencia de [RegistroVerificacion]. La
 * implementación vive en `infrastructure/persistence/RegistroVerificacionRepositoryImpl`.
 */
interface RegistroVerificacionRepository {

    suspend fun guardar(registro: RegistroVerificacion)

    /** Base de `AplicarTopeDiario` (RF-032/RF-033/RF-035). */
    suspend fun contarAprobadosDelDia(usuarioId: UsuarioId, categoria: CategoriaActividad, fecha: LocalDate): Int

    /** Últimos N registros del usuario, más recientes primero (RF-041). */
    suspend fun ultimosN(usuarioId: UsuarioId, n: Int): List<RegistroVerificacion>

    /** Historial completo del usuario, más recientes primero (RF-042). */
    suspend fun todos(usuarioId: UsuarioId): List<RegistroVerificacion>

    /** Huellas perceptuales de fotos ya Aprobadas del usuario en esa categoría (RF-058/RF-059). */
    suspend fun huellasAprobadas(usuarioId: UsuarioId, categoria: CategoriaActividad): List<String>

    /** Suma de pasos de Caminata Aprobados en [fecha] (RF-070, corrección post-QA). */
    suspend fun sumaPasosDelDia(usuarioId: UsuarioId, fecha: LocalDate): Int
}
