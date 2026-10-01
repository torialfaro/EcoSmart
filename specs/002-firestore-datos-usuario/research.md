# Research: Persistencia Remota de Datos de Usuario en Firestore

**Fase**: 0 (Outline & Research) | **Fecha**: 2026-10-01 | **Plan**: [plan.md](./plan.md)

Este documento resuelve cada incógnita técnica del Technical Context de `plan.md` con el
formato Decision / Rationale / Alternatives considered, según exige la Fase 0 de
`/speckit.plan`. Todas las decisiones están acotadas por `spec.md` (en particular su
sección "Clarifications", ya cerrada con 11 preguntas resueltas) y por
`constitution.md` v2.0.0.

---

## 0. Revisión de la decisión rechazada en spec 001: "Firebase Auth + Firestore"

**Decision**: La decisión de `001-ecosmart-mvp/research.md` §0 que descartaba Firebase
Auth + Firestore queda formalmente **superada** para este módulo, no contradicha. Spec 001
la rechazó por un único motivo: "Firebase Auth gestiona el hash/almacenamiento de
contraseñas internamente y no permite implementar un cifrado JWK/JWE propio" — un
conflicto directo con RNF-006/RNF-007 *tal como estaban redactados en ese momento*.

**Rationale**: Este módulo (spec 002) parte de un requisito de negocio nuevo y explícito
del usuario ("contraseñas hasheadas de forma irreversible, no conocidas por la BDD") que
es, precisamente, lo que Firebase Authentication ya hace de forma nativa. La
`constitution.md` v2.0.0 ya registra esta superación formalmente (ver Sync Impact Report
y Principio IV/Stack Tecnológico), y `spec.md` de este módulo la documenta en su primera
Clarification. No hay contradicción: el motivo original de rechazo (conflicto con
RNF-006/007) ya no aplica porque ese mismo requisito fue reemplazado a propósito.

**Alternatives considered**:
- **Mantener Room + JWE/JWK y agregar solo sincronización remota (backend propio +
  PostgreSQL)**: técnicamente posible, pero exige construir desde cero autenticación,
  sesiones, recuperación de contraseña y almacenamiento seguro — todo lo que Firebase
  Authentication ya da gratis y mejor probado. Rechazada por esfuerzo injustificado para
  un proyecto sin presupuesto (Principio VII, diseño pragmático).
- **Supabase (Postgres + Auth + Realtime, también con capa gratuita)**: alternativa válida
  y técnicamente similar. Rechazada únicamente porque el usuario pidió explícitamente
  "Firestore mediante Firebase" en el pedido original de spec 002 — no es una decisión
  técnica abierta.

---

## 1. ¿Quién implementa el "componente de servidor de confianza" (RF-D014/RF-D015)?

**Decision**: Se extiende el backend Python/FastAPI ya existente en `backend/` (deploy en
Render, mismo free tier que EcoGPT) con nuevos endpoints protegidos, en vez de introducir
Cloud Functions de Firebase como pieza nueva de infraestructura.

**Rationale**:
- Reutiliza exactamente lo que `spec.md` de este módulo ya cita como precedente directo
  (RF-074/RF-079 de spec 001: mismo tipo de backend, mismo riesgo de cold-start, misma
  mitigación de ping + 1 reintento, ya aplicada a este módulo vía RF-D015).
  Introducir Cloud Functions (Node.js o Python) sumaría un *segundo* runtime de servidor,
  un segundo pipeline de deploy y un segundo tipo de cold-start a mantener, sin que
  ningún requisito de `spec.md` lo exija — violaría el Principio VII (patrones/infra
  solo si simplifican).
- El backend ya tiene el patrón de autenticar llamadas del cliente mediante un header
  compartido (`X-EcoGPT-Api-Key` / `ECOGPT_SHARED_API_KEY`, ver
  `app/infrastructure/network/EcoGptClient.kt` y `backend/app/main.py`); se extiende ese
  mismo patrón de autenticación de servidor, reemplazando el secreto compartido por
  verificación de **ID Token de Firebase** (ver Decisión 5) para los nuevos endpoints, ya
  que ahora sí importa *qué usuario* hace la petición (antes solo importaba "es la app").
