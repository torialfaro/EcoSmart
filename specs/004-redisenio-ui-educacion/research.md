# Research (Fase 0): Reconstrucción de la Sección Educación

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Fecha**: 2026-10-30

No quedan marcadores `NEEDS CLARIFICATION`. Este documento registra las decisiones técnicas, con
su alternativa descartada, tomadas tras revisar el código actual (`ContenidoEducativoScreen.kt`,
`EcoSmartNavHost.kt`, `HomeScreen.kt`, `ComponentesEcoSmart.kt`, `Tokens.kt`, `Theme.kt`, `app/build.gradle.kts`).

## 1. Formato y resolución de ilustraciones

- **Decision**: VectorDrawable XML en `res/drawable/il_leccion_*.xml`. El JSON guarda el nombre
  del recurso (p. ej. `"il_leccion_circuito_paso1"`); `IlustracionLeccion` lo resuelve con
  `resources.getIdentifier(nombre, "drawable", packageName)` y lo dibuja con `painterResource`.
  Si el nombre no existe (`getIdentifier == 0`), se muestra el fondo pastel de la categoría
  (`RF-ED-017`). Se agrega `res/raw/keep.xml` con `tools:keep="@drawable/il_leccion_*"` para que una
  futura activación de `shrinkResources` no elimine recursos que solo se usan por nombre.
- **Rationale**: `RF-ED-016`/`SC-ED-008` exigen que sumar o reemplazar una ilustración no requiera
  tocar el código de las pantallas; un mapa `nombre → R.drawable.x` en Kotlin lo incumpliría.
  Ya existe el precedente `il_paisaje_colinas.xml`. Los vectores escalan sin bajar calidad y
  diseño puede reemplazarlos exportando SVG → VectorDrawable.
- **Alternatives considered**: (a) dibujo por `Canvas` en código — descartado en clarify por baja
  fidelidad a las escenas de las referencias y por requerir código para cada cambio;
  (b) PNG/WebP en `assets/` con Coil — agrega dependencia y pierde nitidez; (c) mapa estático
  `R.drawable` — rompe `SC-ED-008`.
- **Nota**: `getIdentifier` es desaconsejado para uso general, pero es el mecanismo adecuado cuando
  el nombre proviene de datos; el costo (lookup por nombre de ~7 recursos) es despreciable.
- **Alcance de entrega**: vectores simples de reemplazo para portada + 5 pasos de la lección de
  ejemplo, inspirados en las referencias (envases, contenedor verde, camión, cinta de clasificación,
  banco de plaza); diseño los sustituye editando solo el archivo.

## 2. Librería de interpretación del JSON

- **Decision**: **Moshi** (`moshi-kotlin` + `moshi-kotlin-codegen` vía KSP), ya presentes por el cliente EcoGPT.
  DTOs `@JsonClass(generateAdapter = true)` con campos opcionales anulables; el parser valida y mapea a `LeccionUi`.
- **Rationale**: cero dependencias nuevas (`RF-UI-001`, Principio VII); el codegen evita reflexión y
  funciona con R8 (la configuración de Moshi ya existe en `proguard-rules.pro`).
- **Alternatives considered**: `org.json` (sin tipado, más validación manual); `kotlinx.serialization`
  (plugin y dependencia nuevos sin beneficio).

## 3. Dónde vive la lectura del asset (Principio IV)

- **Decision**: la lectura del asset y el parseo viven en `presentation/education/` (clase
  `CatalogoLecciones`, `@Singleton`, inyecta `@ApplicationContext` y `Moshi`; el parseo en sí es
  una función pura `CatalogoLeccionesParser.parsear(json: String)`).
- **Rationale**: el catálogo no es un dato de usuario ni de negocio (mismo criterio que
  `RF-UI-003b` para Recompensas y que la constitución aplica al "catálogo estático" fuera de la
  regla de persistencia). La Constitución reserva Infraestructura a adaptadores de sensores/APIs
  nativas y persistencia; un asset empaquetado de solo lectura es un recurso de UI. Separar el parser
  puro del lector de `Context` permite probarlo en JVM sin Robolectric.
