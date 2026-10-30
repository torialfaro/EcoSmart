# Feature Specification: Reconstrucción de la Sección Educación: Catálogo de Lecciones Ilustradas y Detalle de Lección

**Feature Branch**: `004-redisenio-ui-educacion`

**Created**: 2026-10-28

**Status**: Draft

**Input**: User description: "Reconstrucción de la sección Educación: lecciones ilustradas sin solapas y detalle de lección. Este módulo continúa a 003-reconstruccion-ui y reemplaza únicamente lo que esa spec define para Educación (RF-UI-013, escenario 3 de la Historia 3, AC-16 y AC-17), y por extensión las tarjetas educativas de Inicio. No cambia reglas de negocio, puntaje, racha, niveles, verificación por IA ni persistencia (RF-UI-001). La fuente de verdad visual son los tableros 'E1 · Educación · listado' y 'E2 · Lección ilustrada' del lienzo de Claude Design."

---

## Relación con las Especificaciones Existentes

Este módulo continúa a [specs/003-reconstruccion-ui/spec.md](../003-reconstruccion-ui/spec.md) y reemplaza únicamente lo que esa especificación define para contenido educativo:

| Requisito / criterio de spec 003 | Qué hace spec 004 |
|---|---|
| `RF-UI-013` (pantalla Educación según §8 / `S-05`) | Reemplazado por RF-ED-001 a RF-ED-010 |
| `RF-UI-002` (alcance de pantallas) | Excepción: se agrega la pantalla hija "Detalle de lección" |
| `RF-UI-002b` y `C-02` (barra inferior de 5 pestañas) | Se reutiliza sin cambios; Educación queda seleccionada (RF-ED-007) |
| `RF-UI-003b` (muestra sin backend, caso Recompensas) | Mismo criterio de aislamiento para el catálogo de lecciones (RF-ED-014) |
| `RF-UI-004` y `RF-UI-005` (sistema de diseño y componentes `C-01` a `C-11`) | Se extienden con tokens y componentes `C-13` a `C-18` (RF-ED-018) |
| `RF-UI-008` (Inicio: carrusel de contenido educativo `C-09`) | Reemplazado en lo educativo por RF-ED-013; el resto de Inicio no cambia |
| `RF-UI-009` (Actividades: listado y filtros) | Se reutiliza sin cambios: "Ver actividades" abre la pestaña Actividades en su estado habitual (RF-ED-012) |
| `RF-UI-015` a `RF-UI-019` (accesibilidad y sin animaciones) | Se heredan y se concretan en RF-ED-019 a RF-ED-021 |
| Historia 3, escenario 3 y `AC-16` (abrir reproductor de video) | Reemplazados: ya no hay videos |
| `AC-17` (segmento seleccionado de Educación) | Deja de aplicar a Educación: el segmento se elimina |

- **Reglas de negocio y datos**: no cambia reglas de negocio, cálculo de puntaje, racha, niveles, verificación por IA ni persistencia (`RF-UI-001`, Principios III y IV).
- **Fuente de verdad visual**: los tableros **"E1 · Educación · listado"** y **"E2 · Lección ilustrada"** del lienzo de Claude Design, autorizados por la enmienda al Principio X de [.specify/memory/constitution.md](../../.specify/memory/constitution.md) (v2.3.0, que incluye las tarjetas educativas de Inicio).
- **Alcance de pantallas**: Educación, Detalle de lección y **únicamente las tarjetas educativas de Inicio**. Actividades, Recompensas, Perfil, Puntos Verdes y el resto de Inicio quedan fuera de alcance y siguen rigiéndose por sus especificaciones.
- **Componentes**: se usan `C-13` a `C-18`. La tarjeta destacada `C-12` del lienzo NO se implementa (ver RF-ED-002). `C-04`, `C-08` y `C-09` dejan de usarse en contenido educativo pero siguen existiendo donde otras pantallas los usan.

---

## Problema y Objetivo

