# Research: Rediseño de la UI de Inicio y Actividad Individual

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Fecha**: 2026-11-01

Cada sección sigue el formato Decisión / Justificación / Alternativas. No quedan marcadores
`NEEDS CLARIFICATION`: las dos ambigüedades que el spec dejó abiertas (§5 y §6) se resuelven aquí
como decisiones de plan y se señalan al usuario por si desea otra opción.

## 1. Estado diario derivado en Presentación

**Decisión**: calcular el estado en una función pura `calcularEstadoDiario(actividades, registros, hoy)`
en `presentation/estadodiario/EstadoDiario.kt`, alimentada por `RegistroVerificacionRepository.todos(usuarioId)`.
Un `CargadorEstadoDiario` (`@Inject`) obtiene el usuario de `SesionUsuario`, llama a `todos()` y
delega en la función. Reglas (del spec, Clarifications 2026-11-01):

- Registro "del día" = `fecha == LocalDate.now()` (misma referencia que `AplicarTopeDiario`) y
  `resultado == APROBADO` (equivale a `consumeTopeDiario()`).
- Categoría **Hecha** = al menos un registro del día en esa categoría.
- Categoría **tope alcanzado** = `topeDiario != null && aprobadosHoy >= topeDiario`, con
  `topeDiario = EstrategiaDePuntaje.para(categoria).topeDiario` (Reciclar 1, Reutilizar 5, Caminar `null`).
- Estado de actividad, con esta precedencia: (1) hay un registro del día con su `actividadId` →
  `COMPLETADA_HOY`; (2) su categoría tiene tope alcanzado → `NO_DISPONIBLE_HOY`; (3) en otro caso →
  `PARA_HACER`.

**Justificación**:
- `RegistroVerificacionRepository.todos()` ya existe y trae `actividadId`, `categoria`, `fecha`,
  `resultado` (`RegistroVerificacion`). No hace falta ningún método nuevo, así que se cumple
  `RF-IA-001` y `SC-IA-009` (cero cambios en `domain/`, `application/`, `infrastructure/`).
- `Actividad.estaDisponibleHoy()` devuelve siempre `true` (RF-056, spec 001) y existe un test que
  depende de eso; cambiarlo sería tocar Dominio.
- `AplicarTopeDiario.verificarDisponibilidad()` (Aplicación) responde por categoría con una consulta
  de conteo al servidor, pero no por actividad, y requeriría 3 consultas por pantalla. Con `todos()` se
  hace una sola lectura y se obtienen los dos niveles.
- Los topes no se duplican: se leen de `EstrategiaDePuntaje`, la única fuente (Principio III).
- Mismo patrón que `PerfilViewModel` / `HistorialViewModel`, que ya leen `todos()` y se refrescan en
  `ON_RESUME`; `HomeScreen` ya llama a `viewModel.refrescar()` en `ON_RESUME`.

**Alternativas descartadas**:
- Agregar `contarAprobadosDelDiaPorActividad()` al puerto: viola `RF-IA-001`; requeriría enmienda.
- Cambiar `estaDisponibleHoy()` para recibir contexto: toca Dominio y rompe sus tests.
- Una consulta de conteo por categoría (`verificarDisponibilidad`): 3 viajes al servidor y no resuelve
  el nivel por actividad.

**Casos borde y riesgos**:
- `todos()` crece con el historial (unos pocos registros por día). Aceptable para el MVP; si el
  historial creciera, el plan de mitigación es filtrar por `fecha` en la consulta, lo que sí sería un
  cambio de Infraestructura y se trataría en otra especificación.
- Los `ActividadId` son UUID aleatorios por instalación (`ActividadId.nuevo()`): tras reinstalar, un
  registro viejo referencia un id inexistente. La coincidencia por id falla (no hay `COMPLETADA_HOY`),
  pero la categoría sigue "Hecha" y su tope sigue calculándose bien. Por eso la precedencia (2) se evalúa
  también cuando falla (1).
- Caminar (sin tope) nunca llega a `NO_DISPONIBLE_HOY`; si su actividad se completó hoy, muestra
  `COMPLETADA_HOY` y las otras de la categoría siguen disponibles (spec, edge case "Misma actividad dos veces").
- Registros rechazados o indeterminados no cuentan.
- Falla de red al leer `todos()`: `estadoDiario = null`; Inicio oculta "Tu progreso de hoy" y las
  tarjetas muestran `PARA_HACER`; el detalle usa `PARA_HACER`. Sin mensaje de error propio (el flujo
  de verificación ya valida el tope contra el servidor antes de permitir el formulario) y sin valores
  inventados.

## 2. Total de categorías y orden

