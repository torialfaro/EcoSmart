# Feature Specification: Reconstrucción Visual de la Interfaz de Usuario

**Feature Branch**: `003-reconstruccion-ui`

**Created**: 2026-10-12

**Status**: Draft

**Input**: User description: "Comenzá un spec nuevo para dedicarse únicamente a la
reconstrucción de la interfaz de la aplicación, basándote en el documento
`especificacion-ui-app-eco.md` con todas las descripciones gráficas que deben
implementarse (pantallas, componentes, tokens de diseño). Todo lo que refiera a lógica
(backend) se ignora: la lógica de la aplicación se mantiene exactamente igual."

## Relación con las Especificaciones 001 y 002

Este módulo **no reemplaza** a `specs/001-ecosmart-mvp/` ni a
`specs/002-firestore-datos-usuario/`: reemplaza únicamente la **capa visual** (Frontend/UI
y, en lo estrictamente necesario para exponerla, Presentación/ViewModel) de las pantallas
ya implementadas. Toda historia de usuario, regla de negocio, cálculo de puntaje, tope
diario, racha, nivel, verificación por IA, permisos de dispositivo y persistencia
(Firestore/Firebase Authentication) definidos en spec 001 y spec 002 **se mantienen sin
cambios**. La única fuente de verdad gráfica de este spec es
[`especificacion-ui-app-eco.md`](../../especificacion-ui-app-eco.md), conforme al
Principio X de `constitution.md` (v2.1.0).

Mapeo de pantallas existentes a su nueva especificación visual:

| Pantalla (hoy) | Especificación visual nueva | Datos/lógica que reutiliza sin cambios |
|---|---|---|
| Home (resumen + listado completo mezclados, 3 tabs: Inicio/Misiones/Perfil) | S-01 Inicio (§4) — pasa a ser solo el resumen | Puntos totales, progreso diario de actividades, catálogo del día (spec 001/002) |
| *(hoy mezclado dentro de Home, sin pantalla propia)* | S-02 Actividades (§5) — nueva pantalla/tab que muestra el listado completo que hoy vive en Home | Catálogo de actividades y su estado idle/in_progress/completed, ya calculado por el dominio existente (spec 001) |
| Detalle de actividad (Caminar y el resto del catálogo real) | S-03 (§6) | Progreso de podómetro/distancia, recompensa en puntos, textos de la actividad (spec 001) |
| *(no existe — ver Clarifications)* | S-04 Recompensas (§7) — nueva pantalla/tab con datos de ejemplo fijos, sin backend | Ninguno: no hay `Reward`/`Redemption` en el dominio; ver Clarifications y Assumptions |
| Contenido educativo (hoy se abre por enlace, no es un tab) | S-05 Educación (§8) — se promueve a tab | Catálogo de contenido educativo (spec 001) |
| Perfil | S-06 (§9) | Nombre, nivel, puntos, estadísticas, racha (spec 001/002) |

Lo que **no** cambia porque es lógica/backend, no interfaz: autenticación, reglas de
puntaje y topes diarios, verificación por IA (EcoGPT), persistencia remota en Firestore,
destinos de pantalla ya existentes y permisos de dispositivo. La única excepción
estructural es la barra de navegación inferior, que pasa de 3 a 5 tabs (ver
Clarifications): es una reorganización de Presentación sobre datos ya existentes, no una
nueva regla de negocio.

## Clarifications

### Session 2026-10-12

- Q: El diseño pide una pantalla/tab "Recompensas" completa (catálogo, categorías,
  costo en puntos), pero el código actual no tiene ningún dato ni entidad de dominio de
  recompensas (no existe `Reward`/`Redemption`, ni backend, ni pantalla). ¿Cómo se
  procede dado que la lógica/backend debe quedar igual? → A: Se construye la pantalla
  con las 6 recompensas de ejemplo que ya trae `especificacion-ui-app-eco.md` §7.2,
  como datos fijos (hardcodeados) dentro de la capa de Presentación — nunca en
  Dominio, Aplicación ni Infraestructura, y nunca persistidos. Ninguna tarjeta canjea
  nada real (el detalle/canje ya estaba fuera de alcance por RF-UI-002); es una muestra
  puramente visual, explícitamente temporal, hasta que una futura spec defina el
  backend real de recompensas.
