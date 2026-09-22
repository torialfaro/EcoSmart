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
        val PUNTOS_DE_EJEMPLO = listOf(
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde Plaza Palermo",
                direccion = "Av. Santa Fe y Av. Sarmiento",
                barrio = "Palermo",
                latitud = -34.5787,
                longitud = -58.4211,
                categoriasQueAcepta = setOf("Papel", "Vidrio", "Plástico"),
            ),
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde Plaza Vicente López",
                direccion = "Av. Las Heras y Pueyrredón",
                barrio = "Recoleta",
                latitud = -34.5875,
                longitud = -58.3927,
                categoriasQueAcepta = setOf("Papel", "Cartón", "Metal"),
            ),
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde Parque Rivadavia",
                direccion = "Av. Rivadavia 4900",
                barrio = "Caballito",
                latitud = -34.6187,
                longitud = -58.4358,
                categoriasQueAcepta = setOf("Vidrio", "Plástico"),
            ),
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde Barrancas de Belgrano",
                direccion = "Juramento y 11 de Septiembre",
                barrio = "Belgrano",
                latitud = -34.5615,
                longitud = -58.4562,
                categoriasQueAcepta = setOf("Papel", "Vidrio", "Metal"),
            ),
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde Plaza Boedo",
                direccion = "Av. Boedo y San Ignacio",
                barrio = "Boedo",
                latitud = -34.6297,
                longitud = -58.4187,
                categoriasQueAcepta = setOf("Papel", "Cartón"),
            ),
            PuntoVerde(
                id = PuntoVerdeId.nuevo(),
                nombre = "Punto Verde Parque Chacabuco",
                direccion = "Av. Eva Perón y Asamblea",
                barrio = "Parque Chacabuco",
                latitud = -34.6357,
                longitud = -58.4351,
                categoriasQueAcepta = setOf("Plástico", "Metal"),
            ),
        )
    }
}
