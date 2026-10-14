---

description: "Task list template for feature implementation"
---

# Tasks: Reconstrucción Visual de la Interfaz de Usuario

**Input**: Design documents from `specs/003-reconstruccion-ui/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [quickstart.md](./quickstart.md) (no hay `contracts/`:
este módulo no expone ninguna interfaz externa nueva, ver plan.md § Project Structure)

**Tests**: Incluidos, pero acotados. `spec.md` no pide un enfoque TDD ni tests de
negocio (este módulo no agrega lógica de negocio, `RF-UI-001`); sin embargo
`research.md` §7 decidió explícitamente agregar tests de Compose UI para los contratos
visuales/de accesibilidad que antes no se probaban de ninguna forma (p. ej. que la
barra inferior exponga 5 ítems, que una tarjeta completada exponga su
`contentDescription`). Los tests unitarios de ViewModels/dominio ya existentes se
ejecutan sin cambios como validación de "cero regresiones" (`SC-UI-004`, tarea de
Polish T045); no se agregan tests de dominio nuevos porque no hay dominio nuevo.

**Organization**: Tareas agrupadas por historia de usuario (spec.md), en el mismo orden
de prioridad (US1=P1, US2=P2, US3=P3, US4=P4, US5=P5). A diferencia del patrón habitual
de historias independientes entre sí, acá cada historia depende explícitamente de que
la anterior esté completa: lo dice el propio spec.md en el "Why this priority" de cada
una (US2-US5 reutilizan los componentes globales que entrega US1; US4 reutiliza
componentes recién probados en US2/US3; US5 queda al final por ser la más compleja).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario a la que pertenece (US1-US5)
- Las rutas de archivo son las reales del repositorio (ver `plan.md` § Project Structure)

## Path Conventions

- Cliente Android: `app/src/main/kotlin/com/ecosmart/presentation/...` (módulo único
  `:app`; este módulo no toca `domain/`, `application/`, `infrastructure/` ni `backend/`)
- Tests de Compose UI (nuevos en este módulo): `app/src/androidTest/kotlin/com/ecosmart/presentation/...`
- Tests unitarios ya existentes (sin cambios, solo se re-ejecutan): `app/src/test/kotlin/...`
- Assets nuevos: `app/src/main/res/drawable/...`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar el terreno mínimo para la pantalla nueva de Recompensas. No se
agrega ninguna dependencia de Gradle (research.md §1: no se incorpora Coil/Glide ni
ninguna otra librería nueva).

- [X] T001 Crear el paquete vacío `app/src/main/kotlin/com/ecosmart/presentation/rewards/`,
      destino de la pantalla nueva de Recompensas (sin dominio/backend propio —
      `RF-UI-003b`)

**Checkpoint**: estructura de carpetas lista; el proyecto sigue compilando sin cambios
funcionales.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Extender la barra de navegación inferior de 3 a 5 tabs y las rutas de
navegación (`RF-UI-002b`). Ninguna historia de usuario puede completarse sin esto, ya
que todas dependen de poder llegar a su pantalla desde un tab de primer nivel.

**⚠️ CRITICAL**: No continuar a la Fase 3 sin completar esta fase.

- [X] T002 Extender el enum `PestanaInferior` a 5 valores (`INICIO`, `ACTIVIDADES`,
      `RECOMPENSAS`, `EDUCACION`, `PERFIL`) y los parámetros de
      `BarraInferiorEcoSmart` (un callback `onXxx` + icono por cada ítem nuevo) en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`
- [X] T003 Agregar las rutas `EcoSmartRoute.Actividades` y `EcoSmartRoute.Recompensas`
      (sin pantalla todavía — se cablean en T031) en
      `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt`
- [X] T004 Reescribir la función privada `BarraInferior(...)` de `EcoSmartNavHost.kt`
      para cablear los 5 tabs (Inicio→Home, Actividades→T003, Recompensas→T003,
      Educación→ContenidoEducativo, Perfil→Perfil), reemplazando el cableado de 3 tabs
      actual, en `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt`
      (depende de T002, T003)
