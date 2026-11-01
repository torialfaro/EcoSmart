package com.ecosmart.presentation.education

import com.ecosmart.domain.valueobject.CategoriaActividad
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.io.IOException

@JsonClass(generateAdapter = true)
data class CatalogoDto(val lecciones: List<LeccionDto?>? = null)

@JsonClass(generateAdapter = true)
data class LeccionDto(
    val id: String? = null,
    val titulo: String? = null,
    val categoria: String? = null,
    val duracionMinutos: Int? = null,
    val resumen: String? = null,
    val introduccion: String? = null,
    val ilustracionPortada: String? = null,
    val descripcionPortada: String? = null,
    val pasos: List<PasoDto?>? = null,
    val tip: TipDto? = null,
    val textoCierre: String? = null,
)

@JsonClass(generateAdapter = true)
data class PasoDto(
    val titulo: String? = null,
    val texto: String? = null,
    val ilustracion: String? = null,
    val descripcionIlustracion: String? = null,
)

@JsonClass(generateAdapter = true)
data class TipDto(val titulo: String? = null, val texto: String? = null)

@Suppress("ReturnCount") // validación con retornos tempranos por campo
object CatalogoLeccionesParser {

    private val adapter by lazy { Moshi.Builder().build().adapter(CatalogoDto::class.java) }

    fun parsear(json: String): ResultadoCatalogo {
        if (json.isBlank()) return ResultadoCatalogo.Error
        val catalogo = try {
            adapter.fromJson(json)
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
            null
        }
        val crudas = catalogo?.lecciones ?: return ResultadoCatalogo.Error
        if (crudas.isEmpty()) return ResultadoCatalogo.Vacio

        val vistos = mutableSetOf<String>()
        val validas = crudas.mapNotNull { it?.aLeccionUi() }.filter { vistos.add(it.id) }
        return if (validas.isEmpty()) ResultadoCatalogo.Error else ResultadoCatalogo.Cargado(validas)
    }

    private fun String?.textoONull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun LeccionDto.aLeccionUi(): LeccionUi? {
        val id = id.textoONull() ?: return null
        val titulo = titulo.textoONull() ?: return null
        val categoria = CategoriaActividad.entries.firstOrNull { it.name == categoria } ?: return null
        val duracion = duracionMinutos?.takeIf { it > 0 } ?: return null
        val resumen = resumen.textoONull() ?: return null
        val introduccion = introduccion.textoONull() ?: return null
        val pasosUi = pasos?.map { it?.aPasoUi() ?: return null }?.takeIf { it.isNotEmpty() } ?: return null
        val tipUi = tip?.let { t ->
            val tituloTip = t.titulo.textoONull()
            val textoTip = t.texto.textoONull()
            if (tituloTip != null && textoTip != null) TipLeccionUi(tituloTip, textoTip) else null
        }
        return LeccionUi(
            id = id,
            titulo = titulo,
            categoria = categoria,
            duracionMinutos = duracion,
            resumen = resumen,
            introduccion = introduccion,
            ilustracionPortada = ilustracionPortada.textoONull(),
            descripcionPortada = descripcionPortada.textoONull() ?: "Ilustración de la lección $titulo",
            pasos = pasosUi,
            tip = tipUi,
            textoCierre = textoCierre.textoONull(),
        )
    }

    private fun PasoDto.aPasoUi(): PasoLeccionUi? {
        val titulo = titulo.textoONull() ?: return null
        val texto = texto.textoONull() ?: return null
        return PasoLeccionUi(
            titulo = titulo,
            texto = texto,
            ilustracion = ilustracion.textoONull(),
            descripcionIlustracion = descripcionIlustracion.textoONull() ?: "Ilustración del paso: $titulo",
        )
    }
}
