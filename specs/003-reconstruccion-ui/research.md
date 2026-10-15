# Research & Decisions: Reconstrucción Visual de la Interfaz de Usuario

Todas las ambigüedades de `Technical Context` quedaron resueltas sin marcadores
`NEEDS CLARIFICATION` pendientes. Dos de ellas se decidieron con el usuario antes de
este documento (ver `spec.md` → Clarifications, sesión 2026-10-12); el resto se decide
acá, a partir de la exploración real del código (`app/src/main/kotlin/com/ecosmart/`).

## 1. No se agrega librería de carga de imágenes remota (Coil/Glide)

- **Decision**: Las imágenes requeridas por el diseño (tarjetas de recompensa, miniaturas
  de video, ilustraciones del planeta/paisaje) se implementan con recursos locales
  (`painterResource` + `res/drawable`), sin agregar Coil, Glide ni ninguna librería de
  carga de imágenes desde red.
- **Rationale**: `Actividad.fotoReferencialUrl` existe en el dominio pero **siempre vale
  `""`** en el catálogo real (`ActividadRepositoryImpl.CATALOGO_INICIAL`); no hay ninguna
  URL real que cargar hoy. Todas las pantallas ya existentes usan `painterResource` con
  drawables locales (`ComponentesEcoSmart.kt`, `ActividadDetalleScreen.kt`). Agregar una
  dependencia nueva para cargar imágenes que no existen violaría el Principio VII
  (Diseño Pragmático — "patrones solo si simplifican"). Las 6 recompensas de ejemplo
  (RF-UI-003b) y las miniaturas de video/ilustraciones decorativas usan también assets
  locales (placeholders libres de derechos mientras el equipo de diseño no entregue
  los archivos finales, ver `spec.md` → Assumptions).
- **Alternatives considered**: Coil (rechazada: no hay fuente real de imágenes todavía;
  se puede agregar en una futura spec si/cuando exista un backend de recompensas con
  imágenes reales); Glide (misma razón, además menos idiomático en Compose que Coil).

## 2. Mapeo de S-03 (Caminar/detalle) a las pantallas Kotlin ya existentes

- **Decision**: `S-03` no se implementa como una pantalla nueva: se reparte entre las dos
  pantallas que ya cubren ese flujo, restyladas según el diseño:
  - `ActividadDetalleScreen` (alcanzada al tocar una tarjeta desde Inicio/Actividades,
    `RF-UI-007`/`AC-05`) adopta el encabezado, subtítulo, píldora de recompensa y las
    filas "Cómo se registra" de `S-03`; para `CAMINAR` muestra el anillo de progreso
    (derivado de la meta/avance ya expuestos por su ViewModel), y para el resto de las
    categorías el indicador "0/1" (`RF-UI-011`).
  - `VerificacionCaminataScreen` (alcanzada al tocar "Realizarla"/iniciar el
    seguimiento) adopta el mismo anillo animado y el botón de estado "¡Seguimiento en
    curso!" de `C-07`, porque es la pantalla que realmente posee el estado de
    seguimiento activo (`CaminataEnCursoStore`, sin cambios de lógica).
- **Rationale**: El flujo real de la app ya separa "ver info de la actividad" de
  "trackear la caminata en curso" en dos pantallas distintas con dos ViewModels
  distintos; fusionarlas en una sola pantalla nueva sería un cambio de navegación/
  arquitectura de Presentación más grande de lo necesario para cumplir el diseño visual,
  y el propio documento de diseño no exige una navegación distinta a la ya existente
  (`RF-UI-001`, no se cambian destinos de navegación).
- **Alternatives considered**: Crear una pantalla `S03Screen.kt` nueva que fusione ambos
  flujos (rechazada: duplicaría estado ya manejado por dos ViewModels, y violaría
  `RF-UI-001`/`RF-UI-002` al introducir un destino de navegación distinto al actual).

## 3. Segmento Videos/Artículos/IA reemplaza al filtro por categoría de Educación

- **Decision**: El segmento superior de `ContenidoEducativoScreen` deja de filtrar por
  `CategoriaActividad` (Reciclar/Reutilizar/Caminar) y pasa a ser el segmento
  Videos/Artículos/IA de `S-05` (`C-04`, 3 opciones). La pestaña "Videos" muestra el
  mismo contenido curado ya existente (6 piezas) como tarjetas de video (`C-09`); las
  pestañas "Artículos" e "IA" muestran un mensaje mínimo de contenido no disponible
  todavía (no el estado "vacío" diseñado en la sección 10, que sigue fuera de alcance
  por `RF-UI-003`), ya que su contenido real está explícitamente fuera de alcance
  (`RF-UI-002`, `especificacion-ui-app-eco.md` §8.2).
