package com.ecosmart.presentation.activitydetail

/** Convierte el texto de pasos de una actividad ("1. ...\n2. ...") en una lista sin numeración. */
object PasosActividadParser {

    private val prefijoNumerico = Regex("""^\d+[.)]\s*""")

    fun parsear(texto: String): List<String> = texto
        .lines()
        .map { it.trim() }
        .map { prefijoNumerico.replace(it, "").trim() }
        .filter { it.isNotEmpty() }
}