- Q: La barra de navegación inferior actual tiene 3 tabs (Inicio, Misiones, Perfil);
  "Actividades" hoy vive mezclada dentro de Home (listado completo en la misma
  pantalla que el resumen) y "Educación" se abre por enlace, no por tab. El nuevo
  diseño pide 5 tabs (Inicio, Actividades, Recompensas, Educación, Perfil). ¿Se
  reestructura la navegación a 5 tabs? → A: Sí. Inicio pasa a mostrar solo el resumen
  (S-01); el listado completo que hoy vive en Home se mueve a una pantalla/tab nueva
  Actividades (S-02); Educación se promueve de enlace a tab; se agrega el tab
  Recompensas. Sigue siendo exclusivamente una reorganización de la capa de
  Presentación/Navegación sobre datos ya existentes (salvo Recompensas, resuelto en el
  punto anterior), sin tocar Dominio, Aplicación ni Infraestructura.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sistema de diseño global aplicado de forma consistente (Priority: P1)

Como usuaria de la app, al navegar entre cualquier par de pantallas veo los mismos
colores, tipografías, espaciados, radios y componentes compartidos (barra inferior,
botones, chips, tarjetas, barras de progreso), de modo que la app se percibe como un
único producto coherente y no como pantallas diseñadas por separado.

**Why this priority**: Es la base de la que dependen las demás historias. Sin el tema y
los componentes globales (`C-01` a `C-11`) definidos una sola vez, reconstruir cada
pantalla por separado produce inconsistencias y duplica trabajo.

**Independent Test**: Puede verificarse inspeccionando únicamente la barra de navegación
inferior y un botón principal en dos pantallas distintas ya existentes, sin que ninguna
pantalla completa esté terminada todavía.

**Acceptance Scenarios**:

1. **Given** la app corriendo con el nuevo tema, **When** se renderiza la barra de
   navegación inferior en cualquier pantalla principal, **Then** coincide con `C-02`:
   5 ítems distribuidos en partes iguales, iconos de 24 pt (relleno verde en el activo,
   línea gris en los inactivos), etiqueta `type.tab`, alto 64 pt + área segura.
2. **Given** un botón principal en cualquier pantalla, **When** se renderiza, **Then**
   coincide con `C-07`: ancho completo, alto 48 pt, forma de píldora, fondo
   `color.green.700`, texto blanco en `type.chip` SemiBold, con sus estados de reposo,
   presionado (oscurecido 8%) y deshabilitado (opacidad 50%).
3. **Given** una fila de chips de filtro o segmento, **When** la usuaria toca una opción,
   **Then** solo esa opción queda seleccionada (fondo `color.green.700`, texto blanco) y
   las demás vuelven a su estado no seleccionado (`C-04`).

---

### User Story 2 - Perfil reconstruido con el nuevo diseño (Priority: P2)

Como usuaria, al abrir la pestaña Perfil veo mi avatar, nombre, nivel, puntos,
estadísticas y menú de cuenta con el nuevo lenguaje visual, manteniendo exactamente los
mismos datos y destinos que ya muestra la app hoy.

**Why this priority**: Es la pantalla más simple del diseño (menor cantidad de estados y
de datos dinámicos), por lo que permite validar el sistema de diseño de punta a punta en
una pantalla real con el menor riesgo.

**Independent Test**: Puede probarse navegando a la pestaña Perfil y comparándola contra
`S-06`, sin que Inicio, Actividades, Caminar, Recompensas o Educación estén reconstruidas
todavía.

**Acceptance Scenarios**:

1. **Given** la pestaña Perfil, **When** se abre la pantalla, **Then** se ven avatar de
   56 pt, nombre, "Nivel N · puntos", una tarjeta de estadísticas de 3 columnas
   (Actividades, Recompensas, Racha) y 5 filas de menú con icono, texto y chevron (`S-06`,
   AC-18).