- **Rationale**: El propio documento de diseño dice que "la pestaña Artículos mostraría
  tarjetas de lectura; la pestaña IA mostraría un chat... el diseño no las muestra, así
  que necesitan su propia especificación" (§8.2) — confirma que no se espera contenido
  real ahí todavía, solo el segmento visual.
- **Alternatives considered**: Mantener el filtro por categoría además del nuevo
  segmento (rechazada: el diseño solo muestra un segmento de 3 opciones, no dos
  controles de filtro superpuestos — duplicaría UI sin base en la especificación).

## 4. `CategoriaActividad` no se modifica pese a los nombres de ejemplo del diseño

- **Decision**: El catálogo real de actividades (10 entidades `Actividad`, repartidas en
  `RECICLAR` ×4, `REUTILIZAR` ×3, `CAMINAR` ×3 — `ActividadRepositoryImpl`) se muestra
  con el componente genérico `C-06`/el esqueleto `S-03`, usando sus datos reales
  (`descripcionCorta`, `puntosBase`, etc.). Los nombres "Separar residuos", "Usar menos
  plástico", "Ahorrar agua", "Elegí tu actividad" que aparecen como ejemplo en
  `especificacion-ui-app-eco.md` §5.2 **no** se modelan como categorías ni actividades
  nuevas.
- **Rationale**: `CategoriaActividad` es un enum cerrado (Principio VI de
  `constitution.md`); el documento de diseño es un mockup con nombres de ejemplo, no una
  fuente de datos de negocio (`RF-UI-001` prohíbe introducir reglas de negocio nuevas).
  Ver corrección aplicada a `RF-UI-011` en `spec.md` durante esta misma fase de plan.
- **Alternatives considered**: Agregar las 3 categorías nuevas sugeridas por el mockup
  (rechazada de plano: es lógica/dominio nuevo, fuera de alcance explícito de este
  módulo).

## 5. Historial dentro de Perfil, no como tab propio

- **Decision**: `HistorialScreen` (hoy alcanzada por el tab "Misiones") deja de ser un
  destino de primer nivel de la barra inferior. Pasa a alcanzarse desde la fila de menú
  "Mis actividades" de la nueva pantalla Perfil (`S06-E`, `especificacion-ui-app-eco.md`
  §9.1), sin cambiar en absoluto la pantalla ni su ViewModel.
- **Rationale**: El nuevo diseño no incluye un tab "Historial"/"Misiones"; sí incluye,
  dentro de Perfil, una fila "Mis actividades" cuyo destino propuesto es exactamente
  "Historial de actividades" — el mismo dato, solo accesible desde otro punto de entrada
  (reorganización de Presentación/Navegación, permitida por `RF-UI-002b`).
- **Alternatives considered**: Mantener "Misiones"/Historial como un sexto tab
  (rechazada: el diseño especifica exactamente 5 tabs, `C-02`).

## 6. Implementación de los tokens de diseño: extender `Theme.kt`, no reescribirlo

- **Decision**: Los tokens de color/tipografía de `especificacion-ui-app-eco.md` §2.1/§2.2
  se mapean a los `Color`/`TextStyle` ya existentes en `Theme.kt` (renombrando/agregando
  alias donde falten, p. ej. un rol de 40 pt para `type.display`), en vez de reemplazar
  el sistema `MaterialTheme` actual. El espaciado/radios/elevación (§2.3), que hoy no
  existen como objeto reutilizable, se agregan en un archivo nuevo `theme/Tokens.kt`.
- **Rationale**: `Theme.kt` ya soporta modo oscuro (`isSystemInDarkTheme()`) con
  `lightColorScheme()`/`darkColorScheme()`; reescribirlo desde cero arriesgaría romper
  ese soporte sin necesidad (no es parte del alcance de este spec). Extenderlo cumple
  `RF-UI-004` (un único lugar reutilizable) sin introducir una segunda fuente de verdad
  de estilos.
