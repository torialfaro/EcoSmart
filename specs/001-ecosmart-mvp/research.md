# Research: EcoSmart — Sistema Completo (MVP)

**Fase**: 0 (Outline & Research) | **Fecha**: 2026-09-20 | **Plan**: [plan.md](./plan.md)

Este documento resuelve cada incógnita técnica (`NEEDS CLARIFICATION`) del
Technical Context de `plan.md` con el formato Decision / Rationale /
Alternatives considered, según exige la Fase 0 de `/speckit.plan`.

---

## 0. Decisión de Arquitectura: ¿EcoSmart necesita un backend propio?

**Decision**: No. EcoSmart MVP es una arquitectura **local-first**: Room es
la única base de datos, y el dispositivo es la única fuente de verdad de
los datos del usuario. Las únicas dependencias de red son servicios de
terceros (EcoGPT, fuente de datos de Puntos Verdes de CABA), no un backend
propiedad de EcoSmart.

**Rationale**:
- `constitution.md` § Stack Tecnológico declara explícitamente
  "Persistencia Local: Room / SharedPreferences" y no menciona ningún
  backend, lenguaje de servidor ni base de datos remota.
- Ningún RF/RNF de `spec.md` pide sincronización entre dispositivos; la
  sección Assumptions de `spec.md` asume "una única cuenta activa por
  sesión" (una cuenta, un dispositivo, para este alcance).
- `openapi.yaml` (pedido explícitamente por el usuario para este plan) solo
  necesita documentar 2 contratos: EcoGPT y sincronización de Puntos
  Verdes — ninguna API "propia" de EcoSmart fue solicitada.
- RNF-006/RNF-007 (contraseña cifrada con JWK/JWE, clave en almacén
  separado de "la base de datos") se satisfacen igual de bien, y de forma
  más simple, con Room (la base de datos) + Android Keystore (el almacén
  de claves separado) que con un backend — Android Keystore es, por
  diseño, un almacén de claves distinto y más seguro que cualquier fila de
  base de datos.

**Alternatives considered**:
- **Backend propio (Ktor/Spring + PostgreSQL)**: permitiría login
  multi-dispositivo real. Rechazado por no estar en el alcance declarado
  ni en la constitución ni en la especificación; introducir uno ahora
  violaría el Principio I ("la especificación manda"), ya que ningún RF lo
  pide, y agregaría superficie de infraestructura (hosting, migraciones,
  autenticación de servidor) fuera del alcance de un MVP académico.
- **Backend-as-a-Service (Firebase Auth + Firestore)**: simplifica login
  con Google, pero **es incompatible con RNF-006/007 tal como están
  redactados**: Firebase Auth gestiona el hash/almacenamiento de
  contraseñas internamente y no permite implementar un cifrado JWK/JWE
  propio sobre esas credenciales. Rechazado por conflicto directo con un
  requisito explícito del proyecto.

**Riesgo documentado**: si en una iteración futura se requiere login
multi-dispositivo, esta decisión debe revisarse (implica introducir un
backend o migrar a una BaaS, y renegociar RNF-006/007 en consecuencia).

**Corrección post-QA (2026-09-23)**: esta decisión se revisó parcialmente.
"EcoGPT" nunca tuvo un backend real detrás — `ECOGPT_BASE_URL` apuntaba por
defecto a `https://api.ecogpt.example/v1/`, un dominio reservado por RFC
2606 que nunca resuelve, así que **toda** verificación de foto fallaba con
un error de conexión, sin importar la conectividad real del dispositivo.
Se decidió introducir un backend mínimo propio (no para datos de usuario
— eso se mantiene 100% local-first, Room sigue siendo la única fuente de
verdad de `Usuario`/`RegistroVerificacion` — sino exclusivamente como
proxy de `POST /verificaciones` hacia una API de IA con visión, capa
gratuita — ver corrección post-QA 2026-09-23 ronda 2 en §2.1). Ver
detalle en §2.1. Esto NO viola el Principio I ni el resto de esta
decisión: `constitution.md` § Stack Tecnológico ya declaraba que "el
cliente de EcoGPT vive en Infraestructura, detrás de una interfaz... de
modo que el proveedor de IA pueda sustituirse sin afectar reglas de
negocio" — un proxy propio hacia un proveedor de IA real es, precisamente,
sustituir el proveedor detrás de esa interfaz, no introducir un backend de
datos de usuario.

