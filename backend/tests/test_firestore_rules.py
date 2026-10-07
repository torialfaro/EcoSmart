"""Rules unit tests (T017, spec 002-firestore-datos-usuario) — RF-D010/RF-D014.

Corren contra el Firebase Local Emulator Suite
(`firebase emulators:start --only firestore,auth`, ver quickstart.md §4/§7). Usan la
API REST de los emuladores directamente (no el Admin SDK, que bypassa las Reglas de
Seguridad por diseño — ver `firebase_admin_setup.py` — y por lo tanto no sirve para
probar que un *cliente* las respeta).

Se saltean automáticamente si los emuladores no están corriendo.
"""

import os
import uuid

import pytest
import requests

AUTH_EMULATOR_HOST = os.environ.get("FIREBASE_AUTH_EMULATOR_HOST", "localhost:9099")
FIRESTORE_EMULATOR_HOST = os.environ.get("FIRESTORE_EMULATOR_HOST", "localhost:8080")
PROJECT_ID = os.environ.get("GCLOUD_PROJECT", "demo-ecosmart")


def _emuladores_disponibles() -> bool:
    try:
        requests.get(f"http://{FIRESTORE_EMULATOR_HOST}/", timeout=1)
        requests.get(f"http://{AUTH_EMULATOR_HOST}/", timeout=1)
        return True
    except requests.exceptions.ConnectionError:
        return False


pytestmark = pytest.mark.skipif(
    not _emuladores_disponibles(),
    reason="Requiere 'firebase emulators:start --only firestore,auth' corriendo (quickstart.md §4).",
)


def _crear_usuario_de_prueba() -> tuple[str, str]:
    """Crea una cuenta en el emulador de Auth y devuelve `(uid, idToken)`."""
    email = f"{uuid.uuid4()}@ejemplo.com"
    respuesta = requests.post(
        f"http://{AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp",
        params={"key": "fake-api-key"},
        json={"email": email, "password": "Contrasena123", "returnSecureToken": True},
        timeout=5,
    )
    respuesta.raise_for_status()
    datos = respuesta.json()
    return datos["localId"], datos["idToken"]


def _url_documento_usuario(uid: str) -> str:
    return (
        f"http://{FIRESTORE_EMULATOR_HOST}/v1/projects/{PROJECT_ID}"
        f"/databases/(default)/documents/usuarios/{uid}"
    )


def _headers(id_token: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {id_token}"}


def test_usuario_no_puede_leer_el_documento_de_otro_usuario():
    _, token_a = _crear_usuario_de_prueba()
    uid_b, _ = _crear_usuario_de_prueba()

    respuesta = requests.get(_url_documento_usuario(uid_b), headers=_headers(token_a), timeout=5)

    assert respuesta.status_code == 403


def test_usuario_puede_crear_su_propio_documento_con_campos_no_restringidos():
    uid_a, token_a = _crear_usuario_de_prueba()

    cuerpo = {"fields": {"nombre": {"stringValue": "Ana"}}}
    respuesta = requests.patch(_url_documento_usuario(uid_a), headers=_headers(token_a), json=cuerpo, timeout=5)

    assert respuesta.status_code == 200


def test_usuario_no_puede_escribir_puntos_historicos_directamente():
    uid_a, token_a = _crear_usuario_de_prueba()

    cuerpo = {"fields": {"puntosHistoricos": {"integerValue": "999999"}}}
    respuesta = requests.patch(_url_documento_usuario(uid_a), headers=_headers(token_a), json=cuerpo, timeout=5)

    assert respuesta.status_code == 403


def test_usuario_no_puede_escribir_resultado_en_un_registro_de_verificacion():
    uid_a, token_a = _crear_usuario_de_prueba()
    url = (
        f"http://{FIRESTORE_EMULATOR_HOST}/v1/projects/{PROJECT_ID}"
        f"/databases/(default)/documents/usuarios/{uid_a}/registrosVerificacion/reg-1"
    )

    cuerpo = {"fields": {"resultado": {"stringValue": "APROBADO"}}}
    respuesta = requests.patch(url, headers=_headers(token_a), json=cuerpo, timeout=5)

    assert respuesta.status_code == 403


def test_admin_sdk_si_puede_escribir_puntos_historicos(monkeypatch):
    uid_a, _ = _crear_usuario_de_prueba()
    monkeypatch.setenv("FIRESTORE_EMULATOR_HOST", FIRESTORE_EMULATOR_HOST)

    from app.firebase_admin_setup import obtener_firestore

    db = obtener_firestore()
    db.collection("usuarios").document(uid_a).set({"puntosHistoricos": 100}, merge=True)

    doc = db.collection("usuarios").document(uid_a).get()
    assert doc.to_dict()["puntosHistoricos"] == 100
