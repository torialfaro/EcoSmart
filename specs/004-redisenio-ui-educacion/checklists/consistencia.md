# Consistency & Cohesion Checklist: Reconstrucción de la Sección Educación

**Purpose**: Detectar inconsistencias de redacción, contradicciones internas y desalineaciones con la Constitución v2.3.0 y el spec 003 antes de `/speckit.plan`.
**Created**: 2026-10-30
**Feature**: [spec.md](../spec.md)

**Note**: Cada ítem evalúa la calidad de los requisitos (no la implementación). Un ítem sin marcar `[ ]` indica un hallazgo que requiere corregir el spec; la causa se cita entre paréntesis.

## Alcance y contradicciones entre secciones

- [x] CHK001 ¿Se resuelve la contradicción sobre Inicio? RF-ED-001 ordena reemplazar el carrusel C-09 de `HomeScreen`, pero "Alcance de pantallas" declara Inicio fuera de alcance, y la enmienda v2.2.0 de la Constitución dice que "el resto de las pantallas no se toca" y que C-09 "sigue existiendo en Inicio". [Conflicto: spec L18 y L95 vs. constitution Principio X]
- [x] CHK002 ¿"Fuera de Alcance" lista explícitamente la excepción de Inicio (o la excluye), de modo que no contradiga la tarjeta de lección de Inicio de RF-ED-001 y la Assumption final? [Gap, Fuera de Alcance]
- [x] CHK003 ¿El cambio en Inicio tiene historia de usuario, escenario de aceptación y criterio de éxito propios? Hoy solo aparece en RF-ED-001, la Assumption y Clarifications (Principio I: nada sin historia asociada). [Gap, Traceability]
- [x] CHK004 ¿La frase "retirando los videos de toda la aplicación" (Clarifications) es coherente con "Reproductor de video" fuera de alcance y con AC-16 de spec 003 (reproductor existente)? Falta declarar qué ocurre con el reproductor y las rutas de video ya implementadas. [Ambiguity, Clarifications Q1]
- [x] CHK005 ¿La Assumption "reutiliza el formato de tarjeta de lección o banner" es una única decisión? La alternativa "o" deja indefinido el componente de Inicio y su destino ("Educación o detalle de la lección destacada"). [Ambiguity, Assumptions]

## Terminología y nomenclatura

- [x] CHK006 ¿Se usa un único nombre para el módulo? Título "Reconstrucción…", carpeta/branch `redisenio-ui-educacion`, y la constitución dice "rediseño/reconstrucción" indistintamente. [Consistency]
- [x] CHK007 ¿Se usa un único prefijo de requisitos? Este spec usa `RF-ED-`, pero la memoria del repo y la constitución mencionan convenciones por módulo (`RF-UI-`, `RF-D`); confirmar que `RF-ED-`/`SC-ED-` no choque con otros specs. [Consistency]
- [x] CHK008 ¿El spec define los componentes C-12 a C-18 que usa? RF-ED-008 menciona "tarjeta de cierre C-17" y la constitución enumera C-12..C-18, pero el spec nunca los define ni los lista (solo describe sus partes en prosa). [Gap, RF-ED-003/006/008]
- [x] CHK009 ¿La terminología "tarjeta destacada" / "lección destacada" / "tarjeta resumen" está unificada y mapeada a un componente concreto? [Ambiguity, RF-ED-001, RF-ED-003]
- [x] CHK010 ¿"usuaria" y "persona usuaria"/"usuarios" se usan con criterio único? La sección Personas habla de "Usuarias y usuarios" y las historias de "usuaria". [Consistency, menor]
- [x] CHK011 ¿"tip" / "Tip para que cuente" / "consejo" y "tarjeta de cierre" / "tarjeta de llamado a la acción" / "Ahora te toca a vos" se refieren a nombres canónicos únicos? [Consistency]

## Requisitos incompletos o sin trazabilidad