- [X] T005 Quitar el tab "Misiones" de todo punto de entrada de primer nivel: el
      `composable(EcoSmartRoute.Historial...)` deja de recibir `barraInferior`
      (`HistorialScreen` pasa a alcanzarse únicamente desde la fila "Mis actividades"
      de Perfil, ver T022 — research.md §5), en
      `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt` (depende de T004)

**Checkpoint**: la app compila y navega entre 5 destinos de primer nivel (3 de ellos —
Actividades, Recompensas— todavía sin pantalla propia); ninguna pantalla tiene todavía
el nuevo lenguaje visual.

---

## Phase 3: User Story 1 - Sistema de diseño global aplicado de forma consistente (Priority: P1) 🎯 MVP técnico

**Goal**: Un único conjunto de tokens (color/tipografía/espaciado/radios) y de
componentes compartidos (`C-01` a `C-11`) del que dependen las demás historias.

**Independent Test**: Inspeccionar la barra de navegación inferior y un botón principal
aplicados sobre `HomeScreen`/`PerfilScreen` tal como existen hoy (sin reconstruir el
resto de su contenido todavía) y confirmar que se ven idénticos entre sí.

### Tests for User Story 1

- [X] T006 [P] [US1] Test de Compose UI: `BarraInferiorEcoSmart` expone 5 ítems y el
      ítem seleccionado coincide con el `PestanaInferior` recibido, en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/theme/BarraInferiorEcoSmartTest.kt`
- [X] T007 [P] [US1] Test de Compose UI: `BotonPrincipal` en estado deshabilitado no
      dispara `onClick` y reduce su opacidad al 50% (`RF-UI-005`/sección 10.2), en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/theme/BotonPrincipalTest.kt`

### Implementation for User Story 1

- [X] T008 [P] [US1] Definir los tokens de espaciado/radios/elevación
      (`space.screen-x`, `space.gap-card`, `space.section`, `radius.card`,
      `radius.media`, `radius.pill`, `radius.icon-circle`, `shadow.card`) en un archivo
      nuevo `app/src/main/kotlin/com/ecosmart/presentation/theme/Tokens.kt`
      (data-model.md §1)
- [X] T009 [P] [US1] Alinear la paleta de color de `Theme.kt` con los nombres de token
      de §2.1 (agregar alias donde falte: `color.state.progress`, `color.state.idle`,
      `color.water`, etc.) sin romper el soporte de modo oscuro ya existente, en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/Theme.kt`
- [X] T010 [US1] Agregar el rol tipográfico faltante `type.display` (40pt Bold) y
      alinear el resto de roles de `EcoSmartTypography` con §2.2 en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/Theme.kt` (depende de T009,
      mismo archivo)
- [X] T011 [P] [US1] Implementar `ChipFiltro`/`SegmentoEcoSmart` (`C-04`: selección
      única, variantes de chips sueltos, segmento de 2 opciones y segmento de 3
      opciones) en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`
- [X] T012 [P] [US1] Implementar `BarraProgresoLineal` (`C-05`: riel `color.track`,
      relleno `color.green.500`, ancho = valor/meta) en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`
- [X] T013 [US1] Extender `TarjetaEcoSmart` con una variante `TarjetaActividad` (`C-06`:
      compacta/completa × idle/in_progress/completed, deriva el estado comparando
      progreso actual contra meta ya calculado por el dominio — `RF-UI-006`), exponiendo
      nombre/progreso/puntos/estado como un único elemento fusionado
      (`Modifier.semantics(mergeDescendants = true)` + `contentDescription` compuesto,
      p. ej. "Caminar, 1.200 de 2.000 metros, 50 puntos, en curso" — `RF-UI-018`,
      `AC-21`) en `ComponentesEcoSmart.kt` (depende de T012 para la barra interna)
- [X] T014 [P] [US1] Implementar `BotonPrincipal` (`C-07`: reposo/presionado
      oscurecido 8%/deshabilitado opacidad 50%, variante con flecha →) en
      `ComponentesEcoSmart.kt`