- **Problema**: hoy Educación muestra un segmento Videos / Artículos / IA, de los cuales Artículos e IA no tienen contenido, y una lista de tarjetas de video oscuras y poco diferenciadas entre sí. La persona usuaria no encuentra un motivo para entrar, ni el contenido explica por sí mismo cómo se hace algo.
- **Objetivo**: que Educación sea un catálogo de lecciones visuales, atractivas y fáciles de elegir, organizadas por categoría de actividad (Reciclar, Reutilizar, Caminar), y que cada lección se entienda mirando dibujos explicativos paso a paso e invite a pasar a la práctica en la sección Actividades.

## Personas Usuarias

Usuarias y usuarios de la app (en este documento, "la usuaria") que quieren aprender a reciclar, reutilizar o caminar mejor, sin necesidad de leer textos largos ni ver videos.

## Glosario

- **Lección**: unidad de contenido ilustrado del catálogo. Todas las lecciones tienen el mismo peso: no existe una lección "destacada".
- **Categoría**: `Reciclar`, `Reutilizar` o `Caminar` (las ya existentes en el dominio). Es el único criterio de agrupación.
- **Tarjeta de lección** (`C-13`): tarjeta tocable que representa una lección.
- **Tarjeta de cierre** (`C-17`): tarjeta "Ahora te toca a vos" al final del detalle; invita a ir a la sección Actividades en general, no a una actividad en particular.
- **Tip** (`C-16`): tarjeta de consejo "Tip para que cuente".

---

## User Scenarios & Testing

### User Story 1 - Elegir una lección desde un catálogo visual (Priority: P1)

Como usuaria, abro Educación y veo, sin solapas ni filtros, las lecciones agrupadas por categoría, cada una con su dibujo, categoría, duración y un resumen corto, para decidir rápido qué leer.

**Why this priority**: es el punto de entrada a Educación; elimina solapas vacías y tarjetas de video genéricas.

**Independent Test**: abrir Educación y comprobar que no existe el segmento Videos / Artículos / IA ni "Videos destacados / Ver todos", que no hay ninguna tarjeta destacada, y que cada tarjeta muestra ilustración, categoría, duración, cantidad de pasos, título y resumen.

**Acceptance Scenarios**:
1. **Given** que abro Educación, **When** se carga la pantalla, **Then** veo el encabezado (volver, título, subtítulo, avatar con mi inicial), una sección por cada categoría con lecciones y, dentro de cada una, sus tarjetas de lección apiladas, y la barra inferior con Educación resaltada.
2. **Given** la pantalla de Educación, **When** toco cualquier tarjeta, **Then** se abre el detalle de esa lección.
3. **Given** que hay más contenido del que entra en pantalla, **When** deslizo hacia arriba, **Then** la lista hace scroll y la barra inferior queda fija.
4. **Given** que una categoría no tiene lecciones, **When** se carga Educación, **Then** esa sección no se muestra.
5. **Given** que el archivo del catálogo está dañado y no puede interpretarse, **When** abro Educación, **Then** veo el encabezado, el mensaje "No pudimos cargar las lecciones" con un botón "Reintentar" y la barra inferior; la app no se cierra.

---

### User Story 2 - Entender una lección con dibujos paso a paso (Priority: P2)

Como usuaria, en el detalle de una lección veo una portada, una introducción y una secuencia numerada de pasos, cada uno con su ilustración, título y texto breve, para entender el proceso sin leer mucho.

**Why this priority**: es el núcleo pedagógico de la sección.

**Independent Test**: abrir la lección de ejemplo "El circuito del reciclaje en CABA" y comprobar que muestra 5 pasos ilustrados, un tip y la tarjeta de cierre.

**Acceptance Scenarios**:
1. **Given** que abro una lección, **When** se presenta el detalle, **Then** veo categoría, duración y cantidad de pasos, el título, la introducción y los pasos numerados "Paso N de M".
2. **Given** que estoy en el detalle de una lección y había hecho scroll en Educación, **When** toco la flecha de volver (o el gesto/botón de retroceso del sistema), **Then** regreso a Educación en la misma posición de scroll.
3. **Given** que la lección tiene un tip, **When** llego al final de los pasos, **Then** el tip aparece después de ellos; si no lo tiene, no se muestra ningún espacio vacío.

---

### User Story 3 - Pasar de aprender a hacer (Priority: P3)

Como usuaria, al terminar una lección puedo ir directo a la sección Actividades, para poner en práctica lo aprendido.

