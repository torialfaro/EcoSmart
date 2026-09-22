# Functional Quality Checklist: EcoSmart — Sistema Completo (MVP)

**Purpose**: Validar completitud, claridad y consistencia interna de `spec.md` antes de iniciar `/speckit.plan`. Estos ítems evalúan la calidad de los REQUISITOS (si están bien escritos), no el comportamiento de una implementación.
**Created**: 2026-09-20
**Feature**: [spec.md](../spec.md)
**Depth**: Standard — gate de calidad previo a planificación técnica
**Audience**: Autor / equipo del proyecto

## 1. Resumen Ejecutivo

**Dictamen global: `PASS`** *(actualizado 2026-09-20; ver historial de warnings resueltos más abajo)*

`spec.md` cubre las 13 historias de usuario, RF-001 a RF-059, RNF-001 a RNF-008,
8 edge cases, 8 criterios de éxito (SC-001 a SC-008), 6 entidades clave y las 10
ambigüedades detectadas (todas marcadas `✅ Resuelto`). La especificación es
internamente trazable en los 5 puntos de coherencia cruzada solicitados
(Niveles, Podómetro, EcoGPT/Indeterminado, Topes/Duplicados, Seguridad).

**Actualización 2026-09-20 (revisión dirigida a FR duplicados, performance y
responsabilidades)**: se resolvieron 3 de los 4 hallazgos originales y se
cerró 1 hallazgo nuevo:

- ✅ **Resuelto** — RF-020 ya no dice "radio razonable"; ahora remite a RF-053
  (3 km), y RF-018 remite a RF-052/RF-053 para el mecanismo de datos.
- ✅ **Resuelto** — RF-056 duplicaba la regla de tope diario de RF-034; ahora
  remite a RF-034/RF-035 y solo aporta la información nueva (365 días, sin
  horario).
- ✅ **Resuelto (nuevo)** — no había límite de tiempo para la respuesta de
  EcoGPT (SC-002 excluía explícitamente el tiempo de red). Se agregó
  RNF-008 (timeout de 30 s), el Edge Case "Timeout de EcoGPT" y SC-008.

**Actualización 2026-09-20 (cuestionario dirigido a los 4 pendientes)**: se
resolvieron los 4 hallazgos restantes mediante 4 preguntas interactivas con
casos hipotéticos:

- ✅ **Resuelto** — RF-004 ahora exige mínimo 8 caracteres con letras y
  números.
- ✅ **Resuelto** — RF-048/RF-049 ahora definen que el bloque de puntaje se
  cuenta sobre pasos (133 pasos = 1 bloque), no sobre metros redondeados;
  RF-031 se alineó a la misma redacción.
- ✅ **Resuelto** — se agregó la entidad "Permiso de Dispositivo" (3
  estados: `NO_SOLICITADO` / `OTORGADO` / `DENEGADO`) a Key Entities.
- ✅ **Resuelto** — US11 (escenario 2) ahora dice "3 días consecutivos" en
  vez de "varios días consecutivos".

**No quedan hallazgos abiertos** (26/26 ítems de este checklist pasan). El
dictamen global pasa de `PASS WITH WARNINGS` a **`PASS`**.

## 2. Matriz de Cobertura y Trazabilidad

| Dominio funcional | Historias de Usuario | RF | RNF | Edge Cases | SC |
|---|---|---|---|---|---|
| Autenticación, Registro y Perfil | US1, US2, US3 | RF-001–RF-010 | RNF-006, RNF-007 | Correo ya registrado | SC-001 |
| Home, Contenido y Puntos Verdes | US4, US5, US6, US7 | RF-011–RF-020, RF-052, RF-053 | RNF-002 | Sin Puntos Verdes en el radio | SC-004, SC-005 |
| Verificación Podómetro / EcoGPT | US8, US9 | RF-021–RF-030, RF-048–RF-051, RF-058, RF-059 | RNF-003, RNF-005 | Corte de conexión, Lecturas anómalas, Imagen corrupta, Fotografía duplicada | SC-002, SC-006, SC-007 |
| Puntuación y Topes Diarios | US9, US10 | RF-031–RF-035, RF-049, RF-050, RF-056, RF-057 | RNF-003, RNF-004, RNF-008 | Tope diario superado, Timeout de EcoGPT | SC-003, SC-008 |
| Perfil, Métricas, Historial y Niveles | US11, US12 | RF-036–RF-042, RF-046, RF-047 | RNF-004 | — | — |
| Roles y Moderación (transversal) | US1–US13 | RF-054, RF-055 | — | — | — |
| Permisos del Dispositivo | US13 | RF-043–RF-045 | RNF-001 | Denegación permanente | — |
| Seguridad de Credenciales (transversal) | US1, US3 | RF-004, RF-008 | RNF-006, RNF-007 | — | — |

