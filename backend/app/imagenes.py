"""Normalización de la imagen recibida antes de enviarla a Gemini.

El cliente Android envía la imagen con Content-Type "image/*" (comodín, no
un tipo concreto — ver `VerificarFotoConIA.kt`), así que no confiamos en el
Content-Type del multipart: decodificamos los bytes con Pillow para
detectar el formato real, y siempre reenviamos JPEG a la API de Gemini
para no depender de qué formato haya llegado.
"""

import io
from typing import Tuple

from PIL import Image, UnidentifiedImageError

TAMANO_MAXIMO_LADO_PX = 1568  # tamaño razonable de entrada para modelos de visión
MEDIA_TYPE_SALIDA = "image/jpeg"


class ImagenNoSoportadaError(Exception):
    """La imagen está corrupta o Pillow no pudo decodificarla como imagen."""


def preparar_imagen(datos: bytes) -> Tuple[bytes, str]:
    if not datos:
        raise ImagenNoSoportadaError("Imagen vacía")

    try:
        imagen = Image.open(io.BytesIO(datos))
        imagen.load()
    except (UnidentifiedImageError, OSError) as error:
        raise ImagenNoSoportadaError(str(error)) from error

    imagen = imagen.convert("RGB")
    if max(imagen.size) > TAMANO_MAXIMO_LADO_PX:
        ratio = TAMANO_MAXIMO_LADO_PX / max(imagen.size)
        nuevo_tamano = (round(imagen.width * ratio), round(imagen.height * ratio))
        imagen = imagen.resize(nuevo_tamano)

    buffer = io.BytesIO()
    imagen.save(buffer, format="JPEG", quality=85)
    return buffer.getvalue(), MEDIA_TYPE_SALIDA
