---

description: "Lista de tareas del rediseño de Inicio y Actividad individual (spec 005)"
---

# Tasks: Rediseño de la UI de Inicio y Actividad Individual

**Input**: Design documents from `specs/005-redisenio-ui-inicio-actividad/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/estado-actividad.md](./contracts/estado-actividad.md),
[quickstart.md](./quickstart.md)

**Tests**: Incluidos. Este módulo no agrega reglas de negocio (`RF-IA-001`), pero sí una derivación de
estado con reglas propias de presentación (categoría "Hecha", tope alcanzado, selección de tarjetas),
un parser de pasos y contratos visuales y de accesibilidad nuevos (`research.md` §9). Se escriben
primero los tests JVM (JUnit 5) y de componente/pantalla (Compose UI Test) y se confirma que fallan;
los tests unitarios existentes se re-ejecutan sin cambios como validación de "cero regresiones"
(`SC-IA-009`).

**Organization**: Tareas agrupadas por historia de usuario del spec (US1 = P1 Inicio, US2 = P1 detalle
de actividad, US3 = P3 "Aprendé primero"). La fase Fundacional (estado diario, parser, tokens, logos,
componentes base `C-19` y `C-23`) bloquea a todas las historias. US1 y US2 son independientes entre sí
una vez terminada la fase Fundacional, pero ambas editan `EcoSmartNavHost.kt` y
`ComponentesInicioActividad.kt`, por lo que se implementan en orden. US3 depende de US2.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario (US1–US3); sin etiqueta en Setup, Fundacional y Polish
- Las rutas son las reales del repositorio (ver `plan.md` § Source Code)

## Path Conventions

- Presentación: `app/src/main/kotlin/com/ecosmart/presentation/...` (módulo único `:app`; este módulo **no**
  toca `domain/`, `application/`, `infrastructure/`, `backend/` ni `firestore.rules`)
- Recursos: `app/src/main/res/drawable/...`
- Tests JVM: `app/src/test/kotlin/com/ecosmart/presentation/...`
- Tests Compose UI: `app/src/androidTest/kotlin/com/ecosmart/presentation/...`
- Nota de herramienta: nunca importar `androidx.compose.foundation.layout.weight` (error de K2, ver memoria del repo)
- Comandos: Gradle 8.9 cacheado + JDK 21, siempre con `--no-daemon` (ver `quickstart.md`)

---

## Análisis de impacto sobre código existente y backend

### Backend / Firestore: sin impacto

- `backend/` y `firestore.rules` no cambian. El estado diario se lee con
  `RegistroVerificacionRepository.todos(usuarioId)`, método que ya existe y que ya usan Perfil e Historial.
- Ninguna tarea requiere endpoint, colección, índice ni regla nuevos. `T044` verifica con `git diff` que
  `domain/`, `application/`, `infrastructure/`, `backend/` y `firestore.rules` queden sin cambios.

### Tareas que editan archivos existentes y compartidos

| Tarea | Archivo existente | Quién más lo usa | Riesgo | Mitigación (tarea) |
|---|---|---|---|---|
| T010 | `Tokens.kt`, `Theme.kt`, `CategoriaUi.kt` | Todas las pantallas | Bajo: solo se agregan símbolos; `iconoCategoria()` no se toca | T045 verifica que el diff sea aditivo |
| T025, T026 | `BarraInferiorEcoSmart` en `ComponentesEcoSmart.kt` y `BarraInferior` en `EcoSmartNavHost.kt` | Todas las pantallas con barra inferior (Inicio, Actividades, Recompensas, detalle de actividad, Educación y su detalle, Caminata, Perfil) | **Alto**: cambia la barra de toda la app (6.º ícono, parámetro nuevo) | T018 (test de la barra con 6 ítems), T046 (regresión de navegación en todas las pestañas) |
| T023, T024 | `HomeScreen.kt`, `HomeViewModel.kt`, `HomeUiState` | Solo Inicio; `HomeContenido` lo usa `HomeScreenTest` | Medio: `ActividadItemUi` y el contador "progreso general" desaparecen y el test actual **fallará** por diseño | T017 reescribe `HomeScreenTest` primero |
| T026 | `EcoSmartNavHost.kt` (Home: quitar `onVerPuntosVerdes` flotante) | Navegación | Medio | T046 |
| T030, T031 | `ActividadDetalleScreen.kt`, `ActividadDetalleViewModel.kt` | Detalle de Caminar (medible) comparte el archivo | Medio: regresión en Caminar | T028 y T040 (test de que Caminar conserva su diseño) |
| T038 | `EcoSmartNavHost.kt` (resaltado al abrir lección desde actividad) | Detalle de lección | Bajo | T046 |

**Decisión de tarea (reduce riesgo)**: el encabezado verde con esquinas redondeadas y la tarjeta de puntos
blanca superpuesta se crean como composables **nuevos** (`EncabezadoInicio`, `TarjetaPuntosInicio`) en
`ComponentesInicioActividad.kt`, en lugar de modificar `EncabezadoEcoSmart` y `TarjetaPuntos`, que usan
otras pantallas. Es coherente con `RF-IA-003`/`RF-IA-004` (solo se piden en Inicio) y evita que un cambio de
comportamiento por defecto afecte a las demás pantallas.

---

## Phase 1: Setup

**Purpose**: Estructura mínima. No se agrega ninguna dependencia de Gradle (`research.md` §7).

- [X] T001 Crear el paquete `app/src/main/kotlin/com/ecosmart/presentation/estadodiario/` y las carpetas de tests `app/src/test/kotlin/com/ecosmart/presentation/estadodiario/` y `app/src/test/kotlin/com/ecosmart/presentation/activitydetail/`
- [X] T002 Ejecutar la línea base `:app:testDebugUnitTest :app:compileDebugAndroidTestKotlin --no-daemon` y registrar que está en verde antes de empezar (referencia para `SC-IA-009`)

---

## Phase 2: Foundational (bloquea a todas las historias)

**Purpose**: Estado diario derivado, parser de pasos, tokens, logos y componentes base.

### Tests primero (deben fallar antes de implementar)

- [X] T003 [P] Test JUnit 5 `EstadoDiarioTest` en `app/src/test/kotlin/com/ecosmart/presentation/estadodiario/EstadoDiarioTest.kt`: sin registros → todas `PARA_HACER` y 0/3; 1 aprobado de Reciclar → categoría hecha, esa actividad `COMPLETADA_HOY`, las otras de Reciclar `NO_DISPONIBLE_HOY`; 2 de Reutilizar → hecha y las demás `PARA_HACER`; 5 de Reutilizar → las no hechas `NO_DISPONIBLE_HOY`; Caminar nunca `NO_DISPONIBLE_HOY`; rechazados/indeterminados y registros de otro día no cuentan; registro con `actividadId` inexistente cuenta para la categoría pero no marca actividad; `total == CategoriaActividad.entries.size` (contrato `estado-actividad.md`, `RF-IA-005`, `RF-IA-019b`)
- [X] T004 [P] Test JUnit 5 `SeleccionActividadesDeHoyTest` en `app/src/test/kotlin/com/ecosmart/presentation/estadodiario/SeleccionActividadesDeHoyTest.kt`: una tarjeta por categoría en orden Reciclar/Reutilizar/Caminar; primera `PARA_HACER`; si no hay, primera `COMPLETADA_HOY`; si no hay, primera del catálogo; categoría sin actividades se omite (`RF-IA-006a`)
- [X] T005 [P] Test JUnit 5 `PasosActividadParserTest` en `app/src/test/kotlin/com/ecosmart/presentation/activitydetail/PasosActividadParserTest.kt`: quita prefijos "1." y "1)", descarta líneas vacías, texto sin números, texto vacío → lista vacía, más o menos de tres pasos sin forzar (`RF-IA-015`)
- [X] T006 [P] Test Compose `ComponentesInicioActividadTest` (parte 1) en `app/src/androidTest/kotlin/com/ecosmart/presentation/theme/ComponentesInicioActividadTest.kt`: `C-23` muestra "Hacerla ahora", "Completada hoy" y "Volvé mañana" según estado; zona táctil >= 48 dp; `C-19` marcado decorativo; con tilde expone semántica "Completada" (`RF-IA-006`, `RF-IA-014`, `RF-IA-022`)

### Implementación

- [X] T007 Crear `app/src/main/kotlin/com/ecosmart/presentation/estadodiario/EstadoDiario.kt`: `EstadoActividadUi`, `EstadoCategoriaUi`, `EstadoDiarioUi`, `ActividadInicioUi`, `calcularEstadoDiario(actividades, registros, hoy)` y `seleccionarActividadesDeHoy(...)` como funciones puras, leyendo topes de `EstrategiaDePuntaje.para(categoria).topeDiario` (hace pasar T003 y T004)
- [X] T008 Crear `app/src/main/kotlin/com/ecosmart/presentation/estadodiario/CargadorEstadoDiario.kt` (`@Inject`): obtiene el usuario de `SesionUsuario`, lee `RegistroVerificacionRepository.todos(usuarioId)` y delega en `calcularEstadoDiario`; ante error devuelve `null` (contrato, sección Errores)
- [X] T009 [P] Crear `app/src/main/kotlin/com/ecosmart/presentation/activitydetail/PasosActividadParser.kt` (hace pasar T005)
- [X] T010 [P] Agregar, solo de forma aditiva, los tokens de `RF-IA-021` en `app/src/main/kotlin/com/ecosmart/presentation/theme/Tokens.kt` y `Theme.kt` (degradés por categoría, verdes y ámbar de logos, radios 24 y 28, sombra suave) y, en `app/src/main/kotlin/com/ecosmart/presentation/comun/CategoriaUi.kt`, `logoCategoria()` y `degradePanelCategoria()` con `when` exhaustivo; no modificar `iconoCategoria()`
- [X] T011 [P] Crear el vector `app/src/main/res/drawable/logo_categoria_reciclar.xml` (disco blanco, tres flechas de reciclaje en verdes, corazón ámbar, dibujo central propio; mismo lenguaje que `logo_ecosmart_icono`)
- [X] T012 [P] Crear el vector `app/src/main/res/drawable/logo_categoria_reutilizar.xml` (mismo marco; dibujo central propio)
- [X] T013 [P] Crear el vector `app/src/main/res/drawable/logo_categoria_caminar.xml` (mismo marco; dibujo central propio)
- [X] T014 [P] Crear el vector `app/src/main/res/drawable/ic_camara.xml` (botón principal del detalle)
- [X] T015 Crear `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesInicioActividad.kt` con `LogoCategoria` (`C-19`, tamaños 44/76/120, atenuado, insignia de tilde) y `BotonLlamadoAccion` (`C-23`, tres estados) (hace pasar T006)

**Checkpoint**: T003–T006 en verde; las historias pueden empezar.

---

## Phase 3: User Story 1 - Inicio: ver mi avance del día y elegir qué hacer (Priority: P1) 🎯 MVP

**Goal**: Inicio con el nuevo diseño (I1/I2), datos reales, progreso de hoy y barra de seis íconos.

**Independent Test**: escenarios I-1 a I-10 de `quickstart.md`; `SC-IA-001`, `SC-IA-002`, `SC-IA-003`, `SC-IA-004`.

### Tests primero

- [X] T016 [P] [US1] Ampliar `ComponentesInicioActividadTest` (parte 2): `C-20` con 3 y con 4 categorías (contador "N/total", un segmento y una columna por categoría, "Hecha"/"Pendiente", logo atenuado en pendiente, nota "Hacé al menos una de cada categoría por día"); `C-21` con títulos de 1, 2 y 3 líneas mide lo mismo y no se superpone; semántica "categoría, título, puntos, estado"
- [X] T017 [P] [US1] Reescribir `app/src/androidTest/kotlin/com/ecosmart/presentation/home/HomeScreenTest.kt`: sin "N/10 actividades", sin "Tu progreso general", sin "0 / 1", sin enlace "Ver todas" en el encabezado de actividades, sin botón flotante; con "Tu progreso de hoy", botón "Ver todas las actividades" y tarjetas de lección; `HomeContenido` sin Hilt
- [X] T018 [P] [US1] Ajustar `app/src/androidTest/kotlin/com/ecosmart/presentation/theme/BarraInferiorEcoSmartTest.kt`: seis ítems, "Puntos Verdes" ahora en la barra, selección y callbacks por pestaña incluido `onPuntosVerdes`
- [X] T019 [P] [US1] Test de Inicio con `fontScale = 1.3f` (sin textos cortados) y comprobación de zonas táctiles >= 48 dp de la barra de seis ítems en `HomeScreenTest.kt` (`RF-IA-022`, `SC-IA-006`, `SC-IA-007`)

### Implementación

- [X] T020 [P] [US1] Agregar a `ComponentesInicioActividad.kt` `TarjetaProgresoDiario` (`C-20`) con barra segmentada y columnas por categoría (`RF-IA-005`)
- [X] T021 [P] [US1] Agregar a `ComponentesInicioActividad.kt` `TarjetaActividadConBoton` (`C-21`): panel con degradé de categoría, `LogoCategoria`, chip, "+N pts", título hasta 3 líneas y `BotonLlamadoAccion`; alto único (`RF-IA-006`, `RF-UI-006`)
- [X] T022 [P] [US1] Agregar a `ComponentesInicioActividad.kt` `EncabezadoInicio` (degradé verde, esquinas inferiores redondeadas, saludo, subtítulo de marca, avatar con inicial) y `TarjetaPuntosInicio` (blanca, superpuesta, logo de la app, "Tus puntos", total real, hojita, chevron) (`RF-IA-003`, `RF-IA-004`)
- [X] T023 [US1] Modificar `app/src/main/kotlin/com/ecosmart/presentation/home/HomeViewModel.kt`: inyectar `CargadorEstadoDiario`, reemplazar `ActividadItemUi`/contadores "0/1" por `estadoDiario: EstadoDiarioUi?` y `actividadesInicio: List<ActividadInicioUi>`; `refrescar()` recarga el estado en `ON_RESUME`; sin valores de ejemplo por defecto (`RF-IA-019`)
- [X] T024 [US1] Reescribir `app/src/main/kotlin/com/ecosmart/presentation/home/HomeScreen.kt`: orden `RF-IA-002` (encabezado, tarjeta de puntos, "Tu progreso de hoy" solo si `estadoDiario != null`, carrusel "Actividades de hoy" sin "Ver todas" en el encabezado, botón "Ver todas las actividades" de contorno, "Contenido para vos" intacto de spec 004); eliminar la tarjeta "Tu progreso general" y el botón flotante del mapa (`RF-IA-007`, `RF-IA-008`, `RF-IA-010`)
- [X] T025 [US1] Cambiar `BarraInferiorEcoSmart` en `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt`: agregar `PestanaInferior.PUNTOS_VERDES` como sexto ítem y el parámetro `onPuntosVerdes`; `contentDescription` "Puntos Verdes"; 48 dp por ítem (`RF-IA-009`)
- [X] T026 [US1] Actualizar `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt`: `BarraInferior` navega a `EcoSmartRoute.PuntosVerdes` con `irAPestana`; en Home quitar el parámetro `onVerPuntosVerdes` del botón flotante; la tarjeta de puntos sigue abriendo Perfil (`research.md` §5, §6)
- [X] T026a [US1] En `EcoSmartNavHost.kt`, pasar `barraInferior = { BarraInferior(navController, PestanaInferior.PUNTOS_VERDES) }` a `PuntosVerdesScreen` (ya declara el parámetro `barraInferior` y usa `Scaffold`) y comprobar en `PuntosVerdesScreen.kt` que la barra no tapa el mapa ni sus controles (`RF-IA-009`)
- [X] T026b [US1] Verificar en cada destino con barra (Actividades, Recompensas, Educación y su detalle, detalle de actividad, Caminata, Perfil, Puntos Verdes) que usa `BarraInferior(...)` y por tanto muestra los seis íconos con su pestaña resaltada; corregir cualquier pantalla que declare su propia barra

> **Orden**: T017–T019 antes que T020–T026 (los tests deben fallar primero). T025 antes que T026.

**Checkpoint**: Inicio completo y probable por sí solo (escenarios I-1 a I-10).

---

## Phase 4: User Story 2 - Actividad individual: hacerla desde su detalle (Priority: P1)

**Goal**: detalle de actividad no medible con el diseño D1/D2, estados y datos reales.

**Independent Test**: escenarios D-1 a D-4, D-6 y D-7 de `quickstart.md`; `SC-IA-001`, `SC-IA-005`.

### Tests primero

- [X] T027 [P] [US2] Ampliar `ComponentesInicioActividadTest` (parte 3): `C-22` con y sin tilde y chip "Completada"; `C-24` con 1, 3 y 5 pasos; semántica de cada uno
- [X] T028 [P] [US2] Ajustar `app/src/androidTest/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleScreenTest.kt`: estado para hacer (botón "¡Hacela ahora y sumá N puntos!" con N real, nota "Sacás una foto, la describís y listo", pasos numerados, tip "Qué tiene que verse en la foto"), estado completada (tilde, chip, mensaje "¡Listo por hoy! …", sin botón), sin "Realizarla" ni "0 / 1" ni colinas; Caminar conserva su diseño (`AnilloProgreso`, "1.200 m")

### Implementación

- [X] T029 [P] [US2] Agregar a `ComponentesInicioActividad.kt` `PanelActividad` (`C-22`) y `PasoNumerado` (`C-24`) (`RF-IA-012`, `RF-IA-015`)
- [X] T030 [US2] Modificar `app/src/main/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleViewModel.kt`: inyectar `CargadorEstadoDiario`, exponer `estado`, `pasos` (vía `PasosActividadParser`) y `tip` (`resultadoEsperado` o `null`); recarga en `ON_RESUME` si hace falta (data-model §3)
- [X] T031 [US2] Modificar `app/src/main/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleScreen.kt`: rama no medible (Reciclar/Reutilizar) con el nuevo orden `RF-IA-018` (encabezado con volver 48×48, título 22/28 y subtítulo con hojita en línea; `PanelActividad`; botón principal con `ic_camara` y nota, o mensaje de completada/no disponible; "Cómo se registra" con `PasoNumerado`; `TarjetaTip` de spec 004 con `ic_bombilla`); la rama medible (Caminar) no cambia
- [X] T032 [US2] Verificar en `EcoSmartNavHost.kt` que `onRealizarla` sigue abriendo la captura de foto para no medibles y la de caminata para Caminar (sin cambios de comportamiento)

> **Orden**: T027–T028 antes que T029–T032.

**Checkpoint**: Inicio y detalle completos (MVP funcional de ambas pantallas).

---

## Phase 5: User Story 3 - Aprender antes de hacer (Priority: P3)

**Goal**: "Aprendé primero" abre la primera lección de la categoría.

**Independent Test**: escenario D-5 de `quickstart.md`.

### Tests primero

- [X] T033 [P] [US3] Test Compose de `C-25` `TarjetaEnlaceLeccion` (texto, zona táctil, callback) en `ComponentesInicioActividadTest.kt` y caso en `ActividadDetalleScreenTest.kt`: sin lecciones de la categoría el bloque no se muestra y no deja espacio vacío
- [X] T034 [P] [US3] Test JVM de la elección de la lección (primera de la categoría según el catálogo, `null` si no hay) en `app/src/test/kotlin/com/ecosmart/presentation/activitydetail/LeccionRelacionadaTest.kt`

### Implementación

- [X] T035 [P] [US3] Agregar `TarjetaEnlaceLeccion` (`C-25`) a `ComponentesInicioActividad.kt`
- [X] T036 [US3] Modificar `ActividadDetalleViewModel.kt` para inyectar `CatalogoLecciones` y exponer `leccionRelacionadaId` (primera lección de la categoría; `null` si el catálogo falla o no hay), con la elección en una función pura
- [X] T037 [US3] Agregar el bloque "Aprendé primero" a `ActividadDetalleScreen.kt` y el parámetro `onVerLeccion` (`RF-IA-017`)
- [X] T038 [US3] En `EcoSmartNavHost.kt`: pasar `onVerLeccion` a `ContenidoEducativoDetalle.crearRuta(id)` desde el detalle y, en la ruta del detalle de lección, resaltar `PestanaInferior.ACTIVIDADES` si el destino anterior es `ActividadDetalle` (`research.md` §8)

**Checkpoint**: las tres historias completas.

---

## Phase 6: Polish & Cross-Cutting

- [X] T039 Retirar código muerto verificando antes sus usos con una búsqueda de referencias: `IndicadorNoMedible`, `TarjetaInformativaDetalle`/`TarjetaTipDetalle` (si quedaron sin uso), `il_paisaje_colinas.xml` (si Caminar no lo usa), `ActividadItemUi` y el bloque "Tu progreso general"; conservar `AnilloProgreso` y `iconoCategoria()`
- [X] T040 Test de regresión de Caminar: su detalle conserva el diseño actual (`ActividadDetalleScreenTest.kt`)
- [X] T041 `:app:ktlintCheck :app:detekt --continue --no-daemon`: sin violaciones nuevas; si el formulario quita imports usados en lambdas, restaurarlos (ver memoria del repo)
- [X] T042 Calcular y registrar los contrastes de `quickstart.md` § Accesibilidad (texto sobre cada degradé de panel, blanco sobre el verde del encabezado, "Pendiente", texto sobre el ámbar del tip); ajustar el tono dentro de los tokens si algún par queda < 4,5:1 (`RF-IA-022`, `SC-IA-007`)
- [X] T043 Revisión de código: ninguna animación ni transición en Inicio ni en el detalle (`RF-IA-023`, `SC-IA-008`) y ningún valor de ejemplo (`Viky`, `0/100`, `0/3`, `+50`) como constante (`SC-IA-002`)
- [X] T044 Verificar `SC-IA-009`: `git diff --stat -- app/src/main/kotlin/com/ecosmart/domain app/src/main/kotlin/com/ecosmart/application app/src/main/kotlin/com/ecosmart/infrastructure backend firestore.rules` sin cambios
- [X] T045 Verificar que los cambios en `Tokens.kt`, `Theme.kt` y `CategoriaUi.kt` sean solo aditivos (`git diff`)
- [X] T046 Regresión de navegación y barra en todas las pestañas: Inicio, Actividades, Recompensas, Educación, Perfil y Puntos Verdes con la barra de seis íconos, más el detalle de actividad y el detalle de lección
- [ ] T047 Ejecutar `:app:testDebugUnitTest :app:compileDebugAndroidTestKotlin --no-daemon` y las instrumentadas de presentación en el emulador `Pixel_7` (`SC-IA-004`, `SC-IA-006`)
- [X] T048 Build de release (`:app:assembleRelease --no-daemon`) y confirmar que R8 no elimina los nuevos logos ni clases (`res/raw/keep.xml` ya protege recursos por nombre; agregar `logo_categoria_*` si se resuelven por nombre)
- [ ] T049 Recorrer `quickstart.md` completo (escenarios I-1 a I-10 y D-1 a D-7) en el emulador y comparar cada pantalla con los tableros I1, I2, D1 y D2
- [X] T050 Actualizar `specs/005-redisenio-ui-inicio-actividad/spec.md` (Status → Implemented) y la nota de `especificacion-ui-app-eco.md` §4 y S-03 indicando que Inicio y el detalle de actividad no medible fueron reemplazados por el spec 005

---

## Dependencias y orden de ejecución

- **Phase 1 → Phase 2 → Phase 3/4/5 → Phase 6**.
- **Dentro de Foundational**: T003–T006 (tests) antes de T007–T015; T011–T014 son independientes entre sí.
- **US1** requiere Foundational. **US2** requiere Foundational y comparte `ComponentesInicioActividad.kt` y `EcoSmartNavHost.kt` con US1, por lo que va después. **US3** requiere US2 (usa su ViewModel y pantalla).
- **Tareas de alto riesgo**: T025/T026 (barra global) y T031 (rama de Caminar): hacerlas con sus tests ya escritos.

### Oportunidades de paralelismo

- T003, T004, T005, T006 (tests, archivos distintos).
- T011, T012, T013, T014 (vectores) y T009, T010.
- T016, T017, T018, T019 (tests de US1) y T020, T021, T022 (componentes de US1, mismo archivo: coordinar para no pisarse; se pueden escribir en paralelo si se fusionan al final).

## Estrategia de implementación

1. **MVP**: Phases 1–3 (Inicio). Es la pantalla principal y entrega el rediseño más visible; validar con I-1 a I-10.
2. **Incremento 2**: Phase 4 (detalle de actividad). Validar con D-1 a D-4, D-6, D-7.
3. **Incremento 3**: Phase 5 (Aprendé primero) y Polish.
4. Cada incremento termina con tests unitarios y de componentes en verde, lint limpio y `git diff` sin cambios en capas inferiores.

## Trazabilidad requisito → tareas

| Requisito | Tareas |
|---|---|
| RF-IA-001, SC-IA-009 | T044 |
| RF-IA-002, 003, 004, 007, 008, 010 | T022, T024 |
| RF-IA-005, RF-IA-019b | T003, T007, T016, T020 |
| RF-IA-006, 006a | T004, T007, T021, T023 |
| RF-IA-009 | T018, T025, T026, T026a, T026b |
| RF-IA-011 a 014, 018 | T028, T029, T031 |
| RF-IA-015, 016 | T005, T009, T031 |
| RF-IA-017 | T033–T038 |
| RF-IA-019 | T008, T023, T043 |
| RF-IA-020, 021 | T010–T015 |
| RF-IA-022, 023 | T019, T042, T043 |
