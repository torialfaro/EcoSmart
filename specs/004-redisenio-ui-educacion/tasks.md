---

description: "Lista de tareas de la reconstrucción de la sección Educación (spec 004)"
---

# Tasks: Reconstrucción de la Sección Educación

**Input**: Design documents from `specs/004-redisenio-ui-educacion/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/lecciones.schema.json](./contracts/lecciones.schema.json),
[quickstart.md](./quickstart.md)

**Tests**: Incluidos. Este módulo no agrega reglas de negocio (`RF-UI-001`), pero sí reglas de validación
del catálogo (parser) y contratos visuales/de accesibilidad nuevos (`research.md` §9). Se escriben primero
los tests del parser y de agrupación (JVM, JUnit 5) y los de pantalla/componente (Compose UI Test); los
tests unitarios existentes se re-ejecutan sin cambios como validación de "cero regresiones" (`SC-ED-005`).

**Organization**: Tareas agrupadas por historia de usuario del spec (US1=P1 listado, US2=P2 detalle,
US3=P3 cierre hacia Actividades, US4=P3 tarjetas en Inicio). La fase Fundacional (tokens, parser, catálogo,
vectores, componentes base) bloquea a todas las historias. Después, US1 y US2 comparten `EcoSmartNavHost.kt`
y `ComponentesLeccion.kt`, por lo que se implementan en orden; US3 depende de US2 y US4 depende de US1 y US2.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario (US1–US4); sin etiqueta en Setup, Fundacional y Polish
- Las rutas son las reales del repositorio (ver `plan.md` § Source Code)

## Path Conventions

- Presentación: `app/src/main/kotlin/com/ecosmart/presentation/...` (módulo único `:app`; este módulo **no** toca
  `domain/`, `application/`, `infrastructure/` ni `backend/`)
- Recursos: `app/src/main/res/...`, `app/src/main/assets/educacion/...`
- Tests JVM: `app/src/test/kotlin/com/ecosmart/presentation/education/...`
- Tests Compose UI: `app/src/androidTest/kotlin/com/ecosmart/presentation/...`
- Nota de herramienta: nunca importar `androidx.compose.foundation.layout.weight` (error de K2, ver memoria del repo)

---

## Análisis de impacto sobre código existente y backend

Revisión del código actual (2026-10-30) para detectar tareas que **editan** archivos compartidos o que podrían necesitar backend.

### Backend / Firestore: sin impacto

- `backend/` (FastAPI) y `firestore.rules` no contienen nada de contenido educativo, lecciones ni videos (búsqueda sin resultados relevantes); el catálogo es un asset local de solo lectura (`RF-ED-014`).
- Ninguna tarea requiere endpoint, colección ni regla nuevos. `T046` verifica que `backend/`, `firestore.rules` y las capas `domain/`, `application/`, `infrastructure/` queden sin cambios.
- `CatalogoLecciones` (T010) solo **consume** `Moshi`, ya provisto por `infrastructure/di/NetworkModule.kt` (`provideMoshi`); no se edita ese módulo.

### Tareas que editan archivos existentes y compartidos

| Tarea | Archivo existente | Quién más lo usa | Riesgo | Mitigación (tarea) |
|---|---|---|---|---|
| T005, T006, T007 | `Theme.kt`, `Tokens.kt`, `CategoriaUi.kt` | Todas las pantallas | Bajo: solo se agregan símbolos | T051 verifica que el diff sea solo aditivo |
| T020 | `EncabezadoEcoSmart` en `ComponentesEcoSmart.kt` | 12 pantallas: Inicio, Actividades, Recompensas, Detalle de actividad, Perfil edición, Registro, Puntos Verdes, Caminata, permisos (2), `EncabezadoConUsuario` | **Alto**: cualquier cambio de comportamiento por defecto afecta toda la app | T048 (test de regresión de defaults) y T049 (revisión de las pantallas) |
| T023, T030, T035, T039 | `EcoSmartNavHost.kt` | Toda la navegación | **Alto** en T035: `ir(ruta)` de `BarraInferior` pasa a ser compartida por las 5 pestañas | T050 (regresión de navegación) |
| T031, T040 | `ContenidoEducativoScreen.kt` (eliminado), `TarjetaVideo` (eliminado) | `SegmentoEcoSmart`/`EnlaceSeccion` del mismo archivo siguen usados por Actividades e Inicio | Medio: referencias huérfanas | Búsqueda previa de referencias dentro de T031/T040 |
| T036, T038 | `HomeScreen.kt` / `HomeContenido` y `HomeScreenTest.kt` | `HomeScreenTest` hoy afirma "¿Cómo afecta la contaminación?" y "Tu guía de reciclaje" | Medio: el test actual **fallará** por diseño | T036 actualiza el test; `HomeContenido` queda sin Hilt (recibe las lecciones por parámetro) |
| T002, T012, T013, T014 | `res/raw`, `res/drawable`, `assets/` | Build de release (`isMinifyEnabled = true`) | Bajo | T052 (build de release) |

Los nuevos archivos (`education/*`, `ComponentesLeccion.kt`, tests) no afectan código existente.

---

## Phase 1: Setup

**Purpose**: Estructura mínima. No se agrega ninguna dependencia de Gradle (`research.md` §2).

- [X] T001 Crear el paquete `app/src/main/kotlin/com/ecosmart/presentation/education/` y la carpeta `app/src/main/assets/educacion/`
- [X] T002 [P] Crear `app/src/main/res/raw/keep.xml` con `tools:keep="@drawable/il_leccion_*"` para que los recursos usados solo por nombre no se eliminen (`research.md` §1)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Tokens, modelos, parser, catálogo, ilustraciones y componentes base que usan todas las historias.

**⚠️ CRITICAL**: Ninguna historia puede comenzar hasta completar esta fase.

### Tests (escribirlos primero y verificar que fallan)

- [X] T003 [P] Crear `app/src/test/kotlin/com/ecosmart/presentation/education/CatalogoLeccionesParserTest.kt` (JUnit 5): archivo válido; `{"lecciones": []}` → `Vacio`; JSON truncado/sin raíz → `Error`; lección sin título, `id` vacío, categoría desconocida, sin pasos o duración ≤ 0 descartada; todas inválidas → `Error`; `id` duplicado conserva la primera; `cantidadPasos` derivado de `pasos.size` y campo `cantidadPasos` del archivo ignorado; `textoCierre` ausente/vacío → `null`
- [X] T004 [P] Crear `app/src/test/kotlin/com/ecosmart/presentation/education/AgrupacionLeccionesTest.kt` (JUnit 5): orden fijo Reciclar/Reutilizar/Caminar, orden del catálogo dentro de cada sección, secciones vacías omitidas, selección de las 2 primeras lecciones para Inicio (`data-model.md` §4)

### Implementation

- [X] T005 [P] Agregar a `app/src/main/kotlin/com/ecosmart/presentation/theme/Theme.kt` los colores pastel (`#FFE8BE`, `#E3F2F6`, `#DCEFE6`, `#E4EEF3`, `#DFF0E6`; alias `#E6F2DF` = `ColorGreen100`), textos `#3E4A40`/`#4A554C`, `#8A5A00`, alias del tip (`ColorTipFondo`, `ColorTipBorde`, `ColorTipTitulo`, `ColorTipTexto` sobre los valores de racha existentes) y los estilos tipográficos 30/36, 24/32, 18/24, 15/22, 14/20, 14/21 (`RF-ED-018`, `data-model.md` §5)
- [X] T006 [P] Agregar a `app/src/main/kotlin/com/ecosmart/presentation/theme/Tokens.kt` `RadiusTarjetaLeccion` (24), `RadiusHoja` (28), `SpaceGapTarjetaLeccion` (16), `SpaceSeccionEducacion` (28) y las formas `RoundedCornerShape` correspondientes
- [X] T007 [P] Extender `app/src/main/kotlin/com/ecosmart/presentation/comun/CategoriaUi.kt` con funciones de mapeo por categoría con `when` exhaustivo: colores del chip (fondo/texto), fondo pastel de ilustración (`RF-ED-005`) y la lista de orden fijo de secciones
- [X] T008 [P] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/LeccionUi.kt` con `LeccionUi`, `PasoLeccionUi`, `TipLeccionUi` y `sealed class ResultadoCatalogo` (`Cargado`/`Vacio`/`Error`) según `data-model.md` §2–§3
- [X] T009 Crear `app/src/main/kotlin/com/ecosmart/presentation/education/CatalogoLeccionesParser.kt`: DTOs Moshi `@JsonClass(generateAdapter = true)` y `parsear(json: String): ResultadoCatalogo` con las reglas de validación de `research.md` §4 (depende de T008; debe hacer pasar T003)
- [X] T010 Crear `app/src/main/kotlin/com/ecosmart/presentation/education/CatalogoLecciones.kt`: `@Singleton` con `@ApplicationContext` y `Moshi`, `suspend obtener()` en `Dispatchers.IO` con caché en memoria y `recargar()`; archivo ausente → `Error` (depende de T009)
- [X] T011 [P] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/AgrupacionLecciones.kt` con la agrupación por categoría (orden fijo) y la selección de las 2 primeras para Inicio (debe hacer pasar T004; depende de T007 y T008)
- [X] T012 [P] Crear los 6 vectores simples de reemplazo `app/src/main/res/drawable/il_leccion_circuito_portada.xml` e `il_leccion_circuito_paso1.xml` … `paso5.xml` (envases, contenedor verde, camión, cinta de clasificación, banco de plaza; estilo plano de las referencias; `RF-ED-016`)
- [X] T013 [P] Crear `app/src/main/res/drawable/ic_bombilla.xml` (ícono del tip `C-16`)
- [X] T014 Crear `app/src/main/assets/educacion/lecciones.json` con la lección "El circuito del reciclaje en CABA" (5 pasos, tip y `textoCierre`) según `data-model.md` §1 y validar contra `contracts/lecciones.schema.json` (depende de T012)
- [X] T015 Crear `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesLeccion.kt` con `IlustracionLeccion` (resuelve el drawable por nombre con `getIdentifier`; nombre ausente o inexistente → fondo pastel de la categoría, `RF-ED-017`) y `ChipCategoria` (`C-14`, 28 dp, variante translúcida) (depende de T005–T007)
- [X] T016 [P] Crear `app/src/androidTest/kotlin/com/ecosmart/presentation/theme/ComponentesLeccionTest.kt` con las pruebas de `IlustracionLeccion` (fallback ante nombre inexistente, descripción accesible, adorno sin descripción) y `ChipCategoria` (3 categorías) (depende de T015)

**Checkpoint**: tokens, catálogo parseable y probado, vectores y componentes base listos; `gradle :app:testDebugUnitTest` en verde.

---

## Phase 3: User Story 1 - Elegir una lección desde un catálogo visual (Priority: P1) 🎯 MVP

**Goal**: Educación muestra el catálogo agrupado por categoría, sin segmento ni videos, con estados vacío y de error.

**Independent Test**: Abrir Educación y verificar encabezado (título 30/36, hojita inline, avatar con inicial real, flecha 48×48), una sección por categoría con tarjetas `C-13`, barra inferior con Educación resaltada, sin "Videos destacados"/"Ver todos"; con catálogo vacío y dañado ver los mensajes correspondientes.

### Tests for User Story 1 (primero, deben fallar)

- [X] T017 [P] [US1] Crear `app/src/androidTest/kotlin/com/ecosmart/presentation/education/EducacionScreenTest.kt`: sin segmento Videos/Artículos/IA ni "Videos destacados"/"Ver todos"; sección "Reciclar" con la lección; categorías sin lecciones omitidas; estado vacío ("Todavía no hay lecciones disponibles"); estado de error con "Reintentar"; inicial del usuario en el avatar; tarjeta como único elemento tocable con descripción accesible
- [X] T018 [P] [US1] Agregar a `ComponentesLeccionTest.kt` pruebas de `TarjetaLeccion` con `fontScale = 1.3f` (título largo sin recorte ni superposición con chips) y zona táctil ≥ 48×48
- [X] T048 [P] [US1] Crear `app/src/androidTest/kotlin/com/ecosmart/presentation/theme/EncabezadoEcoSmartTest.kt` (regresión de `C-03`): con los parámetros por defecto el título usa `TypeTitle`, la hojita queda a la derecha del subtítulo y el avatar/flecha se comportan como antes; con `hojaInline = true` el ícono forma parte del texto del subtítulo

### Implementation for User Story 1

- [X] T019 [US1] Agregar `TarjetaLeccion` (`C-13`, variante `compacta`) en `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesLeccion.kt`: radio 24, fondo blanco con borde y sombra suave, ilustración ≥ 132 dp, `ChipCategoria` + "N min · N pasos", título 18/24, resumen 14/20, "Ver lección →", `clickable(role = Button)` con `semantics(mergeDescendants = true)` (depende de T015)
- [X] T020 [US1] Extender `EncabezadoEcoSmart` en `app/src/main/kotlin/com/ecosmart/presentation/theme/ComponentesEcoSmart.kt` con parámetros opcionales `estiloTitulo`, `colorTitulo` y `hojaInline` (hojita al final del texto con contenido inline; el subtítulo puede ocupar 2 líneas); valores por defecto = comportamiento actual para no alterar otras pantallas (`research.md` §6)
- [X] T049 [US1] Verificar que ninguno de los llamadores existentes de `EncabezadoEcoSmart` cambió de aspecto tras T020 (Inicio, Actividades, Recompensas, Detalle de actividad, Perfil edición, Registro, Puntos Verdes, Caminata, permisos y `EncabezadoConUsuario`): `T048` en verde y revisión visual de cada una (depende de T020 y T048)
- [X] T021 [US1] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/EducacionViewModel.kt`: `EstadoEducacion` (`Cargando`/`Cargado`/`Vacio`/`Error`) a partir de `CatalogoLecciones`, `reintentar()` → `recargar()` (depende de T010)
- [X] T022 [US1] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/EducacionScreen.kt`: `Scaffold` con encabezado ajustado (`onVolver`, avatar con `nombre.ifBlank { "U" }`), `LazyColumn` con `rememberLazyListState`, secciones por categoría (títulos `TypeSection`, 28 dp entre secciones, 16 dp entre tarjetas, margen lateral 16), estados vacío/error y barra inferior fija; sin animaciones (depende de T011, T019, T020, T021)
- [X] T023 [US1] En `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt` reemplazar `ContenidoEducativoScreen` por `EducacionScreen` en la ruta `contenido-educativo`: `onSeleccionarLeccion` navega a `ContenidoEducativoDetalle.crearRuta(id)` y `onVolver` hace `if (!navController.popBackStack()) navigate(Home)` (`RF-ED-006`)

**Checkpoint**: Educación rediseñada y probada (T017/T018 en verde); aún coexiste el detalle viejo hasta US2.

---

## Phase 4: User Story 2 - Entender una lección con dibujos paso a paso (Priority: P2)

**Goal**: Pantalla hija "Detalle de lección" con portada, introducción, pasos conectados y tip opcional.

**Independent Test**: Abrir la lección de ejemplo y verificar 5 pasos "Paso N de 5" con conectores, tip tras los pasos, barra con la pestaña de origen resaltada, y volver con scroll restaurado.

### Tests for User Story 2 (primero, deben fallar)

- [X] T024 [P] [US2] Crear `app/src/androidTest/kotlin/com/ecosmart/presentation/education/DetalleLeccionScreenTest.kt`: chips de categoría y "N min · N pasos", título, introducción, pasos "Paso N de M" (3, 5 y 7 pasos), tip presente/ausente sin hueco, lección inexistente → `onVolver`, lista de pasos expuesta como colección ordenada, texto al 130% sin cortes
- [X] T025 [P] [US2] Agregar a `ComponentesLeccionTest.kt` pruebas de `PasoIlustrado` (píldora "Paso N de M", ilustración con descripción), `TarjetaTip` y `EncabezadoLeccion` (botón de volver 48×48)

### Implementation for User Story 2

- [X] T026 [US2] Agregar a `ComponentesLeccion.kt` `PasoIlustrado` (`C-15`: radio 24, ilustración ≥ 150 dp, píldora "Paso N de M", título 18/24, texto 14/21 con alturas mínimas), `ConectorPaso` (24 dp con chevron, decorativo) y `TarjetaTip` (`C-16`: fondo `#FFF1D6`, borde `#F2CF8A`, `ic_bombilla`) (depende de T013, T019)
- [X] T027 [US2] Agregar a `ComponentesLeccion.kt` `EncabezadoLeccion` (`C-18`: banda de 208 dp con la portada, botón circular de volver de 48 dp, hoja crema con esquinas superiores de 28 dp superpuesta 28 dp) (depende de T026)
- [X] T028 [US2] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/DetalleLeccionViewModel.kt`: lee `id` de `SavedStateHandle`, estado `Cargando`/`Cargada(leccion)`/`NoEncontrada` desde `CatalogoLecciones` (depende de T010)
- [X] T029 [US2] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/DetalleLeccionScreen.kt`: `EncabezadoLeccion`, chips, título 24/32, introducción 15/22, pasos con conectores (colección ordenada con `collectionInfo`/`collectionItemInfo`), tip opcional, `Scaffold` con barra inferior; `NoEncontrada` ejecuta `onVolver` en `LaunchedEffect` sin mensaje; deja un parámetro `cierre: @Composable () -> Unit` para US3 (depende de T026–T028)
- [X] T030 [US2] En `EcoSmartNavHost.kt` reemplazar el destino `ContenidoEducativoDetalle` por `DetalleLeccionScreen`: la barra inferior resalta `INICIO` si `previousBackStackEntry` es `home` y `EDUCACION` en otro caso; `onVolver = popBackStack()` (`RF-ED-007`, `RF-ED-008`, `research.md` §5)
- [X] T031 [US2] Buscar referencias a `ContenidoEducativoScreen`, `ContenidoEducativoViewModel`, `ContenidoEducativoDetalleScreen`, `SegmentoEducacion` y `CATALOGO_CONTENIDO` en `app/src` y, sin ninguna restante, eliminar `app/src/main/kotlin/com/ecosmart/presentation/home/ContenidoEducativoScreen.kt` (segmento, catálogo con emojis, `enlaceMasInfo`, `ContenidoEducativoViewModel` y detalle viejo); quitar los imports huérfanos en `EcoSmartNavHost.kt`. `SegmentoEcoSmart` y `EnlaceSeccion` viven en `ComponentesEcoSmart.kt` y siguen usados por Actividades e Inicio (depende de T023 y T030)

**Checkpoint**: Educación → Detalle → volver funciona de punta a punta; el código viejo ya no existe.

---

## Phase 5: User Story 3 - Pasar de aprender a hacer (Priority: P3)

**Goal**: La tarjeta de cierre lleva a la pestaña Actividades en general, en todas las lecciones.

**Independent Test**: Tocar "Ver actividades" al final de cualquier lección y verificar que se abre Actividades en su estado habitual; sin `textoCierre` se ve "Poné en práctica lo que aprendiste".

### Tests for User Story 3 (primero, deben fallar)

- [X] T032 [P] [US3] Agregar a `DetalleLeccionScreenTest.kt` y `ComponentesLeccionTest.kt`: la tarjeta de cierre se muestra siempre; texto propio vs. general; botón "Ver actividades" ≥ 48 dp dispara `onVerActividades`; contraste del texto blanco sobre el degradé

### Implementation for User Story 3

- [X] T033 [US3] Agregar `TarjetaLlamadoAccion` (`C-17`) a `ComponentesLeccion.kt`: degradé `#1F5E34 → #0F4D2E`, etiqueta "Ahora te toca a vos", texto principal y botón píldora blanca "Ver actividades →" de 48 dp (depende de T027)
- [X] T034 [US3] En `DetalleLeccionScreen.kt` mostrar `TarjetaLlamadoAccion` al final con `textoCierre ?: "Poné en práctica lo que aprendiste"` y exponer `onVerActividades` (depende de T029, T033)
- [X] T035 [US3] En `EcoSmartNavHost.kt` extraer la lambda privada `ir(ruta)` de `BarraInferior` a una función compartida `irAPestana(navController, ruta)` (`popUpTo(Home)` + `launchSingleTop`) y conectar `onVerActividades` del detalle con `EcoSmartRoute.Actividades.ruta` (`RF-ED-012`)
- [X] T050 [US3] Regresión de navegación tras el refactor de `irAPestana` (impacta las 5 pestañas): ejecutar `BarraInferiorEcoSmartTest` y recorrer manualmente Inicio, Actividades, Recompensas, Educación, Perfil y Puntos Verdes verificando destino, pestaña resaltada y comportamiento del botón atrás (depende de T035)

**Checkpoint**: el detalle cierra con el llamado a Actividades; el cambio de pestaña sigue la navegación habitual.

---

## Phase 6: User Story 4 - Descubrir lecciones desde Inicio (Priority: P3)

**Goal**: "Contenido para vos" en Inicio muestra tarjetas de lección compactas en lugar de tarjetas de video.

**Independent Test**: Abrir Inicio, ver hasta 2 tarjetas de lección, tocar una y verificar que el detalle resalta Inicio y que volver regresa a Inicio.

### Tests for User Story 4 (primero, deben fallar)

- [X] T036 [P] [US4] Actualizar `app/src/androidTest/kotlin/com/ecosmart/presentation/home/HomeScreenTest.kt`: el test `homeScreen_muestraTarjetaPuntosYCarruseles` hoy afirma los títulos de video "¿Cómo afecta la contaminación?" y "Tu guía de reciclaje" y debe reemplazarse; verificar que la sección muestre tarjetas de lección (máx. 2) y ninguna de video, que se omita con lista vacía y que tocar una tarjeta invoque `onVerLeccion(id)`; `HomeContenido` se prueba pasándole `leccionesInicio` (sin Hilt)

### Implementation for User Story 4

- [X] T037 [US4] Crear `app/src/main/kotlin/com/ecosmart/presentation/education/TarjetasEducativasInicio.kt`: ViewModel Hilt pequeño (`LeccionesInicioViewModel`) que expone las 2 primeras lecciones (depende de T010, T011) y el composable sin estado que recibe `lecciones` y `onVerLeccion` y las renderiza con `TarjetaLeccion(compacta = true)` en fila con scroll horizontal; no renderiza nada si la lista está vacía
- [X] T038 [US4] En `app/src/main/kotlin/com/ecosmart/presentation/home/HomeScreen.kt` reemplazar el carrusel de `TarjetaVideo` por `TarjetasEducativasInicio`, mantener el título "Contenido para vos" (omitiéndolo si no hay tarjetas) y cambiar el parámetro `onVerContenidoEducativo` por `onVerLeccion: (String) -> Unit`; `HomeScreen` obtiene las lecciones del ViewModel y las pasa a `HomeContenido(leccionesInicio, ...)` (sin Hilt dentro de `HomeContenido`, para no romper `HomeScreenTest`); el resto de Inicio no cambia (`RF-ED-013`)
- [X] T039 [US4] En `EcoSmartNavHost.kt` conectar `onVerLeccion` de `HomeScreen` con `navigate(EcoSmartRoute.ContenidoEducativoDetalle.crearRuta(id))`
- [X] T040 [US4] Buscar referencias a `TarjetaVideo` en `app/src` y, sin ninguna restante, eliminarla de `ComponentesEcoSmart.kt` (conservar `SegmentoEcoSmart` y `EnlaceSeccion`, que siguen usados por Actividades e Inicio)

**Checkpoint**: Las 4 historias funcionan; no queda ninguna tarjeta de video en la app.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T041 [P] Ejecutar `gradle :app:ktlintCheck :app:detekt` y corregir hallazgos en los archivos nuevos/modificados (métodos ≤ 40 líneas, sin imports de `weight`)
- [X] T042 Ejecutar `gradle :app:testDebugUnitTest` y confirmar que toda la suite existente más T003/T004 está en verde (`SC-ED-005`)
- [X] T043 Ejecutar `gradle :app:connectedDebugAndroidTest` (T016–T018, T024–T025, T032, T036 y las pruebas existentes) — pruebas de spec 004 y de Presentación en verde; quedan 10 pruebas ajenas al spec (`VerificarFotoConIATest`, `RegistroVerificacionRepositoryImplTest`, `UsuarioRepositoryImplTest`, un timeout de `EcoGptClientTest`) que requieren los emuladores de Firebase Auth/Firestore (10.0.2.2:9099/8080) y no corren sin ellos
- [X] T044 Recorrer `quickstart.md` §2 y §3 en dispositivo/emulador: flujos, restauración de scroll, pestaña de origen, flecha de volver y casos de datos (vacío, dañado, lección inválida, ilustración inexistente, lección extra solo editando el JSON → `SC-ED-008`)
- [X] T045 [P] Validar accesibilidad (`quickstart.md` §4): fuente al 130%, TalkBack, contraste ≥ 4,5:1, zonas táctiles ≥ 48×48 y ausencia de animaciones (`SC-ED-004`, `SC-ED-006`)
- [X] T046 Verificar `git diff --stat`: ningún cambio en `domain/`, `application/`, `infrastructure/`, `backend/` ni `firestore.rules` (este módulo no necesita backend; `SC-ED-005`) — verificado: sin cambios de spec 004 en esas rutas; `backend/app/ecogpt.py` y `main.py` figuran modificados desde el 2026-10-30 (reintento 503 de EcoGPT, anterior a este spec)
- [X] T047 Marcar las tareas como completadas aquí y dejar el estado del spec en "Implemented"
- [X] T051 [P] Verificar con `git diff` que los cambios en `Theme.kt`, `Tokens.kt` y `CategoriaUi.kt` son solo líneas agregadas (ningún valor ni función existente modificado)
- [X] T052 Ejecutar `gradle :app:assembleRelease` (R8 activo, `isMinifyEnabled = true`) e instalar el APK: la lección de ejemplo se lee, se parsea con Moshi y sus ilustraciones se resuelven por nombre (`getIdentifier` + `keep.xml`)
- [X] T053 [P] Agregar una nota en `especificacion-ui-app-eco.md` §8 (S-05) indicando que la sección Educación fue reemplazada por `specs/004-redisenio-ui-educacion/spec.md` (Constitución v2.3.0), sin modificar el resto del documento

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Fase 1)**: sin dependencias.
- **Foundational (Fase 2)**: depende de Setup; **bloquea** todas las historias.
- **US1 (Fase 3)**: depende de la Fase 2.
- **US2 (Fase 4)**: depende de la Fase 2 y de `TarjetaLeccion` (T019) por el orden en `ComponentesLeccion.kt`; T031 depende de T023 y T030.
- **US3 (Fase 5)**: depende de US2 (usa `DetalleLeccionScreen` y `EncabezadoLeccion`).
- **US4 (Fase 6)**: depende de US1 (`TarjetaLeccion`) y US2 (destino del detalle).
- **Polish (Fase 7)**: depende de todas las historias. Las tareas de regresión por impacto (T048–T050, T051–T053) se numeran al final pero se ejecutan junto a la tarea que verifican (T048/T049 con US1, T050 con US3).
### Dentro de cada historia

- Tests primero (deben fallar), luego componentes → ViewModel → pantalla → cableado en `EcoSmartNavHost.kt`.
- Tareas que editan el mismo archivo (`ComponentesLeccion.kt`, `EcoSmartNavHost.kt`, `DetalleLeccionScreen.kt`) no son [P] entre sí.

### Parallel Opportunities

- Fase 2: T003, T004, T005, T006, T007, T008, T012, T013 pueden lanzarse juntas; T009 → T010 y T011 siguen a T008/T007.
- US1: T017 y T018 en paralelo; US2: T024 y T025; US3: T032; US4: T036.
- Polish: T041 y T045 en paralelo.

### Parallel Example: Fase Fundacional

```text
Task: "CatalogoLeccionesParserTest.kt"            (T003)
Task: "AgrupacionLeccionesTest.kt"                (T004)
Task: "Colores y tipografías en Theme.kt"         (T005)
Task: "Radios y espacios en Tokens.kt"            (T006)
Task: "Mapeos por categoría en CategoriaUi.kt"    (T007)
Task: "Modelos en LeccionUi.kt"                   (T008)
Task: "Vectores il_leccion_*.xml"                 (T012)
Task: "ic_bombilla.xml"                           (T013)
```

---

## Implementation Strategy

### MVP First (solo User Story 1)

1. Completar Setup y Foundational.
2. Completar US1 (Educación con catálogo, estados y tarjetas).
3. **Detener y validar** con T017/T018 y el recorrido manual 1–2 de `quickstart.md`.

### Entrega incremental

1. Foundational → US1 → validar (MVP).
2. US2 → validar el detalle y retirar el código viejo.
3. US3 → validar el cierre hacia Actividades.
4. US4 → validar Inicio y retirar `TarjetaVideo`.
5. Polish → ktlint/detekt, suites completas, accesibilidad y chequeo de capas.

## Trazabilidad requisito → tareas

| Requisito | Tareas |
|---|---|
| RF-ED-001, 002, 003 | T022, T023, T031 |
| RF-ED-004, 005 | T007, T015, T019 |
| RF-ED-006 | T020, T022, T023 |
| RF-ED-007, 008 | T022, T030 |
| RF-ED-009, 010, 011 | T026, T027, T029, T033, T034 |
| RF-ED-012, 015 | T034, T035 |
| RF-ED-013 | T037, T038, T039 |
| RF-ED-014, 017 | T003, T008–T010, T014, T015 |
| RF-ED-016 | T002, T012, T015 |
| RF-ED-018 | T005, T006 |
| RF-ED-019, 020, 021 | T016–T018, T024, T025, T029, T045 |

## Notes

- [P] = archivos distintos, sin dependencias pendientes.
- Verificar que los tests fallen antes de implementar.
- Hacer commit después de cada tarea o grupo lógico.
- Las ilustraciones de la lección de ejemplo son vectores simples de reemplazo; diseño las sustituye después editando solo los XML (`RF-ED-016`).
- Los textos de la lección de ejemplo son ilustrativos y se validan antes de publicarse.






















