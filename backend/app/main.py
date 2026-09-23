"""Backend real de EcoGPT — implementa `POST /verificaciones` de
`specs/001-ecosmart-mvp/contracts/openapi.yaml`.

Reemplaza al placeholder `https://api.ecogpt.example/v1/` (dominio RFC 2606
que nunca resuelve) que hacía fallar toda verificación con "Sin conexión a
internet". Este servicio es el único que conoce la API key real de Gemini
(`GEMINI_API_KEY`, capa gratuita de Google AI Studio — ver research.md
§2.1); el cliente Android solo conoce un secreto compartido
(`ECOGPT_SHARED_API_KEY`) que autentica sus propias llamadas a este
backend — la key de Gemini nunca viaja al dispositivo ni al APK.
"""

import logging
import os
from typing import Dict

from fastapi import FastAPI, File, Form, Header, UploadFile
from fastapi.responses import JSONResponse

from .ecogpt import GeminiVeredictoInvalidoError, verificar_con_ia
from .imagenes import ImagenNoSoportadaError, preparar_imagen
from .schemas import ErrorEcoGpt, VeredictoEcoGpt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ecogpt")

app = FastAPI(title="EcoGPT", version="1.0.0")

CATEGORIAS_VALIDAS = {"RECICLAR", "REUTILIZAR"}


def _error(status_code: int, codigo: str, mensaje: str) -> JSONResponse:
    return JSONResponse(status_code=status_code, content=ErrorEcoGpt(codigo=codigo, mensaje=mensaje).model_dump())


@app.get("/health")
async def health() -> Dict[str, str]:
    return {"estado": "ok"}


@app.post("/verificaciones", response_model=VeredictoEcoGpt)
async def verificar_foto_actividad(
    x_ecogpt_api_key: str = Header(alias="X-EcoGPT-Api-Key", default=""),
    imagen: UploadFile = File(...),
    categoria: str = Form(...),
    resultadoEsperado: str = Form(...),
    descripcionUsuario: str = Form(...),
    huellasImagenesAprobadasPrevias: str = Form(default=""),
):
    api_key_esperada = os.environ.get("ECOGPT_SHARED_API_KEY", "")
    if not api_key_esperada or x_ecogpt_api_key != api_key_esperada:
        return _error(401, "API_KEY_INVALIDA", "API key inválida o ausente.")

    if categoria not in CATEGORIAS_VALIDAS:
        return _error(400, "CATEGORIA_INVALIDA", f"Categoría '{categoria}' no soportada por EcoGPT.")

    datos_imagen = await imagen.read()

    try:
        imagen_normalizada, media_type = preparar_imagen(datos_imagen)
    except ImagenNoSoportadaError:
        return _error(400, "IMAGEN_NO_SOPORTADA", "La imagen está corrupta o en un formato no soportado.")

    cantidad_huellas_previas = len([h for h in huellasImagenesAprobadasPrevias.split(",") if h.strip()])

    try:
        veredicto, motivo = await verificar_con_ia(
            imagen_bytes=imagen_normalizada,
            media_type=media_type,
            categoria=categoria,
            resultado_esperado=resultadoEsperado,
            descripcion_usuario=descripcionUsuario,
            cantidad_huellas_previas=cantidad_huellas_previas,
        )
    except GeminiVeredictoInvalidoError:
        logger.exception("Gemini no devolvió un veredicto estructurado válido")
        return _error(500, "ERROR_VEREDICTO_IA", "No se pudo interpretar la respuesta de la IA. Reintentá.")
    except Exception:
        logger.exception("Error inesperado llamando a la API de Gemini")
        return _error(500, "ERROR_IA", "Ocurrió un error verificando la foto. Reintentá en unos segundos.")

    return VeredictoEcoGpt(veredicto=veredicto, motivo=motivo)