- **Alternatives considered**: crear `LeccionRepository` en Dominio/Aplicación + implementación en
  Infraestructura — sobreingeniería y toca capas prohibidas por `RF-UI-001`.

## 4. Caché, estados y "Reintentar"

- **Decision**: `CatalogoLecciones.obtener(): ResultadoCatalogo` (suspend, `Dispatchers.IO`) con
  caché en memoria; `recargar()` descarta la caché. `ResultadoCatalogo` es una `sealed class`:
  `Cargado(lecciones: List<LeccionUi>)`, `Vacio`, `Error`. Reglas:
  - JSON ilegible, ausente o sin estructura raíz válida → `Error`.
  - Archivo válido sin lecciones → `Vacio`.
  - Lección inválida (sin título, categoría desconocida, sin pasos, duración ≤ 0, `id` vacío) → se descarta;
    si no queda ninguna válida pero el archivo tenía lecciones → `Error`.
  - `id` duplicado → se conserva la primera ocurrencia y se descartan las siguientes (cierra la ambigüedad menor señalada en clarify).
- **UI**: `EducacionViewModel` expone `EstadoEducacion` (`Cargando`, `Cargado`, `Vacio`, `Error`);
  `Cargando` dura milisegundos y solo muestra encabezado y barra (sin spinner: los estados de
  carga están fuera de alcance). `TarjetasEducativasInicio` y `DetalleLeccionViewModel` consumen el mismo singleton.
- **Alternatives considered**: ViewModel por pantalla leyendo el asset cada vez — duplica parseo y
  dispersa la política de errores.

## 5. Navegación

- **Decision**: reutilizar `EcoSmartRoute.ContenidoEducativo` (`contenido-educativo`) y
  `ContenidoEducativoDetalle` (`contenido-educativo/{id}`) sin renombrarlas; `id` pasa a ser el `id` de la lección.
  - **Pestaña resaltada en el detalle** (`RF-ED-007`): el destino lee
    `navController.previousBackStackEntry?.destination?.route` al componerse: si es `home` → `PestanaInferior.INICIO`,
    en cualquier otro caso → `EDUCACION`. Volver con `popBackStack()` regresa a la pantalla de origen.
  - **Posición de scroll** (`RF-ED-008`): se usa `rememberLazyListState()` (saveable) en el listado
    y `rememberScrollState()` en Inicio; Navigation Compose restaura el estado guardado de la entrada al volver. No requiere código extra.
  - **Flecha de volver del listado** (`RF-ED-006`): `if (!navController.popBackStack()) navigate(Home)`.
  - **Lección inexistente**: `DetalleLeccionViewModel` expone `NoEncontrada` y la pantalla ejecuta `onVolver()` en un `LaunchedEffect`, sin mensaje.
  - **"Ver actividades"** (`RF-ED-012`): se extrae la lambda privada `ir(ruta)` de `BarraInferior` a una función compartida
    (`irAPestana(navController, ruta)`) que hace `navigate(ruta) { popUpTo(Home); launchSingleTop = true }`; el detalle la
    invoca con `EcoSmartRoute.Actividades.ruta`. Volver desde Actividades regresa a Inicio, igual que cualquier cambio de pestaña.
  - **Inicio**: `HomeScreen` recibe `onVerLeccion: (String) -> Unit` que navega a `ContenidoEducativoDetalle.crearRuta(id)`;
    el parámetro `onVerContenidoEducativo` queda sin uso y se retira.
- **Rationale**: mínimo cambio de superficie en el `NavHost` y en la barra inferior; las rutas son internas, no hay deep links.
- **Alternatives considered**: nueva ruta `educacion/{leccionId}` (elegida en clarify de 2026-10-28) — descartada en el plan
  por no aportar nada sobre las existentes y exigir tocar `BarraInferior`; `NavGraph` anidado por pestaña — rompería el patrón actual de una sola capa.

