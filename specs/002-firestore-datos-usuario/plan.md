# Implementation Plan: Persistencia Remota de Datos de Usuario en Firestore

**Branch**: `002-firestore-datos-usuario` | **Date**: 2026-10-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-firestore-datos-usuario/spec.md`

**Note**: Este plan fue generado por `/speckit.plan`. Depende de y se conecta
explícitamente con `001-ecosmart-mvp/plan.md` (ver spec.md → "Relación con la
Especificación 001"); no reemplaza ese plan, solo sustituye su mecanismo de
persistencia.

## Summary

Este módulo mueve la fuente de verdad de los datos de usuario de EcoSmart (perfil,
historial de verificaciones, racha, puntaje, caminata en curso, pasos del día) de Room
local-first (spec 001) a **Firestore** (Firebase), con **Firebase Authentication**
gestionando las credenciales (hash unidireccional, nunca visible desde Firestore ni
desde la app). Para impedir que el cliente Android falsifique puntos/racha/nivel
(RF-D014), se extiende el backend propio ya existente de EcoGPT (`backend/`, Python +
FastAPI, Render) con tres endpoints nuevos protegidos por verificación de ID Token de
Firebase: son el único camino autorizado para escribir esos campos en Firestore,
reforzado además por Reglas de Seguridad de Firestore que los bloquean para el cliente.
No se introduce Cloud Functions ni un segundo runtime de servidor (ver research.md §1).

## Technical Context

**Language/Version**: Kotlin 2.0+ (cliente, sin cambios respecto a spec 001) + Python
3.11 (extensión del backend existente, sin cambios de versión)

**Primary Dependencies**:
- Cliente: `firebase-bom`, `firebase-auth-ktx`, `firebase-firestore-ktx`,
  `com.google.android.gms:play-services-auth` (login con Google, ver research.md §2);
  Hilt (DI, ya presente) para proveer las instancias de `FirebaseAuth`/`FirebaseFirestore`.
- Backend: `firebase-admin` (Python, nuevo) agregado a `requirements.txt`; FastAPI/
  Uvicorn sin cambios.

**Storage**: Firestore (Firebase) — única fuente de verdad remota y autoritativa de
datos de usuario (constitution.md v2.0.0, Principio IV). Room/SharedPreferences, si se
conservan, actúan únicamente como caché de lectura reconstruible (RF-D006) o estado
técnico efímero no sujeto a reglas de negocio (p. ej. la bandera de migración completada,
ver research.md §6); dejan de ser consultados para decisiones de negocio tras la
migración (RF-D011).

**Testing**: Firebase Local Emulator Suite (Firestore + Authentication) para tests de
integración de ambos lados (JUnit5/MockK/Turbine + Robolectric del cliente, pytest del
backend), evitando consumir la cuota gratuita real (ver research.md §5). Las Reglas de
Seguridad se testean con el SDK de testing de reglas de Firebase (`@firebase/rules-unit-testing`
o el Rules Playground, ver quickstart.md §7), no con tests unitarios de Kotlin/Python.

**Target Platform**: Android nativo, mismo `minSdk` 26 / `compileSdk`-`targetSdk` 35 de
spec 001; backend extendido sobre el mismo despliegue de Render (free tier) de EcoGPT.

**Project Type**: mobile-app + backend (mismo módulo único `:app` y mismo servicio
`backend/` de spec 001 — no se agregan módulos Gradle ni servicios nuevos, solo paquetes/
archivos dentro de los ya existentes).

**Performance Goals**: SC-D002 (sincronización entre dispositivos reflejada en <10s bajo
demanda, sin listener permanente — RF-D017); SC-D005 (eliminación de cuenta verificable
en ≤60s); mitigación de cold-start de Render heredada de RF-079/RF-D015 (ping + 1
reintento automático, RF-D016).

**Constraints**: cuota diaria gratuita de Firestore compartida entre todos los usuarios
del proyecto (capa Spark) — tratada como falla transitoria más, nunca como error
distinto a nivel de UX (Assumptions de spec.md); ningún campo de
`puntosHistoricos`/`rachaActual`/`nivel`/`resultado`/`puntosOtorgados` puede ser escrito
por el cliente Android bajo ninguna circunstancia (RF-D014, reforzado en dos capas:
Reglas de Seguridad + ausencia de ese código en el cliente).

**Scale/Scope**: 5 historias de usuario, 17 RF-D, 6 SC-D definidos en `spec.md`; 3
endpoints nuevos de backend (`contracts/openapi.yaml`); 5 colecciones/documentos de
Firestore (`data-model.md`).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Contra `constitution.md` **v2.0.0** (incluye la enmienda que habilita Firestore/Firebase
Authentication como Infraestructura autoritativa, motivada por este mismo módulo).

| # | Principio | Estado | Cómo lo satisface este plan |
|---|-----------|--------|------------------------------|
| I | La Especificación Manda (NON-NEGOTIABLE) | ✅ PASS | Cada decisión de este plan traza a un RF-D/SC-D/Clarification numerado de `spec.md`; ninguna pieza nueva (backend de confianza, Reglas de Seguridad, migración) carece de requisito que la respalde. |
| II | Restricción Estricta de Fases (NON-NEGOTIABLE) | ✅ PASS | Este comando solo produce `plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`; no se escribió código fuente de la app ni del backend. |
| III | Modelado de Dominio con POO Real | ⚠ REIMPLEMENTACIÓN CONTROLADA | Las reglas de negocio ricas ya definidas en spec 001 (`Usuario.nivel()`, `sumarPuntos()`, `registrarActividadAprobadaHoy()`, `RegistroVerificacion.esDuplicadoDe()`, `AplicarTopeDiario`) DEBEN **reimplementarse** en Python dentro del backend de confianza (`backend/app/puntos.py`) — el Kotlin de spec 001 no ejecuta dentro de FastAPI, por lo que no hay un "traslado sin reescritura" literal. Para evitar divergencia entre ambas implementaciones (Principio V, "lógica de negocio duplicada en más de un lugar"), T011d (`tasks.md`) exige un test de paridad que corra los mismos casos de tope diario/racha/nivel/duplicado contra el dominio Kotlin de spec 001 y la reimplementación Python de este módulo. |
| IV | Separación Estricta de Capas | ✅ PASS | Firestore/Firebase Authentication son Infraestructura (constitution.md v2.0.0); el backend de confianza es también Infraestructura (adaptador externo), nunca Dominio. El cliente Android no gana ninguna lógica de negocio nueva: solo construye requests hacia el backend de confianza y lee Firestore. |
| V | Calidad y Claridad de Código | ✅ PASS | Los 3 endpoints nuevos (`contracts/openapi.yaml`) tienen responsabilidad única cada uno (research.md §4); ninguno mezcla "hablar con Gemini" con "otorgar puntos". |
| VI | Tipado y Valores Cerrados (Enums) | ✅ PASS | `data-model.md` reusa los mismos enums de spec 001 (`CategoriaActividad`, `ResultadoVerificacion`, `TipoPermiso`, `EstadoPermiso`) serializados como `string` en Firestore/JSON, sin introducir valores libres nuevos. |
| VII | Diseño Pragmático (Patrones Solo si Simplifican) | ✅ PASS | research.md §1 justifica explícitamente por qué se rechaza Cloud Functions (infraestructura duplicada sin beneficio funcional) a favor de extender el backend ya existente. |
| VIII | Calidad de Pruebas | ⚠ PENDIENTE EN `/speckit.tasks` | research.md §5 define la estrategia (Firebase Local Emulator Suite + testing de Reglas de Seguridad); las tareas concretas de test por RF-D se generan en `/speckit.tasks`, no en este plan. |
| IX | Experiencia de Usuario (UX) Amena y Motivadora | ✅ PASS | RF-D016 (fallas no transitorias) exige mensaje explícito con acción concreta en tono no punitivo, igual que el resto de la app; ninguna pantalla nueva introduce lenguaje culpabilizador. |

**Gate adicional de v2.0.0 (Persistencia/Infraestructura)**: ✅ PASS — Firestore es la
única fuente de verdad de los datos de usuario cubiertos por este spec (RF-D001 a
RF-D013); Room/SharedPreferences quedan limitados a caché reconstruible o estado técnico
efímero (RF-D006, research.md §6), consistente con el Principio IV enmendado.

*Post-Fase 1 (re-chequeo tras data-model.md/contracts/)*: ✅ PASS sin cambios — el
diseño de colecciones y Reglas de Seguridad (data-model.md §6, contracts/firestore.rules)
no introdujo ninguna necesidad de lógica de negocio en el cliente ni en Firestore mismo
(las reglas son control de acceso, no reglas de negocio); toda regla de negocio sigue
viviendo en el backend de confianza, **reimplementada** a partir del dominio de spec 001
y validada contra él mediante un test de paridad (T011d, ver corrección de Principio III
arriba).

## Project Structure

### Documentation (this feature)

```text
specs/002-firestore-datos-usuario/
├── spec.md               # Ya existente (speckit.specify + speckit.clarify + speckit.checklist)
├── plan.md               # Este archivo
├── research.md           # Fase 0 — decisiones de este plan
├── data-model.md         # Fase 1 — esquema de Firestore
├── quickstart.md         # Fase 1 — guía de validación
├── contracts/
│   ├── openapi.yaml       # Endpoints nuevos del backend de confianza
│   └── firestore.rules    # Reglas de Seguridad de Firestore
├── checklists/
│   ├── requirements.md    # Checklist de calidad de speckit.specify (100%)
│   └── quality.md         # Checklist completitud/claridad/consistencia (100%)
└── tasks.md              # Fase 2 — generado por /speckit.tasks (NO por este comando)
```

### Source Code (repository root)

No se agregan módulos Gradle ni servicios nuevos — se extienden los dos proyectos ya
existentes del repositorio:

```text
app/src/main/kotlin/com/ecosmart/
├── domain/                          # Sin cambios de este módulo (reglas de negocio
│                                     #   ya trasladadas al backend de confianza, no
│                                     #   duplicadas aquí — ver Constitution Check III/IV)
├── infrastructure/
│   ├── firebase/                    # NUEVO paquete de este módulo
│   │   ├── FirebaseAuthModule.kt      # Hilt: provee FirebaseAuth
│   │   ├── FirestoreModule.kt         # Hilt: provee FirebaseFirestore
│   │   └── DispositivoIdProvider.kt   # Identificador estable de instalación (RF-D009/caminar)
│   ├── network/
│   │   └── BackendConfianzaClient.kt  # Retrofit: consume contracts/openapi.yaml (este módulo)
│   ├── persistence/room/            # Pasa a rol de caché reconstruible (RF-D006);
│   │                                 #   sin nuevas entidades de negocio en este módulo
│   └── session/
│       └── SesionUsuario.kt          # Reemplazado por el estado de sesión nativo de
│                                      #   FirebaseAuth (ver research.md §2); deja de
│                                      #   usar SharedPreferences propias para el uid
└── ...                               # resto de capas sin cambios estructurales

backend/
├── app/
│   ├── main.py                      # Sin cambios (POST /verificaciones, GET /health)
│   ├── puntos.py                    # NUEVO — endpoints de contracts/openapi.yaml
│   │                                 #   (/registros-verificacion/*, /migracion/*)
│   └── firebase_admin_setup.py      # NUEVO — inicialización de firebase-admin +
│                                      #   verificación de ID Token (research.md §3)
└── requirements.txt                 # + firebase-admin
```

**Structure Decision**: Se mantiene el mismo módulo único `:app` (Principio VII, spec
001) y el mismo servicio `backend/` (research.md §1) — este módulo solo agrega paquetes/
archivos dentro de la estructura ya aprobada en `001-ecosmart-mvp/plan.md`, sin
introducir un segundo proyecto, módulo Gradle o servicio de servidor.

## Complexity Tracking

*Sin violaciones de la Constitution Check que requieran justificación — tabla omitida.*
