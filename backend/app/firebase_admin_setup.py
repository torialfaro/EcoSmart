"""Inicialización de Firebase Admin SDK y verificación de ID Token —
spec 002-firestore-datos-usuario (RF-D014, ver research.md §3).

El cliente Android adjunta el ID Token de Firebase del usuario autenticado como
`Authorization: Bearer <token>` en cada request a los endpoints de `puntos.py`. Este
módulo lo verifica del lado del servidor y expone el `uid` ya verificado — los
endpoints NUNCA deben confiar en un `uid` enviado por el cliente en el body de la
request (esa sería la puerta trasera que RF-D014 busca cerrar).
"""

import os

import firebase_admin
from fastapi import Header, HTTPException
from firebase_admin import auth, credentials, firestore

_app: firebase_admin.App | None = None


def _app_firebase() -> firebase_admin.App:
    """Inicializa la app de Firebase Admin de forma perezosa (recién al primer uso),
    igual que `main.py` lee `ECOGPT_SHARED_API_KEY` en tiempo de request y no de
    import — así un `.env` incompleto no rompe la carga del módulo, solo el request.
    """
    global _app
    if _app is None:
        cred_path = os.environ.get("FIREBASE_SERVICE_ACCOUNT_JSON")
        cred = credentials.Certificate(cred_path) if cred_path else credentials.ApplicationDefault()
        _app = firebase_admin.initialize_app(cred)
    return _app


def obtener_firestore():
    """Cliente de Firestore con credenciales de Admin SDK — bypassa
    `contracts/firestore.rules` por diseño (esas reglas solo rigen al SDK de cliente).
    """
    return firestore.client(app=_app_firebase())


def eliminar_credencial_auth(uid: str) -> None:
    """RF-D012: borra la credencial de Firebase Authentication del usuario."""
    auth.delete_user(uid, app=_app_firebase())


async def verificar_id_token(authorization: str = Header(default="")) -> str:
    """Dependencia de FastAPI: extrae y devuelve el `uid` verificado del ID Token.

    Rechaza la request con 401 si el header falta, no tiene el prefijo `Bearer ` o el
    token no es válido/expiró.
    """
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="ID Token ausente o con formato inválido.")

    token = authorization.removeprefix("Bearer ").strip()
    try:
        decoded = auth.verify_id_token(token, app=_app_firebase())
    except Exception as exc:  # noqa: BLE001 — cualquier fallo de verificación es un 401
        raise HTTPException(status_code=401, detail="ID Token inválido o expirado.") from exc

    return decoded["uid"]
