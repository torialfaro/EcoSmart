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
            // Corrección post-QA (2026-09-23): dataset de ejemplo ampliado de 1 a 4 actividades
            // por categoría (RF-080), cada una con un objetivo distinto dentro de la misma
            // categoría, manteniendo el puntosBase fijo de la grilla de constitution.md
            // ("valores cerrados": Reciclar 50, Reutilizar 100, Caminar 50).
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.RECICLAR,
                descripcionCorta = "Reciclá papel y cartón que ya no uses",
                pasosASeguir = "1. Juntá diarios, revistas, cajas o papel que ya no uses.\n" +
                    "2. Llevalos a un Punto Verde o contenedor de papel.\n" +
                    "3. Sacá una foto del material ya depositado.",
                resultadoEsperado = "Una foto donde se vea papel o cartón depositado en un contenedor de reciclaje.",
                fotoReferencialUrl = "",
                puntosBase = 50,
            ),
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.RECICLAR,
                descripcionCorta = "Reciclá frascos y botellas de vidrio",
                pasosASeguir = "1. Juntá frascos y botellas de vidrio limpios.\n" +
                    "2. Llevalos a un Punto Verde con contenedor de vidrio.\n" +
                    "3. Sacá una foto del material ya depositado.",
                resultadoEsperado = "Una foto donde se vean envases de vidrio depositados en un contenedor de reciclaje.",
                fotoReferencialUrl = "",
                puntosBase = 50,
            ),
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.RECICLAR,
                descripcionCorta = "Dale un destino correcto a un residuo electrónico",
                pasosASeguir = "1. Juntá pilas, cables o aparatos electrónicos en desuso.\n" +
                    "2. Llevalos a un punto de recolección de residuos electrónicos.\n" +
                    "3. Sacá una foto del material ya entregado.",
                resultadoEsperado = "Una foto donde se vean residuos electrónicos entregados en un punto de recolección.",
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
                categoria = CategoriaActividad.REUTILIZAR,
                descripcionCorta = "Transformá una prenda que ya no usás en algo nuevo",
                pasosASeguir = "1. Elegí una prenda de ropa que ya no usás.\n" +
                    "2. Transformala (bolsa, trapo, funda) o preparala para donar.\n" +
                    "3. Sacá una foto del resultado.",
                resultadoEsperado = "Una foto de la prenda transformada o lista para donar/reutilizar.",
                fotoReferencialUrl = "",
                puntosBase = 100,
            ),
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.REUTILIZAR,
                descripcionCorta = "Convertí un envase vacío en algo útil",
                pasosASeguir = "1. Elegí un envase vacío (botella, frasco, lata).\n" +
                    "2. Transformalo en maceta, guardatodo u otro objeto útil.\n" +
                    "3. Sacá una foto del resultado.",
                resultadoEsperado = "Una foto del envase transformado en un objeto nuevo y funcional.",
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
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.CAMINAR,
                descripcionCorta = "Elegí caminar un trayecto que solés hacer en auto o colectivo",
                pasosASeguir = "1. Elegí o aceptá una meta de pasos para este trayecto.\n" +
                    "2. Presioná \"Realizar\" y hacé el trayecto caminando en vez de en auto o colectivo.\n" +
                    "3. El podómetro del dispositivo registra tu avance automáticamente.",
                resultadoEsperado = "El podómetro registra una cantidad de pasos igual o mayor a la meta propuesta.",
                fotoReferencialUrl = "",
                puntosBase = 50,
            ),
            Actividad(
                id = ActividadId.nuevo(),
                categoria = CategoriaActividad.CAMINAR,
                descripcionCorta = "Salí a caminar por una plaza o espacio verde cercano",
                pasosASeguir = "1. Elegí o aceptá una meta de pasos.\n" +
                    "2. Presioná \"Realizar\" y caminá por una plaza o espacio verde cercano.\n" +
                    "3. El podómetro del dispositivo registra tu avance automáticamente.",
                resultadoEsperado = "El podómetro registra una cantidad de pasos igual o mayor a la meta propuesta.",
                fotoReferencialUrl = "",
                puntosBase = 50,
            ),
        )
    }
}
