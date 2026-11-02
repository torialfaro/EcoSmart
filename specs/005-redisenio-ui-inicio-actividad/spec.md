# Feature Specification: Rediseño de la UI de Inicio y Actividad Individual

**Feature Branch**: `005-redisenio-ui-inicio-actividad`

**Created**: 2026-11-01

**Status**: Implemented

**Input**: User description: "Rediseño visual de dos pantallas, Inicio (S-01) y Actividad individual (detalle, S-03 para actividades no medibles), como enmienda al Principio X (Reconstrucción de UI Basada en Especificación de Diseño). Es una reversión únicamente de la UI: el sistema ya tiene backend y nada de lo que se muestra queda fijo en el código. La fuente de verdad visual son los tableros I1, I2, D1 y D2 del lienzo de Claude Design."

---

## Relación con las Especificaciones Existentes

Este módulo continúa a [specs/003-reconstruccion-ui/spec.md](../003-reconstruccion-ui/spec.md) y a [specs/004-redisenio-ui-educacion/spec.md](../004-redisenio-ui-educacion/spec.md), y reemplaza únicamente lo que esas especificaciones definen para Inicio y para el detalle de actividad:

| Requisito / criterio previo | Qué hace spec 005 |
|---|---|
| `RF-UI-008` (Inicio según §4) | Reemplazado por RF-IA-002 a RF-IA-010 |
| `RF-UI-010` / `RF-UI-011` (detalle de actividad, S-03 no medible) | Reemplazados por RF-IA-011 a RF-IA-018 |
| `RF-UI-006` (tarjeta de actividad `C-06`, alto fijo) | Se conserva el alto único y se reemplaza el contenido por `C-21` (RF-IA-006) |
| `RF-UI-002b` y `RF-UI-002c` (barra inferior de 5 pestañas y botón flotante de Puntos Verdes en Inicio) | Excepción: la barra pasa a tener un sexto ícono de Puntos Verdes y se retira el botón flotante (RF-IA-009) |
| `RF-UI-007` (pestaña resaltada) | Se reutiliza: Inicio resaltada en Inicio, Actividades resaltada en el detalle (RF-IA-010, RF-IA-018) |
| `RF-ED-013` (spec 004: tarjetas de lección en "Contenido para vos") | Se reutiliza sin cambios (RF-IA-008) |
| `RF-UI-004` y `RF-UI-005` (sistema de diseño y componentes) | Se extienden con tokens y componentes `C-19` a `C-25` (RF-IA-020, RF-IA-021) |
| `RF-UI-015` a `RF-UI-019` (accesibilidad y sin animaciones) | Se heredan y se concretan en RF-IA-022 y RF-IA-023 |

- **Reglas de negocio y datos**: no cambia ninguna regla de negocio, cálculo de puntaje, tope diario, racha, nivel, verificación por IA ni persistencia (`RF-UI-001`, Principios III y IV). El sistema ya tiene backend: este módulo no crea, no cambia ni reemplaza ninguna regla, entidad, endpoint ni colección.
- **Fuente de verdad visual**: los tableros **I1**, **I2** (Inicio) y **D1**, **D2** (Actividad individual) del lienzo de Claude Design, autorizados por la enmienda al Principio X de [.specify/memory/constitution.md](../../.specify/memory/constitution.md) (**v2.4.0**).
- **Alcance de pantallas**: solo Inicio y Actividad individual (actividades no medibles). Actividades (listado), Recompensas, Perfil, Educación y su detalle, Puntos Verdes y la captura de foto quedan fuera de alcance y se rigen por sus especificaciones.
- **Versión de la constitución**: el rediseño se pidió como v2.3.0, pero esa versión ya está ocupada por la ampliación de Educación a Inicio (spec 004); la enmienda de este módulo queda como **v2.4.0** (cambio MINOR).

---

## Problema y Objetivo