---

## 1. APIs de Sensores de Android

### 1.1 Podómetro (Step Counter)

**Decision**: `Sensor.TYPE_STEP_COUNTER` (conteo acumulado desde el reinicio
del dispositivo) como fuente primaria, leído mediante un `SensorEventListener`
envuelto en un `Flow` (`callbackFlow`) expuesto por
`infrastructure/sensors/PodometroProvider`. El caso de uso `RegistrarCaminata`
guarda el valor base al iniciar la meta y compara contra el valor actual.

**Rationale**: `TYPE_STEP_COUNTER` es más eficiente en batería que
`TYPE_STEP_DETECTOR` (que emite un evento por paso) y alcanza para comparar
"pasos desde que until el usuario aceptó la meta hasta ahora" (RF-021 a
RF-024). El valor es acumulado desde el último reinicio del dispositivo, no
desde la instalación de la app, así que el caso de uso SIEMPRE calcula
`pasosActuales - pasosBase`, nunca usa el valor crudo del sensor.

**Alternatives considered**: `TYPE_STEP_DETECTOR` (evento por paso) —
rechazado por mayor consumo de batería sin aportar precisión adicional para
la regla de negocio (que solo necesita el delta, no cada evento
individual). Google Fit / Health Connect API — evaluado pero descartado
para el MVP por agregar una dependencia de permisos/consentimiento
adicional (Health Connect) fuera del alcance declarado en RF-043
(Galería, Cámara, GPS, Podómetro — no menciona Health Connect).

**Manejo de lecturas anómalas** (Edge Case de `spec.md`): el
`PodometroProvider` descarta (no reporta como válido) cualquier delta entre
dos lecturas consecutivas que supere un umbral físicamente imposible (p.
ej. >10 pasos/segundo sostenidos), delegando en el caso de uso la decisión
de pedir reintentar sin otorgar puntos, tal como exige el Edge Case
correspondiente.

### 1.2 Ubicación (GPS / Puntos Verdes)

**Decision**: `FusedLocationProviderClient` (Google Play Services Location)
con `Priority.PRIORITY_BALANCED_POWER_ACCURACY`, solicitando una única
posición reciente (`getCurrentLocation`) al entrar a la sección "Puntos
Verdes Cercanos", no tracking continuo.

**Rationale**: RF-053 solo necesita un punto de referencia (dirección de
perfil o posición GPS puntual) para filtrar por radio de 3 km; no hay
ningún RF que pida seguimiento de ubicación en tiempo real. Un tracking
continuo consumiría batería innecesariamente y excedería el alcance
declarado ("Explícitamente excluido: Seguimiento GPS en tiempo real" es
consistente con el espíritu de `constitution.md`, aunque esa lista es de
otro proyecto de referencia — aplicamos el mismo principio de
minimalidad).

**Alternatives considered**: `LocationManager` nativo — rechazado por API
más verbosa y sin fusión de proveedores (GPS + red); `requestLocationUpdates`
continuo — rechazado por consumo de batería no justificado por ningún RF.

### 1.3 Cámara y Galería

**Decision**: **CameraX** (`ImageCapture` use case) para tomar fotos, y el
**Photo Picker** de Android (`ActivityResultContracts.PickVisualMedia`) para
seleccionar desde galería.

