package com.ecosmart.application.activity

import com.ecosmart.domain.model.Actividad
import com.ecosmart.domain.repository.ActividadRepository
import com.ecosmart.domain.valueobject.ActividadId
import com.ecosmart.domain.valueobject.CategoriaActividad
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObtenerActividadesFiltradasTest {

    private val actividadRepository = mockk<ActividadRepository>()
    private val obtenerActividadesFiltradas = ObtenerActividadesFiltradas(actividadRepository)

    @Test
    fun `delega en el repositorio con las categorias de interes del usuario`() = runTest {
        val categorias = setOf(CategoriaActividad.RECICLAR, CategoriaActividad.CAMINAR)
        val actividad = Actividad(
            id = ActividadId.nuevo(),
            categoria = CategoriaActividad.RECICLAR,
            descripcionCorta = "desc",
            pasosASeguir = "pasos",
            resultadoEsperado = "resultado",
            fotoReferencialUrl = "",
            puntosBase = 50,
        )
        every { actividadRepository.observarPorCategorias(categorias) } returns flowOf(listOf(actividad))

        val resultado = obtenerActividadesFiltradas(categorias).first()

        assertEquals(listOf(actividad), resultado)
        verify { actividadRepository.observarPorCategorias(categorias) }
    }

    @Test
    fun `no filtra nada del lado del caso de uso si el repositorio ya devuelve vacio`() = runTest {
        every { actividadRepository.observarPorCategorias(emptySet()) } returns flowOf(emptyList())

        val resultado = obtenerActividadesFiltradas(emptySet()).first()

        assertEquals(emptyList<Actividad>(), resultado)
    }
}