**Why this priority**: conecta el aprendizaje con la práctica sin modificar reglas de negocio. Como hay más lecciones que actividades, una lección puede servir para varias actividades y otra para ninguna en particular; por eso el cierre lleva a Actividades en general.

**Independent Test**: tocar "Ver actividades" en la tarjeta de cierre y comprobar que se abre la pestaña Actividades.

**Acceptance Scenarios**:
1. **Given** que estoy al final de cualquier lección, **When** toco "Ver actividades", **Then** se abre la pestaña Actividades, igual que al tocarla en la barra inferior.
2. **Given** que la lección no define un texto de cierre propio, **When** recorro el final de la lección, **Then** la tarjeta de cierre se muestra igual con el texto general "Poné en práctica lo que aprendiste".

---

### User Story 4 - Descubrir lecciones desde Inicio (Priority: P3)

Como usuaria, en Inicio veo tarjetas de lecciones en lugar de tarjetas de video, para llegar a Educación desde la pantalla principal.

**Why this priority**: unifica el contenido educativo en toda la app y retira los videos.

**Independent Test**: abrir Inicio y comprobar que "Contenido para vos" muestra tarjetas de lección compactas y ninguna tarjeta de video.

**Acceptance Scenarios**:
1. **Given** que abro Inicio, **When** se carga la pantalla, **Then** la sección "Contenido para vos" muestra hasta 2 tarjetas de lección compactas, en el orden del catálogo, en lugar del carrusel de videos.
2. **Given** las tarjetas de lección de Inicio, **When** toco una, **Then** se abre el detalle de esa lección y al volver regreso a Inicio.
3. **Given** que el catálogo no tiene lecciones, **When** se carga Inicio, **Then** la sección "Contenido para vos" no se muestra.

---

### Edge Cases

- **Títulos o resúmenes largos**: se admiten varias líneas sin superponerse con chips, ilustración ni otros textos.
- **Texto del sistema al 130%**: ningún título, paso o botón queda cortado; los bloques de texto crecen en altura (alturas mínimas, nunca fijas).
- **Lección con menos o más pasos que la de ejemplo**: la lista de pasos y la numeración "Paso N de M" se adaptan a cualquier cantidad (la cantidad se deriva de la lista de pasos, no se declara aparte).
- **Una sola lección en el catálogo**: se muestra la sección de su categoría con esa única tarjeta.
- **Catálogo vacío** (archivo válido sin lecciones): Educación muestra el encabezado, la barra inferior y el mensaje "Todavía no hay lecciones disponibles"; Inicio omite sus tarjetas educativas.
- **Archivo ausente o con formato dañado** (no puede interpretarse como catálogo): Educación muestra el estado de error "No pudimos cargar las lecciones" con botón "Reintentar" (vuelve a leer el archivo), distinto del estado vacío; Inicio omite sus tarjetas educativas; la app no se cierra ni falla.
- **Lección inválida dentro de un archivo válido** (sin título, categoría desconocida, sin pasos o duración no positiva): esa lección se descarta y el resto del catálogo se muestra con normalidad. Si todas son inválidas, se aplica el estado de error.
- **Lección inexistente** (identificador no encontrado al abrir el detalle): se vuelve a Educación sin mostrar error.
- **Lección sin tip ni texto de cierre propio**: el detalle muestra la tarjeta de cierre con el texto general y no deja espacio vacío donde iría el tip.
- **Sin conexión**: no aplica; el contenido es local.

---

## Requirements

### Functional Requirements

**Pantalla Educación**