2. **Given** cualquier estado, **When** la usuaria toca una fila del menú, **Then** se
   abre exactamente la misma pantalla de destino que ya abre hoy la versión actual de la
   app (AC-19), sin cambios de navegación.

---

### User Story 3 - Actividades, Recompensas y Educación reconstruidas (Priority: P3)

Como usuaria, al abrir las pestañas Actividades, Recompensas o Educación veo sus listados
y grillas con el nuevo lenguaje visual (chips de filtro, tarjetas de actividad, tarjetas
de recompensa, tarjetas de video), conservando los mismos datos, filtros y navegación que
ya existen.

**Why this priority**: Las tres pantallas comparten el mismo patrón de lista/grilla con
chips de filtro, por lo que conviene reconstruirlas juntas después de validar el sistema
de diseño en Perfil.

**Independent Test**: Puede probarse pantalla por pantalla (por ejemplo, solo Recompensas)
comparándola contra su especificación (`S-04`) y sus criterios de aceptación, sin
depender de que Inicio o Caminar estén reconstruidas.

**Acceptance Scenarios**:

1. **Given** la pestaña Actividades, **When** se abre la pantalla, **Then** se ven 6
   tarjetas en 2 columnas y el chip "Todas" seleccionado (`S-02`, AC-06); al tocar
   "Completadas" solo se ven las tarjetas completadas y el chip pasa a verde oscuro con
   texto blanco (AC-07).
2. **Given** la pestaña Recompensas con el segmento "Para adultos" activo, **When** la
   usuaria toca "Para Kids", **Then** la grilla muestra solo las recompensas de Kids
   (`S-04`, AC-13); al tocar el chip "Comida" se ven solo las recompensas de esa
   categoría (AC-14).
3. **Given** la pestaña Educación en la pestaña "Videos", **When** la usuaria toca un
   video, **Then** se abre el mismo reproductor que ya abre hoy la versión actual de la
   app (`S-05`, AC-16).

---

### User Story 4 - Inicio reconstruida (Priority: P4)

Como usuaria, al abrir la app veo un resumen de un vistazo: mis puntos, mi progreso
general del día, mis actividades de hoy y contenido sugerido, con el nuevo lenguaje
visual y sin tener que hacer scroll largo para entender mi estado actual.

**Why this priority**: Es la pantalla de arranque y la más visible, pero combina varios
componentes ya construidos en las historias anteriores (tarjeta de puntos, tarjeta de
actividad, tarjeta de video, botón principal), por lo que conviene abordarla después de
tenerlos probados de forma aislada.

**Independent Test**: Puede probarse abriendo la app y comparando la pantalla de arranque
contra `S-01`, sin que Caminar esté reconstruida todavía (el carrusel de Inicio puede
enlazar a la versión actual de Caminar mientras tanto).

**Acceptance Scenarios**:

1. **Given** 3 de 4 actividades completadas, **When** se abre Inicio, **Then** la barra
   de progreso general se ve al 75% y el texto dice "3/4 actividades" (AC-02).
2. **Given** más actividades que las visibles en el carrusel, **When** la usuaria lo
   desliza, **Then** se mueve en horizontal y la última tarjeta visible queda
   parcialmente cortada (AC-03).
3. **Given** cualquier estado, **When** la usuaria toca "Ver todas las actividades",
   **Then** se abre Actividades con el chip "Todas" seleccionado (AC-04).
4. **Given** cualquier estado, **When** la usuaria toca la tarjeta Caminar, **Then** se
   abre el detalle de Caminar con el mismo progreso que mostraba la tarjeta (AC-05).

---

### User Story 5 - Caminar (detalle) reconstruida (Priority: P5)

Como usuaria, al abrir el detalle de una actividad medible (Caminar) veo mi progreso en
un anillo animado, mi recompensa en puntos y una explicación simple de cómo se registra,
con el nuevo lenguaje visual.

**Why this priority**: Es la pantalla más compleja del diseño (anillo de progreso
animado, ilustración decorativa, múltiples estados del botón), por lo que se aborda al
final, una vez validado el sistema de diseño en las pantallas más simples.

**Independent Test**: Puede probarse abriendo el detalle de Caminar desde Inicio o
Actividades y comparándolo contra `S-03`, de forma independiente del resto de las
pantallas ya reconstruidas.

