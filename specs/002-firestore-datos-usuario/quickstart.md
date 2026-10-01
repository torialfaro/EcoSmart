# Quickstart: Persistencia Remota de Datos de Usuario en Firestore

**Fase**: 1 (Design & Contracts) | **Fecha**: 2026-10-01 | **Plan**: [plan.md](./plan.md)

Guía de validación end-to-end de este módulo, no un manual de implementación (el código
se produce en `/speckit.implement`). Asume el entorno de `001-ecosmart-mvp/quickstart.md`
ya funcionando (Android Studio, backend en `backend/` corriendo localmente o en Render).

## 1. Prerrequisitos adicionales a spec 001

| Herramienta | Versión mínima |
|---|---|
| Firebase CLI | 13+ (`npm install -g firebase-tools`) — solo para los emuladores locales |
| Cuenta de Google / Firebase Console | gratuita, capa "Spark" (ver research.md §0 y Assumptions de spec.md) |
| Python | 3.11 (igual que `backend/`, se agrega `firebase-admin` a `requirements.txt`) |

## 2. Crear el proyecto de Firebase

1. [Firebase Console](https://console.firebase.google.com/) → crear proyecto nuevo
   (capa Spark/gratuita).
2. Habilitar **Authentication** → métodos de acceso: Email/contraseña + Google.
3. Habilitar **Firestore Database** → modo producción (las reglas de
   `contracts/firestore.rules` son las que protegen los datos, no el modo de
   creación).
4. Agregar una app Android (`com.ecosmart.app`, mismo `applicationId` que spec 001) y
   descargar `google-services.json` → copiarlo en `app/google-services.json` (gitignorado,
   análogo a `local.properties`).
5. Generar una clave de cuenta de servicio (Project Settings → Service Accounts →
   "Generate new private key") para el backend → guardarla como
   `backend/.env` → `FIREBASE_SERVICE_ACCOUNT_JSON` (nunca commiteada, mismo patrón que
   `GEMINI_API_KEY`/`ECOGPT_SHARED_API_KEY`).

## 3. Desplegar las Reglas de Seguridad

```bash
firebase login
firebase init firestore   # seleccionar el proyecto creado en el paso 2
# reemplazar el firestore.rules generado por el de este módulo:
cp specs/002-firestore-datos-usuario/contracts/firestore.rules firestore.rules
firebase deploy --only firestore:rules
```

## 4. Levantar los emuladores para desarrollo/tests (sin tocar cuota real)

```bash
firebase emulators:start --only firestore,auth
```

- El cliente Android, en builds de `debug`, DEBE apuntar al emulador
  (`FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)` y
  `FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)` desde un emulador Android;
  usar la IP real del host si es un dispositivo físico en la misma red).
- El backend (pytest) apunta al mismo emulador vía la variable de entorno estándar
  `FIRESTORE_EMULATOR_HOST=localhost:8080`.

## 5. Backend: nuevas variables de entorno (`backend/.env`)

```dotenv
# Ya existentes (spec 001, sin cambios):
GEMINI_API_KEY=...
ECOGPT_SHARED_API_KEY=...

# Nuevas (este módulo):
FIREBASE_SERVICE_ACCOUNT_JSON=./firebase-service-account.json
FIRESTORE_EMULATOR_HOST=localhost:8080   # solo en desarrollo local; ausente en Render
```

## 6. Validar cada historia de usuario manualmente

| US | Pasos de validación manual |
|---|---|
| US1 (recuperar cuenta) | Registrarse, aprobar 1 actividad, desinstalar la app, reinstalar, iniciar sesión → verificar perfil/historial/puntos intactos. |
| US2 (sincronizar entre dispositivos) | Iniciar sesión con la misma cuenta en 2 emuladores, aprobar una actividad en uno, volver a primer plano en el otro → verificar que refleja el cambio (RF-D017: sin listener en vivo, requiere reentrar a la pantalla). |
| US3 (contraseñas fuera de la BDD) | Abrir Firebase Console → Firestore → documento `usuarios/{uid}` de una cuenta de prueba → confirmar que no existe ningún campo de contraseña. |
| US4 (migración) | Con una instalación de **spec 001** (sin este módulo) ya logueada y con historial en Room, actualizar a la build de este módulo, abrir la app → verificar que `usuarios/{uid}` aparece poblado en Firestore y que Room queda vacío tras el primer login. |
| US5 (eliminar cuenta) | Confirmar eliminación desde el perfil → verificar en Firebase Console que el documento, subcolecciones y el usuario de Authentication desaparecieron dentro de SC-D005 (60s). |

## 7. Validar las Reglas de Seguridad (sin escribir código de la app)

Usando el [Rules Playground de Firebase Console](https://firebase.google.com/docs/firestore/security/test-rules-emulator)
o `firebase emulators:exec` con el SDK de testing de reglas:

- Un usuario autenticado como `uid=A` intentando leer `usuarios/B` → DEBE fallar (RF-D010).
- Un usuario autenticado como `uid=A` intentando escribir
  `usuarios/A.puntosHistoricos` directamente → DEBE fallar (RF-D014).
- El mismo intento anterior, pero usando el Admin SDK (simulando al backend) → DEBE
  tener éxito (el Admin SDK bypassa las reglas por diseño).

## 8. Validar los nuevos endpoints del backend

Ver `contracts/openapi.yaml`. Con el backend corriendo localmente
(`uvicorn app.main:app --reload`) y el emulador de Firestore/Auth activo:

```bash
# Obtener un ID Token de prueba del emulador de Auth (ver docs de Firebase Emulator UI,
# normalmente http://localhost:4000/auth), y usarlo acá:
curl -X POST http://localhost:8000/registros-verificacion/caminar \
  -H "Authorization: Bearer <idToken-de-prueba>" \
  -H "Content-Type: application/json" \
  -d '{"accion": "INICIAR", "actividadId": "caminar-01", "metaPasos": 8000, "dispositivoId": "emulador-1"}'
```

Repetir la misma llamada con `dispositivoId` distinto mientras la primera sigue activa →
DEBE responder `409` (RF-D008).