- **RF-ED-001**: Educación NO DEBE mostrar el segmento Videos / Artículos / IA, ni el título "Videos destacados", ni el enlace "Ver todos", ni tarjetas de video.
- **RF-ED-002**: Educación NO DEBE jerarquizar lecciones: no existe tarjeta ni sección "destacada", ni "Para empezar", ni "Más para aprender". Todas las lecciones usan el mismo componente `C-13`. La única agrupación es por categoría.
- **RF-ED-003**: La pantalla DEBE mostrar, de arriba a abajo: encabezado, una sección por categoría con lecciones (orden fijo: Reciclar, Reutilizar, Caminar; título de sección con el nombre de la categoría) con sus tarjetas apiladas en el orden del catálogo, y la barra inferior fija. Las categorías sin lecciones no se muestran. Márgenes laterales de 16, 28 entre secciones y 16 entre tarjetas. El contenido hace scroll vertical y la barra inferior permanece fija.
- **RF-ED-004**: Cada tarjeta de lección (`C-13`) DEBE tener: ancho completo, radio 24, fondo blanco con borde y sombra suave; ilustración de 132 de alto como mínimo con fondo pastel según categoría; chip de categoría y metadato "N min · N pasos"; título 18/24 Bold; resumen 14/20; e indicación "Ver lección" con flecha. Toda la tarjeta DEBE ser un único elemento tocable (zona táctil mínima 48×48) que abre el detalle.
- **RF-ED-005**: Cada categoría DEBE tener su propio chip (`C-14`, píldora de 28 de alto): Reciclar fondo `#E6F2DF` con texto `#1F5E34`; Reutilizar fondo `#FFF1D6` con texto `#8A5A00`; Caminar fondo `#DFF0E6` con texto `#2E7D3E`. Sobre ilustración oscura se usa la variante translúcida. El fondo pastel de la ilustración de la tarjeta DEBE tomarse de los tokens aprobados (Reciclar `#E6F2DF`, Reutilizar `#FFE8BE`, Caminar `#E3F2F6`).
- **RF-ED-006**: El encabezado (`C-03` ajustado) DEBE conservar flecha de volver, título "Educación" (30/36 Bold, `#0F4D2E`), subtítulo "Aprendé, informate, hacé la diferencia" con la hojita a continuación del texto en la misma línea (puede ocupar dos líneas sin separar el ícono) y avatar. La flecha DEBE medir al menos 48×48 y DEBE retroceder igual que el botón atrás del sistema; si no hay pantalla previa en la navegación, lleva a Inicio. El avatar DEBE mostrar siempre la inicial del usuario autenticado en sesión (`nombre.ifBlank { "U" }`), igual que en Inicio y Actividades, sin quedar fija en "U".
- **RF-ED-007**: La barra inferior (`C-02`, `RF-UI-002b`) DEBE mostrar Educación seleccionada (ícono relleno con píldora `#E6F2DF` detrás, igual que Perfil en `RF-UI-014`) en el listado. En el detalle de lección, la barra DEBE resaltar la pestaña desde la que se abrió la lección (Educación o Inicio).
- **RF-ED-008**: Al volver desde el detalle de una lección, DEBE regresarse a la pantalla de origen (Educación o Inicio) y restaurar la posición de scroll que tenía antes de abrirlo.

**Detalle de lección**

- **RF-ED-009**: El sistema DEBE ofrecer una pantalla "Detalle de lección", hija de Educación (excepción autorizada a `RF-UI-002`), con, de arriba a abajo: encabezado `C-18` (banda de 208 con la ilustración de portada, botón de volver circular de 48 y hoja crema de bordes superiores de radio 28 superpuesta 28 sobre la banda); chip de categoría y "N min · N pasos"; título 24/32 Bold; introducción 15/22 (hasta 3 líneas en tamaño normal); pasos `C-15`; tip `C-16` opcional; tarjeta de cierre `C-17` (siempre presente); y la barra inferior fija. Se abre desde cualquier tarjeta de lección de Educación o de Inicio. Una lección DEBE admitir cualquier cantidad de pasos mayor o igual a 1.
- **RF-ED-010**: Cada paso (`C-15`) DEBE ser una tarjeta de radio 24 con ilustración de 150 de alto como mínimo, píldora "Paso N de M" sobre la ilustración, título 18/24 y texto 14/21 (hasta 3 líneas en tamaño normal). Entre un paso y el siguiente DEBE haber un conector visual de 24 con chevron hacia abajo.
- **RF-ED-011**: El tip (`C-16`) DEBE usar fondo `#FFF1D6`, borde `#F2CF8A`, ícono de bombilla y texto de hasta 2 líneas en tamaño normal con color `#6B3300` o `#6B4A12`. La tarjeta de cierre (`C-17`) DEBE usar degradé verde, la etiqueta "Ahora te toca a vos", un texto principal y un botón "Ver actividades" (píldora blanca de 48 de alto con flecha); el texto principal es el texto de cierre propio de la lección si lo define y, si no, el texto general "Poné en práctica lo que aprendiste". El tip DEBE omitirse si la lección no lo define; la tarjeta de cierre no se omite nunca.
- **RF-ED-012**: El botón "Ver actividades" DEBE abrir la pestaña Actividades en su estado habitual, igual que al seleccionarla en la barra inferior, sin filtros ni actividad preseleccionados. NO DEBE crear actividades, ni modificar puntajes, topes o estados de ninguna actividad. El cambio de pestaña sigue la navegación habitual entre pestañas.