**Rationale**: CameraX abstrae las diferencias entre fabricantes de forma
mucho más simple que la Camera2 API cruda, con menos código repetido
(alineado al Principio V, "sin lógica duplicada"). El Photo Picker no
requiere el permiso `READ_MEDIA_IMAGES` en Android 13+ (funciona como un
selector del sistema fuera del sandbox de la app), lo que reduce
fricción de permisos para RF-025, aunque igualmente se solicita el
permiso de Galería en versiones anteriores a Android 13 según RF-043.

**Alternatives considered**: Camera2 API directa — rechazada por mayor
complejidad y más código específico por fabricante sin beneficio para el
alcance del MVP. `Intent.ACTION_GET_CONTENT` para galería — rechazado por
ser una API más antigua y menos consistente que el Photo Picker moderno.

---

## 2. Estrategia de Integración con EcoGPT

**Decision**: Cliente **Retrofit + OkHttp**, con un `OkHttpClient` dedicado
a las llamadas de EcoGPT configurado con `callTimeout = 30s` (RNF-008), sin
reintentos automáticos silenciosos (un timeout se reporta al usuario como
tal, ver Edge Case "Timeout de EcoGPT"; el reintento es una acción
explícita del usuario, "Volver a intentar", RF-030).

**Rationale**: Retrofit + OkHttp es el estándar de facto para clientes HTTP
en Android, con soporte nativo de `multipart/form-data` (necesario para
subir la imagen junto con el prompt de texto) y timeouts configurables por
llamada. Fijar el timeout exactamente en 30 s materializa RNF-008/SC-008
sin ambigüedad.

**Manejo de errores/estados** (mapeo a `ResultadoVerificacion` de
`data-model.md`):

| Condición de red/respuesta | Estado resultante | RF/Edge Case |
|---|---|---|
| Respuesta HTTP 200 con veredicto `APROBADO` | `ResultadoVerificacion.APROBADO` | RF-028 |
| Respuesta HTTP 200 con veredicto `RECHAZADO`/`INDETERMINADO` | `ResultadoVerificacion.RECHAZADO` / `INDETERMINADO` | RF-029, RF-050 |
| Sin conexión al iniciar el envío | Error "sin conexión", foto/descripción conservadas | Edge Case "Corte de conexión" |
| `callTimeout` de 30 s excedido | Error "timeout" (no Rechazado/Indeterminado) | RNF-008, Edge Case "Timeout de EcoGPT" |
| Error 5xx / formato de respuesta inválido | Error genérico de EcoGPT, mismo tratamiento que timeout (reintentar sin consumir tope) | Consistente con RF-030 |

**Fallback sin conexión**: el formulario de verificación (foto +
descripción) permite completarse offline, pero el botón de envío queda
deshabilitado con un mensaje explicativo (tono no punitivo, RNF-001) hasta
detectar conectividad (`ConnectivityManager.NetworkCallback`); no se
encola el envío para reintento automático en segundo plano en este MVP,
porque ninguna US lo pide y agregar una cola de reintentos (WorkManager)
para este flujo sería complejidad no solicitada (Principio VII).

**Alternatives considered**: Ktor Client — funcionalmente equivalente,
rechazado solo por convención (Retrofit es más común en proyectos Android
Kotlin actuales, reduce la curva de aprendizaje del equipo). Corrutinas +
`HttpURLConnection` manual — rechazado por reinventar manejo de
multipart/timeouts que Retrofit/OkHttp ya resuelven.

### 2.1 Backend real de EcoGPT (corrección post-QA, 2026-09-23)

**Decision**: backend mínimo en **Python + FastAPI** (`backend/`, fuera del
módulo Android), desplegado en **Render (free tier)**, que implementa
`POST /verificaciones` del contrato como un proxy hacia una API de IA con
soporte de visión. El backend es la única pieza que conoce la API key
real del proveedor de IA (variable de entorno del servicio); el cliente
Android solo conoce un secreto compartido propio (`ECOGPT_API_KEY` en
`local.properties` ↔ `ECOGPT_SHARED_API_KEY` en Render) que autentica sus
llamadas a este backend — la key del proveedor de IA nunca se embebe en
el APK.

