"""Modelos 1:1 con los schemas de `specs/001-ecosmart-mvp/contracts/openapi.yaml`."""

from typing import Literal, Optional

from pydantic import BaseModel


class VeredictoEcoGpt(BaseModel):
    veredicto: Literal["APROBADO", "RECHAZADO", "INDETERMINADO"]
    motivo: Optional[str] = None


class ErrorEcoGpt(BaseModel):
    codigo: str
    mensaje: str
