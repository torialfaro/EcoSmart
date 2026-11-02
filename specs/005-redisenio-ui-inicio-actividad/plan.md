# Implementation Plan: Rediseño de la UI de Inicio y Actividad Individual

**Branch**: `005-redisenio-ui-inicio-actividad` | **Date**: 2026-11-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/005-redisenio-ui-inicio-actividad/spec.md`

**Note**: Este plan depende de `001-ecosmart-mvp` (módulo `:app`, dominio y topes diarios),
`002-firestore-datos-usuario` (registros de verificación en Firestore, nunca tocados),
`003-reconstruccion-ui` (sistema de diseño y barra inferior `C-02`) y `004-redisenio-ui-educacion`
(catálogo de lecciones y tarjetas educativas de Inicio). Solo cambia Frontend (UI) y, en lo mínimo
necesario, Presentación. `setup-plan.sh` está roto en esta máquina (ver memoria del repo), por lo que
los documentos de este directorio se crearon a mano siguiendo `plan-template.md`.

## Summary

Se reemplaza el contenido de **Inicio** (tarjeta de puntos verde claro, "Tu progreso general 0/10",
enlace "Ver todas", botón flotante del mapa, "0 / 1" en las tarjetas) por el diseño de los tableros
I1/I2, y el **detalle de actividad no medible** (círculo "0 / 1", píldora de recompensa, "Realizarla"
a ancho completo, colinas) por el diseño de D1/D2. La barra inferior pasa a tener seis íconos
(Puntos Verdes como sexto).

Enfoque técnico (detalle en `research.md`):

- **Estado diario calculado en Presentación, sin tocar Dominio/Aplicación/Infraestructura.**
  `RegistroVerificacionRepository.todos(usuarioId)` ya devuelve `actividadId`, `categoria`, `fecha` y
  `resultado`; con eso y `EstrategiaDePuntaje.para(categoria).topeDiario` (dominio público) una función
  pura `calcularEstadoDiario(...)` obtiene, por categoría, "Hecha" y "tope alcanzado", y por actividad,
  *para hacer* / *completada hoy* / *no disponible hoy* (`research.md` §1). `Actividad.estaDisponibleHoy()`
  no se modifica ni se usa.
- **Total del progreso = `CategoriaActividad.entries.size`** (hoy 3), nunca un número fijo (RF-IA-019b).
- **Una tarjeta por categoría** en "Actividades de hoy" con una función pura de selección (RF-IA-006a).
- **Pasos y tip** salen de `pasosASeguir` (parser puro de líneas numeradas) y de `resultadoEsperado`;
  "Aprendé primero" abre la primera lección de la categoría vía `CatalogoLecciones` (spec 004).
- **Componentes nuevos `C-19` a `C-25`** en `theme/ComponentesInicioActividad.kt`; tres logos de
  categoría como VectorDrawable; sin dependencias nuevas.
- **Barra inferior**: `PestanaInferior.PUNTOS_VERDES` y un parámetro `onPuntosVerdes` en el componente
  único `BarraInferiorEcoSmart`; se retira el botón flotante de Inicio.
- Sin animaciones; accesibilidad según RF-IA-022.

## Technical Context

**Language/Version**: Kotlin 2.0.20 (sin cambios)

**Primary Dependencies**: Jetpack Compose (BOM 2024.09.02) + Material3, Navigation Compose 2.8.0,
Hilt 2.52, lifecycle 2.8.6. **Sin dependencias nuevas** (ni `material-icons-extended`: el ícono de
cámara es un vector propio; la bombilla `ic_bombilla.xml` ya existe).

**Storage**: N/A. Solo lectura de datos existentes (Firestore vía `RegistroVerificacionRepository`,
catálogo de actividades vía `ActividadRepository`, lecciones vía `CatalogoLecciones`).

**Testing**: JUnit 5 + MockK (`app/src/test`) para `calcularEstadoDiario`, selección de tarjetas y
parser de pasos; Compose UI Test (`app/src/androidTest`) para componentes y pantallas. Ver
`research.md` §9.

**Target Platform**: Android nativo, `minSdk` 26 / `compileSdk`-`targetSdk` 35 (sin cambios).

**Project Type**: mobile-app — módulo único `:app`; `backend/` y `firestore.rules` no se tocan.

**Performance Goals**: una lectura de `todos()` por carga/`ON_RESUME` (misma que ya hacen Perfil e
Historial); cálculo en memoria. Sin metas de red nuevas. Sin animaciones.

**Constraints**: `RF-UI-001` / `RF-IA-001` (cero cambios en `domain/`, `application/`,
`infrastructure/`), regla de datos `RF-IA-019`, contraste 4,5:1, zonas táctiles 48×48, texto al 130%
con alturas mínimas, sin animaciones, una sola fuente de verdad visual (Constitución v2.4.0).

**Scale/Scope**: 2 pantallas reescritas (Inicio, detalle no medible), 1 componente global modificado
(`BarraInferiorEcoSmart`), 7 componentes nuevos (`C-19` a `C-25`), ~12 tokens nuevos, 4 vectores
(3 logos + cámara), 0 endpoints, 0 entidades de dominio.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Contra `constitution.md` **v2.4.0**.

| # | Principio | Estado | Cómo lo satisface este plan |
|---|-----------|--------|------------------------------|
| I | La Especificación Manda | ✅ PASS | Cada pieza traza a un `RF-IA-xxx`/`SC-IA-xxx` (tabla en `data-model.md` §6). Las 5 clarificaciones del spec están aplicadas. Las dos ambigüedades que el spec dejaba abiertas (destino del chevron y alcance del sexto ícono) se resuelven como decisiones de plan, con la opción conservadora, en `research.md` §5 y §6, y se señalan al usuario. |
| II | Restricción Estricta de Fases | ✅ PASS | Este comando solo produce documentos; no se escribió código de la app. |
| III | Modelado de Dominio con POO Real | ✅ PASS (no aplica) | No se agregan ni cambian entidades. `EstadoDiarioUi` y `EstadoActividadUi` son modelos de Presentación sin reglas de puntaje: los topes se leen de `EstrategiaDePuntaje` (dominio) y no se duplican. |
| IV | Separación Estricta de Capas | ✅ PASS | Todo en `presentation/` + `res/`. Presentación solo lee puertos de dominio ya existentes (`RegistroVerificacionRepository.todos`, `EstrategiaDePuntaje`), igual que `PerfilViewModel`/`HistorialViewModel`. |
| V | Calidad y Claridad de Código | ✅ PASS | Cálculo y selección en funciones puras ≤ 40 líneas; categoría → logo/color con `when` exhaustivo; sin `is` para lógica de negocio. |
| VI | Tipado y Valores Cerrados | ✅ PASS | `EstadoActividadUi` es un enum cerrado; `CategoriaActividad` existente, sin valores nuevos. |
| VII | Diseño Pragmático | ✅ PASS | Se descarta crear un caso de uso/entidad/endpoint de "progreso diario" y una nueva dependencia de íconos (research.md §1, §7). Una función pura + un cargador inyectable bastan. |
| VIII | Calidad de Pruebas | ✅ PASS (plan) | Tests de la función pura (regla de "Hecha", tope, selección) y de UI; ver `research.md` §9. No hay regla de negocio nueva en dominio. |
| IX | UX Amena y Motivadora | ✅ PASS | Frases de invitación ("Hacerla ahora", "¡Hacela ahora y sumá N puntos!", "Volvé mañana", "¡Listo por hoy!"); nada punitivo. |
| X | Reconstrucción de UI Basada en Especificación | ✅ PASS | Fuente de verdad: tableros I1/I2/D1/D2 (enmienda v2.4.0); alcance solo Inicio y detalle no medible; excepciones autorizadas (6º ícono, quitar botón flotante); datos siempre del dominio/backend; sin animaciones; accesibilidad RF-UI-015..018. |

**Gate adicional (hallazgo de este plan)**: `Actividad.estaDisponibleHoy()` devuelve siempre `true`
(RF-056) y ningún caso de uso expone el estado por actividad. En vez de modificar dominio, el estado
se deriva en Presentación de datos ya existentes (`research.md` §1). ✅ PASS.

**Gate adicional 2**: los `ActividadId` son UUID aleatorios por instalación; un registro viejo puede
referirse a un id que ya no existe. El estado por categoría (tope) sigue siendo correcto y tiene
precedencia sobre la coincidencia por id (`research.md` §1, edge case). ✅ PASS.

*Post-Fase 1 (re-chequeo tras data-model.md/contracts/quickstart.md)*: ✅ PASS sin cambios — los
artefactos describen modelos de Presentación, un contrato de estado y componentes; no aparece
necesidad de lógica de negocio nueva.

## Project Structure

### Documentation (this feature)

```text
specs/005-redisenio-ui-inicio-actividad/
├── spec.md                         # speckit.specify + clarify
├── plan.md                         # Este archivo
├── research.md                     # Fase 0
├── data-model.md                   # Fase 1 — modelos de Presentación, estados, tokens, trazabilidad
├── quickstart.md                   # Fase 1 — validación funcional, visual y de accesibilidad
├── contracts/
│   └── estado-actividad.md         # Fase 1 — contrato de derivación de estados y selección de tarjetas
├── checklists/
│   └── requirements.md
└── tasks.md                        # Fase 2 (/speckit.tasks — NO creado por este comando)
```

### Source Code (repository root)

```text
app/src/main/
├── res/drawable/
│   ├── logo_categoria_reciclar.xml        # NUEVO — C-19 (disco blanco + 3 flechas + corazón + dibujo propio)
│   ├── logo_categoria_reutilizar.xml      # NUEVO
│   ├── logo_categoria_caminar.xml         # NUEVO
│   └── ic_camara.xml                      # NUEVO — botón principal del detalle
├── res/drawable/il_paisaje_colinas.xml    # SIN USO tras el cambio (retiro se decide en tasks)
└── kotlin/com/ecosmart/presentation/
    ├── estadodiario/                      # NUEVO paquete
    │   ├── EstadoDiario.kt                # modelos + calcularEstadoDiario + seleccionarActividadesDeHoy (puros)
    │   └── CargadorEstadoDiario.kt        # @Inject: todos(usuarioId) → EstadoDiarioUi
    ├── activitydetail/
    │   ├── PasosActividadParser.kt        # NUEVO — texto numerado → List<String> (puro)
    │   ├── ActividadDetalleViewModel.kt   # + estado diario + lección relacionada
    │   └── ActividadDetalleScreen.kt      # rama no medible nueva (C-22/C-23/C-24/C-25); medible (Caminar) intacta
    ├── home/
    │   ├── HomeViewModel.kt               # + EstadoDiarioUi; HomeUiState sin cifras fijas
    │   └── HomeScreen.kt                  # nuevo orden; sin tarjeta "progreso general", sin botón flotante
    ├── theme/
    │   ├── ComponentesInicioActividad.kt  # NUEVO — C-19…C-25
    │   ├── ComponentesEcoSmart.kt         # BarraInferiorEcoSmart: 6º ícono; EncabezadoEcoSmart: variante con degradé/esquinas
    │   ├── Tokens.kt / Theme.kt           # + tokens de RF-IA-021 (solo aditivos)
    │   └── (comun/CategoriaUi.kt)         # + logo y degradé por categoría (when exhaustivo); iconoCategoria intacto
    └── EcoSmartNavHost.kt                 # BarraInferior con onPuntosVerdes; lección desde Actividad resalta ACTIVIDADES