- [X] T015 [P] [US1] Implementar `EnlaceSeccion` (`C-08`) en `ComponentesEcoSmart.kt`
- [X] T016 [P] [US1] Implementar `TarjetaVideo` (`C-09`: variantes compacta 150×100 y
      ancha full×100, degradé oscuro inferior, botón ▶ circular, título truncado con
      puntos suspensivos si excede 1-2 líneas — mismo criterio que `C-10`, ver Edge
      Cases de `spec.md`) en `ComponentesEcoSmart.kt`
- [X] T017 [P] [US1] Implementar `TarjetaRecompensa` (`C-10`: imagen, título de hasta 2
      líneas truncado, costo + chevron) en `ComponentesEcoSmart.kt`
- [X] T018 [P] [US1] Implementar `TarjetaPuntos` (`C-11`) en `ComponentesEcoSmart.kt`
- [X] T018a [US1] Alinear `EncabezadoEcoSmart` (`C-03`) con `especificacion-ui-app-eco.md`
      §3: título `type.title` (o saludo `type.greeting` en Inicio), subtítulo
      `type.body` + icono de hoja `color.leaf` 12pt, avatar 36pt, flecha de volver
      opcional (24pt, zona táctil 48pt) — único lugar reutilizable para encabezados
      (`RF-UI-004`/`RF-UI-005`, `C-03` no tenía tarea propia), en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`
- [X] T019 [US1] Aplicar `BarraInferiorEcoSmart` (ya extendida a 5 tabs — T002) y
      `BotonPrincipal` (T014) tal cual existen hoy sobre `HomeScreen.kt` y
      `PerfilScreen.kt`, sin reconstruir el resto de su contenido todavía (demostración
      mínima de consistencia visual exigida por el Independent Test de esta historia),
      en
      `app/src/main/kotlin/com/ecosmart/presentation/home/HomeScreen.kt` y
      `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilScreen.kt` (depende
      de T002, T014)

**Checkpoint**: la barra inferior, el botón principal y el encabezado (`C-03`) se ven
idénticos en Home y Perfil; `C-03/04/05/06/08/09/10/11` están listos para que las demás
historias los consuman.

---

## Phase 4: User Story 2 - Perfil reconstruido con el nuevo diseño (Priority: P2)

**Goal**: La pestaña Perfil completa según `S-06`, con los mismos datos y destinos de
hoy.

**Independent Test**: Navegar a Perfil y compararla contra `S-06`, sin que Inicio,
Actividades, Caminar, Recompensas o Educación estén reconstruidas todavía.

### Tests for User Story 2

- [X] T020 [P] [US2] Test de Compose UI: `PerfilScreen` expone avatar, "Nivel N ·
      puntos", una tarjeta de 3 columnas y 5 filas de menú con chevron (`AC-18`), en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/profile/PerfilScreenTest.kt`

### Implementation for User Story 2

- [X] T021 [US2] Reconstruir `PerfilScreen.kt` según `S-06` (avatar 56pt propio de la
      pantalla — Perfil no usa el encabezado genérico `C-03`/T018a, nombre, "Nivel N ·
      puntos", `TarjetaEcoSmart` de 3 columnas con Actividades/Recompensas/Racha ya
      calculadas por `PerfilUiState`, 5 filas de menú con icono + texto + chevron),
      reutilizando `PerfilViewModel` sin cambios — reemplaza la aplicación mínima de
      T019 sobre este archivo, en
      `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilScreen.kt` (depende
      de T013, T018, T019)
- [X] T022 [US2] Cablear la fila de menú "Mis actividades" para que navegue a
      `EcoSmartRoute.Historial` (reemplaza al tab "Misiones" quitado en T005 —
      research.md §5) en `PerfilScreen.kt`/`EcoSmartNavHost.kt` (depende de T005, T021)
- [ ] T023 [US2] Verificar/ajustar que chevrones y filas de menú midan ≥48×48 pt
      (`RF-UI-016`) en `PerfilScreen.kt`