- **Alternatives considered**: Crear un sistema de temas enteramente nuevo en paralelo
  (rechazada: duplicaría la fuente de verdad de estilos, violando el propio `RF-UI-004`
  que este cambio busca cumplir).

## 7. Estrategia de testing para este módulo

- **Decision**: Se agregan tests de Compose UI (`createComposeRule()`,
  `androidx.compose.ui:ui-test-junit4`, ya disponible vía el Compose BOM existente) para
  los contratos visuales más críticos y testeables de cada componente/pantalla
  reconstruida (p. ej.: la barra inferior expone 5 ítems con el seleccionado correcto;
  una tarjeta de actividad completada expone su `contentDescription` de accesibilidad;
  el segmento de Recompensas filtra la lista de ejemplo). No existían tests de Compose
  UI antes de este módulo. Los tests unitarios ya existentes de ViewModels (JUnit 5 +
  MockK + Turbine) se ejecutan sin cambios como validación de "cero regresiones"
  (`SC-UI-004`); no se agregan tests de dominio nuevos porque no hay dominio nuevo.
- **Rationale**: El Principio VIII exige tests para "reglas de negocio importantes"; este
  módulo no agrega reglas de negocio, pero sí introduce contratos visuales/de
  accesibilidad verificables (`RF-UI-015` a `RF-UI-019`) que antes no se probaban de
  ninguna forma — cubrirlos reduce el riesgo de regresión visual futura sin inventar
  tests de dominio innecesarios.
- **Alternatives considered**: No agregar ningún test nuevo (rechazada: dejaría sin
  cobertura automática el único comportamiento que este módulo sí cambia — la UI).

## 8. Overrides post-testing real sobre `especificacion-ui-app-eco.md` (feedback 2026-10-14)

Tras implementar las 46 tareas iniciales (`T001`-`T046`) y testear la app real, la
usuaria reportó 12 puntos de feedback (ver `spec.md` → Clarifications, sesión
2026-10-14). Se documentan acá las decisiones técnicas de cada uno:

- **Puntos y pasos reales, no hardcodeados**: `HomeViewModel`/`ActividadDetalleViewModel`
  dejaron de usar valores fijos (1.250 pts, 1.200 m) y pasaron a leer
  `Usuario.puntosHistoricos` y `PasosDelDiaRepository.pasosDeHoy()` (ambos ya existentes
  desde spec 001, nunca se creó dato nuevo). **Rationale**: el propio `RF-UI-001`/
  Principio X ya exigían reusar datos del dominio; el hardcodeo fue un defecto de
  implementación detectado recién en testing manual, no una decisión de diseño.
- **Metros → pasos**: Se eliminó la conversión pasos→metros (`METROS_POR_PASO`) de
  `VerificacionCaminataScreen` y se cambió el parámetro `unidad` de `AnilloProgreso` a
  "pasos" en todos los llamadores. **Rationale**: no existe ninguna fuente real de
  distancia en metros en el dominio — solo el conteo de pasos del sensor del
  dispositivo (`Sensor.TYPE_STEP_COUNTER` vía `PasosDelDiaRepository`); convertir a
  metros con una constante arbitraria (`METROS_POR_PASO`) sería introducir un dato
  inventado en la capa de Presentación, exactamente lo que `RF-UI-001` prohíbe.
  **Alternatives considered**: pedir una constante de conversión real al backend
  (rechazada: fuera de alcance, requeriría un nuevo endpoint/campo de dominio).
- **Nombres de nivel reales**: `etiquetaNivel(nivel: NivelUsuario): String` en
  `PerfilScreen.kt` reemplaza a `nivelNumero()`, devolviendo "Semilla"/"Brote"/
  "Planta"/"Árbol" directamente desde el enum de dominio ya existente (spec 001).
  **Rationale**: el enum ya traía esos nombres; numerarlos en la UI era redundante e
  inconsistente con el resto de la app (p. ej. mensajes de notificación que ya usan el
  nombre).
- **Barra inferior solo iconos**: `BarraInferiorEcoSmart` quita los `Text(...)` de
  etiqueta y conserva el `contentDescription` en cada `Icon`/`NavigationBarItem` para
  no perder accesibilidad (`RF-UI-018` sigue aplicando). **Rationale**: pedido estético
  explícito de la usuaria tras ver la barra renderizada; no afecta ninguna regla de
  negocio ni test de dominio.