**Decisión**: el total del progreso es `CategoriaActividad.entries.size`; el orden de columnas y de
tarjetas sigue el orden fijo Reciclar, Reutilizar, Caminar ya usado en Educación (`ordenSeccionesEducacion`,
`CategoriaUi.kt`). Si el dominio agrega una categoría, `when` exhaustivo en `CategoriaUi.kt` fuerza al
compilador a pedir su logo y color; el progreso la incluye sin otro cambio.

**Justificación**: `RF-IA-019b` prohíbe un total propio de la UI. Hoy son 3.

**Alternativas**: contar solo categorías con actividades en el catálogo (descartado: el spec fija que
el total es la cantidad de categorías del dominio).

## 3. Selección de la tarjeta por categoría (`RF-IA-006a`)

**Decisión**: función pura `seleccionarActividadesDeHoy(actividades, estado)` que, por categoría en el
orden fijo, devuelve la primera actividad (orden del catálogo) con estado `PARA_HACER`; si no hay,
la primera `COMPLETADA_HOY`; si no hay, la primera del catálogo. Categorías sin actividades se omiten.

**Justificación**: es exactamente la regla clarificada y se puede probar sin Android.

**Alternativas**: mostrar todas las actividades (descartado en Clarifications); mostrar solo las
`PARA_HACER` (descartado: la categoría hecha quedaría sin tarjeta).

## 4. Pasos y tip del detalle

**Decisión**:
- `PasosActividadParser.parsear(texto): List<String>`: divide por saltos de línea, descarta líneas
  vacías y quita el prefijo numérico `^\s*\d+[.)]\s*`. Sin forzar tres pasos (Clarifications). Si el
  texto no tiene líneas, `C-24` no se muestra y "Cómo se registra" omite la tarjeta.
- Tip: `Actividad.resultadoEsperado` con el rótulo "Qué tiene que verse en la foto", en `TarjetaTip`
  (la de lecciones, `ComponentesLeccion.kt`, con `ic_bombilla`). Si está vacío, no se muestra.

**Justificación**: los datos ya están en el dominio como texto (`ActividadRepositoryImpl` guarda
`pasosASeguir` con formato `"1. ...\n2. ..."`); no hay campo de tip. Es lo definido en Clarifications.

**Alternativas**: agregar campo `tip` a `Actividad` (descartado, viola `RF-IA-001`).

## 5. Destino del chevron de la tarjeta de puntos (ambigüedad abierta del spec)

**Decisión de plan**: se conserva el destino actual, **Perfil** (`onVerPerfil`), sin cambio de
comportamiento.

**Justificación**: hoy la tarjeta de puntos ya navega a Perfil (`HomeScreen`, `TarjetaPuntos(onClick =
onVerPerfil)`); el rediseño solo cambia su aspecto. Es la opción que no introduce un destino nuevo y es
reversible con un cambio de una línea. **Se señala al usuario**: si prefiere otro destino (por ejemplo
Recompensas), solo cambia el `onClick` en `EcoSmartNavHost`.

**Alternativas**: Recompensas (descartado sin evidencia de que el tablero lo indique); sin acción (el
chevron pasaría a ser engañoso).

## 6. Sexto ícono de la barra inferior (confirmado por el usuario)

**Decisión de plan**: se aplica de forma **global** en el componente único `BarraInferiorEcoSmart`
(`ComponentesEcoSmart.kt`): se agrega `PestanaInferior.PUNTOS_VERDES` como sexto elemento (a la
derecha de Perfil) y un parámetro `onPuntosVerdes`. `BarraInferior(navController, seleccion)` en
`EcoSmartNavHost.kt` navega con `irAPestana(..., PuntosVerdes.ruta)`.

**Justificación**: hay un solo punto de uso (`EcoSmartNavHost.BarraInferior`) y un solo componente; que
la barra no sea igual en todas las pantallas sería una regresión de coherencia (`RF-UI-002b`). Seis
elementos de 48 dp caben en 360 dp (288 dp). `PuntosVerdesScreen` ya declara el parámetro
`barraInferior` (con valor por defecto vacío) y usa `Scaffold`; `EcoSmartNavHost` no se lo pasa hoy. Por pedido
del usuario (2026-11-01) la barra de seis íconos DEBE verse también en las demás pestañas, así que se le pasa
`BarraInferior(navController, PestanaInferior.PUNTOS_VERDES)` y el ícono queda resaltado allí. Las pantallas
que ya reciben la barra (Actividades, Recompensas, Educación y su detalle, detalle de actividad, Caminata,
Perfil) la heredan sin cambios propios porque todas usan la misma función `BarraInferior`.
`BarraInferiorEcoSmartTest` se ajusta (hoy afirma que "Puntos Verdes" no está en la barra).
**Confirmado por el usuario** como cambio global autorizado por la enmienda v2.4.0.