app/src/test/kotlin/com/ecosmart/presentation/
├── estadodiario/EstadoDiarioTest.kt       # NUEVO (JUnit 5)
└── activitydetail/PasosActividadParserTest.kt   # NUEVO (JUnit 5)

app/src/androidTest/kotlin/com/ecosmart/presentation/
├── theme/ComponentesInicioActividadTest.kt      # NUEVO — C-19…C-25
├── theme/BarraInferiorEcoSmartTest.kt           # AJUSTE — 6 ítems, "Puntos Verdes" ahora en la barra
├── home/HomeScreenTest.kt                       # AJUSTE — sin "N/10", con progreso de hoy
└── activitydetail/ActividadDetalleScreenTest.kt # AJUSTE — estados para hacer / completada
```

**Structure Decision**: módulo único `:app`; nuevo paquete `presentation/estadodiario/` porque Inicio
y el detalle comparten el mismo cálculo y no debe duplicarse. Los componentes visuales van a un
archivo propio de `theme/` (como `ComponentesLeccion.kt` en el spec 004) para respetar `RF-UI-004`/
`RF-UI-005`. `iconoCategoria()` y los íconos `ic_categoria_*` no se tocan: siguen sirviendo a
Actividades (listado), fuera de alcance.

## Complexity Tracking

*Sin violaciones de la Constitution Check que requieran justificación — tabla omitida.*
