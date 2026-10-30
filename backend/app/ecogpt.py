"""Llamada a Gemini (Google AI Studio, capa gratuita) que implementa el
veredicto de EcoGPT.

Corrección post-QA (2026-09-23, ronda 2): se reemplazó Claude/Anthropic
(pago, versión anterior de este archivo) por Gemini, que tiene una capa
gratuita real sin tarjeta de crédito (ver research.md §2.1) — requisito
explícito del usuario: "este es un proyecto escolar... no puedo pagar por
una IA".

RF-027: el prompt combina la foto, el resultado esperado de la actividad y
la descripción del usuario — le pedimos a Gemini que compare las tres
cosas entre sí, no solo que describa la imagen. RNF-001: el tono del
`motivo` debe ser alentador, nunca punitivo, incluso al rechazar.

Se usa salida estructurada nativa de Gemini (`response_mime_type` +
`response_schema`) en vez de pedirle texto libre y parsear con regex/JSON
best-effort: es la forma confiable de obtener siempre un JSON válido con
el shape exacto que espera `VeredictoEcoGpt`.

Corrección post-QA (2026-10-14): la capa gratuita de Gemini responde 503
"UNAVAILABLE" ("This model is currently experiencing high demand") con
cierta frecuencia en horas pico — es un error transitorio del lado de
Google, no un bug nuestro. Se agrega un reintento automático con backoff
ante ese error específico (`google.genai.errors.ServerError`) antes de
propagarlo como falla real, igual que ya se hacía para el cold-start del
hosting (ver `VerificarFotoConIA.kt` del lado Android).
"""

import asyncio
import json
import os
from typing import Optional, Tuple

from google import genai
from google.genai import errors as genai_errors
from google.genai import types

MODELO_GEMINI = os.environ.get("GEMINI_MODEL", "gemini-3.8-flash")
# Respaldo ante 503/404 del modelo principal (configurable con GEMINI_MODEL_RESPALDO).
MODELO_GEMINI_RESPALDO = os.environ.get("GEMINI_MODEL_RESPALDO", "gemini-2.5-flash")
_MODELOS = list(dict.fromkeys(m for m in (MODELO_GEMINI, MODELO_GEMINI_RESPALDO) if m))

_cliente = genai.Client(api_key=os.environ.get("GEMINI_API_KEY", ""))

_INTENTOS_POR_MODELO = 2
_ESPERA_ENTRE_REINTENTOS_S = 1.5

_ESQUEMA_VEREDICTO = {
    "type": "OBJECT",
    "properties": {
        "veredicto": {
            "type": "STRING",
            "enum": ["APROBADO", "RECHAZADO", "INDETERMINADO"],
        },
        "motivo": {
            "type": "STRING",
            "description": (
                "Explicación breve, concreta y con tono alentador (nunca punitivo ni "
                "culpabilizador) de por qué se rechazó o quedó indeterminado. "
                "Cadena vacía si el veredicto es APROBADO."
            ),
        },
    },
    "required": ["veredicto", "motivo"],
}


class GeminiVeredictoInvalidoError(Exception):
    """Gemini respondió sin un JSON válido con la forma esperada de veredicto."""


class GeminiSobrecargadoError(Exception):
    """Gemini devolvió 503 UNAVAILABLE incluso tras reintentar (alta demanda transitoria)."""


def _construir_prompt(
    categoria: str,
    resultado_esperado: str,
    descripcion_usuario: str,
    cantidad_huellas_previas: int,
) -> str:
    return f"""Sos el verificador de actividades ambientales de la app EcoSmart. Tu tarea es decidir si la foto adjunta, junto con la descripción del usuario, corresponden razonablemente a la actividad de "{categoria}" que el usuario dice haber realizado.

Resultado esperado de la actividad (definido por EcoSmart): {resultado_esperado}
Descripción escrita por el usuario sobre lo que hizo: {descripcion_usuario}
Fotos ya aprobadas previamente por este mismo usuario en esta categoría: {cantidad_huellas_previas}

Reglas de evaluación (aplicalas en este orden):
1. APROBADO: la imagen muestra evidencia creíble y consistente con el resultado esperado de la actividad, Y la descripción del usuario es coherente con lo que se ve en la imagen (no está vacía, no es genérica, no contradice la foto).
2. RECHAZADO: la imagen claramente no corresponde a la actividad descripta, parece una foto de stock/internet no tomada por el propio usuario, está manipulada, o la descripción contradice lo que se ve.
3. INDETERMINADO: la imagen tiene mala calidad, luz o encuadre que impide juzgar con confianza (en ese caso el motivo debe invitar a reintentar con mejor foto).

El motivo nunca debe sonar punitivo o culpabilizador, incluso al rechazar — sé breve, concreto y alentador. Respondé únicamente con el JSON pedido, con tu decisión final."""


async def _generar_con_respaldo(contenido):
    """Prueba el modelo principal y, si Google lo devuelve saturado (5xx) o inexistente (404),
    pasa al modelo de respaldo. Tiempo total acotado para entrar en el timeout de 30 s de la app."""
    ultimo_error: Optional[Exception] = None
    for modelo in _MODELOS:
        for intento in range(_INTENTOS_POR_MODELO):
            try:
                return await _cliente.aio.models.generate_content(
                    model=modelo,
                    contents=contenido,
                    config=types.GenerateContentConfig(
                        response_mime_type="application/json",
                        response_schema=_ESQUEMA_VEREDICTO,
                    ),
                )
            except genai_errors.ServerError as error:
                ultimo_error = error
                if intento < _INTENTOS_POR_MODELO - 1:
                    await asyncio.sleep(_ESPERA_ENTRE_REINTENTOS_S)
            except genai_errors.ClientError as error:
                if getattr(error, "code", None) != 404:
                    raise
                ultimo_error = error
                break
    raise GeminiSobrecargadoError(str(ultimo_error)) from ultimo_error


async def verificar_con_ia(
    imagen_bytes: bytes,
    media_type: str,
    categoria: str,
    resultado_esperado: str,
    descripcion_usuario: str,
    cantidad_huellas_previas: int,
) -> Tuple[str, Optional[str]]:
    prompt = _construir_prompt(categoria, resultado_esperado, descripcion_usuario, cantidad_huellas_previas)
    contenido = [types.Part.from_bytes(data=imagen_bytes, mime_type=media_type), prompt]

    respuesta = await _generar_con_respaldo(contenido)

    texto = respuesta.text
    if not texto:
        raise GeminiVeredictoInvalidoError("Gemini no devolvió contenido de texto")

    try:
        datos = json.loads(texto)
    except json.JSONDecodeError as error:
        raise GeminiVeredictoInvalidoError(f"Respuesta de Gemini no es JSON válido: {texto!r}") from error

    veredicto = datos.get("veredicto")
    if veredicto not in ("APROBADO", "RECHAZADO", "INDETERMINADO"):
        raise GeminiVeredictoInvalidoError(f"Veredicto inesperado: {veredicto!r}")

    motivo = (datos.get("motivo") or "").strip() or None
    if veredicto == "APROBADO":
        motivo = None

    return veredicto, motivo