**Acceptance Scenarios**:

1. **Given** un progreso de 1.200 de 2.000 m, **When** se abre la pantalla, **Then** el
   anillo está al 60%, el centro dice "1.200 m" y "de 2.000 m", y la píldora dice "+ 50
   puntos" (AC-09).
2. **Given** el seguimiento activo, **When** la usuaria camina, **Then** el valor del
   anillo y el arco se actualizan sin salir de la pantalla (AC-10).
3. **Given** cualquier estado, **When** la usuaria toca la flecha de volver, **Then**
   vuelve a la pantalla de la que venía (AC-12).

### Edge Cases

- ¿Qué pasa cuando el título de una recompensa o de una tarjeta de video excede el
  espacio disponible? Se corta con puntos suspensivos tras 1-2 líneas según el
  componente (`C-10`, §7.3).
- ¿Qué pasa cuando el usuario del sistema tiene activado el tamaño de texto al 130%?
  Ningún texto de título, valor o botón debe quedar cortado en ninguna pantalla (AC-20).
- ¿Qué pasa cuando el usuario tiene activa la preferencia de "reducir movimiento" del
  sistema? Todas las animaciones (anillo, barras, contador de puntos, transiciones) se
  omiten y se muestra directamente el estado final (§10.4).
- ¿Qué pasa cuando el catálogo real de actividades tiene más o menos elementos que
  los 6 mostrados en el diseño (o cuando cambia la muestra fija de recompensas, ver
  Clarifications)? Los componentes (`C-06`, `C-10`) deben soportar cualquier cantidad
  de elementos sin romper el layout de grilla/carrusel.
- ¿Qué pasa con las pantallas y estados explícitamente fuera de alcance (ver Requisitos
  de alcance)? Conservan su apariencia e implementación actuales sin cambios.

## Requirements *(mandatory)*

### Requisitos de alcance (no negociables)

- **RF-UI-001**: El sistema DEBE preservar sin cambios toda la lógica de negocio, el
  cálculo de puntaje, los topes diarios, la racha, los niveles, las reglas de
  verificación por IA, la persistencia (Firestore/Firebase Authentication) y los
  destinos de navegación ya implementados en `001-ecosmart-mvp` y
  `002-firestore-datos-usuario`. Este spec únicamente modifica la capa Frontend (UI) y,
  en lo estrictamente necesario para exponer el nuevo estado visual, Presentación/
  ViewModel (Principio X de `constitution.md`).
- **RF-UI-002**: El sistema NO DEBE introducir nuevas reglas de negocio, ni nuevas
  pantallas de detalle más allá de las reorganizaciones de navegación explícitamente
  permitidas por RF-UI-002b. Las pantallas y flujos referenciados pero no diseñados en
  `especificacion-ui-app-eco.md` §1.3 (detalle/canje de recompensa, reproductor de
  video, listado de videos destacados, pestañas Artículos/IA, filas de menú de Perfil,
  destino de la tarjeta de puntos, pestaña "Para Kids") quedan fuera de alcance:
  conservan su UI y navegación actuales hasta que tengan su propia especificación de
  diseño.
- **RF-UI-002b**: El sistema DEBE reestructurar la barra de navegación inferior de 3 a 5
  tabs (Inicio, Actividades, Recompensas, Educación, Perfil — ver Clarifications): Inicio
  pasa a mostrar únicamente el resumen (S-01), el listado completo de actividades que
  hoy vive mezclado dentro de Home se muestra en una pantalla/tab nueva Actividades
  (S-02) reutilizando los mismos datos, y Educación se promueve de enlace a tab. Esta
  reorganización es exclusivamente de Presentación/Navegación sobre datos ya existentes
  y no constituye una excepción a RF-UI-002.
- **RF-UI-003**: El sistema NO DEBE implementar los estados e interacciones
  transversales de la sección 10 de `especificacion-ui-app-eco.md` (carga/skeleton,
  vacío, error de red, sin conexión, pull-to-refresh, ni los estados de Caminar
  "Seguimiento en pausa" / "Activar seguimiento" / "¡Meta cumplida!") mientras sigan
  marcados como "propuesta" en el documento fuente. Únicamente se implementa el "camino
  feliz" de cada pantalla (datos cargados, con conexión, seguimiento activo).