**Checkpoint**: Perfil queda completo y demostrable de forma independiente (`AC-18`,
`AC-19`).

---

## Phase 5: User Story 3 - Actividades, Recompensas y Educación reconstruidas (Priority: P3)

**Goal**: Las 3 pantallas de listado/grilla (`S-02`, `S-04`, `S-05`) con el nuevo
lenguaje visual, conservando datos/filtros/navegación ya existentes (o, en el caso de
Recompensas, con la muestra de ejemplo resuelta en Clarifications).

**Independent Test**: Probar cada una por separado (p. ej. solo Recompensas) contra su
especificación y criterios de aceptación, sin depender de que Inicio o Caminar estén
reconstruidas.

### Tests for User Story 3

- [ ] T024 [P] [US3] Test de Compose UI: `ActividadesScreen` filtra por chip (Todas/En
      progreso/Completadas — `AC-06`/`AC-07`), en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/home/ActividadesScreenTest.kt`
- [ ] T025 [P] [US3] Test de Compose UI: `RecompensasScreen` filtra por segmento (Para
      adultos/Para Kids) y por chip de categoría (`AC-13`/`AC-14`), en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/rewards/RecompensasScreenTest.kt`

### Implementation for User Story 3

- [ ] T026 [US3] Crear `ActividadesScreen.kt` (`S-02`) extrayendo el listado completo
      que hoy vive mezclado dentro de `HomeScreen.kt`, con el encabezado `C-03`/T018a
      (título "Actividades" + subtítulo), los chips de filtro de T011 operando sobre
      el estado idle/in_progress/completed ya calculado por el dominio, y grilla de 2
      columnas de `TarjetaActividad` en su variante completa (con "+ N pts" —
      `RF-UI-009`), en
      `app/src/main/kotlin/com/ecosmart/presentation/home/ActividadesScreen.kt`
      (depende de T003, T011, T013, T018a)
- [ ] T027 [US3] Si `ActividadesScreen` no puede reutilizar tal cual
      `HomeUiState.actividadesPorCategoria` (p. ej. porque `HomeScreen`/T033 ya lo
      recortó a "solo hoy"), exponer en `HomeViewModel` un `StateFlow` equivalente con
      el listado completo, sin cálculo de negocio nuevo, solo exposición de estado ya
      existente, en
      `app/src/main/kotlin/com/ecosmart/presentation/home/HomeViewModel.kt`
- [ ] T028 [P] [US3] Crear `RecompensaEjemplo.kt` (data-model.md §3:
      `RecompensaEjemplo`, `CategoriaRecompensaEjemplo`, las 6 recompensas de ejemplo de
      `especificacion-ui-app-eco.md` §7.2 con las erratas de copy ya corregidas —
      `RF-UI-003b`) en
      `app/src/main/kotlin/com/ecosmart/presentation/rewards/RecompensaEjemplo.kt`
      (depende de T001)
- [ ] T029 [US3] Crear `RecompensasScreen.kt` (`S-04`: encabezado `C-03`/T018a, segmento
      Para adultos/Para Kids, chips de categoría con scroll horizontal, grilla de 2
      columnas de `TarjetaRecompensa`) consumiendo únicamente `RecompensaEjemplo.kt` —
      ninguna tarjeta canjea nada real (`RF-UI-012`), en
      `app/src/main/kotlin/com/ecosmart/presentation/rewards/RecompensasScreen.kt`
      (depende de T011, T017, T018a, T028)
- [ ] T030 [US3] Restylar `ContenidoEducativoScreen.kt` a `S-05`: encabezado `C-03`/
      T018a, reemplazar el filtro por `CategoriaActividad` por el segmento Videos/
      Artículos/IA (research.md §3), mostrar el contenido curado ya existente como
      `TarjetaVideo` ancha bajo "Videos" con enlace "Ver todos", y un mensaje mínimo de
      contenido no disponible bajo Artículos/IA (fuera de alcance por `RF-UI-002`; el
      segmento sí cambia a verde oscuro al seleccionarse — `AC-17` parcial, ver
      `spec.md` → `SC-UI-003`), en
      `app/src/main/kotlin/com/ecosmart/presentation/home/ContenidoEducativoScreen.kt`
      (depende de T011, T016, T018a)