## 3. Checklist de Verificación por Criterio

### [x] Completitud de Historias de Usuario y Requisitos

- [x] CHK001 - ¿Las 13 historias de usuario (US1–US13) tienen escenarios de aceptación en formato Given-When-Then? [Completeness, Spec §User Scenarios]
- [x] CHK002 - ¿Los requisitos funcionales cubren los 5 dominios declarados sin huecos de numeración entre RF-001 y RF-059? [Completeness, Spec §Requirements]
- [x] CHK003 - ¿Está formalizado como entidad/valor de dominio el estado de "permiso de dispositivo", dado que US13 y RF-043–RF-045 dependen de él? [Gap, Spec §Key Entities] — **Resuelto 2026-09-20**: se agregó la entidad "Permiso de Dispositivo" (3 estados) a Key Entities.
- [x] CHK004 - ¿Los Edge Cases cubren al menos un escenario por cada dependencia externa crítica (EcoGPT, podómetro, GPS/Puntos Verdes, permisos)? [Coverage, Spec §Edge Cases]
- [x] CHK005 - ¿Existen requisitos de reintento para el flujo de verificación con IA ante un resultado no aprobado? [Completeness, Spec §RF-030]
- [x] CHK026 - ¿Existe un requisito de performance (límite de tiempo de respuesta) para la dependencia externa crítica EcoGPT? [Completeness, Spec §RNF-008] — **Resuelto 2026-09-20**: antes no había ningún límite definido (SC-002 excluía expresamente el tiempo de red); se agregó RNF-008 (timeout de 30 s), el Edge Case "Timeout de EcoGPT" y SC-008.

### Claridad y Unicidad *(dimensión de apoyo — sin gate propio, ver hallazgos en Resumen Ejecutivo)*

- [x] CHK006 - ¿Es "radio razonable" (RF-020) un término cuantificado, dado que el Edge Case asociado y RF-053 ya lo definen en 3 km? [Ambiguity/Consistency, Spec §RF-020] — **Resuelto 2026-09-20**: RF-020 ahora remite a RF-053.
- [x] CHK007 - ¿Está cuantificada la "política mínima de seguridad" exigida a la contraseña en RF-004 (longitud mínima, composición)? [Ambiguity, Spec §RF-004] — **Resuelto 2026-09-20**: mínimo 8 caracteres con letras y números.
- [x] CHK008 - ¿Está cuantificada la equivalencia de conversión de pasos a metros? [Clarity, Spec §RF-048]
- [x] CHK009 - ¿Es inequívoco si el "bloque completo de 100 metros" (RF-049) se mide en metros acumulados (pasos × 0,75, redondeados) o en bloques fijos de 133 pasos, dado que 133 × 0,75 = 99,75 m ≠ 100 m exactos? [Ambiguity, Spec §RF-048–RF-049] — **Resuelto 2026-09-20**: el bloque se cuenta sobre pasos (133 pasos = 1 bloque); la conversión a metros es solo informativa.
- [x] CHK010 - ¿Están cuantificados los umbrales numéricos de cada nivel de usuario? [Clarity, Spec §RF-046]
- [x] CHK011 - ¿Está cuantificado el radio de Puntos Verdes en el requisito que lo define (RF-053)? [Clarity, Spec §RF-053]

### Consistencia Interna *(dimensión de apoyo — sin gate propio, ver hallazgos en Resumen Ejecutivo)*

