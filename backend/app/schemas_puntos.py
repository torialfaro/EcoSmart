"""Modelos 1:1 con los schemas de `specs/002-firestore-datos-usuario/contracts/openapi.yaml`
(endpoints del backend de confianza) — separados de `schemas.py` (EcoGPT, spec 001) porque
cubren un contrato distinto, con su propio ciclo de vida (Principio V, responsabilidad única).
"""

from datetime import date
from typing import Literal, Optional

from pydantic import BaseModel

CategoriaActividad = Literal["RECICLAR", "REUTILIZAR", "CAMINAR"]
ResultadoVerificacion = Literal["APROBADO", "RECHAZADO", "INDETERMINADO"]


class SolicitudRegistroReciclarReutilizar(BaseModel):
    actividadId: str
    categoria: CategoriaActividad
    fecha: date
    resultado: ResultadoVerificacion
    motivoIA: Optional[str] = None
    huellaImagen: str


class SolicitudCaminar(BaseModel):
    accion: Literal["INICIAR", "ACTUALIZAR_PROGRESO", "COMPLETAR"]
    actividadId: Optional[str] = None
    metaPasos: Optional[int] = None
    pasosLogrados: Optional[int] = None
    dispositivoId: Optional[str] = None


class SolicitudPasosDelDia(BaseModel):
    pasosHoy: int
    fecha: date


class RegistroVerificacionOtorgado(BaseModel):
    id: str
    resultado: ResultadoVerificacion
    puntosOtorgados: int
    puntosHistoricosActualizados: int
    rachaActual: int
    nivel: str


class ErrorBackendConfianza(BaseModel):
    codigo: str
    mensaje: str


class PerfilMigracion(BaseModel):
    """Campos de `UsuarioRoomEntity` (spec 001) a subir una única vez (RF-D011). A
    diferencia de `/registros-verificacion/*`, acá SÍ viajan `puntosHistoricos`/
    `rachaActual`/`ultimaActividadAprobadaEn`: son el progreso histórico ya acumulado
    bajo spec 001, que no tiene otra fuente de verdad más que el propio Room local."""

    nombre: str
    apellido: str
    nombreUsuario: str
    barrio: Optional[str] = None
    telefono: str
    categoriasDeInteres: list[str]
    puntosHistoricos: int = 0
    rachaActual: int = 0
    ultimaActividadAprobadaEn: Optional[date] = None


class RegistroMigracion(BaseModel):
    """`id` es la PK local de `RegistroVerificacionRoomEntity` (spec 001), reusada como
    ID determinístico del documento Firestore migrado — así una segunda subida con los
    mismos datos sobrescribe en vez de duplicar (idempotencia sin bandera separada)."""

    id: str
    actividadId: str
    categoria: CategoriaActividad
    fecha: date
    resultado: ResultadoVerificacion
    motivoIA: Optional[str] = None
    puntosOtorgados: int = 0
    huellaImagen: Optional[str] = None
    pasosRegistrados: Optional[int] = None


class SolicitudMigracion(BaseModel):
    perfil: PerfilMigracion
    historial: list[RegistroMigracion]