## 6. Encabezado ajustado (`C-03`)

- **Decision**: extender `EncabezadoEcoSmart` con dos parámetros opcionales que conservan el
  comportamiento actual por defecto: `estiloTitulo: TextStyle = TypeTitle` y `colorTitulo: Color = ColorTextPrimary`
  (Educación pasa `TypeTituloEducacion` 30/36 Bold y `ColorGreen900`); y un `mostrarHojaSubtitulo` que ya existe, pero la hojita
  pasa a mostrarse **inline** al final del texto (`Text` con `InlineTextContent`/`appendInlineContent`), de modo que si el subtítulo
  ocupa dos líneas el ícono no se separa del texto (`RF-ED-006`). El `Row` actual cambia a `Text` con ícono inline solo cuando
  `hojaInline = true`, para no alterar el resto de las pantallas.
- **Rationale**: `RF-ED-006` pide cambios solo para Educación; el resto de pantallas conserva su estilo (`RF-UI-014`, etc.).
- **Alternativa descartada**: crear un encabezado nuevo — duplica `C-03`.

## 7. Tokens de diseño (`RF-ED-018`, `RF-UI-004`)

- **Decision**: reutilizar valores ya definidos para no duplicar colores:
  `ColorRecompensaCardBg` (`#FFF1D6`) → fondo del tip; `ColorRachaBorde` (`#F2CF8A`) → borde del tip;
  `ColorRachaTextoNumero` (`#6B3300`) y `ColorRachaTextoMensaje` (`#6B4A12`) → textos del tip; `ColorGreen100` (`#E6F2DF`), `ColorGreen700` (`#1F5E34`),
  `ColorGreen500` (`#2E7D3E`), `ColorGreen900` (`#0F4D2E`) ya existen. Se crean **alias semánticos** (`ColorTipFondo`, `ColorTipBorde`, `ColorTipTitulo`,
  `ColorTipTexto`) para que Educación no dependa de nombres de "racha".
  Colores nuevos: pasteles `#FFE8BE`, `#DCEFE6`, `#E3F2F6`, `#E4EEF3`, `#DFF0E6`; textos `#3E4A40`, `#4A554C`; chip Reutilizar texto `#8A5A00`.
  Radios nuevos: 24 (`RadiusTarjetaLeccion`), 28 (`RadiusHoja`). Espacios: `SpaceGapTarjetaLeccion = 16`, `SpaceSeccionEducacion = 28`
  (los existentes `SpaceGapCard = 12` y `SpaceSection = 24` no coinciden y no se modifican).
  Tipografías nuevas: 30/36 Bold, 24/32 Bold, 18/24 Bold, 15/22, 14/20 y 14/21 (Nunito).
  Cada token se usa por nombre; no se admiten valores sueltos en pantallas.
- **Rationale**: `RF-ED-018` exige registrar y usar tokens; las alias evitan acoplar el significado.

## 8. Accesibilidad (`RF-ED-019`, `RF-ED-021`)

- Tarjeta de lección: `Modifier.clickable(role = Role.Button)` + `semantics(mergeDescendants = true)` con descripción
  "Categoría, título, N minutos, N pasos"; zona táctil ≥ 48×48 (la tarjeta completa).
- Ilustraciones informativas: `contentDescription` del JSON; adornos (destello, conector, hojita): `contentDescription = null`.
- Lista de pasos: `Column` con `semantics { collectionInfo = CollectionInfo(rowCount = n, columnCount = 1) }` y
  `collectionItemInfo` por paso; la píldora "Paso N de M" forma parte del texto del paso.
- Texto al 130%: sin alturas fijas; usar `heightIn(min = …)` y `Modifier.defaultMinSize` para ilustraciones (132/150/208) y botones (48); los títulos sin `maxLines` ni `overflow = Ellipsis`; introducción y paso "hasta 3 líneas" se interpreta como mínimo del bloque, no límite de render.
- Contraste: chips, texto secundario `#3E4A40`/`#4A554C` sobre blanco/crema ≥ 4,5:1; texto blanco del cierre sobre degradé verde `#1F5E34 → #0F4D2E` ≥ 4,5:1 (verificar en quickstart).
- Sin animaciones: no se usan `animate*`, `AnimatedVisibility` ni transiciones personalizadas en Educación (`RF-ED-020`).