- **Problema**: (1) el indicador "0/1" de la actividad individual y de las tarjetas de Inicio queda fuera de lugar y no invita a actuar; (2) los logos de actividad no combinan con el logo de la app; (3) "Tu progreso general" muestra "0/10 actividades", que no corresponde a las categorías reales ni a los topes del backend; (4) Inicio repite el enlace "Ver todas" tres veces y superpone un botón flotante del mapa sobre otros elementos.
- **Objetivo**: que Inicio responda de un vistazo "cuánto hice hoy y qué puedo hacer ahora", y que el detalle de una actividad lleve a la persona directamente a hacerla, con una identidad visual coherente con la app y sin mostrar ningún dato inventado.

## Personas Usuarias

Usuarias y usuarios de la app (en este documento, "la usuaria") que quieren ver su avance del día y elegir una actividad sostenible para hacer.

## Glosario

- **Categoría**: `Reciclar`, `Reutilizar` o `Caminar` (las que existen hoy en el dominio, `CategoriaActividad`). Si el dominio incorpora otra, el rediseño la contempla sin cambiar su estructura.
- **Logo de categoría** (`C-19`): recurso gráfico propio de cada categoría. Mismo lenguaje visual que el logo de la app: disco blanco, tres flechas de reciclaje en verdes, corazón ámbar y un dibujo central propio.
- **Estado de actividad**: uno de tres, derivado de lo que el dominio y el backend ya saben: *para hacer*, *completada hoy* (la propia actividad tiene una verificación aprobada hoy) o *no disponible hoy* (la actividad no se hizo hoy y su categoría ya alcanzó su tope diario real).
- **Categoría "Hecha"**: categoría con al menos una verificación aprobada hoy. Si no, está "Pendiente".
- **Progreso de hoy** (`C-20`): resumen del día con un tramo y una columna por categoría.
- **Tarjeta de actividad** (`C-21`): tarjeta del carrusel de Inicio con panel de color, datos de la actividad y botón de acción.
- **Panel de actividad** (`C-22`): bloque superior del detalle con el logo grande de la categoría.
- **Botón de llamado a la acción** (`C-23`): botón con una frase que invita a actuar; tiene un estado por cada estado de actividad.

---

## User Scenarios & Testing

### User Story 1 - Ver mi avance del día y elegir qué hacer desde Inicio (Priority: P1)

Como usuaria, abro Inicio y veo mi saludo, mis puntos, mi progreso de hoy y las actividades que puedo hacer, cada una con un botón que me invita a hacerla.

**Why this priority**: es la pantalla principal; reemplaza un progreso engañoso ("0/10") y un indicador que no motiva ("0/1").

**Independent Test**: abrir Inicio con una cuenta real y comprobar que el nombre, los puntos, las categorías del progreso y cada actividad coinciden con los datos del sistema, y que no aparece "0/1", "0/10" ni "Ver todas".

**Acceptance Scenarios**:
1. **Given** que abro Inicio, **When** se carga la pantalla, **Then** veo de arriba a abajo: encabezado verde con saludo y avatar, tarjeta de puntos, "Tu progreso de hoy", "Actividades de hoy", botón "Ver todas las actividades", "Contenido para vos" y la barra inferior con Inicio resaltada.
2. **Given** el progreso de hoy, **When** se muestra, **Then** el contador es "N/total", donde total es la cantidad de categorías del dominio, y hay un tramo y una columna por categoría con su logo y el estado "Hecha" o "Pendiente".
3. **Given** una actividad para hacer, **When** la veo en el carrusel, **Then** su tarjeta muestra el logo, la categoría, "+N pts", el título (hasta 3 líneas) y el botón "Hacerla ahora".
4. **Given** una actividad ya completada hoy, **When** la veo, **Then** su botón dice "Completada hoy" y no invita a repetirla.
5. **Given** una actividad no disponible hoy, **When** la veo, **Then** su botón dice "Volvé mañana".
6. **Given** que toco una tarjeta de actividad o su botón, **When** corresponde hacerla, **Then** se abre el detalle de esa actividad.

---

### User Story 2 - Hacer una actividad desde su detalle (Priority: P1)

Como usuaria, en el detalle de una actividad veo su logo, sus puntos, un botón claro para hacerla y los pasos para lograrlo.

**Why this priority**: es el paso que convierte la intención en una verificación; hoy el botón "Realizarla" a ancho completo y el "0 / 1" no guían.