**Inicio**

- **RF-ED-013**: En Inicio, el carrusel de tarjetas de video `C-09` de la sección "Contenido para vos" DEBE reemplazarse por hasta 2 tarjetas de lección compactas (mismo contenido esencial que `C-13`: ilustración, chip de categoría, título y duración), tomadas en el orden del catálogo. Tocar una abre el detalle de esa lección. Si el catálogo está vacío, la sección completa se omite. El resto de Inicio NO cambia.

**Contenido y datos**

- **RF-ED-014**: El catálogo de lecciones DEBE leerse de un archivo local embebido en la app, separado del código, cuya edición permita agregar o modificar lecciones sin tocar reglas de negocio ni requerir backend. Cada lección DEBE tener: identificador único, título, categoría (una de las tres existentes), duración en minutos, resumen, introducción, ilustración y descripción de portada, lista de pasos (título, texto, ilustración y descripción de cada uno), tip opcional y texto de cierre opcional. La cantidad de pasos se deriva de la lista. Una lección que incumpla estas reglas se descarta y un archivo ausente o con formato dañado DEBE producir el estado de error de Educación (ver Edge Cases). La lección de ejemplo inicial DEBE ser "El circuito del reciclaje en CABA" (Reciclar, 4 min, 5 pasos: separar en casa, llevar al contenedor verde, recolección, clasificación por material, nuevos productos), con tip y texto de cierre ("Llevá tus envases limpios a un Punto Verde").
- **RF-ED-015**: Las lecciones NO DEBEN asociarse a actividades concretas ni a categorías de actividades para el cierre: hay más lecciones que actividades, por lo que una lección puede servir a varias actividades o a ninguna. Las lecciones NO DEBEN guardar identificadores de actividades del catálogo (en el repositorio esos identificadores se generan aleatoriamente y no son estables). La categoría de una lección solo sirve para agrupar y rotular.
- **RF-ED-016**: Las ilustraciones (portada y pasos) DEBEN ser recursos vectoriales de la app (dibujos escalables en el estilo plano y colorido de los tableros: envases, contenedor verde, camión recolector, cinta de clasificación, banco de plaza), referenciados desde el archivo del catálogo por nombre. Sumar o reemplazar una ilustración DEBE requerir solo agregar o editar el archivo vectorial y su referencia en el catálogo, sin cambiar el código de las pantallas. Cada lección DEBE poder reutilizar ilustraciones de otras. Como parte de esta tarea se DEBEN crear vectores simples de reemplazo para la portada y los 5 pasos de la lección de ejemplo, inspirados en las referencias gráficas, de modo que diseño pueda sustituirlos después editando solo el archivo vectorial.
- **RF-ED-017**: Si en el catálogo falta o es inválida una ilustración referenciada, la pantalla DEBE mostrar un fondo pastel de la categoría en su lugar, sin fallar.

**Diseño, accesibilidad y comportamiento**

