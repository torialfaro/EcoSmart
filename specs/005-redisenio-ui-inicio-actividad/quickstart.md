# Quickstart: validar el rediseño de Inicio y Actividad individual

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Contrato**: [contracts/estado-actividad.md](./contracts/estado-actividad.md)

Guía de validación, no de implementación. Se ejecuta tras `/speckit.implement`.

## Prerrequisitos

- JDK 21 y Gradle 8.9 (ver memoria del repo): `JAVA_HOME` apuntando al JDK 21; `gradle.bat` de
  `%USERPROFILE%\.gradle\wrapper\dists\gradle-8.9-bin`; usar `--no-daemon`.
- Emulador `Pixel_7` (API 35) o dispositivo API 26+, con una cuenta de prueba.
- Para los escenarios con datos reales: una cuenta con y otra sin verificaciones aprobadas hoy.

## 1. Pruebas automáticas

```powershell
# Unitarias (estado diario, selección, parser de pasos) + compilación de pruebas instrumentadas
gradle :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin --no-daemon
# Lint
gradle :app:ktlintCheck :app:detekt --continue --no-daemon
# Instrumentadas de presentación (necesita emulador/dispositivo)
gradle :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.package=com.ecosmart.presentation" --no-daemon
```

Esperado: todo en verde; las 10 pruebas de integración que requieren emuladores de Firebase no
forman parte de este módulo (ver nota en la memoria del repo).

## 2. Escenarios manuales (Inicio)

| # | Pasos | Resultado esperado | Requisito |
|---|---|---|---|
| I-1 | Abrir Inicio con la cuenta sin actividad hoy | Orden: encabezado verde con saludo y avatar, tarjeta de puntos, "Tu progreso de hoy 0/3", tres tarjetas ("Hacerla ahora"), "Ver todas las actividades" (contorno), "Contenido para vos", barra con Inicio resaltada | RF-IA-002 |
| I-2 | Comparar nombre y puntos con el Perfil | Coinciden con los datos reales (no "Viky", 0, ni 100 de ejemplo) | RF-IA-019, SC-IA-002 |
| I-3 | Buscar elementos eliminados | No hay "Tu progreso general", "N/10", enlace "Ver todas" en el encabezado de actividades, botón flotante del mapa ni "0 / 1" | RF-IA-010, SC-IA-001 |
| I-4 | Aprobar una verificación de Reciclar y volver a Inicio | "1/3"; segmento y columna de Reciclar "Hecha" (logo sin atenuar); tarjeta de Reciclar "Completada hoy" | RF-IA-005, 006 |
| I-5 | Con Reciclar hecha, abrir el detalle de otra actividad de Reciclar | Mensaje "Volvé mañana" (sin botón de captura) | contrato, tabla de botón |
| I-6 | Aprobar 2 de Reutilizar | Reutilizar "Hecha"; otras actividades de Reutilizar siguen "Hacerla ahora" | Clarifications 1 |
| I-7 | Títulos largos y texto al 130% | Todas las tarjetas del mismo alto, título hasta 3 líneas, nada se superpone ni se corta | SC-IA-004, SC-IA-006 |
| I-8 | Tocar la tarjeta de puntos | Abre Perfil (decisión de plan, research §5) | RF-IA-004 |
| I-9 | Tocar el sexto ícono de la barra | Abre Puntos Verdes, que muestra la misma barra con ese ícono resaltado | RF-IA-009 |
| I-11 | Recorrer Actividades, Recompensas, Educación (y una lección), el detalle de una actividad, Caminata y Perfil | Todas muestran la barra de seis íconos, con su pestaña resaltada y navegación correcta desde cada ícono | RF-IA-009 |
| I-10 | Modo avión con la cuenta con datos | Se oculta "Tu progreso de hoy"; las tarjetas muestran "Hacerla ahora"; sin valores inventados | contrato, Errores |

## 3. Escenarios manuales (Actividad individual)

