# Implementation Plan: Reconstrucción de la Sección Educación

**Branch**: `004-redisenio-ui-educacion` | **Date**: 2026-10-30 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-redisenio-ui-educacion/spec.md`

**Note**: Este plan depende de `001-ecosmart-mvp` (módulo `:app`, dominio), `002-firestore-datos-usuario`
(persistencia, nunca tocada) y `003-reconstruccion-ui` (sistema de diseño `C-01`–`C-11`, barra
inferior, encabezado). Solo agrega/restyla la capa Frontend (UI) y, en lo mínimo necesario,
Presentación. `setup-plan.sh` está roto en esta máquina (ver memoria del repo), por lo que los
documentos de este directorio se crearon a mano siguiendo `plan-template.md`.

## Summary

Se reemplaza la pantalla Educación (segmento Videos/Artículos/IA + tarjetas de video + catálogo
embebido en código con emojis) por un catálogo de **lecciones ilustradas** agrupadas por
categoría, una pantalla hija **Detalle de lección** (portada, introducción, pasos numerados con
conector, tip opcional y tarjeta de cierre), y las tarjetas educativas de Inicio, que dejan de
ser tarjetas de video `C-09`.

Enfoque técnico (detalle en `research.md`):

- El catálogo vive en `app/src/main/assets/educacion/lecciones.json`, se interpreta con **Moshi**
  (ya presente) a través de un parser puro (`String → resultado`) testeable en JVM, y se expone
  mediante un `CatalogoLecciones` singleton con caché en memoria y `recargar()` para "Reintentar".
- Las ilustraciones son **VectorDrawable XML** (`res/drawable/il_leccion_*.xml`) referenciados
  desde el JSON por nombre y resueltos en tiempo de ejecución, para que sumar o reemplazar una
  ilustración no requiera tocar código (RF-ED-016, SC-ED-008). Se crean vectores simples para la
  lección de ejemplo (portada + 5 pasos).
- Se reutilizan las rutas existentes `contenido-educativo` y `contenido-educativo/{id}` (el
  detalle pasa a ser el Detalle de lección), evitando renombrar destinos y la barra inferior.
- La relación lección→práctica ya no es por actividad: la tarjeta de cierre abre la pestaña
  Actividades (`RF-ED-012`, `RF-ED-015`), porque los `ActividadId` del repositorio son UUID
  aleatorios (`ActividadId.nuevo()`) y no pueden referenciarse desde un archivo.
- Ningún archivo de `domain/`, `application/` ni `infrastructure/` se modifica.

## Technical Context

**Language/Version**: Kotlin 2.0.20 (sin cambios)

**Primary Dependencies**: Jetpack Compose (BOM 2024.09.02) + Material3, Navigation Compose 2.8.0,
Hilt 2.52 + Hilt Navigation Compose 1.2.0, Moshi + `moshi-kotlin-codegen` (ya en `app/build.gradle.kts`
por el cliente EcoGPT). **Sin dependencias nuevas** (ni Coil/Glide: las ilustraciones son recursos locales).

**Storage**: N/A. El catálogo es un asset de solo lectura empaquetado en el APK; no hay
persistencia de progreso ni de "lección leída" (fuera de alcance).

**Testing**: JUnit 5 + MockK (ya usados en `app/src/test`) para parser/catálogo/agrupación;
Compose UI Test (`createComposeRule`, ya usado en `app/src/androidTest`) para pantallas y
tarjetas. Ver research.md §9.

**Target Platform**: Android nativo, `minSdk` 26 / `compileSdk`-`targetSdk` 35 (sin cambios).

**Project Type**: mobile-app — módulo único `:app`; `backend/` no se toca.

**Performance Goals**: Lectura del JSON (≈ pocos KB) en `Dispatchers.IO` sin bloquear el hilo
principal; sin metas de red. Sin animaciones (`RF-ED-020`).

**Constraints**: `RF-UI-001` (no tocar Dominio/Aplicación/Infraestructura), `RF-UI-004` (tokens
únicos), `RF-ED-021` (contraste 4,5:1, zonas táctiles 48×48, texto al 130% con alturas mínimas),
sin animaciones ni transiciones, una sola fuente de verdad visual (Constitución v2.3.0).

**Scale/Scope**: 2 pantallas (Educación reescrita + Detalle de lección nueva), 1 sección
modificada en Inicio, 6 componentes nuevos (`C-13` a `C-18`), ~20 tokens nuevos, 1 asset JSON,
7 vectores (6 ilustraciones + 1 de respaldo si diseño lo pide), 0 endpoints, 0 entidades de dominio.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Contra `constitution.md` **v2.3.0**.

| # | Principio | Estado | Cómo lo satisface este plan |
|---|-----------|--------|------------------------------|
| I | La Especificación Manda | ✅ PASS | Cada componente y archivo traza a un `RF-ED-xxx`/`SC-ED-xxx`; la tabla de trazabilidad está en `data-model.md` y `research.md` §11. Las decisiones de última hora (cierre a Actividades en general, vectores, origen de pestaña) ya están en `spec.md` → Clarifications. |
| II | Restricción Estricta de Fases | ✅ PASS | Este comando solo produce documentos (`plan.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/`); no se escribió código de la app. |
| III | Modelado de Dominio con POO Real | ✅ PASS (no aplica) | No se agregan ni cambian entidades de dominio. `LeccionUi` es un modelo de Presentación sin reglas de negocio (puntaje, topes, rachas). |
| IV | Separación Estricta de Capas | ✅ PASS | Todo en `presentation/` (+ assets y `res/`). El catálogo reutiliza `CategoriaActividad` solo como tipo de lectura. Leer un asset empaquetado no es acceso a sensores ni a Room (justificación en research.md §3). |
| V | Calidad y Claridad de Código | ✅ PASS | Parser, catálogo, mapeos y pantallas en archivos separados con métodos ≤ 40 líneas; sin `is` para ramificar lógica de negocio: el resultado del catálogo es una `sealed class` y las categorías se mapean con `when` exhaustivo. |
| VI | Tipado y Valores Cerrados | ✅ PASS | `ResultadoCatalogo` (sealed) y `CategoriaActividad` (enum existente, **sin valores nuevos**). La categoría del JSON se valida contra el enum; una categoría desconocida descarta la lección. |
| VII | Diseño Pragmático | ✅ PASS | Se descarta Coil/Glide, `kotlinx.serialization`, Room/DataStore y una capa repositorio/use case: un parser puro + un singleton con caché resuelven el problema (research.md §2–§4). |
| VIII | Calidad de Pruebas | ✅ PASS (plan) | Estrategia definida en research.md §9; las tareas concretas se generan en `/speckit.tasks`. No hay regla de negocio nueva que exija tests de dominio; sí se prueba el parser (reglas de validación del catálogo) y la UI. |
| IX | UX Amena y Motivadora | ✅ PASS | Mensajes del estado de error y vacío redactados sin tono punitivo ("No pudimos cargar las lecciones", con acción "Reintentar"); cierre invita ("Poné en práctica lo que aprendiste"). |
| X | Reconstrucción de UI Basada en Especificación | ✅ PASS | Fuente de verdad: tableros E1/E2 del lienzo Claude Design para Educación y sus tarjetas en Inicio (enmienda v2.2.0/v2.3.0); resto de pantallas sin cambios; sin lógica de negocio. Se omite deliberadamente `C-12` (sin tarjeta destacada) por decisión de spec. |

**Gate adicional (hallazgo de este plan)**: los `ActividadId` se generan con `UUID.randomUUID()`
en cada instalación (`ActividadRepositoryImpl`, `ActividadId.nuevo()`), por lo que ninguna lección puede guardar
un identificador de actividad. Es coherente con `RF-ED-015`; no requiere cambios de Dominio. ✅ PASS.

*Post-Fase 1 (re-chequeo tras data-model.md/contracts/quickstart.md)*: ✅ PASS sin cambios —
los artefactos de Fase 1 describen un contrato de archivo JSON, modelos de Presentación y
tokens; no aparece ninguna necesidad de lógica de negocio.

## Project Structure

### Documentation (this feature)

```text
specs/004-redisenio-ui-educacion/
├── spec.md                 # speckit.specify + clarify
├── plan.md                 # Este archivo
├── research.md             # Fase 0
├── data-model.md           # Fase 1 — modelos de Presentación, estados, tokens, trazabilidad
├── quickstart.md           # Fase 1 — validación visual / accesibilidad / navegación
├── contracts/
│   └── lecciones.schema.json   # Fase 1 — contrato del archivo del catálogo
├── checklists/
│   ├── requirements.md
│   └── consistencia.md
└── tasks.md                # Fase 2 (/speckit.tasks — NO creado por este comando)
```

### Source Code (repository root)

```text
app/src/main/
├── assets/educacion/
│   └── lecciones.json                      # NUEVO — catálogo (contrato en contracts/)
├── res/drawable/
│   ├── il_leccion_circuito_portada.xml     # NUEVO — vectores simples de reemplazo (RF-ED-016)
│   ├── il_leccion_circuito_paso1.xml … paso5.xml
│   └── ic_bombilla.xml                     # NUEVO — ícono del tip (C-16)
├── res/raw/keep.xml                        # NUEVO — protege il_leccion_* de la poda de recursos
└── kotlin/com/ecosmart/presentation/
    ├── education/                          # NUEVO paquete (reemplaza home/ContenidoEducativoScreen.kt)
    │   ├── LeccionUi.kt                    # modelos de Presentación (data-model.md)
    │   ├── CatalogoLeccionesParser.kt      # String → ResultadoCatalogo (puro, sin Android)
    │   ├── CatalogoLecciones.kt            # @Singleton: lee el asset, caché, recargar()
    │   ├── EducacionViewModel.kt           # estado del listado (Cargando/Cargado/Vacio/Error)
    │   ├── EducacionScreen.kt              # S-05 rediseñada (RF-ED-001…008)
    │   ├── DetalleLeccionViewModel.kt      # lección por id (SavedStateHandle)
    │   ├── DetalleLeccionScreen.kt         # pantalla hija (RF-ED-009…012)
    │   └── TarjetasEducativasInicio.kt     # sección "Contenido para vos" de Inicio (RF-ED-013)
    ├── theme/
    │   ├── ComponentesLeccion.kt           # NUEVO — C-13…C-18 + IlustracionLeccion
    │   ├── Tokens.kt                       # + radios, espacios, tipografías de Educación
    │   ├── Theme.kt                        # + colores pastel/texto secundario/tip (alias de existentes)
    │   └── ComponentesEcoSmart.kt          # EncabezadoEcoSmart: estilo de título/hojita inline (cambio mínimo)
    ├── comun/CategoriaUi.kt                # + estilo de chip y fondo pastel por categoría (when exhaustivo)
    ├── home/HomeScreen.kt                  # reemplaza el carrusel TarjetaVideo por TarjetasEducativasInicio
    ├── home/ContenidoEducativoScreen.kt    # ELIMINADO (segmento, catálogo con emojis, detalle viejo)
    └── EcoSmartNavHost.kt                  # rutas existentes → nuevas pantallas; origen de pestaña; "Ver actividades"