- **RF-UI-003b**: Dado que no existe entidad de dominio ni backend de recompensas (ver
  Clarifications), la pantalla Recompensas DEBE construirse con las 6 recompensas de
  ejemplo de `especificacion-ui-app-eco.md` §7.2 como datos fijos embebidos únicamente
  en la capa de Presentación (nunca en Dominio, Aplicación, Infraestructura ni
  persistidos), dejando explícito en el código que es una muestra visual temporal sin
  función de canje real.

### Sistema de diseño (tokens y componentes globales)

- **RF-UI-004**: El sistema DEBE definir, en un único lugar reutilizable por todas las
  pantallas, los tokens de la sección 2 de `especificacion-ui-app-eco.md`: paleta de
  color (§2.1), tipografía (§2.2), espaciado/radios/elevación (§2.3) e iconografía
  (§2.4). Ninguna pantalla DEBE usar valores de color, tamaño de fuente, espaciado o
  radio que no provengan de ese conjunto único de tokens.
- **RF-UI-005**: El sistema DEBE implementar cada componente global `C-01` a `C-11`
  (barra de estado, barra de navegación inferior, encabezado de pantalla, chip/segmento,
  barra de progreso lineal, tarjeta de actividad, botón principal, enlace de sección,
  tarjeta de video, tarjeta de recompensa, tarjeta de puntos) como una única pieza
  reutilizada por todas las pantallas que lo necesiten.
- **RF-UI-006**: La tarjeta de actividad (`C-06`) DEBE soportar sus dos variantes
  visuales (compacta/centrada en Inicio, completa/alineada a la izquierda con "+ N pts"
  en Actividades) y sus tres estados (sin iniciar, en curso, completada), derivando el
  estado únicamente de comparar el progreso actual contra la meta de la actividad
  (idle/in_progress/completed) ya calculado por el dominio existente.
- **RF-UI-007**: La barra de navegación inferior (`C-02`) DEBE resaltar el tab
  "Actividades" también dentro de la pantalla de detalle de cualquier actividad, por ser
  una pantalla hija de Actividades (§1.2, §6).

### Pantallas

- **RF-UI-008**: La pantalla Inicio DEBE reconstruirse según §4: saludo con nombre de
  pila, avatar, subtítulo de marca, tarjeta de puntos (`C-11`), tarjeta de progreso
  general (`C-05`) con la fracción "completadas/total" ya calculada por el dominio
  existente, carrusel horizontal de tarjetas de actividad (`C-06`) del día, botón "Ver
  todas las actividades" (`C-07`) y carrusel de contenido educativo (`C-09`). El saludo y
  la barra inferior permanecen fijos mientras el resto hace scroll vertical.
- **RF-UI-009**: La pantalla/tab nueva Actividades (ver RF-UI-002b) DEBE construirse
  según §5: título/subtítulo, chips de filtro de selección única (Todas/En
  progreso/Completadas) operando sobre los mismos datos ya filtrables por estado que
  hoy muestra Home, y grilla de 2 columnas de tarjetas `C-06` en su variante completa
  (con "+ N pts").
- **RF-UI-010**: El detalle de Caminar DEBE reconstruirse según §6: flecha de volver,
  título/subtítulo, anillo de progreso (arco desde las 12 en punto, sentido horario,
  ángulo = 360° × valor ÷ meta), píldora de recompensa, botón de estado mostrando
  únicamente el estado "activo" del camino feliz (ver RF-UI-003), las dos filas
  informativas de "Cómo se registra" y la ilustración decorativa de paisaje. El arco se
  anima de 0 al valor actual al abrir la pantalla.