El prompt enviado al modelo combina, en una sola llamada multimodal: la
imagen, `Actividad.resultadoEsperado` y la descripción del usuario, y le
pide comparar las tres cosas entre sí (no solo describir la imagen) antes
de decidir Aprobado/Rechazado/Indeterminado — implementa RF-027
literalmente. La salida se fuerza a estructura fija (JSON con
`veredicto`/`motivo`, ver detalle del mecanismo abajo), en vez de parsear
texto libre, para que la respuesta sea siempre parseable sin heurísticas
frágiles.

**Corrección post-QA, ronda 2 (2026-09-23)**: la primera implementación de
este backend usaba **Claude (Anthropic)**, con salida forzada vía *tool
use* (`tool_choice` fijo a la herramienta `reportar_veredicto`). El
usuario aclaró explícitamente que este es un proyecto escolar sin
presupuesto para una API paga ("no puedo pagar por una IA"), así que se
reemplazó por **Gemini (Google AI Studio)**, que ofrece una capa gratuita
real sin tarjeta de crédito. El mecanismo de salida estructurada
equivalente en Gemini es `response_mime_type="application/json"` +
`response_schema` (un JSON Schema con tipos en mayúsculas: `OBJECT`,
`STRING`, etc.) en `GenerateContentConfig`, en vez de *tool use* — mismo
efecto (JSON siempre parseable), mecanismo nativo distinto. Modelo usado:
`gemini-2.5-flash` (configurable vía `GEMINI_MODEL`, ver
`backend/app/ecogpt.py`).

**Rationale**: Python + FastAPI fue elegido por el usuario del proyecto
sobre la alternativa recomendada (Kotlin + Ktor, que hubiera mantenido un
único lenguaje en todo el repo) porque el ecosistema de SDKs/ejemplos de
IA está mayormente en Python. Render free tier fue elegido por su
simplicidad de despliegue (Blueprint desde `render.yaml`, HTTPS
automático) para un MVP académico sin presupuesto de infraestructura.
Gemini free tier fue elegido, sobre otras opciones gratuitas, por ser la
más madura/confiable: no pide tarjeta de crédito, tiene cuota diaria
generosa en los modelos Flash, y soporte nativo de visión multimodal en
una sola llamada.

**Riesgo documentado — cold start vs. timeout fijo de 30s**: el plan free
de Render duerme el servicio tras ~15 min de inactividad; la primera
request tras dormir puede tardar 30-50+ segundos en responder, mientras
que el `callTimeout` del cliente Android está fijo en 30s (RNF-008/SC-008,
no negociable sin reabrir ese requisito). Esto puede manifestarse como un
falso "Timeout" en la primera verificación del día aunque el backend esté
sano. Mitigación sugerida (no implementada): un ping externo periódico a
`/health`, o migrar a un plan de Render sin sleep. Ver `backend/README.md`
§3.

**Riesgo documentado — calidad de verificación con un modelo gratuito**:
`gemini-2.5-flash` es un modelo más liviano que Claude Sonnet; es
razonablemente confiable para esta tarea (comparar una foto contra una
descripción textual corta), pero puede tener más falsos
Aprobado/Rechazado en casos ambiguos que un modelo de mayor capacidad. Se
acepta este trade-off por ser un requisito explícito del usuario (costo
cero) para un proyecto académico, no para producción.

**Alternatives considered**: Claude/Anthropic (fue la primera
implementación real de este backend; descartado en la ronda 2 por ser una
API paga, incompatible con el requisito explícito de costo cero de este
proyecto escolar); OpenRouter con modelos marcados ":free" (también sin
costo, descartado por catálogo de modelos gratuitos con visión menos
estable en el tiempo y límites de tasa más estrictos que Gemini); Kotlin +
Ktor para el backend (recomendado inicialmente por consistencia de
lenguaje con el resto del repo, descartado por preferencia explícita del
usuario por Python); llamar a la API de IA directamente desde el cliente
Android sin backend intermedio (más simple, pero expone la API key dentro
del APK — riesgo de abuso de la cuota gratuita por terceros, descartado
incluso siendo gratis); Railway/Fly.io como hosting (funcionalmente
equivalentes a Render, descartados por preferencia explícita del usuario).