**Independent Test**: abrir el detalle de una actividad de Reciclar o Reutilizar y comprobar que el botón principal muestra los puntos reales de esa actividad y abre la captura de foto.

**Acceptance Scenarios**:
1. **Given** que abro el detalle de una actividad para hacer, **When** se carga, **Then** veo encabezado con volver, título y subtítulo; el panel con el logo grande, la categoría y los puntos; el botón "¡Hacela ahora y sumá N puntos!" con ícono de cámara y la nota "Sacás una foto, la describís y listo"; "Cómo se registra" con sus pasos numerados; el tip; "Aprendé primero"; y la barra inferior con Actividades resaltada.
2. **Given** el botón principal, **When** lo toco, **Then** se abre la captura de foto existente, sin cambios en su comportamiento.
3. **Given** que la actividad ya se completó hoy, **When** abro el detalle, **Then** el logo lleva una insignia de tilde, el chip dice "Completada", y en lugar del botón veo el mensaje "¡Listo por hoy! Volvé mañana para sumar otra vez."
4. **Given** el detalle, **When** toco la flecha de volver o el gesto del sistema, **Then** regreso a la pantalla de origen.

---

### User Story 3 - Aprender antes de hacer (Priority: P3)

Como usuaria, desde el detalle de una actividad puedo abrir la lección relacionada antes de hacerla.

**Why this priority**: conecta la práctica con el contenido educativo del spec 004 sin cambiar reglas.

**Independent Test**: tocar "Aprendé primero" y comprobar que se abre una lección y que al volver regreso al detalle.

**Acceptance Scenarios**:
1. **Given** que la categoría de la actividad tiene lecciones, **When** toco "Aprendé primero", **Then** se abre la primera lección de esa categoría y al volver regreso a la actividad.
2. **Given** que la categoría de la actividad no tiene lecciones, **When** abro su detalle, **Then** el bloque "Aprendé primero" no se muestra y no deja espacio vacío.

---

### Edge Cases

- **Títulos largos**: la tarjeta de actividad admite hasta 3 líneas y todas las tarjetas miden lo mismo (`RF-UI-006`); el texto adicional se recorta con puntos suspensivos, nunca se superpone con el chip, los puntos o el botón.
- **Texto del sistema al 130%**: ningún título, botón, contador ni columna del progreso queda cortado; los bloques crecen en altura (alturas mínimas, nunca fijas).
- **Sin actividades que mostrar**: "Actividades de hoy" no muestra tarjetas vacías ni enlaces rotos; el botón "Ver todas las actividades" sigue disponible.
- **Más de una actividad por categoría**: el carrusel muestra una sola tarjeta por categoría según `RF-IA-006a`; las demás actividades se acceden desde Actividades.
- **Tope alcanzado**: al completar la única actividad de Reciclar permitida por día, las demás actividades de Reciclar pasan a "Volvé mañana" y la categoría queda "Hecha"; las de Caminar nunca pasan a "Volvé mañana".
- **Misma actividad dos veces el mismo día**: una actividad con verificación aprobada hoy siempre se muestra "Completada hoy" y no se ofrece de nuevo ese día, aunque el tope de su categoría lo permita; las otras actividades de la categoría siguen disponibles hasta el tope.
- **Nombre vacío**: el saludo y la inicial del avatar usan el criterio ya vigente (`nombre.ifBlank`), sin inventar un nombre.
- **Puntos en cero**: se muestra el valor real "0", no un texto alternativo.
- **Cantidad de categorías distinta de 3**: el progreso se adapta (tramos, columnas y contador) sin cambiar código de la pantalla.
- **Catálogo de lecciones vacío o dañado**: "Contenido para vos" se omite, como define el spec 004.
- **Datos aún cargando**: se muestra la estructura de la pantalla sin valores inventados; nunca se muestra "0/3" ni un nombre de ejemplo mientras carga.
- **Actividad medible (Caminar)**: su detalle queda fuera de alcance y conserva su diseño actual.

---

## Requirements

### Functional Requirements

**Generales y datos**

