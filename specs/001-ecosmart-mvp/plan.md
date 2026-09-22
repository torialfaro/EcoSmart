# Implementation Plan: EcoSmart — Sistema Completo (MVP)

**Branch**: `001-ecosmart-mvp` | **Date**: 2026-09-20 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-ecosmart-mvp/spec.md`

**Note**: Este plan fue generado por `/speckit.plan`. Ver `.specify/templates/plan-template.md` para el flujo de ejecución del comando.

## Summary

EcoSmart es una app Android nativa que gamifica hábitos sostenibles (Reciclar,
Reutilizar, Caminar) mediante un sistema de puntos, niveles y verificación
automática de actividades: el podómetro nativo valida las caminatas y **EcoGPT**
(IA externa) valida fotos de reciclaje/reutilización. El enfoque técnico es una
**arquitectura Android local-first**: un único módulo Gradle (`:app`) separado
en capas por paquete (Dominio / Aplicación / Infraestructura / Presentación,
Principio IV), con **Room** como base de datos local y **Android Keystore**
como almacén de claves separado para el cifrado JWK/JWE de contraseñas
(RNF-006/RNF-007). Las únicas dependencias de red externas son **EcoGPT**
(verificación de fotos) y la fuente de datos de **Puntos Verdes de CABA**
(sincronización en segundo plano); no se introduce un backend propio de
EcoSmart porque ni `constitution.md` ni `spec.md` lo requieren (ver
`research.md` → Decisión de Arquitectura).

## Technical Context

**Language/Version**: Kotlin 2.0+ (JVM target 17), Android Gradle Plugin 8.x

**Primary Dependencies**: Jetpack Compose (UI), Hilt (inyección de
dependencias), Room (persistencia local), WorkManager (sincronización en
segundo plano de Puntos Verdes), Retrofit + OkHttp (cliente HTTP para EcoGPT
y sincronización de Puntos Verdes), CameraX + Photo Picker (foto/galería),
FusedLocationProviderClient de Google Play Services (GPS), Sensor API nativa
`TYPE_STEP_COUNTER`/`TYPE_STEP_DETECTOR` (podómetro), Nimbus JOSE+JWT
(cifrado JWK/JWE de contraseñas), Jetpack Security Crypto + Android Keystore
(protección de la clave de cifrado)

**Storage**: Room (SQLite) — única base de datos de la app, local al
dispositivo. Android Keystore como almacén de claves separado (no es una
tabla de Room) para la clave JWK que cifra las contraseñas.

**Testing**: JUnit5 + MockK + Turbine para tests unitarios de dominio y
casos de uso (sin dependencias de Android); Robolectric + Room in-memory
para tests de integración de repositorios/DAOs; Compose UI Test / Espresso
para tests de presentación críticos (flujo de verificación, permisos).

**Target Platform**: Android nativo, `minSdk` 26 (Android 8.0 — cobertura
amplia del Sensor API de podómetro), `targetSdk`/`compileSdk` 35.

**Project Type**: mobile-app (single Gradle module `:app`, separación por
paquete en vez de módulos Gradle — ver Project Structure y Principio VII).

**Performance Goals**: timeout de EcoGPT ≤ 30 s (RNF-008/SC-008); navegación
a actividades/perfil/Puntos Verdes en ≤ 2 toques desde el menú principal
(RNF-002); Home debe renderizar la lista de actividades ya filtrada por
categoría sin bloquear el hilo principal (uso de `Flow`/coroutines para
lectura de Room).

**Constraints**: offline-parcial — el catálogo de actividades, el perfil,
el historial y los Puntos Verdes ya sincronizados deben verse sin conexión
(Room es la fuente de verdad local); la verificación de Reciclar/Reutilizar
y la sincronización de Puntos Verdes SÍ requieren conexión (ver Edge Cases
de `spec.md` para el comportamiento ante corte de red). La clave de cifrado
de contraseñas NUNCA debe residir en la misma tabla/base que las
contraseñas cifradas (RNF-007).

**Scale/Scope**: MVP de un solo dispositivo por usuario (spec.md, Assumptions:
"cada usuario tiene una única cuenta activa por sesión"); 13 historias de
usuario, 59 RF, 8 RNF definidos en `spec.md`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Principio | Estado | Cómo lo satisface este plan |
|---|-----------|--------|------------------------------|
| I | La Especificación Manda (NON-NEGOTIABLE) | ✅ PASS | Todo artefacto de este plan (entidades, contratos, estructura) se deriva de RF/RNF/US numerados en `spec.md`; ninguna funcionalidad nueva se introduce sin trazabilidad. |
| II | Restricción Estricta de Fases (NON-NEGOTIABLE) | ✅ PASS | Este comando solo produce documentos de diseño (`plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`); no se escribe código fuente de la app. La implementación queda reservada a `/speckit.implement`. |
| III | Modelado de Dominio con POO Real | ✅ PASS | `data-model.md` define entidades ricas (`Actividad.puedeRepetirseHoy()`, `Usuario.calcularNivel()`, `RegistroVerificacion.esDuplicadoDe()`), no clases anémicas. |
| IV | Separación Estricta de Capas | ✅ PASS | Project Structure define paquetes `domain` / `application` / `infrastructure` / `presentation` con regla de dependencia unidireccional; `domain` no importa Android SDK ni Room. |
| V | Calidad y Claridad de Código | ✅ PASS (gate de diseño; se re-verifica en `/speckit.implement`) | Los casos de uso en `application` son de responsabilidad única (uno por verbo de negocio: `RegistrarCaminata`, `VerificarFotoConIA`, etc.); no se definen "god services". |
| VI | Tipado y Valores Cerrados | ✅ PASS | `data-model.md` define 4 `enum class` de Kotlin (`CategoriaActividad`, `ResultadoVerificacion`, `EstadoPermiso`, `NivelUsuario`) para todo valor cerrado identificado en `spec.md`. |
| VII | Diseño Pragmático | ✅ PASS | Se justifica cada patrón usado (Repository, Strategy para el cálculo de puntos por categoría, Factory para el mapeo IA→estado) en la sección "Patrones de Diseño Aplicados" más abajo; se descartó explícitamente multi-módulo Gradle y un backend propio por no estar justificados (ver Complexity Tracking y `research.md`). |
| VIII | Calidad de Pruebas | ✅ PASS (gate de diseño) | `quickstart.md` documenta cómo correr la suite; `data-model.md` y los casos de uso quedan diseñados para ser testeables sin Android (dominio puro). Las reglas críticas de `spec.md` (topes diarios, niveles, timeout, duplicados) quedan mapeadas 1:1 a casos de uso testeables. |
| IX | UX Amena y Motivadora | ✅ PASS | `data-model.md` y `contracts/openapi.yaml` modelan el estado "Indeterminado"/timeout con mensajes de causa + acción (no solo códigos de error), consistente con RNF-001 y el tono no punitivo. |

**Resultado**: 9/9 PASS. Ninguna violación requiere entrada en Complexity
Tracking más allá de las dos decisiones ya justificadas ahí (single-module
vs. multi-module, y "sin backend propio").

**Re-chequeo post-diseño (tras Fase 1 — `research.md`, `data-model.md`,
`contracts/openapi.yaml`, `quickstart.md`)**: se revisaron los 4 artefactos
generados contra los 9 principios; no se detectó ninguna violación nueva.
En particular: `data-model.md` mantiene el dominio libre de imports de
Room/Android (IV), encapsula reglas de negocio en las entidades en vez de
en DAOs o ViewModels (III), y los 4 `enum class` cubren todos los valores
cerrados identificados en `spec.md` (VI). `contracts/openapi.yaml` modela
`motivo` como obligatorio en rechazos/indeterminados, preservando el tono
explicativo no punitivo (IX). Sigue en **9/9 PASS**.

## Project Structure

### Documentation (this feature)

```text
specs/001-ecosmart-mvp/
├── plan.md              # Este archivo (/speckit.plan)
├── research.md          # Fase 0 (/speckit.plan)
├── data-model.md         # Fase 1 (/speckit.plan)
├── quickstart.md         # Fase 1 (/speckit.plan)
├── contracts/
│   └── openapi.yaml      # Fase 1 (/speckit.plan) — contratos EcoGPT y Puntos Verdes
├── checklists/
│   ├── requirements.md
│   └── functional-quality.md
└── tasks.md              # Fase 2 (/speckit.tasks — NO se crea en este comando)
```

### Source Code (repository root)

**Structure Decision**: módulo Gradle único `:app` con separación estricta
por paquete (no por módulo Gradle). Se descartó una estructura multi-módulo
(`:core:domain`, `:core:data`, `:feature:*`) porque, para el tamaño de este
MVP (13 US, sin equipos paralelos trabajando en features aisladas), agrega
tiempo de build y complejidad de configuración no justificados —decisión
documentada como excepción pragmática del Principio VII en Complexity
Tracking. La regla de dependencia (UI → Presentación → Aplicación →
Dominio; Infraestructura implementa interfaces de Dominio/Aplicación) se
hace cumplir por convención de paquete + `internal`/`ktlint` (detección de
imports de Android SDK dentro de `domain` se agrega como chequeo de calidad
en `/speckit.implement`, no como límite físico de módulo).

```text
app/
├── src/main/kotlin/com/ecosmart/
│   ├── domain/                       # Kotlin puro — CERO imports de Android/Room
│   │   ├── model/                    # Usuario, Actividad, RegistroVerificacion,
│   │   │                             # PuntoVerde, PermisoDispositivo (entidades ricas)
│   │   ├── valueobject/              # Enums cerrados: CategoriaActividad,
│   │   │                             # ResultadoVerificacion, EstadoPermiso, NivelUsuario
│   │   └── repository/               # Interfaces (contratos) que Infraestructura implementa
│   │
│   ├── application/                  # Casos de uso (orquestan Dominio, sin lógica propia)
│   │   ├── auth/                     # RegistrarUsuario, IniciarSesion, EditarPerfil
│   │   ├── activity/                 # RegistrarCaminata, VerificarFotoConIA,
│   │   │                             # AplicarTopeDiario, CalcularPuntaje
│   │   ├── profile/                  # CalcularNivelUsuario, CalcularRacha, ObtenerHistorial
│   │   └── permission/               # EvaluarEstadoPermiso
│   │
│   ├── infrastructure/               # Implementaciones concretas
│   │   ├── persistence/room/         # AppDatabase, *Dao, *RoomEntity, *Mapper
│   │   ├── security/                 # JweGestorClaves (JWK en Android Keystore),
│   │   │                             # CifradorContrasena
│   │   ├── sensors/                  # PodometroProvider, UbicacionProvider, CamaraProvider
│   │   ├── network/                  # EcoGptClient (Retrofit), PuntosVerdesSyncClient,
│   │   │                             # WorkManager Worker de sincronización
│   │   └── di/                       # Módulos Hilt (bindings de repository → impl)
│   │
│   └── presentation/                 # ViewModels + Compose UI, sin lógica de negocio
│       ├── auth/
│       ├── home/
│       ├── activitydetail/
│       ├── verification/
│       ├── profile/
│       └── permissions/
│
└── src/test/kotlin/com/ecosmart/     # Unit tests (domain + application, sin Android)
    └── androidTest/kotlin/com/ecosmart/  # Integration/UI tests (Room, Compose)