## 9. Estrategia de pruebas (Principio VIII)

- **JVM (JUnit 5)**: `CatalogoLeccionesParserTest` — archivo válido, vacío, ilegible, sin raíz, lección inválida descartada,
  todas inválidas → `Error`, categoría desconocida, `id` duplicado, duración no positiva, pasos vacíos, `cantidadPasos` derivado de la lista;
  `AgrupacionLeccionesTest` — orden fijo Reciclar/Reutilizar/Caminar, orden de catálogo dentro de cada categoría, secciones vacías omitidas, selección de las 2 primeras para Inicio.
- **Compose UI Test (androidTest)**: Educación (sin segmento ni "Videos destacados", secciones por categoría, estado vacío y de error con "Reintentar",
  inicial real del avatar), Detalle (pasos "Paso N de M", tip omitido si falta, cierre siempre presente, texto general vs. propio, fallback de ilustración),
  tarjeta (único elemento tocable), `HomeScreenTest` actualizado (tarjetas de lección, sin tarjetas de video). Prueba de texto al 130% con `LocalDensity` con `fontScale = 1.3f`.
- **Manual (quickstart.md)**: restauración de scroll, pestaña resaltada según origen, flecha de volver, TalkBack.
- **Sin regresiones**: se corre la suite existente de `app/src/test` (`SC-ED-005`).

## 10. Limpieza del código actual

- Se elimina `home/ContenidoEducativoScreen.kt`: `SegmentoEducacion`, `CATALOGO_CONTENIDO` (6 contenidos con emojis marcados como placeholder), `enlaceMasInfo`
  (abría una búsqueda externa en Google), `ContenidoEducativoViewModel` y `ContenidoEducativoDetalleScreen`.
- **Contenido migrado**: solo la lección de ejemplo "El circuito del reciclaje en CABA" (la única con contenido y estructura definidos por el spec). Los otros 5 contenidos se descartan
  porque no son lecciones ilustradas ni tienen ilustraciones ni validación editorial; se podrán sumar editando únicamente `lecciones.json` + vectores.
- `TarjetaVideo` (`C-09`) y `SegmentoEcoSmart`/`EnlaceSeccion` seguirán definidos si otros usos los necesitan (Actividades usa el segmento; `EnlaceSeccion` se verifica).
  `TarjetaVideo` queda sin referencias: se decide su retiro en `/speckit.tasks` (retiro seguro, sin tests que la usen).

## 11. Trazabilidad requisito → decisión

| Requisito | Decisión |
|---|---|
| RF-ED-001/002/003 | EducacionScreen sin segmento; secciones por categoría (§5, plan Structure) |
| RF-ED-004/005 | `TarjetaLeccion` (`C-13`) + `ChipCategoria` (`C-14`), tokens (§7) |
| RF-ED-006 | `EncabezadoEcoSmart` con parámetros opcionales (§6), `popBackStack()` o Home (§5) |
| RF-ED-007/008 | Pestaña según `previousBackStackEntry`; estado saveable (§5) |
| RF-ED-009/010/011 | `DetalleLeccionScreen`, `EncabezadoLeccion`, `PasoIlustrado`, `TarjetaTip`, `TarjetaLlamadoAccion` |
| RF-ED-012/015 | `irAPestana(Actividades)` (§5) |
| RF-ED-013 | `TarjetasEducativasInicio` (2 primeras lecciones) |
| RF-ED-014/017 | Parser + `CatalogoLecciones` (§2–§4), fallback pastel (§1) |
| RF-ED-016 | VectorDrawable por nombre (§1) |
| RF-ED-018 | Tokens (§7) |
| RF-ED-019/020/021 | Accesibilidad (§8) |