- **RF-IA-001**: El módulo NO DEBE modificar ninguna regla de negocio, cálculo de puntaje, tope diario, racha, nivel, verificación por IA, entidad, endpoint ni persistencia (`RF-UI-001`).
- **RF-IA-019 (regla de datos, no negociable)**: Todo valor visible DEBE provenir del dominio y del backend existentes: nombre de la usuaria, puntos, progreso diario, estado de cada categoría, títulos de actividades, puntos de cada actividad, pasos, tip, categoría y estado de la actividad. Los valores de los tableros (por ejemplo "Viky", 0 y 100 puntos, "0/3", "+50 puntos" y los nombres de actividades) son ilustrativos y NO DEBEN usarse como constantes ni como valores por defecto.
- **RF-IA-019b**: La cantidad de categorías del progreso diario DEBE obtenerse de las categorías reales del dominio y de las reglas de tope ya vigentes. La UI NO DEBE asumir ni calcular un total propio. La UI solo asocia cada categoría con su logo y su color.

**Inicio**

- **RF-IA-002**: Inicio DEBE mostrar, de arriba a abajo: encabezado verde, tarjeta de puntos, "Tu progreso de hoy", "Actividades de hoy" (carrusel horizontal), botón "Ver todas las actividades", "Contenido para vos" (carrusel horizontal) y la barra inferior fija con Inicio resaltada. El contenido hace scroll vertical; la barra permanece fija.
- **RF-IA-003**: El encabezado DEBE ser un bloque verde con degradé y esquinas inferiores redondeadas, con el saludo con el nombre de la usuaria, el subtítulo de marca y el avatar a la derecha con la inicial real.
- **RF-IA-004**: La tarjeta de puntos DEBE ser una tarjeta blanca superpuesta al encabezado, con el logo de la app, la etiqueta "Tus puntos", el total real de puntos en tamaño grande con la hojita, y un chevron. El destino del chevron queda pendiente de aclaración.
- **RF-IA-005**: "Tu progreso de hoy" (`C-20`) DEBE mostrar el título, el contador "N/total", una barra segmentada con un tramo por categoría, una columna por categoría con su logo (atenuado si está pendiente) y el estado "Hecha" o "Pendiente", y la nota informativa "Hacé al menos una de cada categoría por día". Una categoría está "Hecha" con al menos una verificación aprobada hoy y "Pendiente" en caso contrario; el contador N cuenta las categorías "Hechas". Reemplaza a "Tu progreso general", su barra lineal, el "N/10 actividades" y su enlace "Ver todas".
- **RF-IA-006**: Cada tarjeta de actividad (`C-21`) DEBE mostrar un panel superior con el color de su categoría y su logo, el chip de categoría, "+N pts", el título (hasta 3 líneas) y un botón de llamado a la acción (`C-23`): "Hacerla ahora" (para hacer), "Completada hoy" (completada) o "Volvé mañana" (no disponible hoy). "Volvé mañana" solo aparece cuando el tope diario real de la categoría ya se alcanzó (Reciclar tras 1 actividad aprobada, Reutilizar tras 5; Caminar nunca, por no tener tope); mientras el tope permita más, la actividad sigue en "Hacerla ahora". Todas las tarjetas DEBEN medir lo mismo (`RF-UI-006`). NO DEBE mostrarse el indicador "0 / 1".
- **RF-IA-006a**: "Actividades de hoy" DEBE mostrar exactamente una tarjeta por categoría (en el orden Reciclar, Reutilizar, Caminar), con la primera actividad del catálogo de esa categoría que no esté completada hoy y no esté en "Volvé mañana". Si todas las de la categoría están completadas o no disponibles hoy, la tarjeta muestra la primera completada hoy y, si no hay, la primera del catálogo. Las demás actividades solo se ven en Actividades. Una categoría sin actividades en el catálogo no muestra tarjeta.
- **RF-IA-007**: El botón "Ver todas las actividades" DEBE tener estilo de contorno (no de relleno) y abrir la pestaña Actividades, igual que al seleccionarla en la barra inferior.
- **RF-IA-008**: "Contenido para vos" DEBE mostrar las tarjetas de lección compactas definidas en `RF-ED-013` (spec 004), sin cambios.
- **RF-IA-009**: La barra inferior DEBE tener un sexto ícono que abre Puntos Verdes y DEBE mostrarse con los seis íconos en todas las pantallas que hoy muestran la barra (Inicio, Actividades, Recompensas, Educación y su detalle, detalle de actividad, Caminata y Perfil) y también en Puntos Verdes, donde ese ícono queda resaltado. La barra es el mismo componente en todas ellas (sin versiones distintas). El botón flotante circular del mapa DEBE retirarse de Inicio.
- **RF-IA-010**: Inicio NO DEBE mostrar la tarjeta de puntos de fondo verde claro con chevron, la tarjeta "Tu progreso general", el enlace "Ver todas" del encabezado de "Actividades de hoy" ni el "0 / 1" de las tarjetas.