```

## Patrones de Diseño Aplicados *(Principio VII — solo si simplifican)*

- **Repository**: `domain/repository/*Repository` (interfaces) implementadas
  en `infrastructure/persistence/room`. Justificación: permite testear los
  casos de uso de `application` con un fake/in-memory repository sin Room ni
  Android, y aísla un eventual cambio de motor de persistencia.
- **Strategy**: una `EstrategiaDePuntaje` por `CategoriaActividad`
  (`CaminataPuntajeStrategy`, `FotoPuntajeStrategy` parametrizada por
  puntos/tope) para evitar un `when` gigante y repetido en cada caso de uso
  que calcula puntos. Justificación: RF-031/RF-032/RF-033 tienen fórmulas
  distintas por categoría; sin Strategy, la lógica se duplicaría entre
  `RegistrarCaminata` y `VerificarFotoConIA`.
- **Factory** (simple, función factory, no clase): `ResultadoVerificacion.desde(respuestaEcoGpt)`
  mapea la respuesta cruda de EcoGPT al enum de dominio. Justificación:
  centraliza en un solo lugar la interpretación del contrato externo
  (`contracts/openapi.yaml`), evitando que la capa de infraestructura
  "filtre" strings crudos de la IA hacia el dominio.
- **Explícitamente NO aplicados**: Observer/EventBus (WorkManager + Flow de
  Room ya cubren la reactividad necesaria), Singleton manual (Hilt provee
  scopes), Builder (las entidades del dominio son pequeñas; un `data class`
  con `copy()` alcanza). Forzarlos violaría el Principio VII.

## Complexity Tracking

> Fill ONLY if Constitution Check has violations that must be justified

| Decisión que se aparta del "default" | Por qué se necesita | Alternativa más simple descartada y por qué |
|---|---|---|
| Módulo Gradle único (`:app`) en vez de multi-módulo por capa | El MVP tiene 13 US y un equipo pequeño; separar en `:core:domain`, `:core:data`, `:feature:*` no reduce el tiempo de build de forma medible a esta escala. | Multi-módulo Gradle — rechazado por agregar configuración de build (versión catalogs, API vs implementation deps entre módulos) sin beneficio medible para este alcance. |
| Sin backend propio de EcoSmart (arquitectura local-first) | Ni `constitution.md` (stack: "Android Nativo… Persistencia Local: Room/SharedPreferences") ni ningún RF/RNF de `spec.md` piden sincronización multi-dispositivo; las únicas dependencias externas declaradas son EcoGPT y la fuente de Puntos Verdes de CABA. | Backend propio (p. ej. Ktor + PostgreSQL) para centralizar usuarios — rechazado por estar fuera del alcance declarado en `constitution.md` y no ser requerido por ningún RF; ver detalle y riesgo en `research.md`. |