- **RF-UI-011**: El detalle de cualquier otra actividad del catálogo real (categorías
  RECICLAR y REUTILIZAR; `especificacion-ui-app-eco.md` §5.2 usa "Separar residuos",
  "Usar menos plástico" y "Ahorrar agua" solo como ejemplos ilustrativos, no como
  categorías nuevas: `CategoriaActividad` sigue siendo el enum cerrado ya definido en
  spec 001, Principio VI) DEBE reutilizar el mismo esqueleto visual de Caminar (§6.3),
  sustituyendo el anillo de progreso por el indicador "0/1" porque ninguna de esas dos
  categorías es medible por distancia, y adaptando título/subtítulo/recompensa/textos
  de "Cómo se registra" a cada actividad según los datos ya existentes del dominio
  (`descripcionCorta`, `pasosASeguir`, `resultadoEsperado`, `puntosBase`).
- **RF-UI-012**: La pantalla/tab nueva Recompensas (ver RF-UI-002b, RF-UI-003b) DEBE
  construirse según §7: título/subtítulo, segmento "Para adultos"/"Para Kids", chips de
  categoría con scroll horizontal, y grilla de 2 columnas de tarjetas de recompensa
  (`C-10`) con imagen, título (hasta 2 líneas, truncado con puntos suspensivos si
  excede), costo en formato "N.NNN pts" (punto como separador de miles) y chevron.
- **RF-UI-013**: La pantalla/tab Educación (ver RF-UI-002b) DEBE construirse según §8:
  título/subtítulo, segmento Videos/Artículos/IA, título de sección "Videos destacados"
  con enlace "Ver todos", y tarjetas de video anchas (`C-09`) con metadato "N min ·
  Categoría" sobre degradé oscuro.
- **RF-UI-014**: La pantalla Perfil DEBE reconstruirse según §9: avatar de 56 pt,
  nombre, "Nivel N · puntos", tarjeta de estadísticas de 3 columnas (Actividades,
  Recompensas, Racha, usando los valores ya calculados por el dominio existente) y lista
  de 5 filas de menú con icono, texto y chevron.

### Accesibilidad y adaptabilidad

- **RF-UI-015**: Todo texto normal DEBE mantener una relación de contraste mínima de
  4,5:1 contra su fondo, incluyendo texto blanco sobre miniaturas de video/foto
  (degradé oscuro inferior obligatorio en `C-09`) y texto gris sobre el fondo crema de
  pantalla.
- **RF-UI-016**: Toda zona táctil (ítems de la barra inferior, flecha de volver,
  chevrones, chips) DEBE medir como mínimo 48 × 48 pt, incluso cuando el icono visual
  sea más chico.
- **RF-UI-017**: El sistema DEBE soportar el tamaño de texto del sistema hasta 130% sin
  cortar ni truncar contenido crítico (títulos, valores numéricos, texto de botones) en
  ninguna pantalla (AC-20).