app/src/test/kotlin/com/ecosmart/presentation/education/
├── CatalogoLeccionesParserTest.kt          # NUEVO (JUnit 5)
└── AgrupacionLeccionesTest.kt              # NUEVO (JUnit 5)

app/src/androidTest/kotlin/com/ecosmart/presentation/
├── education/EducacionScreenTest.kt        # NUEVO (Compose UI Test)
├── education/DetalleLeccionScreenTest.kt   # NUEVO
├── theme/ComponentesLeccionTest.kt         # NUEVO — tarjetas, chip, paso, tip, cierre
└── home/HomeScreenTest.kt                  # AJUSTE — tarjetas de lección en lugar de video
```

**Structure Decision**: módulo único `:app`, nuevo paquete `presentation/education/` (la
funcionalidad dejó de ser "contenido embebido" en `home/`). Los componentes visuales `C-13`–`C-18`
van a `theme/ComponentesLeccion.kt` para respetar `RF-UI-004`/`RF-UI-005` (un único lugar de
sistema de diseño) sin inflar `ComponentesEcoSmart.kt`. `TarjetaVideo` (`C-09`) queda sin usos
tras este cambio; su retiro se decide en `/speckit.tasks` (research.md §10).

## Complexity Tracking

*Sin violaciones de la Constitution Check que requieran justificación — tabla omitida.*