- **RF-ED-018**: Los colores, radios y espaciados nuevos DEBEN registrarse en el sistema de diseño único (`RF-UI-004`), los componentes `C-13` a `C-18` DEBEN sumarse a `C-01` a `C-11` (`RF-UI-005`) y ninguna pantalla de Educación o tarjeta de Inicio puede usar valores sueltos. Tokens a registrar: fondos pastel `#FFE8BE`, `#DCEFE6`, `#E6F2DF`, `#E3F2F6`, `#E4EEF3`, `#FFF1D6`, `#DFF0E6`; textos secundarios `#3E4A40` y `#4A554C`; tip `#6B3300` y `#6B4A12` con borde `#F2CF8A`; radios 24 y 28; separación entre tarjetas 16 y entre secciones 28; sombras de tarjeta suaves.
- **RF-ED-019**: Toda ilustración DEBE exponerse a un lector de pantalla con su descripción del catálogo; los adornos (destellos, conectores, hojita) DEBEN marcarse como decorativos (`RF-UI-018`). Cada tarjeta de lección DEBE leerse como un único elemento (categoría, título, duración, pasos), igual que las tarjetas de actividad en `RF-UI-018`. Los pasos DEBEN exponerse como lista ordenada.
- **RF-ED-020**: La sección NO DEBE usar animaciones ni transiciones (coherente con `RF-UI-019`).
- **RF-ED-021**: Contraste mínimo de 4,5:1 (`RF-UI-015`), zonas táctiles de al menos 48×48 (`RF-UI-016`) y soporte de texto del sistema hasta 130% sin cortes (`RF-UI-017`); las alturas del diseño (texto de paso de 3 líneas, título de 2 líneas) DEBEN tratarse como mínimas para que el texto ampliado no se corte (`AC-20`).

### Key Entities (no de dominio)

- **Lección**: identificador, título, categoría, duración en minutos, resumen, introducción, ilustración y descripción de portada, pasos, tip opcional y texto de cierre opcional.
- **Paso**: orden (implícito en la lista), título, texto breve, ilustración y descripción de la ilustración.
- **Catálogo de lecciones**: lista ordenada de lecciones leída de un archivo local en la capa de Presentación, sin persistencia remota ni reglas de negocio (mismo criterio de aislamiento que `RF-UI-003b`).

---

## Success Criteria

### Measurable Outcomes

- **SC-ED-001**: En Educación e Inicio no aparece ninguno de los elementos de `RF-ED-001`, ninguna tarjeta de video ni ninguna tarjeta destacada, verificable por inspección visual de ambas pantallas.
- **SC-ED-002**: Cada tarjeta de lección muestra, sin abrirla, su categoría, duración, cantidad de pasos, título y resumen, verificable por inspección visual en Educación y en Inicio.
- **SC-ED-003**: Partiendo desde la pestaña Educación (sin contar el toque que la abre), se llega a la sección Actividades en 2 toques (1: tarjeta de lección; 2: "Ver actividades") y al detalle de una actividad en 3 (3: tarjeta de actividad).
- **SC-ED-004**: Con el tamaño de texto del sistema al 130%, ninguna pantalla de Educación, detalle ni tarjeta de Inicio muestra texto cortado o superpuesto.
- **SC-ED-005**: Cero regresiones: los tests automatizados de lógica ya existentes siguen pasando al 100% y no se modifica ninguna regla de negocio.
- **SC-ED-006**: Con lector de pantalla, el 100% de las ilustraciones informativas se anuncian con su descripción, los adornos no se anuncian y los pasos se recorren en orden como "Paso N de M".
- **SC-ED-007**: Al volver desde un detalle, la posición de scroll de Educación coincide con la previa (verificable en una lección ubicada fuera de la primera pantalla).
- **SC-ED-008**: Agregar una lección nueva al catálogo, con ilustraciones ya existentes, se logra editando únicamente el archivo del catálogo y sin cambios en el código de las pantallas.

---

## Validación de Diseño

No es un criterio de aceptación del software: es una validación del diseño que se hace con el prototipo o la app terminada, fuera del ciclo de implementación.

- **Prueba de comprensión**: con 5 personas usuarias nuevas, al menos 4 identifican la categoría, la duración y el tema de cada lección sin abrirla. Si no se alcanza, se ajusta el diseño (chips, jerarquía de texto, resúmenes) y no bloquea la entrega de este spec.

---

## Assumptions

- El contenido de la lección de ejemplo es ilustrativo y se valida antes de publicarse.
- Las ilustraciones de la lección de ejemplo se entregan primero como vectores simples de reemplazo (RF-ED-016) y se sustituyen por las finales de diseño sin tocar el código. Este spec no define su producción final.
- Los colores de fondo pastel por categoría (RF-ED-005) son una asignación inicial tomada de los tokens aprobados y puede ajustarse a los tableros sin cambiar el requisito.
- Plataforma Android nativo con Jetpack Compose, igual que el resto de la app.
- El orden del catálogo es el orden del archivo; no implica prioridad entre lecciones.