- [x] CHK012 ¿Cómo se determina qué lección es la "destacada"? Key Entities no incluye atributo `destacada` ni orden; la selección impacta Educación (E1) y la tarjeta de Inicio. [Gap, Key Entities]
- [x] CHK013 ¿El criterio de "color de chip propio" por categoría (RF-ED-004) está cuantificado? No se listan colores ni se referencian los tokens aprobados en la enmienda (#E6F2DF/#1F5E34, #FFF1D6/#8A5A00, #DFF0E6/#2E7D3E). [Gap, RF-ED-004]
- [x] CHK014 ¿RF-ED-012 enumera los tokens nuevos (pasteles, textos secundarios, tip, radios 24/28, separaciones 16/28)? Hoy solo exige "agregarlos" sin lista, por lo que no es verificable. [Measurability, RF-ED-012]
- [x] CHK015 ¿Las medidas del diseño (altura de ilustración 192/132/150, banda 208, flecha 48×48, tipografías 30/36, 22/28, 18/24, 14/20, 24/32) están capturadas o referenciadas desde el spec? Solo se cita 48×48. [Gap]
- [x] CHK016 ¿Existe requisito que cubra "regreso a Educación en la misma posición de scroll" (Historia 2, escenario 2)? Ningún RF-ED lo menciona. [Gap, Traceability]
- [x] CHK017 ¿Existe requisito que cubra el scroll vertical con barra inferior fija (Historia 1, escenario 3)? RF-ED-002 solo dice "barra inferior fija". [Gap]
- [x] CHK018 ¿El avatar con la inicial del usuario en sesión (requisito heredado de RF-UI-013) quedó cubierto? RF-ED-005 dice "conservar avatar" sin exigir la inicial real. [Gap, RF-ED-005]
- [x] CHK019 ¿Se especifica el estado de Educación resaltada en la barra inferior (píldora #E6F2DF) o se remite a RF-UI-014(7)/C-02? [Clarity, RF-ED-002/006]
- [x] CHK020 ¿Se declara cuántas lecciones hay en la muestra inicial y su contenido (lección "El circuito del reciclaje en CABA" con 5 pasos, tip y actividad)? La versión anterior del spec lo detallaba; el texto actual solo lo cita como ejemplo y en Assumptions. [Gap, versión previa vs. actual]
- [x] CHK021 ¿Se declara a qué actividad concreta apunta la lección de ejemplo (ID del catálogo)? Sin esto, SC-ED-003 y la Historia 3 no son testeables. [Measurability]

## Detalles de implementación filtrados en el spec

- [x] CHK022 ¿Se retiran o se justifican los detalles técnicos en requisitos de negocio: ruta `educacion/{leccionId}`, `EcoSmartNavHost`, "Canvas vectoriales nativos en Kotlin", `app/src/main/assets/educacion/lecciones.json`, `HomeScreen`? Contradicen el criterio "sin detalles de implementación" ya marcado como cumplido en `requirements.md`. [Consistency, RF-ED-006/007/013]
- [x] CHK023 ¿La Assumption "ilustraciones son recursos vectoriales propios provistos por diseño" es compatible con RF-ED-007 (se dibujan por código con Canvas)? Un mismo documento afirma dos orígenes distintos. [Conflict, Assumptions vs. RF-ED-007]
- [x] CHK024 ¿"Key Entities" ("archivo local de recursos (JSON) parseado en Presentación") y RF-ED-013 (assets) son coherentes con el criterio original "muestra fija en Presentación (mismo criterio que RF-UI-003b)"? Ya no es una muestra en código sino un archivo; revisar que RF-UI-003b siga aplicando. [Consistency]
- [x] CHK025 ¿La ruta `educacion/{leccionId}` convive sin ambigüedad con la ruta actual de Educación (`ContenidoEducativo`)? El spec no indica si se renombra, se reemplaza o se agrega. [Ambiguity, RF-ED-006]

## Casos borde y estados

- [x] CHK026 ¿"Fuera de Alcance" excluye "estados de carga, vacío o error" pero RF-ED-013 carga un JSON desde assets? Falta definir qué ocurre si el archivo falta o está malformado, y con cero lecciones (solo se define el caso de una). [Gap, Edge Cases]
- [x] CHK027 ¿Se define el comportamiento ante un `leccionId` inexistente en la ruta de detalle (p. ej., deep link o JSON editado)? [Gap, Exception flow]
- [x] CHK028 ¿El edge case "Sin conexión o contenido sin cargar: fuera de alcance (RF-UI-003)" sigue siendo válido ahora que el contenido es local? La referencia a RF-UI-003 y la condición "sin conexión" ya no aplican con assets. [Consistency, Edge Cases]
- [x] CHK029 ¿Se contempla el caso en que `cantidadPasos` declarado no coincida con la cantidad real de pasos del JSON ("N pasos" vs. "Paso N de M")? [Gap, Data consistency]
- [x] CHK030 ¿Se define el comportamiento cuando `actividadRelacionadaId` existe pero la actividad está completada o con tope diario alcanzado? RF-ED-008 solo cubre nulo/inexistente. [Coverage]

## Criterios de éxito y medibilidad

- [x] CHK031 ¿SC-ED-003 es consistente? Dice "no más de 3 toques" y enumera solo 2 ("tarjeta", "Ver actividad"); no aclara si cuenta el toque en la pestaña Educación. [Ambiguity, SC-ED-003]
- [x] CHK032 ¿SC-ED-001 cubre también la ausencia de videos en Inicio, dado que RF-ED-001 los retira allí? [Coverage, SC-ED-001]
- [x] CHK033 ¿Existe criterio de éxito para accesibilidad de lector de pantalla (RF-ED-009) y ausencia de animaciones (RF-ED-010)? Ambos RF no tienen SC asociado. [Gap, Measurability]
- [x] CHK034 ¿SC-ED-002 (prueba con 5 usuarios, 4 de 5) es viable y está dentro del alcance del spec, o debe pasar a validación de diseño? [Measurability]
- [x] CHK035 ¿SC-ED-005 "tests de lógica existentes siguen pasando" es verificable sin introducir tests de UI/Compose nuevos que el Principio VIII no exige? [Clarity]

## Referencias cruzadas y alineación con documentos rectores

- [x] CHK036 ¿RF-ED-011 cita `RF-UI-015` a `RF-UI-017` mientras la constitución v2.2.0 exige `RF-UI-015` a `RF-UI-018`? Además RF-UI-018 en spec 003 trata de tarjetas de actividad, no de ilustraciones; aclarar qué cubre RF-ED-009. [Conflict, RF-ED-011 vs. constitution]
- [x] CHK037 ¿RF-ED-010 referencia `RF-UI-019` (animaciones) como hace la constitución? [Traceability, RF-ED-010]
- [x] CHK038 ¿La lista de lo reemplazado ("RF-UI-013, escenario 3 de la Historia 3, AC-16 y AC-17") es exacta? En spec 003, AC-16 es el reproductor de video (Educación, "Videos"), y AC-17 se cita como condicional a pantallas fuera de alcance; verificar que AC-17 realmente corresponde a Educación. [Traceability, spec 003 L365 y L670]
- [x] CHK039 ¿La enmienda de la constitución autoriza excepciones a `RF-UI-002` y `RF-UI-013`, pero el spec no menciona `RF-UI-002` en su sección de relación? [Traceability, Relación con especificaciones]
- [x] CHK040 ¿Se actualiza spec 003 (RF-UI-013, escenario 3 de la Historia 3, RF-UI-008/S-01 si cambia Inicio) con una nota "reemplazado por spec 004" para evitar requisitos duplicados contradictorios? [Consistency, cross-spec]
- [x] CHK041 ¿El encabezado "Status: Draft" y la nota de `requirements.md` ("listo para /speckit.plan") son consistentes con los hallazgos de este checklist? [Consistency]

## Revisión posterior (estado de error y referencias)

- [x] CHK042 ¿Se distingue el catálogo vacío del archivo ausente o dañado, con mensaje y acción propios ("Reintentar") en Educación y omisión limpia en Inicio? [Coverage, Edge Cases, Historia 1 escenario 5]
- [x] CHK043 ¿Se define qué ocurre con una lección inválida dentro de un archivo válido y cuándo se pasa al estado de error (todas inválidas)? [Completeness, RF-ED-014]
- [x] CHK044 ¿SC-ED-003 aclara que no cuenta el toque en la pestaña y enumera los 3 toques (tarjeta, "Ver actividades", tarjeta de actividad)? [Clarity]
- [x] CHK045 ¿La tabla de relación con spec 003 cubre `RF-UI-002b`/`C-02`, `RF-UI-003b`, `RF-UI-004`/`005`, `RF-UI-009` y `RF-UI-015` a `019`, con referencias RF-ED vigentes? [Traceability]
- [x] CHK046 ¿RF-ED-005 y RF-ED-007 están redactados sin texto mezclado (se corrigió una edición que había cruzado ambos requisitos)? [Consistency]
- [x] CHK047 ¿La sesión de Clarifications 2026-10-30 registra cada decisión posterior, incluido el estado de error? [Traceability]
- [x] CHK048 ¿La tarjeta de cierre `C-17` lleva a Actividades en general en todos los lugares del spec (Glosario, Historia 3, RF-ED-011, RF-ED-012, RF-ED-015, SC-ED-003, Fuera de Alcance, tabla de relación), sin restos de filtro por categoría? [Consistency]
- [x] CHK049 ¿Está definido qué muestra la tarjeta de cierre cuando la lección no tiene texto de cierre propio (texto general "Poné en práctica lo que aprendiste") y que nunca se omite? [Completeness, RF-ED-011]
- [x] CHK050 ¿La categoría de la lección queda limitada a agrupar y rotular, sin relación con actividades (RF-ED-015)? [Clarity]
- [x] CHK051 ¿Está definido quién provee los vectores de la lección de ejemplo (vectores simples de reemplazo creados en esta tarea, sustituibles por diseño)? [Completeness, RF-ED-016]
- [x] CHK052 ¿Está definida la pestaña resaltada en el detalle y el destino al volver según el origen (Educación o Inicio)? [Clarity, RF-ED-007/008]
- [x] CHK053 ¿Está definido el destino de la flecha de volver del listado de Educación (atrás del sistema; Inicio si no hay historial)? [Clarity, RF-ED-006]

## Notes

- Revisión 2026-10-30: el spec fue reescrito (Inicio en alcance, ilustraciones vectoriales, catálogo sin jerarquía, relación por categoría, scroll, avatar, estados locales). CHK007: no hay otros specs con prefijo `RF-ED-`/`SC-ED-`. CHK035: Principio VIII exige tests solo de reglas de negocio; SC-ED-005 se verifica corriendo la suite existente. CHK034: la prueba con 5 usuarios pasó a la sección "Validación de Diseño" del spec (ya no es criterio de éxito). CHK040: spec 003 ahora indica que Educación y el carrusel educativo de Inicio se rigen por spec 004. No quedan ítems abiertos.
- Cambio de fondo: `actividadRelacionadaId` se descartó porque los IDs del catálogo de actividades son UUID aleatorios (`ActividadId.nuevo()`); además hay más lecciones que actividades, por lo que la tarjeta de cierre lleva a Actividades en general y no a una actividad o categoría concreta.