- [x] CHK012 - ¿Es consistente el criterio de radio de Puntos Verdes entre RF-018–RF-020, el Edge Case correspondiente y RF-053? [Consistency, Spec §RF-020 vs §RF-053] — **Resuelto 2026-09-20**: mismo fix que CHK006.
- [x] CHK013 - ¿Es consistente el tratamiento de "Indeterminado" entre US9, RF-028–RF-030 y RF-050–RF-051? [Consistency, Spec §US9, §RF-050]
- [x] CHK014 - ¿Es consistente la regla de topes diarios entre US10, RF-032–RF-035 y RF-057? [Consistency, Spec §US10, §RF-057]
- [x] CHK015 - ¿Es consistente el mecanismo de protección de contraseñas entre RNF-006, RNF-007, Assumptions y el Registro de Ambigüedades? [Consistency, Spec §RNF-006–007]
- [x] CHK016 - ¿Es consistente el modelo de niveles (no descendente, no consume puntos) entre US11, RF-040, RF-046 y RF-047? [Consistency, Spec §US11, §RF-046–047]
- [x] CHK025 - ¿RF-034 y RF-056 describen la regla de disponibilidad/tope diario sin duplicarse entre sí? [Consistency, Spec §RF-034, §RF-056] — **Resuelto 2026-09-20**: antes RF-056 repetía íntegramente la cláusula de tope diario de RF-034; ahora remite a RF-034/RF-035 y solo aporta la info de "365 días sin horario".

### [x] Métricas y Criterios de Éxito Medibles

- [x] CHK017 - ¿Son medibles los Success Criteria (SC-001 a SC-008) sin referencias a tecnología? [Measurability, Spec §Success Criteria]
- [x] CHK018 - ¿Cada SC-XXX es verificable de forma independiente de la implementación? [Measurability, Spec §Success Criteria]
- [x] CHK019 - ¿Es "3 días consecutivos" (US11, escenario 2) un valor testeable sin margen de interpretación? [Clarity, Spec §US11] — **Resuelto 2026-09-20**: se reemplazó "varios" por "3 días consecutivos".

### [x] Tratamiento de Excepciones y Casos Borde

- [x] CHK020 - ¿Existen requisitos para el escenario de fotografías duplicadas (fraude/reuso)? [Coverage, Spec §RF-058–RF-059, §Edge Cases]
- [x] CHK021 - ¿Existen requisitos de comportamiento ante denegación permanente de un permiso? [Coverage, Spec §Edge Cases]
- [x] CHK022 - ¿Existen requisitos para el escenario de correo ya registrado durante el alta? [Coverage, Spec §US1]

### [x] Trazabilidad de Ambigüedades Resueltas

- [x] CHK023 - ¿Cada ambigüedad detectada (`AMBIGÜEDAD-001` a `005`) tiene una decisión final y RF/RNF derivados trazables? [Traceability, Spec §Registro de Ambigüedades]
- [x] CHK024 - ¿La sección `## Clarifications` referencia cada ambigüedad resuelta con su pregunta y respuesta? [Traceability, Spec §Clarifications]

**Resultado**: 26/26 ítems pasan. 0 fallas abiertas.

## 4. Recomendaciones de Transición a `/speckit.plan`

**La especificación está lista para avanzar a `/speckit.plan`.** No quedan
hallazgos abiertos de completitud, claridad ni consistencia en `spec.md`.

**Historial de hallazgos, todos resueltos**:

- *1ª pasada*: RF-020/RF-018 desactualizados ("radio razonable" vs 3 km),
  RF-034/RF-056 duplicados, ausencia de requisito de performance para
  EcoGPT (RNF-008 + SC-008 agregados).
- *2ª pasada*: RF-004 (política de contraseña cuantificada), RF-048/RF-049
  (criterio de bloque de puntaje sobre pasos, no metros), Key Entities
  (entidad "Permiso de Dispositivo" agregada), US11 ("3 días consecutivos"
  en vez de "varios").

No hay más tareas de calidad de requisitos pendientes antes de planificar.