**Actividad individual**

- **RF-IA-011**: El encabezado del detalle DEBE tener la flecha de volver de 48×48, el título de la actividad en 22/28 y un subtítulo con la hojita en línea con el texto.
- **RF-IA-012**: El panel de actividad (`C-22`) DEBE mostrar el logo grande de la categoría, el chip de categoría y la píldora con los puntos reales de la actividad, sobre el degradé de su categoría.
- **RF-IA-013**: El botón principal DEBE decir "¡Hacela ahora y sumá N puntos!" con ícono de cámara, con N igual a los puntos reales de la actividad, abrir la captura de foto existente y llevar debajo la nota "Sacás una foto, la describís y listo".
- **RF-IA-014**: Si la actividad está completada hoy, el logo DEBE llevar una insignia de tilde, el chip DEBE decir "Completada" y, en lugar del botón principal, DEBE mostrarse el mensaje "¡Listo por hoy! Volvé mañana para sumar otra vez."
- **RF-IA-015**: "Cómo se registra" DEBE mostrar los pasos de la actividad (las líneas numeradas de su texto de pasos, una por paso, tantos como haya y sin forzar tres) como pasos numerados (`C-24`) dentro de una tarjeta, con círculos de número, en lugar de una lista de texto corrido.
- **RF-IA-016**: El tip DEBE mostrarse en una tarjeta ámbar con ícono de bombilla, igual a la de las lecciones, con el rótulo "Qué tiene que verse en la foto" y el resultado esperado de la actividad como contenido. La tarjeta no se muestra si el resultado esperado está vacío.
- **RF-IA-017**: El enlace "Aprendé primero" (`C-25`) DEBE abrir la primera lección de la categoría de la actividad, según el orden del catálogo de lecciones, y al volver regresa al detalle. Si la categoría no tiene lecciones, el bloque no se muestra.
- **RF-IA-018**: El detalle DEBE mostrar, de arriba a abajo: encabezado, panel con logo, botón principal (o mensaje de completada), "Cómo se registra", tip, "Aprendé primero" y la barra inferior con Actividades resaltada (`RF-UI-007`). NO DEBE mostrar el círculo "0 / 1 completada", la píldora de recompensa a ancho completo, el botón "Realizarla" a ancho completo ni la ilustración decorativa de colinas.

**Sistema de diseño y accesibilidad**

- **RF-IA-020**: Los logos de categoría (`C-19`) DEBEN ser tres (Reciclar, Reutilizar, Caminar), con el mismo lenguaje visual del logo de la app, y estar disponibles en tamaños 44, 76 y 120. Reemplazan a los logos de actividad actuales.
- **RF-IA-021**: Los componentes nuevos son `C-19` LogoCategoria, `C-20` TarjetaProgresoDiario, `C-21` TarjetaActividadConBoton, `C-22` PanelActividad, `C-23` BotonLlamadoAccion (estados: para hacer, completada, no disponible hoy), `C-24` PasoNumerado y `C-25` TarjetaEnlaceLeccion. Los tokens nuevos son:
  - Degradés de panel por categoría: Reciclar `#E6F2DF` a `#CFE8C6`; Reutilizar `#FFF1D6` a `#FFE2A8`; Caminar `#DFF0E6` a `#BFE0CC`.
  - Verdes y ámbar de los logos: `#3E8E4A`, `#1F5E34`, `#7DB574` y `#E9A23B`.
  - Radios de 24 y 28 y sombras suaves de tarjeta.
