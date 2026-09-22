package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.CategoriaActividad

private const val PASOS_POR_BLOQUE = 133
private const val PUNTOS_POR_BLOQUE_CAMINATA = 50
private const val PUNTOS_FOTO_RECICLAR = 50
private const val TOPE_DIARIO_RECICLAR = 1
private const val PUNTOS_FOTO_REUTILIZAR = 100
private const val TOPE_DIARIO_REUTILIZAR = 5

/**
 * Strategy (plan.md § Patrones de Diseño Aplicados): evita repetir un
 * `when` de fórmulas/topes por categoría en cada caso de uso que calcula
 * puntos (RF-031/RF-032/RF-033 difieren entre sí). `topeDiario = null`
 * significa "sin tope" (Caminar no tiene uno).
 */
sealed class EstrategiaDePuntaje(val topeDiario: Int?) {

    /** `cantidad` es la unidad propia de cada estrategia: pasos para Caminata, fotos aprobadas para Foto. */
    abstract fun calcularPuntos(cantidad: Int): Int

    /** RF-031/RF-049 — 50 puntos por cada bloque completo de 133 pasos; los pasos remanentes no puntúan. */
    data object CaminataPuntajeStrategy : EstrategiaDePuntaje(topeDiario = null) {
        override fun calcularPuntos(cantidad: Int): Int = (cantidad / PASOS_POR_BLOQUE) * PUNTOS_POR_BLOQUE_CAMINATA
    }

    /** RF-033 — 50 puntos por foto aprobada de Reciclar, tope de 1 por día. */
    data object FotoReciclarPuntajeStrategy : EstrategiaDePuntaje(topeDiario = TOPE_DIARIO_RECICLAR) {
        override fun calcularPuntos(cantidad: Int): Int = cantidad * PUNTOS_FOTO_RECICLAR
    }

    /** RF-032 — 100 puntos por foto aprobada de Reutilizar, tope de 5 por día. */
    data object FotoReutilizarPuntajeStrategy : EstrategiaDePuntaje(topeDiario = TOPE_DIARIO_REUTILIZAR) {
        override fun calcularPuntos(cantidad: Int): Int = cantidad * PUNTOS_FOTO_REUTILIZAR
    }

    companion object {
        fun para(categoria: CategoriaActividad): EstrategiaDePuntaje = when (categoria) {
            CategoriaActividad.CAMINAR -> CaminataPuntajeStrategy
            CategoriaActividad.RECICLAR -> FotoReciclarPuntajeStrategy
            CategoriaActividad.REUTILIZAR -> FotoReutilizarPuntajeStrategy
        }
    }
}