- **RF-UI-018**: Cada tarjeta de actividad DEBE exponerse a un lector de pantalla como
  un único elemento con nombre, progreso, puntos y estado (por ejemplo: "Caminar, 1.200
  de 2.000 metros, 50 puntos, en curso"); el anillo de progreso de Caminar DEBE leerse
  como un porcentaje (por ejemplo: "60 por ciento"); las ilustraciones decorativas
  (planeta, paisaje) DEBEN marcarse para que el lector de pantalla las ignore.
- **RF-UI-019**: Toda animación (barras/anillo de progreso, contador de puntos, tarjeta
  que se completa, transición entre pantallas) DEBE omitirse y mostrar el estado final
  directamente cuando el usuario tenga activa la preferencia del sistema de "reducir
  movimiento".

### Key Entities

No agrega entidades de dominio ni datos persistidos (ver RF-UI-001). Reutiliza, sin
cambios, las entidades ya definidas en `001-ecosmart-mvp/data-model.md` (`Usuario`,
`Actividad`, su progreso diario, `EducationItem`-equivalente de contenido educativo)
únicamente como origen de los datos que la nueva UI muestra. La única excepción es la
muestra fija de recompensas (ver Clarifications, RF-UI-003b): no es una entidad de
dominio sino una lista de datos estáticos dentro de la capa de Presentación, sin
persistencia ni identidad de negocio.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-UI-001**: Las 6 pantallas principales (Inicio, Actividades, Caminar, Recompensas,
  Educación, Perfil) usan exclusivamente los colores, tipografías, espaciados y radios
  definidos por el sistema de diseño único (RF-UI-004), verificable por inspección
  visual de cada pantalla contra la sección 2 de `especificacion-ui-app-eco.md`.
- **SC-UI-002**: Una comparación lado a lado entre cada una de las 6 pantallas
  reconstruidas y su especificación (`Sxx-x` correspondiente) no muestra diferencias
  perceptibles de layout, color ni tipografía para un revisor humano.
- **SC-UI-003**: El 100% de los criterios de aceptación AC-01 a AC-21 de
  `especificacion-ui-app-eco.md` §12 que apliquen al alcance de este spec (excluyendo
  los que dependan de pantallas **o estados** fuera de alcance, ver RF-UI-002/RF-UI-003
  — en concreto, AC-11 y AC-15 dependen de estados/flujos explícitamente excluidos por
  RF-UI-003/RF-UI-002 aunque su pantalla sí esté en alcance, y AC-17 solo aplica en la
  parte de "el segmento seleccionado pasa a verde oscuro", no en "cambia el contenido
  de la pantalla" para Artículos/IA) pasan sin requerir ningún cambio de lógica de
  negocio ni de navegación.
- **SC-UI-004**: Cero regresiones funcionales: el 100% de los tests automatizados de
  lógica de negocio ya existentes (puntaje, topes diarios, racha, verificación,
  persistencia) siguen pasando después de reconstruir la UI.
- **SC-UI-005**: Una auditoría de accesibilidad (contraste, tamaño de zonas táctiles,
  escalado de texto al 130%) no encuentra incumplimientos en ninguna de las 6 pantallas
  reconstruidas.

## Assumptions

- Las pantallas y flujos marcados "sin diseñar" en §1.3 y los estados de la sección 10
  de `especificacion-ui-app-eco.md` mantienen su apariencia e implementación actuales
  hasta que reciban su propia especificación de diseño, conforme al Principio X de
  `constitution.md` (v2.1.0).
- La pantalla Recompensas se construye con datos de ejemplo fijos en la capa de
  Presentación (sin backend ni persistencia), y la barra de navegación se reestructura
  de 3 a 5 tabs, según lo resuelto en Clarifications.
- Las preguntas de producto abiertas en §13.2 (destino de la tarjeta de puntos, función
  de "Elegí una actividad", reinicio diario, verificación no medible, canje de
  recompensa, alcance de la pestaña IA, cálculo de nivel/racha, modo offline, plataforma
  objetivo) son decisiones de lógica/negocio fuera de alcance de este spec; se resuelven,
  si corresponde, en una iteración de `001-ecosmart-mvp` o `002-firestore-datos-usuario`,
  no en este módulo.
- Las inconsistencias de texto detectadas en §13.1 (ítems 1 y 2: "Tu guía de reciclaje
  no" y "Combo café + medi… medialuna") se corrigen como parte de este spec usando el
  texto final que el propio documento indica ("Tu guía de reciclaje" y "Combo café +
  medialuna"), por ser erratas de copy y no decisiones de producto.
- El punto de partida de cada pantalla es la implementación ya existente de
  `001-ecosmart-mvp` (estado, navegación y datos reales ya conectados); este spec
  reemplaza únicamente las vistas y los recursos de estilo, reutilizando los mismos
  datos y flujos ya conectados, sin reescribir la capa de Aplicación ni de Dominio.
- Las medidas de `especificacion-ui-app-eco.md` están tomadas "a ojo" según el propio
  documento; se usan como la mejor referencia disponible, y ceden ante cualquier archivo
  de diseño oficial futuro con medidas exactas.
- La plataforma objetivo es Android nativo, consistente con el stack ya definido en
  `constitution.md`; no se evalúa iOS ni multiplataforma.
- Las fotografías e ilustraciones referenciadas (planeta con hojas, paisaje de colinas,
  miniaturas de recompensa/video) se obtienen de assets a proveer por el equipo de
  diseño, o de placeholders libres de derechos equivalentes mientras tanto; este spec no
  resuelve el origen final de esos archivos.