---

## 3. Detección de Imágenes Duplicadas (RF-058/RF-059, SC-007)

**Decision**: enfoque en dos capas:

1. **Pre-filtro local (cliente)**: al capturar/seleccionar la foto, el
   dispositivo calcula un **hash perceptual (dHash de 64 bits)** de la
   imagen y lo compara contra los hashes de las fotos ya **Aprobadas** por
   ese usuario (guardados en `RegistroVerificacion.huellaImagen`, ver
   `data-model.md`). Si la distancia de Hamming entre hashes es menor a un
   umbral (p. ej. ≤ 5 bits de diferencia sobre 64), se rechaza el envío
   **sin siquiera llamar a EcoGPT** (ahorra el costo de red/IA y respeta
   el timeout).
2. **Verificación semántica remota (EcoGPT)**: si pasa el pre-filtro, el
   cliente igual envía a EcoGPT — junto con la foto y la descripción— los
   `huellaImagen` (u otro identificador liviano) de las últimas fotos
   Aprobadas del usuario en esa categoría, para que EcoGPT pueda detectar
   duplicados "semánticos" (mismo objeto, ángulo/luz distintos) que un
   hash perceptual simple no siempre captura, tal como exige literalmente
   RF-059 ("el sistema DEBE enviar cada imagen a EcoGPT para detectar si
   es idéntica").

**Rationale**: un hash exacto (SHA-256 del archivo) solo detecta bytes
idénticos y se rompe con la mínima recompresión JPEG; un hash perceptual
tolera variaciones triviales de compresión mientras sigue siendo barato de
calcular en el dispositivo (sin red). Combinarlo con la verificación de
EcoGPT cumple la letra de RF-059 y reduce el costo/latencia promedio (el
pre-filtro atrapa el caso más común — reenviar exactamente la misma foto —
sin gastar el presupuesto de 30 s de RNF-008 ni la llamada a la IA).

**Alternatives considered**: solo hash exacto (SHA-256) — rechazado por
ser trivialmente evadible (recomprimir la imagen cambia el hash).
Solo verificación remota vía EcoGPT (sin pre-filtro local) — funcionalmente
válida y más simple, pero implica depender de la red incluso para el caso
trivial de "la misma foto sin modificar", contradiciendo el espíritu de
RNF-004 (mantener la lógica de negocio desacoplada y no depender
innecesariamente de servicios externos para una validación que puede
resolverse localmente).

---

## 4. Cifrado Reversible de Contraseñas (JWK/JWE) y Gestión de Claves

**Decision**: librería **Nimbus JOSE+JWT** (`com.nimbusds:nimbus-jose-jwt`,
pura JVM/Kotlin, funciona sin cambios en Android) para representar la clave
de cifrado como un **JWK simétrico (`oct`, algoritmo `A256GCM`)** y la
contraseña cifrada como un **JWE en serialización compacta**. La clave JWK
se genera una única vez por instalación y se guarda **envuelta por Android
Keystore** usando `androidx.security.crypto` (`MasterKey` +
`EncryptedFile`), en un archivo separado del archivo de base de datos de
Room — nunca en una tabla de Room ni en `SharedPreferences` sin cifrar.

**Rationale**: satisface literalmente RNF-006 (contraseña cifrada en
formato JWK/JWE, nunca texto plano) y RNF-007 (clave en almacén separado
de la base de datos): Android Keystore es un almacén de claves respaldado
por hardware (TEE/StrongBox cuando está disponible) físicamente distinto
del archivo SQLite de Room, por lo que una filtración del archivo de base
de datos por sí sola no expone la clave. Nimbus JOSE+JWT es la
implementación de referencia del estándar JOSE en el ecosistema
Java/Kotlin, evitando implementar primitivas criptográficas a mano
(Principio V, evitar reinventar lógica sensible).

**Alternatives considered**: `androidx.security.crypto` `EncryptedSharedPreferences`
directamente (sin JOSE) — más simple, pero no produce un artefacto en
formato JWK/JWE como pide explícitamente RNF-006; se descartó por no
cumplir la letra del requisito. Implementación manual de AES-GCM sin
librería JOSE — rechazada por reinventar serialización/format que Nimbus
ya provee de forma auditada.

**Riesgo aceptado** (heredado de `spec.md` § Assumptions): a diferencia de
un hash unidireccional, la seguridad de la contraseña depende de que la
clave nunca se filtre junto con Room; Android Keystore mitiga esto porque,
en la mayoría de los dispositivos, ni siquiera el sistema operativo puede
exportar el material de la clave en texto plano (solo puede pedirle a
Keystore que cifre/descifre).

---

## 5. Sincronización de Puntos Verdes (RF-052)

**Decision**: el dataset de Puntos Verdes se empaqueta como un archivo
JSON en `assets/` (fuente inicial), se carga a Room en el primer arranque,
y un `Worker` de **WorkManager** (`PeriodicWorkRequest`, restringido a
`NetworkType.CONNECTED`) intenta refrescar el dataset contra el endpoint
descripto en `contracts/openapi.yaml` cuando hay conexión.

**Rationale**: WorkManager garantiza que la sincronización en segundo
plano respete las restricciones del sistema operativo (Doze, batería) y
se reintente automáticamente si falla, sin que la app necesite estar en
primer plano — exactamente el comportamiento que RF-052 pide ("actualización
en segundo plano cuando haya conexión disponible").

**Alternatives considered**: sincronizar solo al abrir la sección Puntos
Verdes (sin WorkManager) — más simple, pero no cumple "en segundo plano"
tal como lo redacta RF-052, y dejaría datos desactualizados si el usuario
no visita la sección seguido.

---

## 6. Visualización en Mapa de Puntos Verdes (RF-065, corrección post-QA 2026-09-22)

**Decision**: **OpenStreetMap** vía la librería **`osmdroid`**
(`org.osmdroid:osmdroid-android`), envuelta en Compose con `AndroidView`
(mismo patrón ya usado para `PreviewView` de CameraX). El `MapView` se
centra en el promedio de coordenadas de los Puntos Verdes encontrados para
el barrio del usuario (RF-066), con un `Marker` por punto.

**Rationale**: research.md §0 ya estableció que EcoSmart es local-first y
evita dependencias externas con fricción de configuración (se descartó
Firebase por el mismo motivo). Google Maps SDK exige crear un proyecto en
Google Cloud Console, generar una API key y habilitar facturación (aunque
tenga capa gratuita) — fricción incompatible con un proyecto académico sin
backend propio. `osmdroid` renderiza tiles de OpenStreetMap sin ninguna
clave ni cuenta, alineado con el resto de las decisiones técnicas del
proyecto.

**Alternatives considered**: **Google Maps SDK + Compose** (`maps-compose`)
— más pulido visualmente y con mejor soporte oficial de Google, pero
rechazado por la fricción de API key/facturación explicada arriba.
**Sin mapa, solo listado de texto** (como estaba hasta esta corrección) —
rechazado explícitamente por el usuario del proyecto al pedir "formato de
mapa".

**Alcance excluido**: no se implementa detección geográfica real
(point-in-polygon contra límites oficiales de barrio + GPS del usuario) —
decisión explícita documentada en `spec.md` § Assumptions
("Alcance de precisión geográfica"). El mapa muestra los Puntos Verdes que
ya coinciden por texto de barrio (RF-063), no reordena ni filtra por
proximidad real.