- **Dropdown de categoría en Actividades**: Se agregó `DropdownCategoria` (nuevo
  composable privado, `DropdownMenu`/`DropdownMenuItem` de Material3) con
  `FiltroCategoria` (TODAS/RECICLAR/REUTILIZAR/CAMINAR, mapeado 1 a 1 a
  `CategoriaActividad`), combinado con el filtro de estado ya existente mediante un
  `&&` simple sobre la lista ya cargada por el ViewModel. **Rationale**: no requiere
  tocar el ViewModel ni el repositorio — el filtrado por categoría se resuelve en la
  capa de Presentación sobre la misma lista que ya expone `ActividadesViewModel`.
- **Mismo diseño de filtro para Actividades y Educación**: El filtro de estado de
  Actividades pasó de `FilaChipsFiltro` (chips sueltos con scroll) a `SegmentoEcoSmart`
  (el mismo componente que ya usaba Educación para Videos/Artículos/IA, ver decisión
  #3). **Rationale**: reusar el componente ya existente en vez de crear uno nuevo
  mantiene un único lugar de verdad para el estilo de segmento (`RF-UI-004`).
- **Alto uniforme de tarjetas**: `TarjetaActividad` (en `ComponentesEcoSmart.kt`) pasó de
  altura intrínseca (`wrapContentHeight`) a altura fija por variante (176.dp compacta /
  186.dp completa) vía `Modifier.height(...)`, con un `Spacer(Modifier.weight(1f))`
  antes del contenido inferior (puntos/check) para mantenerlo siempre alineado abajo
  pese al alto fijo. **Rationale**: el layout de grilla/carrusel se veía irregular con
  alturas variables; fijar el alto es puramente visual, no cambia ningún dato mostrado.
- **Nombre de actividad en la pantalla de foto**: `VerificacionFotoViewModel` agrega
  `nombreActividad: String` a su `UiState`, poblado en `init` desde
  `actividadRepository.buscarPorId(actividadId)?.descripcionCorta` (dato ya existente,
  solo no se mostraba antes). `VerificacionFotoScreen` lo renderiza como
  `headlineSmall`/`FontWeight.Bold` arriba de todo, antes de las pestañas Cámara/
  Galería. **Rationale**: única excepción documentada a `RF-UI-002` para esta pantalla
  "fuera de alcance" — el pedido es mínimo (un texto más) y no toca la lógica de
  verificación por IA.
- **Recompensas sin distinción de público**: Se eliminó por completo `enum class
  PublicoRecompensa` y el campo `publico` de `RecompensaEjemplo`, junto con las 3
  entradas de ejemplo "para Kids" y el segmento "Para adultos"/"Para Kids" de
  `RecompensasScreen`. **Rationale**: pedido explícito de la usuaria; el catálogo de
  ejemplo ya era un dato de Presentación puro (decisión #1 de este documento), así que
  quitar un campo no afecta ninguna regla de negocio.
- **Imágenes de ejemplo para recompensas**: Se agregaron 3 drawables vectoriales nuevos
  (`ic_recompensa_comida.xml`, `ic_recompensa_descuentos.xml`,
  `ic_recompensa_productos.xml`, placeholders libres de derechos) y una función
  `imagenPorCategoria(categoria): Int` que asigna una de las 3 a cada
  `RecompensaEjemplo` según su categoría; `imagenDrawableRes` pasa a ser obligatorio
  (no nulo) en el modelo (ver `data-model.md` §3). **Rationale**: consistente con la
  decisión #1 (sin Coil/Glide, solo recursos locales); 3 imágenes por categoría
  (en vez de 6 únicas) evita crear más assets de los necesarios para una muestra
  puramente visual.
- **"Eliminar cuenta" solo en Editar perfil**: El botón y su `AlertDialog` de
  confirmación se quitaron de `PerfilScreen`/`PerfilContenido` y se agregaron a
  `PerfilEdicionScreen`/`CamposDePerfil`, reusando exactamente los mismos métodos ya
  existentes de `PerfilViewModel` (`solicitarEliminarCuenta`/`confirmarEliminarCuenta`/
  `cancelarEliminarCuenta`) — `PerfilEdicionScreen` ahora inyecta tanto
  `PerfilEdicionViewModel` como `PerfilViewModel`. **Rationale**: pedido de reubicación
  de UI; cero lógica nueva, solo mover el punto de entrada visual a la pantalla donde
  la usuaria ya edita datos sensibles de su cuenta.