- [ ] T031 [US3] Cablear `EcoSmartRoute.Actividades`/`EcoSmartRoute.Recompensas` (T003)
      a `ActividadesScreen`/`RecompensasScreen` con la barra inferior de 5 tabs, en
      `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt` (depende de
      T004, T026, T029)

**Checkpoint**: Actividades, Recompensas y Educación quedan demostrables de forma
independiente entre sí (`AC-06`, `AC-07`, `AC-13`, `AC-14`, `AC-16`; `AC-17` solo en su
parte de segmento visual, ver `spec.md` → `SC-UI-003`).

---

## Phase 6: User Story 4 - Inicio reconstruida (Priority: P4)

**Goal**: La pantalla de arranque (`S-01`) como resumen de un vistazo, combinando los
componentes ya probados en las historias anteriores.

**Independent Test**: Abrir la app y comparar la pantalla de arranque contra `S-01`, sin
que Caminar esté reconstruida todavía (el carrusel puede enlazar a la versión actual de
Caminar mientras tanto).

### Tests for User Story 4

- [ ] T032 [P] [US4] Test de Compose UI: con 3 de 4 actividades completadas, Inicio
      muestra la barra de progreso general al 75% y el texto "3/4 actividades"
      (`AC-02`), en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/home/HomeScreenTest.kt`

### Implementation for User Story 4

- [ ] T033 [US4] Recortar `HomeScreen.kt` a resumen (`S-01`): saludo + avatar (`C-03`/
      T018a, variante `type.greeting`), `TarjetaPuntos` (`C-11`), tarjeta de progreso
      general con `BarraProgresoLineal` (`C-05`) y fracción completadas/total,
      carrusel de `TarjetaActividad` del día, `BotonPrincipal` "Ver todas las
      actividades →", carrusel de `TarjetaVideo` de contenido (`RF-UI-008`), marcando
      la ilustración del planeta como decorativa/ignorada por el lector de pantalla
      (`RF-UI-018`, mismo criterio que T040 para el paisaje de Caminar) — reemplaza la
      aplicación mínima de T019 sobre este archivo, en
      `app/src/main/kotlin/com/ecosmart/presentation/home/HomeScreen.kt` (depende de
      T013, T014, T016, T018, T018a, T026)
- [ ] T034 [US4] Confirmar que el saludo y la barra inferior quedan fijos mientras el
      resto hace scroll vertical (estructura `Scaffold` + `LazyColumn` apropiada) en
      `HomeScreen.kt`
- [ ] T035 [US4] Conectar "Ver todas las actividades" para que abra `ActividadesScreen`
      con el chip "Todas" preseleccionado (`AC-04`), en
      `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt`/
      `ActividadesScreen.kt` (depende de T026, T033)

**Checkpoint**: Inicio queda demostrable de forma independiente (`AC-02`, `AC-03`,
`AC-04`, `AC-05`).

---

## Phase 7: User Story 5 - Caminar (detalle) reconstruida (Priority: P5)

**Goal**: El detalle de actividad (`S-03`), con el anillo de progreso animado para
Caminar y el esqueleto genérico para el resto del catálogo.

**Independent Test**: Abrir el detalle de Caminar desde Inicio o Actividades y
compararlo contra `S-03`, de forma independiente del resto de las pantallas ya
reconstruidas.

### Tests for User Story 5

- [ ] T036 [P] [US5] Test de Compose UI: el anillo de progreso de Caminar expone el
      `contentDescription` de porcentaje (p. ej. "60 por ciento" — `RF-UI-018`,
      `AC-09`), en
      `app/src/androidTest/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleScreenTest.kt`

### Implementation for User Story 5

- [ ] T037 [P] [US5] Implementar el composable de anillo de progreso animado (arco
      desde las 12 en punto, sentido horario, ángulo = 360° × valor/meta, trazo 8pt,
      riel `color.track`, arco `color.green.700`, animado de 0 al valor actual con
      `Canvas` + `Animatable`) en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`
      (reutilizable por T038/T039)
