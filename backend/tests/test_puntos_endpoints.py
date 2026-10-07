"""Tests de contrato (T034, spec 002-firestore-datos-usuario) de los endpoints nuevos
del backend de confianza (`contracts/openapi.yaml`). Corren contra el Firebase Local
Emulator Suite (quickstart.md §4/§8) con un ID Token real del emulador de
Authentication — no el Admin SDK, que bypassea las Reglas de Seguridad por diseño.

Nota TDD: este test se escribe ANTES de implementar el arbitraje de `caminataEnCurso`
entre dispositivos (T036) — tasks.md lo marca explícitamente como un caso que DEBE
fallar hasta que T036 esté implementado.
"""

import os
import uuid

import pytest
import requests
from fastapi.testclient import TestClient

from app.main import app

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

cliente = TestClient(app)


def _crear_usuario_de_prueba() -> str:
    """Crea una cuenta en el emulador de Auth y devuelve su ID Token."""
    email = f"{uuid.uuid4()}@ejemplo.com"
    respuesta = requests.post(
        f"http://{AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp",
        params={"key": "fake-api-key"},
        json={"email": email, "password": "Contrasena123", "returnSecureToken": True},
        timeout=5,
    )
    respuesta.raise_for_status()
    return respuesta.json()["idToken"]


def test_segunda_caminata_iniciada_desde_otro_dispositivo_responde_409():
    encabezados = {"Authorization": f"Bearer {_crear_usuario_de_prueba()}"}
    cuerpo_base = {"accion": "INICIAR", "actividadId": "caminar-01", "metaPasos": 8000}

    primera = cliente.post(
        "/registros-verificacion/caminar",
        headers=encabezados,
        json={**cuerpo_base, "dispositivoId": "dispositivo-a"},
    )
    assert primera.status_code == 200

    segunda = cliente.post(
        "/registros-verificacion/caminar",
        headers=encabezados,
        json={**cuerpo_base, "dispositivoId": "dispositivo-b"},
    )

    assert segunda.status_code == 409


def test_subir_datos_locales_dos_veces_no_duplica_el_historial():
    encabezados = {"Authorization": f"Bearer {_crear_usuario_de_prueba()}"}
    cuerpo = {
        "perfil": {
            "nombre": "Ada",
            "apellido": "Lovelace",
            "nombreUsuario": "ada",
            "barrio": "PALERMO",
            "telefono": "+5491112345678",
            "categoriasDeInteres": ["RECICLAR"],
            "puntosHistoricos": 150,
            "rachaActual": 3,
            "ultimaActividadAprobadaEn": "2026-10-01",
        },
        "historial": [
            {
                "id": "local-1",
                "actividadId": "reciclar-01",
                "categoria": "RECICLAR",
                "fecha": "2026-10-01",
                "resultado": "APROBADO",
                "puntosOtorgados": 50,
                "huellaImagen": "abc123",
            },
        ],
    }

    primera = cliente.post("/migracion/subir-datos-locales", headers=encabezados, json=cuerpo)
    assert primera.status_code == 200

    segunda = cliente.post("/migracion/subir-datos-locales", headers=encabezados, json=cuerpo)
    assert segunda.status_code == 200

    uid = requests.get(
        f"http://{AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:lookup",
        params={"key": "fake-api-key"},
        json={"idToken": encabezados["Authorization"].removeprefix("Bearer ")},
        timeout=5,
    ).json()["users"][0]["localId"]
    historial = requests.get(
        f"http://{FIRESTORE_EMULATOR_HOST}/v1/projects/{PROJECT_ID}/databases/(default)/documents/"
        f"usuarios/{uid}/registrosVerificacion",
        timeout=5,
    ).json()
    assert len(historial.get("documents", [])) == 1


def test_eliminar_cuenta_borra_documento_subcolecciones_y_credencial():
    email = f"{uuid.uuid4()}@ejemplo.com"
    password = "Contrasena123"
    alta = requests.post(
        f"http://{AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp",
        params={"key": "fake-api-key"},
        json={"email": email, "password": password, "returnSecureToken": True},
        timeout=5,
    )
    alta.raise_for_status()
    uid = alta.json()["localId"]
    encabezados = {"Authorization": f"Bearer {alta.json()['idToken']}"}

    # Siembra perfil + historial (vía el endpoint de migración) para probar que las
    # subcolecciones también se borran, no solo el documento padre.
    cuerpo_migracion = {
        "perfil": {
            "nombre": "Grace",
            "apellido": "Hopper",
            "nombreUsuario": "grace",
            "telefono": "+5491112345678",
            "categoriasDeInteres": ["RECICLAR"],
        },
        "historial": [
            {
                "id": "local-1",
                "actividadId": "reciclar-01",
                "categoria": "RECICLAR",
                "fecha": "2026-10-01",
                "resultado": "APROBADO",
                "puntosOtorgados": 50,
                "huellaImagen": "abc123",
            },
        ],
    }
    assert cliente.post("/migracion/subir-datos-locales", headers=encabezados, json=cuerpo_migracion).status_code == 200

    respuesta = cliente.delete("/cuenta", headers=encabezados)
    assert respuesta.status_code == 200

    documento = requests.get(
        f"http://{FIRESTORE_EMULATOR_HOST}/v1/projects/{PROJECT_ID}/databases/(default)/documents/usuarios/{uid}",
        timeout=5,
    )
    assert documento.status_code == 404

    historial = requests.get(
        f"http://{FIRESTORE_EMULATOR_HOST}/v1/projects/{PROJECT_ID}/databases/(default)/documents/"
        f"usuarios/{uid}/registrosVerificacion",
        timeout=5,
    ).json()
    assert historial.get("documents", []) == []

    reintento_login = requests.post(
        f"http://{AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword",
        params={"key": "fake-api-key"},
        json={"email": email, "password": password, "returnSecureToken": True},
        timeout=5,
    )
    assert reintento_login.status_code != 200
