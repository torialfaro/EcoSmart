package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.PuntoVerde
import com.ecosmart.domain.repository.PuntoVerdeRepository
import com.ecosmart.domain.valueobject.PuntoVerdeId
import com.ecosmart.infrastructure.persistence.room.puntoverde.PuntoVerdeDao
import com.ecosmart.infrastructure.persistence.room.puntoverde.PuntoVerdeMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuntoVerdeRepositoryImpl @Inject constructor(
    private val puntoVerdeDao: PuntoVerdeDao,
) : PuntoVerdeRepository {

    override suspend fun obtenerTodos(): List<PuntoVerde> =
        puntoVerdeDao.obtenerTodos().map(PuntoVerdeMapper::toDomain)

    override suspend fun reemplazarTodos(puntos: List<PuntoVerde>) {
        puntoVerdeDao.reemplazarTodos(puntos.map(PuntoVerdeMapper::toRoomEntity))
    }

    override suspend fun sembrarSiEstaVacio() {
        if (puntoVerdeDao.contar() > 0) return
        puntoVerdeDao.insertarTodos(PUNTOS_DE_EJEMPLO.map(PuntoVerdeMapper::toRoomEntity))
    }

    private companion object {
        /**
         * Coordenadas APROXIMADAS del centro de cada uno de los 48 barrios de
         * CABA (corrección post-QA 2026-09-22, RF-066): dataset de EJEMPLO
         * para que la búsqueda por barrio (RF-063) y el mapa (RF-065) tengan
         * al menos 1 resultado en cualquier barrio elegido — NO es la fuente
         * oficial de CABA (RF-019), que requeriría una integración real con
         * datos.buenosaires.gob.ar todavía no implementada (research.md §5).
         */
        val COORDENADAS_POR_BARRIO: List<Triple<String, Double, Double>> = listOf(
            Triple("Agronomía", -34.5925, -58.4875),
            Triple("Almagro", -34.6083, -58.4205),
            Triple("Balvanera", -34.6087, -58.4055),
            Triple("Barracas", -34.6420, -58.3820),
            Triple("Belgrano", -34.5615, -58.4562),
            Triple("Boedo", -34.6297, -58.4187),
            Triple("Caballito", -34.6187, -58.4358),
            Triple("Chacarita", -34.5875, -58.4530),
            Triple("Coghlan", -34.5605, -58.4665),
            Triple("Colegiales", -34.5745, -58.4495),
            Triple("Constitución", -34.6265, -58.3810),
            Triple("Flores", -34.6285, -58.4635),
            Triple("Floresta", -34.6295, -58.4805),
            Triple("La Boca", -34.6345, -58.3630),
            Triple("La Paternal", -34.5985, -58.4700),
            Triple("Liniers", -34.6435, -58.5225),
            Triple("Mataderos", -34.6595, -58.5075),
            Triple("Monte Castro", -34.6180, -58.5050),
            Triple("Monserrat", -34.6110, -58.3810),
            Triple("Nueva Pompeya", -34.6485, -58.4180),
            Triple("Núñez", -34.5450, -58.4640),
            Triple("Palermo", -34.5787, -58.4211),
            Triple("Parque Avellaneda", -34.6415, -58.4740),
            Triple("Parque Chacabuco", -34.6357, -58.4351),
            Triple("Parque Chas", -34.5920, -58.4720),
            Triple("Parque Patricios", -34.6355, -58.4025),
            Triple("Puerto Madero", -34.6090, -58.3630),
            Triple("Recoleta", -34.5875, -58.3927),
            Triple("Retiro", -34.5920, -58.3745),
            Triple("Saavedra", -34.5540, -58.4880),
            Triple("San Cristóbal", -34.6220, -58.4000),
            Triple("San Nicolás", -34.6035, -58.3810),
            Triple("San Telmo", -34.6210, -58.3720),
            Triple("Vélez Sarsfield", -34.6320, -58.4920),
            Triple("Versalles", -34.6280, -58.5230),
            Triple("Villa Crespo", -34.5990, -58.4390),
            Triple("Villa del Parque", -34.6015, -58.4885),
            Triple("Villa Devoto", -34.5985, -58.5115),
            Triple("Villa General Mitre", -34.6070, -58.4780),
            Triple("Villa Lugano", -34.6785, -58.4715),
            Triple("Villa Luro", -34.6395, -58.5010),
            Triple("Villa Ortúzar", -34.5825, -58.4685),
            Triple("Villa Pueyrredón", -34.5825, -58.5010),
            Triple("Villa Real", -34.6180, -58.5225),
            Triple("Villa Riachuelo", -34.6810, -58.4480),
            Triple("Villa Santa Rita", -34.6180, -58.4785),
            Triple("Villa Soldati", -34.6660, -58.4285),
            Triple("Villa Urquiza", -34.5715, -58.4885),
        )

        val PUNTOS_DE_EJEMPLO: List<PuntoVerde> = COORDENADAS_POR_BARRIO.map { (barrio, lat, lng) ->
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde $barrio",
                direccion = "Zona de $barrio, CABA",
                barrio = barrio,
                latitud = lat,
                longitud = lng,
                categoriasQueAcepta = setOf("Papel", "Vidrio", "Plástico", "Metal"),
            )
        }
    }
}