**Alternativas**: sexto ícono solo en Inicio y el detalle (descartado: dos barras distintas);
dejar el botón flotante (contradice `RF-IA-009`).

## 7. Logos de categoría y cámara

**Decisión**: tres VectorDrawable `logo_categoria_{reciclar,reutilizar,caminar}.xml` (viewport único,
escalables a 44/76/120 dp) y `ic_camara.xml`. Composable `LogoCategoria(categoria, tamaño, atenuado,
conTilde, contentDescription)` mapea con `when` exhaustivo. "Atenuado" = alfa reducido de forma que el
texto asociado ("Pendiente") siga cumpliendo contraste; el logo se marca decorativo porque el texto
adyacente lo describe.

**Justificación**: sin Coil/Glide ni íconos extendidos (`libs.versions.toml` no incluye
`material-icons-extended`); consistente con las ilustraciones del spec 004. `iconoCategoria()` se deja
para Actividades (listado), fuera de alcance; esa pantalla conservará los íconos viejos y la
inconsistencia visual entre pantallas queda documentada como consecuencia aceptada del alcance.

**Alternativas**: reutilizar `ic_categoria_*` (no cumple el pedido de nuevos logos); PNG (descartado:
rasterizados no escalan a tres tamaños).

## 8. Origen de pestaña al abrir una lección desde la actividad

**Decisión**: `ContenidoEducativoDetalle` ya decide el resaltado con `previousBackStackEntry == Home`
(INICIO) o, en otro caso, EDUCACION. Se amplía: si el destino anterior es `ActividadDetalle`, se
resalta ACTIVIDADES. Cambio de una condición en `EcoSmartNavHost`.

**Justificación**: la persona viene del detalle de una actividad; resaltar Educación sería confuso.

## 9. Estrategia de pruebas (Principio VIII)

- **JUnit 5 (JVM)**: `EstadoDiarioTest` (categoría Hecha con registro aprobado; rechazado no cuenta;
  Reciclar tras 1 → `NO_DISPONIBLE_HOY` para las otras; Reutilizar con 1–4 sigue disponible y tras 5
  `NO_DISPONIBLE_HOY`; Caminar nunca; registro de otro día no cuenta; id inexistente no rompe la
  categoría; total = `CategoriaActividad.entries.size`), `seleccionarActividadesDeHoy` (una por
  categoría, regla de fallback, categoría sin actividades) y `PasosActividadParserTest` (numeración,
  líneas vacías, sin números, texto vacío).
- **Compose UI Test**: `ComponentesInicioActividadTest` (cada estado de `C-23`, `C-20` con 3 y con
  N categorías, `C-19` con tilde/atenuado, semántica), `BarraInferiorEcoSmartTest` (6 ítems y callbacks),
  `HomeScreenTest` (sin "N/10", sin "0 / 1", sin "Ver todas" del encabezado, botón "Ver todas las
  actividades", progreso "N/total"), `ActividadDetalleScreenTest` (estados para hacer y completada, sin
  "Realizarla").
- Accesibilidad: verificación de tamaños mínimos 48×48 y texto al 130% con `fontScale` en un test de
  pantalla.
- Fuera de automatización: contraste calculado en `quickstart.md` y comparación visual con los tableros.

## 10. Código a retirar

Candidatos a quedar sin uso: `IndicadorNoMedible`, `TarjetaInformativaDetalle` y
`TarjetaTipDetalle` en la rama no medible, `il_paisaje_colinas.xml` (si Caminar no lo usa), el bloque
"Tu progreso general" y el botón flotante de `HomeScreen`. `AnilloProgreso` se conserva (lo usa
Caminar). Su retiro y el de los tests asociados se decide en `/speckit.tasks`, tras verificar usos
con una búsqueda de referencias.

## 11. Trazabilidad requisito → decisión

| Requisito | Decisión |
|---|---|
| RF-IA-001, SC-IA-009 | §1 (sin tocar capas inferiores) |
| RF-IA-002, 003, 004, 007, 008, 010 | Reescritura de `HomeScreen`; chevron §5 |
| RF-IA-005, RF-IA-019b | §1, §2 |
| RF-IA-006, 006a | §1, §3 |
| RF-IA-009 | §6 |
| RF-IA-011 a 014, 018 | Rama no medible de `ActividadDetalleScreen`; §1 |
| RF-IA-015, 016 | §4 |
| RF-IA-017 | `CatalogoLecciones` (spec 004) + §8 |
| RF-IA-019 | `EstadoDiarioUi` y `HomeUiState` sin constantes de ejemplo |
| RF-IA-020, 021 | §7 y `data-model.md` §5 |
| RF-IA-022, 023 | §9, `quickstart.md` |