- [ ] T038 [US5] Restylar `ActividadDetalleScreen.kt` a `S-03` (research.md §2):
      flecha de volver, encabezado `C-03`/T018a (título/subtítulo), anillo (T037) para
      `CAMINAR` o indicador "0/1" para el resto del catálogo (`RF-UI-010`/`RF-UI-011`),
      píldora de recompensa, las dos filas informativas de "Cómo se registra",
      reutilizando `ActividadDetalleViewModel` sin cambios, en
      `app/src/main/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleScreen.kt`
      (depende de T018a, T037)
- [ ] T039 [US5] Restylar `VerificacionCaminataScreen.kt` con el mismo anillo (T037) y
      el botón de estado "¡Seguimiento en curso!" (`C-07`/T014, mostrando únicamente el
      estado activo del camino feliz — `RF-UI-003`), en
      `app/src/main/kotlin/com/ecosmart/presentation/verification/VerificacionCaminataScreen.kt`
      (depende de T014, T037)
- [ ] T040 [US5] Agregar la ilustración decorativa de paisaje pegada al borde inferior
      de `ActividadDetalleScreen.kt`, marcada para que el lector de pantalla la ignore
      (`RF-UI-018`)
- [ ] T040a [US5] Agregar el parámetro `barraInferior` (selección `ACTIVIDADES`) a
      `ActividadDetalleScreen`/`VerificacionCaminataScreen` y pasarlo en sus
      `composable(...)` de `EcoSmartNavHost.kt`, para que la barra inferior se vea y
      resalte "Actividades" en el detalle de cualquier actividad (`RF-UI-007`), en
      `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt` (depende de
      T004, T038, T039)

**Checkpoint**: Caminar/detalle queda demostrable de forma independiente (`AC-09`,
`AC-10`, `AC-12`), con la barra inferior resaltando "Actividades" (`RF-UI-007`); todas
las historias de `spec.md` quedan implementadas.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Verificaciones transversales a las 6 pantallas reconstruidas.

- [ ] T041 [P] Auditoría de contraste ≥4,5:1 en las 6 pantallas reconstruidas
      (`RF-UI-015`, `SC-UI-005`)
- [ ] T042 [P] Auditoría de zonas táctiles ≥48×48 pt en las 6 pantallas (`RF-UI-016`,
      `SC-UI-005`)
- [ ] T043 [P] Verificar el escalado de texto del sistema al 130% sin cortes de
      contenido crítico en las 6 pantallas (`RF-UI-017`, `AC-20`, `SC-UI-005`)
- [ ] T044 [P] Verificar que toda animación (anillo, barras, contador de puntos,
      transición entre pantallas) respeta la preferencia de "reducir movimiento" del
      sistema (`RF-UI-019`), en
      `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`
- [ ] T045 Ejecutar `./gradlew :app:testDebugUnitTest` y confirmar que el 100% de los
      tests ya existentes (dominio, casos de uso, ViewModels) siguen en verde sin
      cambios (`SC-UI-004`)
- [ ] T046 Ejecutar de punta a punta la guía de validación de `quickstart.md`
      (secciones 2 a 6) antes de dar por cerrado el módulo

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — puede empezar de inmediato.
- **Foundational (Phase 2)**: Depende de Setup — BLOQUEA a todas las historias (sin 5
  tabs/rutas, ninguna pantalla nueva es alcanzable).
- **US1 (Phase 3)**: Depende de Foundational. Es la base técnica de la que dependen
  **todas** las demás historias (a diferencia del patrón habitual de historias
  mutuamente independientes — así lo indica el propio spec.md).
- **US2 (Phase 4)**: Depende de US1 (consume `TarjetaEcoSmart`/`TarjetaPuntos`/barra
  inferior/botón ya aplicados).
- **US3 (Phase 5)**: Depende de US1 (chips, `TarjetaVideo`, `TarjetaRecompensa`). No
  depende de US2.