| # | Pasos | Resultado esperado | Requisito |
|---|---|---|---|
| D-1 | Abrir el detalle de una actividad de Reciclar o Reutilizar para hacer | Encabezado (volver 48×48, título, subtítulo con hojita en línea), panel con logo grande + chip + píldora de puntos, botón "¡Hacela ahora y sumá N puntos!" con cámara y nota, "Cómo se registra" con pasos numerados, tip ámbar "Qué tiene que verse en la foto", "Aprendé primero", barra con Actividades resaltada | RF-IA-011 a 018 |
| D-2 | Verificar que N es el puntaje real | Coincide con `puntosBase` de esa actividad | RF-IA-013 |
| D-3 | Tocar el botón principal | Abre la captura de foto (flujo existente) | RF-IA-013 |
| D-4 | Abrir el detalle de una actividad completada hoy | Insignia de tilde en el logo, chip "Completada", mensaje "¡Listo por hoy! …", sin botón | RF-IA-014 |
| D-5 | Tocar "Aprendé primero" | Abre la primera lección de la categoría; al volver, regresa al detalle; barra con Actividades resaltada | RF-IA-017 |
| D-6 | Buscar elementos eliminados | No hay círculo "0 / 1 completada", píldora a ancho completo, botón "Realizarla" ni colinas | SC-IA-001 |
| D-7 | Abrir el detalle de una actividad de Caminar | Conserva su diseño actual (fuera de alcance) | Fuera de alcance |

## 4. Accesibilidad

- TalkBack: cada tarjeta se lee como un elemento ("Reciclar, {título}, {N} puntos, {estado}"); los
  logos decorativos no se leen por separado; los botones anuncian su acción.
- Zonas táctiles >= 48×48 dp (flecha de volver, botones, íconos de la barra con 6 elementos).
- Contraste >= 4,5:1: calcular (herramienta de contraste) texto sobre cada degradé de panel, texto
  blanco sobre verde del encabezado, "Pendiente" atenuado y texto sobre el ámbar del tip; registrar el
  resultado en el PR. Si un par falla, ajustar el tono dentro de los tokens del spec.
- Texto del sistema al 130% en ambas pantallas.
- Sin animaciones ni transiciones (revisión de código y recorrido manual).

### Resultado del cálculo de contraste (T042, WCAG 2.x, mínimo 4,5:1)

| Par (texto / fondo) | Ratio |
|---|---|
| Verde 900 sobre degradés de panel (Reciclar 8,55 / 7,55; Reutilizar 8,86 / 7,87; Caminar 8,36 / 6,95; inicio / fin) | ≥ 6,95 |
| Texto principal sobre el final del degradé de Reutilizar (peor caso) | 11,82 |
| Píldora "+N puntos" (verde 900 sobre blanco) | 9,90 |
| Chip Reciclar / Reutilizar | 6,69 / 5,31 |
| Chip Caminar: verde 500 sobre `#DFF0E6` fallaba (4,31); `ColorChipCaminarTexto` pasó a verde 700 | 6,55 |
| Blanco sobre el verde del encabezado (verde 700 / verde 900) | 7,75 / 9,90 |
| Subtítulo crema sobre el encabezado (verde 700 / verde 900) | 7,17 / 9,15 |
| "Pendiente" sobre tarjeta blanca; "Hecha" | 9,30; 7,75 |
| "Completada" (verde 700 sobre verde 100); "Volvé mañana" | 6,69; 8,08 |
| Nota "Sacás una foto…" sobre el fondo de pantalla | 4,83 |
| Tip: título / texto sobre el ámbar | 8,96 / 7,20 |
| Mensaje de estado; subtítulo de "Aprendé primero" | 8,55; 8,03 |

## 5. Verificación de capas

```powershell
git diff --stat -- app/src/main/kotlin/com/ecosmart/domain app/src/main/kotlin/com/ecosmart/application app/src/main/kotlin/com/ecosmart/infrastructure backend firestore.rules
```

Esperado: sin cambios (SC-IA-009).