---

## Fuera de Alcance

- Reproductor de video y retiro de sus pantallas o rutas existentes (se deja de enlazarlas; su limpieza queda para el plan).
- Solapa o sección de Artículos y pestaña o asistente de IA.
- Buscador o filtros de lecciones.
- Progreso o marcado de "lección leída" y puntos por leer.
- Estados de red (carga, sin conexión). Se cubren únicamente los casos locales de `Edge Cases` (catálogo vacío, archivo dañado, lección inválida o inexistente).
- Cambios en Actividades, Recompensas, Perfil, Puntos Verdes y el resto de Inicio.

---

## Clarifications

### Session 2026-10-28
- Q: ¿Qué pasa con el carrusel educativo de Inicio, que usa tarjetas de video `C-09`? → A: se reemplaza por tarjetas de lección (decisión original, ampliada el 2026-10-30: Inicio entra en alcance solo para estas tarjetas).
- Q: ¿Dónde se aloja el catálogo de lecciones? → A: en un archivo local embebido editable en el repositorio, separado del código.
- Q: ¿Cómo se estructura la navegación al detalle? → A: como pantalla hija de Educación que se apila sobre el listado y mantiene la barra inferior con Educación resaltada.

### Session 2026-10-30
- Q: ¿Inicio queda dentro del alcance? → A: sí, solo por sus tarjetas educativas (RF-ED-013); la Constitución se enmienda a v2.3.0.
- Q: ¿Ilustraciones dibujadas por código o recursos provistos por diseño? → A: recursos vectoriales de diseño (reemplaza la decisión previa de dibujarlas por código), por ser el formato más fiel a las referencias gráficas y el más editable sin tocar código (RF-ED-016).
- Q: ¿Hay lección destacada o jerarquía entre lecciones? → A: no; catálogo plano agrupado solo por categoría (RF-ED-002). Se descarta la tarjeta `C-12` y las secciones "Para empezar" / "Más para aprender".
- Q: ¿Cómo se asocia cada lección con su actividad relacionada? → A: de forma inicial se decidió asociarla por categoría y no por identificador (los identificadores del catálogo de actividades se generan aleatoriamente); esa decisión quedó reemplazada por la de la sesión 2026-10-30 (continuación).
- Q: ¿Cómo se vuelve a Educación y qué muestra el avatar? → A: restaurando la posición de scroll (RF-ED-008) y mostrando la inicial real del usuario (RF-ED-006).
- Q: ¿Qué ocurre si el archivo del catálogo está dañado? → A: Educación muestra un estado de error propio ("No pudimos cargar las lecciones" con "Reintentar"), distinto del catálogo vacío; una lección inválida se descarta sin afectar al resto (Edge Cases, RF-ED-014).

### Session 2026-10-30 (revisión de ambigüedades)
- Q: ¿Quién provee los vectores de la lección de ejemplo (portada + 5 pasos)? → A: se crean vectores simples de reemplazo dentro de esta tarea, inspirados en las referencias; diseño los sustituye después editando solo el archivo vectorial (RF-ED-016, Assumptions).
- Q: ¿Qué pestaña se resalta en el detalle y a dónde se vuelve según desde dónde se abrió? → A: el detalle conserva la pestaña de origen (Educación o Inicio) y volver regresa a esa pantalla en su posición de scroll (RF-ED-007, RF-ED-008).
- Q: ¿A dónde lleva la flecha de volver del listado de Educación? → A: retrocede como el botón atrás del sistema y, si no hay pantalla previa, lleva a Inicio (RF-ED-006).

### Session 2026-10-30 (continuación)
- Q: Hay más lecciones que actividades, así que muchas lecciones no tienen una actividad propia o sirven para varias. ¿A dónde lleva la tarjeta de cierre "Ahora te toca a vos"? → A: a la sección Actividades en general (el estado habitual de la pestaña, sin filtro ni actividad preseleccionados). La tarjeta se muestra en todas las lecciones; su texto principal es el de cierre propio de la lección si lo define y, si no, "Poné en práctica lo que aprendiste" (RF-ED-011, RF-ED-012, RF-ED-015).