- **RF-IA-022**: Ambas pantallas DEBEN cumplir `RF-UI-015` a `RF-UI-018`: contraste de texto de al menos 4,5:1; zonas táctiles de al menos 48×48; texto del sistema hasta 130% sin cortes; cada tarjeta y botón expuestos a un lector de pantalla con un nombre que incluya categoría, título, puntos y estado; logos y adornos decorativos marcados como tales cuando el texto ya los describe.
- **RF-IA-023**: Ninguna de las dos pantallas DEBE tener animaciones ni transiciones (`RF-UI-019`).

### Key Entities (no de dominio)

Este módulo no agrega entidades de dominio ni datos persistidos. Usa los datos existentes:

- **Actividad** (catálogo): categoría, descripción corta, pasos a seguir, resultado esperado, puntos base.
- **Categoría de actividad** (`CategoriaActividad`): fuente del total del progreso diario y de la asociación con logo y color.
- **Perfil / puntos del usuario**: nombre, inicial y puntos acumulados.
- **Registros de verificación aprobados**: base del estado "hecha / completada hoy" de cada categoría o actividad, según lo que el dominio ya calcula.
- **Lección**: contenido del spec 004, solo como destino de "Aprendé primero" y de "Contenido para vos".

---

## Success Criteria

### Measurable Outcomes

- **SC-IA-001**: En Inicio y en el detalle no aparece ninguno de los elementos eliminados (`RF-IA-010`, `RF-IA-018`), ningún "0 / 1", ningún "N/10" ni el botón flotante del mapa, verificable por inspección visual de ambas pantallas.
- **SC-IA-002**: Ningún texto ni número visible en las dos pantallas coincide con un valor fijo del código: cambiar el nombre, los puntos o los registros del día de una cuenta de prueba cambia lo mostrado sin tocar código, verificable con al menos dos cuentas distintas.
- **SC-IA-003**: El total del progreso de hoy es igual a la cantidad de categorías del dominio, verificable agregando o quitando una categoría de prueba y comprobando que el contador y las columnas se adaptan.
- **SC-IA-004**: Todas las tarjetas del carrusel de "Actividades de hoy" miden exactamente lo mismo con títulos de 1, 2 y 3 líneas, verificable por inspección visual.
- **SC-IA-005**: Desde Inicio, la persona llega a la captura de foto de una actividad en no más de 2 toques (tarjeta o botón y botón principal del detalle).
- **SC-IA-006**: Con el texto del sistema al 130%, ningún elemento de las dos pantallas queda cortado, verificable por pruebas de interfaz y revisión manual.
- **SC-IA-007**: Contraste de texto >= 4,5:1 y zonas táctiles >= 48×48 en todos los elementos interactivos, verificable por pruebas de interfaz.
- **SC-IA-008**: No se agrega ninguna animación ni transición, verificable por revisión de código y recorrido manual.
- **SC-IA-009**: Cero cambios en `domain/`, `application/`, `infrastructure/`, `backend/` y `firestore.rules`, verificable con `git diff --stat`, y la suite de pruebas existente sigue en verde.

---

## Validación de Diseño

- Los tableros **I1** y **I2** (Inicio) y **D1** y **D2** (Actividad individual) del lienzo de Claude Design son la referencia visual. Los estados que muestran son: Inicio con progreso 0/total y con una categoría hecha; detalle para hacer; detalle completada.
- Antes de implementar se compara cada pantalla con su tablero en un dispositivo de referencia (API 26+) y con texto del sistema al 100% y al 130%.
- Los valores que aparecen en los tableros (nombre, puntos, "0/3", "+50 puntos", títulos) se tratan como ejemplos y no como contenido.

## Assumptions

- Las tres categorías actuales del dominio son Reciclar, Reutilizar y Caminar; los tres logos de categoría se entregan como recursos gráficos y no como imágenes remotas.
- El detalle de una actividad medible (Caminar) conserva su diseño actual porque este módulo solo abarca el detalle de actividades no medibles.
- La captura de foto, el reporte de resultado y el resto del flujo de verificación se mantienen sin cambios.
- La barra inferior con seis íconos se aplica en todas las pantallas que hoy muestran la barra de cinco y en Puntos Verdes; el sexto ícono agrega el acceso a Puntos Verdes y no cambia el resto de las pestañas.
- Los textos de los tableros ("Hacerla ahora", "Completada hoy", "Volvé mañana", "Aprendé primero", etc.) son los textos finales de interfaz; los datos que los acompañan no lo son.

