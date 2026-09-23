# Quickstart: EcoSmart — Sistema Completo (MVP)

**Fase**: 1 (Design & Contracts) | **Fecha**: 2026-09-20 | **Plan**: [plan.md](./plan.md)

Guía de validación end-to-end del entorno de desarrollo, no un manual de
implementación (el código en sí se produce en `/speckit.implement`).

## 1. Prerrequisitos

| Herramienta | Versión mínima |
|---|---|
| Android Studio | Ladybug (2024.2) o superior |
| JDK | 17 |
| Android Gradle Plugin | 8.5+ |
| Kotlin | 2.0+ |
| `compileSdk` / `targetSdk` | 35 |
| `minSdk` | 26 |
| Dispositivo/emulador | Android 8.0+, con Google Play Services (para `FusedLocationProviderClient`) |

## 2. Clonar y abrir el proyecto

```bash
git clone <url-del-repositorio> EcoSmart
cd EcoSmart
# Abrir con Android Studio: File → Open → seleccionar la carpeta EcoSmart
```

## 3. Configurar secretos locales (`local.properties`)

`local.properties` **nunca se commitea** (ya está en `.gitignore` del
proyecto Android estándar). Agregar al final del archivo, en la raíz del
proyecto:

```properties
# API key del backend propio de EcoGPT (ver backend/README.md — es un secreto
# inventado por vos, ECOGPT_SHARED_API_KEY en Render, NO la key de Gemini)
ECOGPT_API_KEY=reemplazar-con-el-mismo-valor-de-ECOGPT_SHARED_API_KEY-en-render

# URL del backend real desplegado (ver backend/README.md §3); el placeholder
# https://api.ecogpt.example/v1/ nunca resuelve — usarlo deja toda
# verificación fallando con "Sin conexión a internet" (CORRECCIÓN-011)
ECOGPT_BASE_URL=https://ecosmart-ecogpt.onrender.com/

# URL base de la fuente de sincronización de Puntos Verdes de CABA
PUNTOS_VERDES_BASE_URL=https://datos.buenosaires.gob.ar/api/ecosmart-puntos-verdes/v1
```

Estas claves se exponen a `BuildConfig` mediante el bloque
`buildConfigField` de `app/build.gradle.kts`, nunca hardcodeadas en el
código fuente (consistente con Principio V y con RNF-006/RNF-007 sobre no
commitear secretos).

**Clave de cifrado de contraseñas (JWK) — nada que configurar a mano**: la
clave JWK que cifra las contraseñas (RNF-006/RNF-007) se genera
automáticamente en el primer arranque de la app y se guarda protegida por
**Android Keystore** (ver `research.md` §4); no requiere ninguna variable
en `local.properties` ni se genera manualmente.

## 4. Compilar y ejecutar

```bash
./gradlew assembleDebug
./gradlew installDebug   # con un emulador/dispositivo conectado
```

## 5. Escenarios de validación end-to-end

Estos escenarios usan directamente los criterios Given-When-Then de
`spec.md` como guion de prueba manual/exploratoria una vez implementado el
feature; no reemplazan a los tests automatizados (§6).

1. **Registro + preferencias** (US1, US2): registrar una cuenta nueva,
   marcar "Reciclar" y "Caminar" como categorías de interés → verificar
   que la Home solo muestra esas dos secciones.
2. **Caminata** (US8): aceptar una meta de caminata, caminar (o simular
   pasos en el emulador con `adb emu sensor set step-count <n>` si el
   emulador lo soporta, o mockeando `PodometroProvider` en debug) →
   verificar que se aprueba y suma 50 puntos por cada bloque de 133 pasos.
3. **Verificación con IA** (US9): enviar una foto de Reciclar → verificar
   los 3 caminos (Aprobado, Rechazado, Indeterminado) usando respuestas
   simuladas del backend de EcoGPT (mock server local, ver §7) y el caso
   de timeout forzando una respuesta > 30 s.
4. **Tope diario** (US10): completar 1 verificación Aprobada de Reciclar
   → intentar una segunda el mismo día → verificar que el sistema la
   bloquea antes de permitir el envío (RF-035).
5. **Permisos** (US13): denegar el permiso de Cámara → intentar verificar
   una actividad de Reciclar → verificar que se bloquea el formulario y
   se muestra la guía paso a paso hacia Ajustes.

## 6. Ejecutar la suite de pruebas automatizadas

```bash
# Tests unitarios de dominio y casos de uso (JUnit5 + MockK, sin Android)
./gradlew testDebugUnitTest

# Tests de integración (Room in-memory con Robolectric, y Compose UI Test / Espresso)
./gradlew connectedDebugAndroidTest   # requiere emulador/dispositivo conectado

# Suite completa (unitarios + lint + detekt/ktlint si están configurados)
./gradlew check
```

Las reglas críticas que **deben** tener test automatizado, según el
Principio VIII de `constitution.md` y §Requirements de `spec.md`, incluyen
como mínimo:

- Cálculo de puntos por categoría (RF-031 a RF-033, RF-049).
- Aplicación de topes diarios, incluyendo que `INDETERMINADO`/`RECHAZADO`
  no los consumen (RF-032/033/035, RF-050).
- Cálculo de nivel de usuario a partir del puntaje histórico (RF-046/047).
- Cálculo de racha, incluyendo el reinicio tras un día sin actividad
  (RF-038/039).
- Detección de duplicados por hash perceptual (RF-058/059).
- Mapeo de timeout/errores de EcoGPT a `ResultadoVerificacion` (RNF-008).

## 7. Mock server de EcoGPT para desarrollo/tests

Para no depender del backend real de EcoGPT (§8) durante el desarrollo
local ni en `connectedDebugAndroidTest`, se recomienda un servidor HTTP
embebido de pruebas (p. ej. `MockWebServer` de OkHttp) que sirva las
respuestas del contrato `contracts/openapi.yaml` (`/verificaciones`),
incluyendo una ruta configurable para simular una respuesta lenta (> 30 s)
y validar el manejo de timeout de RNF-008 sin depender de la
disponibilidad del backend real.

## 8. Backend real de EcoGPT

`https://api.ecogpt.example/v1/` (el default histórico de
`ECOGPT_BASE_URL`) es un dominio de documentación (RFC 2606) que nunca
resuelve — la app compila y corre igual, pero toda verificación de foto
falla con "Sin conexión a internet" hasta que se configura un backend
real (CORRECCIÓN-011/012, RF-073/RF-074). Ver `backend/README.md` para el
desarrollo local, cómo obtener una API key gratuita de Gemini (sin
tarjeta de crédito), y cómo desplegarlo en Render.
