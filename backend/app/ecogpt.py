"""Llamada a Claude (Anthropic) que implementa el veredicto de EcoGPT.

RF-027: el prompt combina la foto, el resultado esperado de la actividad y
la descripción del usuario. RNF-001: el tono del `motivo` debe ser
alentador, nunca punitivo, incluso al rechazar.

Salida estructurada: se fuerza el uso de una herramienta (`tool_choice`) cuyo
`input_schema` es el shape de `VeredictoEcoGpt`, lo que garantiza un JSON válido.
"""

import base64
import logging
import os
from typing import Optional, Tuple

import anthropic

MODELO_CLAUDE = os.environ.get("ANTHROPIC_MODEL", "claude-haiku-4-5")
# Respaldos ante sobrecarga/404 del principal (lista separada por comas).
MODELOS_CLAUDE_RESPALDO = [
    m.strip() for m in os.environ.get("ANTHROPIC_MODELOS_RESPALDO", "").split(",") if m.strip()
]
_MODELOS = list(dict.fromkeys([MODELO_CLAUDE, *MODELOS_CLAUDE_RESPALDO]))

# La app corta a los 30 s (RNF-008): timeout y reintentos acotados.
_cliente = anthropic.AsyncAnthropic(
    api_key=os.environ.get("ANTHROPIC_API_KEY", ""),
    timeout=20.0,
    max_retries=2,
)

logger = logging.getLogger("ecogpt")

_HERRAMIENTA_VEREDICTO = {
    "name": "emitir_veredicto",
    "description": "Emite el veredicto final de la verificación de la actividad.",
    "input_schema": {
        "type": "object",
        "properties": {
            "veredicto": {"type": "string", "enum": ["APROBADO", "RECHAZADO", "INDETERMINADO"]},
            "motivo": {
                "type": "string",
                "description": (
                    "Explicación breve, concreta y con tono alentador (nunca punitivo ni "
                    "culpabilizador) de por qué se rechazó o quedó indeterminado. "
                    "Cadena vacía si el veredicto es APROBADO."
                ),
            },
        },
        "required": ["veredicto", "motivo"],
    },
}


class ClaudeVeredictoInvalidoError(Exception):
    """Claude respondió sin un veredicto válido con la forma esperada."""


class ClaudeSobrecargadoError(Exception):
    """Claude está saturado (429/5xx/529) incluso tras reintentar."""


class ClaudeModeloNoDisponibleError(Exception):
    """Ningún modelo configurado existe o está habilitado para la API key."""


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

El motivo nunca debe sonar punitivo o culpabilizador, incluso al rechazar — sé breve, concreto y alentador. Respondé usando la herramienta emitir_veredicto con tu decisión final."""


async def _generar_con_respaldo(mensajes):
    ultimo_error: Optional[Exception] = None
    hubo_sobrecarga = False
    for modelo in _MODELOS:
        try:
            return await _cliente.messages.create(
                model=modelo,
                max_tokens=512,
                tools=[_HERRAMIENTA_VEREDICTO],
                tool_choice={"type": "tool", "name": "emitir_veredicto"},
                messages=mensajes,
            )
        except anthropic.NotFoundError as error:
            logger.warning("Claude %s no disponible (404): %s", modelo, error)
            ultimo_error = error
        except (anthropic.RateLimitError, anthropic.InternalServerError, anthropic.APIConnectionError) as error:
            logger.warning("Claude %s saturado o sin conexión: %s", modelo, error)
            hubo_sobrecarga = True
            ultimo_error = error
    if hubo_sobrecarga:
        raise ClaudeSobrecargadoError(str(ultimo_error)) from ultimo_error
    raise ClaudeModeloNoDisponibleError(str(ultimo_error)) from ultimo_error


async def verificar_con_ia(
    imagen_bytes: bytes,
    media_type: str,
    categoria: str,
    resultado_esperado: str,
    descripcion_usuario: str,
    cantidad_huellas_previas: int,
) -> Tuple[str, Optional[str]]:
    prompt = _construir_prompt(categoria, resultado_esperado, descripcion_usuario, cantidad_huellas_previas)
    mensajes = [
        {
            "role": "user",
            "content": [
                {
                    "type": "image",
                    "source": {
                        "type": "base64",
                        "media_type": media_type,
                        "data": base64.standard_b64encode(imagen_bytes).decode("ascii"),
                    },
                },
                {"type": "text", "text": prompt},
            ],
        }
    ]

    respuesta = await _generar_con_respaldo(mensajes)

    datos = next((b.input for b in respuesta.content if getattr(b, "type", "") == "tool_use"), None)
    if not isinstance(datos, dict):
        raise ClaudeVeredictoInvalidoError("Claude no devolvió un veredicto estructurado")

    veredicto = datos.get("veredicto")
    if veredicto not in ("APROBADO", "RECHAZADO", "INDETERMINADO"):
        raise ClaudeVeredictoInvalidoError(f"Veredicto inesperado: {veredicto!r}")

    motivo = (datos.get("motivo") or "").strip() or None
    if veredicto == "APROBADO":
        motivo = None

    return veredicto, motivo