- **US4 (Phase 6)**: Depende de US1 y de US3·T026 (necesita que exista
  `ActividadesScreen` como destino de "Ver todas las actividades").
- **US5 (Phase 7)**: Depende de US1 (botón de estado). No depende de US2/US3/US4 más
  allá de poder navegarse desde Inicio/Actividades (ya resuelto en Foundational).
- **Polish (Phase 8)**: Depende de que las historias que se quieran pulir estén
  completas.

### Parallel Opportunities

- Dentro de Foundational: T002 y T003 son de archivos relacionados pero distintos
  (ComponentesEcoSmart.kt vs EcoSmartNavHost.kt) — pueden avanzar en paralelo antes de
  T004.
- Dentro de US1: T008, T009, T011, T012, T014, T015, T016, T017, T018 tocan
  `Tokens.kt`/`Theme.kt`/`ComponentesEcoSmart.kt` en funciones independientes entre sí
  — marcadas `[P]` donde no comparten una misma función/bloque.
- US3 y US5 pueden desarrollarse en paralelo por personas distintas una vez completada
  US1 (no comparten archivos), aunque el orden sugerido de entrega sigue siendo
  P1→P2→P3→P4→P5.

---

## Parallel Example: User Story 1

```bash
# Tests de US1 en paralelo:
Task: "Test de Compose UI de BarraInferiorEcoSmart (5 ítems) en BarraInferiorEcoSmartTest.kt"
Task: "Test de Compose UI de BotonPrincipal deshabilitado en BotonPrincipalTest.kt"

# Componentes de US1 sin dependencias entre sí, en paralelo:
Task: "Implementar ChipFiltro/SegmentoEcoSmart (C-04) en ComponentesEcoSmart.kt"
Task: "Implementar BarraProgresoLineal (C-05) en ComponentesEcoSmart.kt"
Task: "Implementar BotonPrincipal (C-07) en ComponentesEcoSmart.kt"
Task: "Implementar EnlaceSeccion (C-08) en ComponentesEcoSmart.kt"
Task: "Implementar TarjetaVideo (C-09) en ComponentesEcoSmart.kt"
Task: "Implementar TarjetaRecompensa (C-10) en ComponentesEcoSmart.kt"
Task: "Implementar TarjetaPuntos (C-11) en ComponentesEcoSmart.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1 (Setup) + Phase 2 (Foundational).
2. Completar Phase 3 (US1): sistema de diseño + componentes globales.
3. **STOP y VALIDAR**: comparar la barra inferior y el botón principal entre Home y
   Perfil (Independent Test de US1). Esto es un MVP **técnico** (la base reutilizable),
   no todavía una pantalla terminada de cara al usuario.
4. Si se necesita un MVP demostrable de cara al usuario, sumar también Phase 4 (US2 —
   Perfil completo): es la primera pantalla 100% reconstruida.

### Incremental Delivery

1. Setup + Foundational + US1 → base de diseño lista.
2. + US2 (Perfil) → primera pantalla completa demostrable.
3. + US3 (Actividades/Recompensas/Educación) → 3 pantallas más.
4. + US4 (Inicio) → pantalla de arranque reconstruida.
5. + US5 (Caminar) → las 6 pantallas principales reconstruidas (alcance completo de
   `spec.md`).

### Parallel Team Strategy

Con más de una persona disponible:

1. Completar juntos Setup + Foundational + US1 (base compartida).
2. A partir de ahí: Persona A → US2; Persona B → US3; luego ambas convergen en US4
   (depende de US3) y se reparten US5.

---

## Notes

- `[P]` = archivos o funciones distintas, sin dependencias pendientes entre sí.
- La etiqueta `[Story]` traza cada tarea a su historia de usuario en `spec.md`.
- Ninguna tarea de este archivo toca `domain/`, `application/`, `infrastructure/` ni
  `backend/` (`RF-UI-001`) — si una tarea de implementación pareciera requerirlo,
  releer `RF-UI-001`/Principio X antes de continuar.
- Commitear después de cada tarea o grupo lógico de tareas.
