package com.ecosmart.application.activity

import com.ecosmart.domain.model.PuntoVerde
import com.ecosmart.domain.repository.PuntoVerdeRepository
import com.ecosmart.domain.valueobject.Barrio
import javax.inject.Inject

/**
 * US7 — Puntos Verdes del barrio del usuario (RF-018, RF-020, RF-053
 * revisadas post-QA: la dirección del perfil pasó de texto libre a un
 * desplegable con los barrios de CABA, y ese valor es ahora la clave de
 * búsqueda — reemplaza el filtro por radio GPS de 3 km). Si no hay
 * ninguno, devuelve lista vacía; la UI es responsable de informarlo
 * explícitamente (RF-020).
 */
class ObtenerPuntosVerdesDelBarrio @Inject constructor(
    private val puntoVerdeRepository: PuntoVerdeRepository,
) {
    suspend operator fun invoke(barrio: Barrio): List<PuntoVerde> =
        puntoVerdeRepository.obtenerTodos().filter { it.barrio.equals(barrio.nombreVisible, ignoreCase = true) }
}