## Fuera de Alcance

- Cualquier cambio en reglas de negocio, topes, puntaje, racha, niveles, verificación por IA, backend o persistencia.
- Actividades (listado), Recompensas, Perfil, Educación y su detalle, Puntos Verdes, la captura de foto y el detalle de actividades medibles.
- Nuevos contenidos educativos o cambios al catálogo de lecciones del spec 004.
- Animaciones, transiciones y modo oscuro específico para estas pantallas.
- Definir el destino de la tarjeta de puntos si no se aclara (ver abajo).

---

## Ambigüedades abiertas (para `/speckit.clarify`)

Resolver antes de `/speckit.plan`. Hasta entonces, la UI NO DEBE inventar una regla para ninguno de estos puntos (Principio I).

1. ~~Contradicción entre la regla enunciada y los topes reales~~ — resuelta el 2026-11-01 (ver Clarifications).
2. ~~Estado "no disponible hoy"~~ — resuelta en el plan (`research.md` §1): se deriva en Presentación de los registros de verificación existentes y de `EstrategiaDePuntaje`, sin cambiar el dominio.
3. **Destino de la tarjeta de puntos (decisión provisoria del plan).** Se conserva el destino actual, Perfil (`research.md` §5). Pendiente de confirmación del usuario.
4. ~~Lección relacionada~~ — resuelta el 2026-11-01 (ver Clarifications).
5. ~~Más de una actividad por categoría~~ — resuelta el 2026-11-01 (ver Clarifications).
6. ~~Origen de los pasos y del tip~~ — resuelta el 2026-11-01 (ver Clarifications).
7. ~~Alcance del sexto ícono de la barra~~ — resuelta por el usuario el 2026-11-01: la barra nueva se ve en todas las pestañas (ver `RF-IA-009`).

## Clarifications

### Session 2026-11-01

- Q: ¿Qué versión de la constitución corresponde a esta enmienda? → A: v2.4.0 (MINOR). La v2.3.0 sugerida en el pedido ya está ocupada por la ampliación de Educación a Inicio.
- Q: ¿Qué significa "Hecha" y cómo se trata el tope por categoría, dado que la regla enunciada ("una por categoría por día") contradice los topes reales (Reciclar 1, Reutilizar 5, Caminar sin tope)? → A: Opción A. La categoría está "Hecha" con al menos una verificación aprobada hoy; "Volvé mañana" solo cuando se alcanzó el tope real; la nota pasa a "Hacé al menos una de cada categoría por día". Se respeta la regla de negocio actual.
- Q: ¿Qué actividades muestra el carrusel "Actividades de hoy" cuando hay más de una por categoría? → A: Opción B. Una sola por categoría: la primera del catálogo que no esté completada hoy (si todas lo están, la primera completada hoy). Las demás se ven en Actividades.
- Q: ¿De dónde salen los pasos y el tip del detalle? → A: Opción A. Pasos: líneas numeradas del texto de pasos existente (tantos como haya). Tip: el resultado esperado de la actividad, con el rótulo "Qué tiene que verse en la foto". Sin cambios de dominio.
- Q: ¿Cómo se asocia cada actividad con su lección ("Aprendé primero")? → A: Opción A. Por categoría: abre la primera lección de la categoría de la actividad según el catálogo; si no hay, el bloque se oculta.
- Q: ¿Qué pasa con una actividad ya completada hoy cuando su categoría no alcanzó el tope? → A: Opción A. Siempre se muestra "Completada hoy" y no se repite el mismo día; las otras actividades de la categoría siguen disponibles hasta el tope. El estado sale de los registros aprobados del día existentes, sin datos nuevos.
- Q: ¿Los valores de los tableros pueden usarse como datos de ejemplo en la app? → A: No. Son ilustrativos y no se llevan al código como constantes ni valores por defecto (RF-IA-019).
