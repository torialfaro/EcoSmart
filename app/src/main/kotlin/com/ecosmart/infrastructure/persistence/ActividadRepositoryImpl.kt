package com.ecosmart.infrastructure.persistence

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadDao
import com.ecosmart.infrastructure.persistence.room.actividad.ActividadMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActividadRepositoryImpl @Inject constructor(
    private val actividadDao: ActividadDao,
) : ActividadRepository {

    override fun observarPorCategorias(categorias: Set<CategoriaActividad>): Flow<List<Actividad>> =
        actividadDao.findByCategorias(categorias.map { it.name }).map { it.map(ActividadMapper::toDomain) }

    override suspend fun buscarPorId(id: ActividadId): Actividad? =
        actividadDao.findById(id.valor)?.let(ActividadMapper::toDomain)

    override suspend fun sembrarCatalogoSiEstaVacio() {
        if (actividadDao.contar() > 0) return
        actividadDao.insertarTodas(CATALOGO_INICIAL.map(ActividadMapper::toRoomEntity))
    }

    private companion object {
        val CATALOGO_INICIAL = listOf(
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.RECICLAR,
                descripcionCorta = "Llevá tus envases limpios a un Punto Verde",
                pasosASeguir = "1. Juntá botellas, latas o cartón limpios y secos.\n" +
                    "2. Llevalos a un Punto Verde cercano.\n" +
                    "3. Sacá una foto del material ya depositado.",
                resultadoEsperado = "Una foto donde se vean los materiales reciclables en un contenedor de reciclaje.",
                fotoReferencialUrl = "",
                puntosBase = 50,
            ),
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.REUTILIZAR,
                descripcionCorta = "Dale una segunda vida a un objeto que ibas a tirar",
                pasosASeguir = "1. Elegí un objeto que ibas a descartar.\n" +
                    "2. Transformalo o reutilizalo para un nuevo propósito.\n" +
                    "3. Sacá una foto del resultado.",
                resultadoEsperado = "Una foto del objeto reutilizado, distinto de su uso original.",
                fotoReferencialUrl = "",
                puntosBase = 100,
            ),
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.CAMINAR,
                descripcionCorta = "Caminá una meta de pasos y sumá puntos automáticamente",
                pasosASeguir = "1. Elegí o aceptá una meta de pasos.\n" +
                    "2. Presioná \"Realizar\" y salí a caminar.\n" +
                    "3. El podómetro del dispositivo registra tu avance automáticamente.",
                resultadoEsperado = "El podómetro registra una cantidad de pasos igual o mayor a la meta propuesta.",
                fotoReferencialUrl = "",
                puntosBase = 50,
            ),
        )
    }
}