- `requirements.txt` ya es Python; se agrega una única dependencia (`firebase-admin`) en
  vez de introducir un lenguaje/runtime nuevo.

**Alternatives considered**:
- **Cloud Functions for Firebase** (Node.js/TypeScript o Python): integración más directa
  con los triggers nativos de Firestore (`onWrite`, `onCall`), pero exige aprender/montar
  un segundo pipeline de deploy (Firebase CLI) para un proyecto que ya tiene uno (Render)
  funcionando y documentado en `backend/README.md`. Rechazada por redundancia de
  infraestructura sin beneficio funcional adicional.
- **Cloud Run / Cloud Functions en Python para reusar el mismo lenguaje**: mismo problema
  de infraestructura duplicada que la opción anterior, solo que en Python; no resuelve el
  problema de tener dos plataformas de deploy.

---

## 2. SDK de Firebase para Android (cliente)

**Decision**: Firebase Android SDK vía BoM (`firebase-bom`), módulos
`firebase-auth-ktx` y `firebase-firestore-ktx`, más `com.google.android.gms:play-services-auth`
para el flujo de "Continuar con Google" (ya contemplado como requisito desde spec 001,
US1).

**Rationale**: Es el único camino soportado oficialmente para hablar con Firebase
Authentication/Firestore desde Android; el BoM fija versiones compatibles entre módulos
sin pinear cada una a mano, consistente con cómo ya se gestiona `composeBom` en este
proyecto (`gradle/libs.versions.toml`). `play-services-auth` es la integración
más simple y mejor documentada para obtener un `idToken` de Google y canjearlo por una
credencial de Firebase Auth (`GoogleAuthProvider.getCredential`), sin agregar una
dependencia de Jetpack Credential Manager (más nueva, pero con mayor superficie de
migración para el flujo ya construido en spec 001).

**Alternatives considered**:
- **Jetpack Credential Manager + Google Identity Services** (recomendado por Google para
  proyectos nuevos desde 2024): más moderno, pero requiere reescribir el flujo de login
  con Google ya implementado en spec 001 sin que ningún requisito lo pida. Se deja como
  mejora futura, no bloqueante para este módulo.

---

## 3. Autenticación de los nuevos endpoints del backend (RF-D014/RF-D015)

**Decision**: El cliente Android adjunta el **ID Token de Firebase** del usuario
autenticado (`FirebaseUser.getIdToken()`) como `Authorization: Bearer <token>` en cada
llamada a los nuevos endpoints del backend; el backend lo verifica con
`firebase_admin.auth.verify_id_token(...)` antes de escribir nada en Firestore, y extrae
el `uid` verificado del token en vez de confiar en un `uid` enviado por el cliente en el
body de la request.

**Rationale**: Es el mecanismo estándar de Firebase para que un backend confíe en la
identidad de un usuario sin volver a pedirle credenciales; impide que un cliente
manipulado se haga pasar por otro usuario simplemente cambiando un campo `uid` en el
body (si el backend confiara en ese campo, RF-D014 quedaría roto por esta puerta trasera).
El endpoint de EcoGPT (`POST /verificaciones`) no cambia — sigue usando el secreto
compartido existente, porque ese endpoint no otorga puntos directamente (lo hace el
endpoint nuevo que recibe su veredicto); ver Decisión 4.

**Alternatives considered**:
- **Reusar `ECOGPT_SHARED_API_KEY` también para los endpoints nuevos**: insuficiente,
  porque ese secreto autentica "es la app", no "es este usuario" — no permite al backend
  saber a qué `uid` de Firestore atribuir los puntos sin confiar ciegamente en el cliente.

---

## 4. Flujo de otorgamiento de puntos: integración con el endpoint EcoGPT existente

