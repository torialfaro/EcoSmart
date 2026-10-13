# Implementation Plan: Reconstrucción Visual de la Interfaz de Usuario

**Branch**: `003-reconstruccion-ui` | **Date**: 2026-10-12 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-reconstruccion-ui/spec.md`

**Note**: Este plan fue generado por `/speckit.plan`. Depende de `001-ecosmart-mvp/plan.md`
(estructura del módulo `:app`, dominio, casos de uso) y de `002-firestore-datos-usuario`
(persistencia remota, nunca tocada por este módulo); no reemplaza ninguno de los dos,
solo agrega/restyla la capa Frontend (UI) y, en lo mínimo necesario, Presentación.

## Summary

Este módulo reconstruye visualmente las 6 pantallas principales de EcoSmart (Inicio,
Actividades, Caminar/detalle de actividad, Recompensas, Educación, Perfil) según
`especificacion-ui-app-eco.md`, definiendo un sistema de diseño único (tokens +
componentes `C-01`–`C-11`) reutilizado por todas ellas, y reestructurando la barra de
navegación inferior de 3 tabs (Inicio/Misiones/Perfil) a 5 tabs (Inicio/Actividades/
Recompensas/Educación/Perfil — `RF-UI-002b`). Ninguna regla de negocio, cálculo de
puntaje, persistencia o verificación por IA cambia: todo el trabajo vive en
`presentation/` (Compose) y, para la pantalla Recompensas —que hoy no tiene ningún dato
ni backend—, en una lista de ejemplo estática embebida solo en esa capa (`RF-UI-003b`,
sin tocar Dominio/Aplicación/Infraestructura). La investigación de Fase 0
(`research.md`) resuelve, entre otras cosas, por qué no se agrega una librería de
carga de imágenes remota y cómo se mapea la pantalla `S-03` a las dos pantallas
Kotlin ya existentes que hoy cubren ese flujo.

## Technical Context

**Language/Version**: Kotlin 2.0.20 (sin cambios respecto a spec 001/002)

**Primary Dependencies**: Jetpack Compose (BOM `2024.09.02`) + Material3, Navigation
Compose `2.8.0`, Hilt `2.52` + Hilt Navigation Compose `1.2.0` — todas ya presentes, sin
versiones nuevas. No se agrega ninguna dependencia de carga de imágenes remota (Coil/
Glide): ver research.md §1.

**Storage**: N/A para este módulo (RF-UI-001) — Room/Firestore sin cambios. Única
excepción: la muestra fija de 6 recompensas de ejemplo (RF-UI-003b) vive como una lista
`List<RecompensaEjemplo>` embebida en código de Presentación, no persistida, no
expuesta por ningún repositorio.

**Testing**: Compose UI Test (`androidx.compose.ui:ui-test-junit4`, ya incluido vía el
mismo Compose BOM) con `createComposeRule()` para los nuevos tests de pantallas/
componentes — no existían antes de este módulo (ver research.md §7). JUnit 5 + MockK +
Turbine (ya usados en `app/src/test`) se reutilizan solo si un ViewModel existente
necesita exponer un campo adicional de solo-lectura para la UI (p. ej. derivar
idle/in_progress/completed), nunca para lógica de negocio nueva.

**Target Platform**: Android nativo, `minSdk` 26 / `compileSdk`-`targetSdk` 35 (sin
cambios respecto a spec 001).

**Project Type**: mobile-app — módulo único `:app`, sin tocar `backend/` (a diferencia
de spec 002, este módulo no tiene contraparte de servidor).

**Performance Goals**: Animaciones de 250–800 ms (`especificacion-ui-app-eco.md` §10.4)
corriendo a la tasa de refresco nativa de Compose (`animateFloatAsState`/
`Animatable`), sin metas de red nuevas (no hay llamadas nuevas a ningún backend).

**Constraints**: Debe respetar `RF-UI-001` (no tocar Dominio/Aplicación/Infraestructura
salvo lectura de datos ya expuestos); reutilizar tal cual los `UiState` ya expuestos por
`HomeViewModel`, `PerfilViewModel`, `ActividadDetalleViewModel`,
`ContenidoEducativoViewModel`, `HistorialViewModel`; cumplir `RF-UI-015` a `RF-UI-019`
(contraste 4,5:1, zonas táctiles 48×48 pt, texto del sistema al 130%, lectura por lector
de pantalla, respeto a "reducir movimiento").

**Scale/Scope**: 6 pantallas reconstruidas, 11 componentes globales (`C-01`–`C-11`),
reestructuración de 3→5 tabs, 1 pantalla nueva sin equivalente previo (Recompensas, con
datos de ejemplo estáticos), 0 endpoints nuevos, 0 entidades de dominio nuevas.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Contra `constitution.md` **v2.1.0** (incluye el Principio X, agregado específicamente
para habilitar este módulo).

| # | Principio | Estado | Cómo lo satisface este plan |
|---|-----------|--------|------------------------------|
| I | La Especificación Manda (NON-NEGOTIABLE) | ✅ PASS | Cada pantalla/componente de este plan traza a un `RF-UI-xxx`/`SC-UI-xxx` numerado de `spec.md`; los dos hallazgos nuevos de esta fase (sin datos de Recompensas, 3→5 tabs) ya fueron resueltos en `spec.md` → Clarifications antes de continuar. |
| II | Restricción Estricta de Fases (NON-NEGOTIABLE) | ✅ PASS | Este comando solo produce `plan.md`, `research.md`, `data-model.md`, `quickstart.md`; no se escribió código fuente de la app. |
| III | Modelado de Dominio con POO Real | ✅ PASS (no aplica) | Este módulo no agrega ni modifica entidades de dominio (`RF-UI-001`); la muestra de Recompensas es un dato de Presentación sin reglas de negocio propias (no hay tope diario, racha ni verificación asociada a ella). |
| IV | Separación Estricta de Capas (Clean Architecture) | ✅ PASS | Todo el trabajo vive en `presentation/` (Compose + lectura de `UiState` ya expuestos). Ningún archivo de `domain/`, `application/` o `infrastructure/` se modifica. |
| V | Calidad y Claridad de Código | ✅ PASS | Los componentes globales (`C-01`–`C-11`) se extraen una sola vez a `theme/ComponentesEcoSmart.kt`/`theme/Tokens.kt`; ninguna pantalla duplica estilos sueltos (`RF-UI-004`). |
| VI | Tipado y Valores Cerrados (Enums) | ✅ PASS | `PestanaInferior` se extiende como `enum class` (5 valores) en vez de strings sueltos; `CategoriaActividad` **no** se modifica ni se le agregan valores nuevos pese a que el documento de diseño use nombres de ejemplo que no son categorías reales (ver `RF-UI-011` corregido y research.md §4). |
| VII | Diseño Pragmático (Patrones Solo si Simplifican) | ✅ PASS | Se descarta agregar Coil/Glide (research.md §1) por no resolver ningún problema real (no hay URLs de imagen reales todavía); se reutiliza el patrón ya existente (`painterResource` + drawables locales). |
| VIII | Calidad de Pruebas | ⚠ PENDIENTE EN `/speckit.tasks` | research.md §7 define la estrategia (Compose UI Test por pantalla/componente + cero regresiones en los tests ya existentes, `SC-UI-004`); las tareas concretas se generan en `/speckit.tasks`. |
| IX | Experiencia de Usuario (UX) Amena y Motivadora | ✅ PASS | No se introduce ningún mensaje ni estado punitivo nuevo; los estados de error/vacío (sección 10 del doc de diseño) quedan explícitamente fuera de alcance (`RF-UI-003`), por lo que no hay riesgo de un tono distinto al ya aprobado en spec 001. |
| X | Reconstrucción de UI Basada en Especificación de Diseño (NON-NEGOTIABLE) | ✅ PASS | Única fuente de verdad visual: `especificacion-ui-app-eco.md`; alcance limitado a Frontend/Presentación (`RF-UI-001`); nada de la sección 10/13 marcado "propuesta" se implementa (`RF-UI-003`). |

**Gate adicional (hallazgo de Technical Context)**: La pantalla Recompensas no tiene
dominio/backend propio. Esto **no** es una violación del Principio X sino el caso que el
propio principio anticipa ("prohíbe implementar lo marcado propuesta/a confirmar... sin
pasar antes por `/speckit.clarify`"): se resolvió explícitamente en `spec.md` →
Clarifications (datos de ejemplo fijos, solo en Presentación, sin canje real) antes de
continuar con este plan. ✅ PASS.

*Post-Fase 1 (re-chequeo tras data-model.md/quickstart.md)*: ✅ PASS sin cambios — el
"modelo de datos" de Fase 1 son tokens de diseño y una lista de Presentación no
persistida (data-model.md), no entidades de dominio; no se introdujo ninguna necesidad
de lógica de negocio nueva en ningún punto del diseño.

## Project Structure

### Documentation (this feature)

```text
specs/003-reconstruccion-ui/
├── spec.md               # Ya existente (speckit.specify, con Clarifications de esta fase)
├── plan.md                # Este archivo
├── research.md            # Fase 0 — decisiones de este plan
├── data-model.md          # Fase 1 — tokens de diseño + modelo de Presentación de Recompensas
├── quickstart.md           # Fase 1 — guía de validación visual/accesibilidad
├── checklists/
│   └── requirements.md    # Checklist de calidad de speckit.specify (100%)
└── tasks.md               # Fase 2 — generado por /speckit.tasks (NO por este comando)
```

No se genera `contracts/`: este módulo no expone ni consume ninguna interfaz externa
nueva (no hay endpoints de backend ni cambios de Reglas de Seguridad de Firestore).

### Source Code (repository root)

No se agregan módulos Gradle ni servicios nuevos — se extiende únicamente el paquete
`presentation/` ya existente del módulo `:app`:

```text
app/src/main/kotlin/com/ecosmart/presentation/
├── theme/
│   ├── Theme.kt                      # Alinea paleta/tipografía ya existente con los
│   │                                  #   nombres de token de especificacion-ui-app-eco.md §2
│   │                                  #   (sin romper el dark theme ya soportado)
│   ├── Tokens.kt                      # NUEVO — espaciado/radios/elevación (§2.3) que
│   │                                  #   hoy no existen como objeto reutilizable
│   └── ComponentesEcoSmart.kt         # Se extiende: encabezado de pantalla (C-03,
│                                       #   título/saludo + subtítulo+hoja), chip/segmento
│                                       #   (C-04), barra de progreso lineal (C-05),
│                                       #   tarjeta de actividad con 2 variantes/3 estados
│                                       #   (C-06), botón principal (C-07), enlace de
│                                       #   sección (C-08), tarjeta de video (C-09),
│                                       #   tarjeta de recompensa (C-10), tarjeta de
│                                       #   puntos (C-11); `PestanaInferior` pasa de 3 a
│                                       #   5 valores (C-02)
├── home/
│   ├── HomeScreen.kt                  # Se recorta a resumen (S-01): saludo, tarjeta de
│   │                                  #   puntos, progreso general, carrusel del día,
│   │                                  #   botón "Ver todas", carrusel de contenido
│   ├── HomeViewModel.kt                # Sin cambios de lógica; a lo sumo expone el mismo
│   │                                  #   estado ya calculado de forma más conveniente
│   │                                  #   para el resumen (p. ej. solo "hoy")
│   ├── ActividadesScreen.kt            # NUEVO (S-02) — listado completo que hoy vive
│   │                                  #   mezclado dentro de HomeScreen, con chips de
│   │                                  #   filtro (Todas/En progreso/Completadas)
│   └── ContenidoEducativoScreen.kt     # Restyle a S-05: segmento Videos/Artículos/IA
│                                       #   (Artículos/IA con placeholder mínimo, fuera
│                                       #   de alcance de contenido real — RF-UI-002)
├── activitydetail/
│   └── ActividadDetalleScreen.kt       # Restyle a S-03: anillo de progreso para Caminar,
│                                       #   indicador "0/1" para el resto (RF-UI-011);
│                                       #   gana barra inferior con "Actividades"
│                                       #   resaltado (RF-UI-007)
├── verification/
│   └── VerificacionCaminataScreen.kt   # Restyle: anillo + píldora de recompensa + botón
│                                       #   de estado "¡Seguimiento en curso!" (RF-UI-010);
│                                       #   gana barra inferior con "Actividades"
│                                       #   resaltado (RF-UI-007)
├── rewards/                            # NUEVO paquete (sin equivalente previo)
│   ├── RecompensasScreen.kt             # S-04: segmento Para adultos/Para Kids, chips de
│   │                                   #   categoría, grilla de tarjetas C-10
│   └── RecompensaEjemplo.kt             # Lista estática de 6 recompensas de ejemplo
│                                       #   (RF-UI-003b) — vive solo acá, no en domain/
├── profile/
│   └── PerfilScreen.kt                 # Restyle a S-06: avatar 56pt, tarjeta de
│                                       #   estadísticas de 3 columnas, 5 filas de menú
│                                       #   (una de ellas navega a HistorialScreen, que
│                                       #   deja de ser un tab de primer nivel)
└── EcoSmartNavHost.kt                  # + rutas Actividades/Recompensas; la barra
                                        #   inferior pasa a cablearse con 5 tabs
```

**Structure Decision**: Se mantiene el mismo módulo único `:app` (Principio VII, spec
001); este módulo solo agrega/edita archivos dentro de `presentation/`, sin introducir
un segundo proyecto, módulo Gradle o paquete fuera de esa capa.

## Complexity Tracking

*Sin violaciones de la Constitution Check que requieran justificación — tabla omitida.*