**Decision**: El endpoint `POST /verificaciones` de EcoGPT (`backend/app/main.py`,
spec 001) **no se modifica** y sigue devolviendo solo el veredicto (Aprobado/Rechazado/
Indeterminado + motivo), sin tocar Firestore. El cliente Android, tras recibir ese
veredicto, llama a un **endpoint nuevo** (`POST /registros-verificacion`, ver
`contracts/openapi.yaml` de este módulo) pasándole el veredicto ya obtenido; ese endpoint
nuevo es quien escribe en Firestore (`RegistroVerificacion` + actualización de
`puntosHistoricos`/`rachaActual`/`nivel`) tras verificar el ID Token.

**Rationale**: Mantiene `POST /verificaciones` con una única responsabilidad (hablar con
Gemini y devolver un veredicto), consistente con cómo ya está diseñado en spec 001;
separar "obtener el veredicto de la IA" de "persistir el resultado y otorgar puntos"
evita acoplar el endpoint de IA (que ya tiene timeouts estrictos de RNF-008/SC-008, 30s)
a la disponibilidad de Firestore. Para Caminar (RF-D015), que no pasa por EcoGPT, el
cliente llama directamente al endpoint nuevo con el resultado de comparar el podómetro
contra la meta.

**Alternatives considered**:
- **Fusionar ambos pasos en una sola llamada** (que `/verificaciones` ya escriba en
  Firestore): más simple para el cliente (una sola llamada), pero mezclaría dos
  responsabilidades distintas en un único endpoint con timeouts distintos, y obligaría a
  repetir la llamada a Gemini si solo falla la escritura en Firestore. Rechazada por
  Principio V (código predecible, responsabilidad única).

---

## 5. Testing contra Firebase sin depender de la nube real

**Decision**: Se usa el **Firebase Local Emulator Suite** (emuladores de Firestore y
Authentication) para tests de integración, tanto del backend (pytest) como del cliente
Android (tests instrumentados/Robolectric que apunten al emulador vía
`useEmulator(host, port)`).

**Rationale**: Evita que correr la suite de tests consuma la cuota gratuita real de
Firestore (relevante dado RF-D016/Assumptions sobre el límite diario compartido) y evita
que los tests dependan de red/credenciales reales. Es el mecanismo oficial recomendado
por Firebase para testing, y reemplaza directamente al patrón "Room in-memory" que usaba
spec 001 para testear DAOs.

**Alternatives considered**:
- **Mockear el SDK de Firestore directamente** (sin emulador): más rápido de ejecutar,
  pero no valida Reglas de Seguridad reales (RF-D010/RF-D014 dependen de que esas reglas
  se comporten como se espera) — un mock nunca podría probar que una escritura prohibida
  es efectivamente rechazada por el servidor.

---

## 6. Migración de datos locales existentes (RF-D011)

**Decision**: Se agrega una bandera local mínima (`SharedPreferences`,
`migracion_firestore_completada: Boolean`) que el cliente consulta en el primer login
posterior a esta actualización; si es `false` y existen datos en Room, sube perfil +
historial a Firestore (vía el endpoint nuevo de migración, ver `contracts/openapi.yaml`),
y solo si la subida se confirma exitosa, borra las tablas de Room y marca la bandera en
`true`.

**Rationale**: Esta bandera no es "dato de usuario" (no tiene reglas de negocio, puntaje
ni historial) sino un marcador técnico de instalación — es coherente con la excepción ya
documentada en `spec.md` (RF-D006 permite estado efímero/técnico local, lo que prohíbe es
que datos de negocio dependan únicamente del dispositivo). Verificar éxito antes de
borrar evita perder datos si la subida falla a mitad de camino (Edge Case ya cubierto en
`spec.md`).

**Alternatives considered**:
- **Detectar migración pendiente solo por "¿existen filas en Room?"** (sin bandera
  separada): parecía más simple, pero falla si la migración sube los datos y el borrado
  de Room se interrumpe (p. ej. la app se cierra a mitad de camino) — al reabrir, Room
  seguiría teniendo filas y se repetiría la subida, violando "sin duplicar registros en
  inicios posteriores" (RF-D011). La bandera separada resuelve este caso de forma simple.
