# Data Model: Reconstrucción Visual de la Interfaz de Usuario

Este módulo **no agrega ni modifica entidades de dominio** (`RF-UI-001`). Lo que sigue
no es un modelo de negocio: es (1) el modelo de los tokens de diseño que vive en la capa
de Presentación, y (2) el único dato nuevo de este spec — la muestra estática de
Recompensas —, que tampoco es una entidad de dominio.

## 1. Tokens de diseño (Presentación — `theme/`)

No son clases de dominio ni se persisten; son constantes/objetos de Kotlin que el resto
de la UI consume por nombre, derivados 1:1 de `especificacion-ui-app-eco.md` §2.

| Grupo | Origen (doc §) | Dónde vive | Ejemplos de campos |
|---|---|---|---|
| Paleta de color | §2.1 | `theme/Theme.kt` (alias sobre `Color` ya existentes) | `color.bg.screen` (#FBF8F0), `color.green.700` (#1F5E34), `color.state.progress`, `color.track` |
| Tipografía | §2.2 | `theme/Theme.kt` (`TextStyle` con familia Nunito ya cargada) | `type.display` (40pt Bold), `type.points` (32pt Bold), `type.tab` (10pt Medium) |
| Espaciado/radios/elevación | §2.3 | `theme/Tokens.kt` (nuevo) | `space.screen-x` (16dp), `space.gap-card` (12dp), `radius.card` (16dp), `radius.pill` (999/pill), `shadow.card` |
| Iconografía | §2.4 | `res/drawable` + `theme/ComponentesEcoSmart.kt` | Iconos lineales 24×24pt, iconos de actividad sobre círculo de 36–40pt |

**Validación/regla de uso** (de `RF-UI-004`): ninguna pantalla debe declarar un valor de
color, tipografía, espaciado o radio que no provenga de esta tabla. Esto se revisa por
inspección visual (`SC-UI-001`), no por un validador automático (fuera de alcance).

## 2. Componentes globales (Presentación — `theme/ComponentesEcoSmart.kt`)

Cada fila es un composable reutilizado por ≥2 pantallas (`RF-UI-005`); "Estados" lista
las variantes visuales que debe soportar.

| ID | Composable (nombre propuesto) | Estados/variantes | Pantallas que lo usan |
|---|---|---|---|
| C-01 | *(dibujado por el SO, no aplica)* | — | Todas |
| C-02 | `BarraInferiorEcoSmart` (`PestanaInferior`: 6 valores, incluye `PUNTOS_VERDES` — `RF-UI-002c`) | seleccionado/no seleccionado | Inicio, Actividades, Recompensas, Educación, Puntos Verdes, Perfil |
| C-03 | `EncabezadoEcoSmart` | con/sin avatar, con/sin flecha de volver | Todas |
| C-04 | `ChipFiltro`/`SegmentoEcoSmart` (nuevo) | seleccionado, no seleccionado, deshabilitado | Actividades, Recompensas, Educación |
| C-05 | `BarraProgresoLineal` (nuevo) | 0–100% | Inicio (progreso general), tarjeta de actividad medible |
| C-06 | `TarjetaActividad` (extiende `TarjetaEcoSmart`) | compacta/completa × idle/in_progress/completed | Inicio (carrusel), Actividades (grilla) |
| C-07 | `BotonPrincipal` (nuevo) | reposo, presionado, deshabilitado | Inicio, Caminar (estado de seguimiento) |
| C-08 | `EnlaceSeccion` (nuevo) | — | Inicio, Educación |
| C-09 | `TarjetaVideo` (nuevo) | compacta (150×100), ancha (full×100) | Inicio, Educación |
| C-10 | `TarjetaRecompensa` (nuevo) | costo habilitado/deshabilitado (visual, no funcional) | Recompensas |
| C-11 | `TarjetaPuntos` (nuevo) | — | Inicio |

**Puntos Verdes y `C-02`** (`RF-UI-002c`, research.md §9): "Puntos Verdes" usa `C-02`
(la barra inferior) igual que las otras 5 pantallas, pero `PuntosVerdesScreen` en sí
misma **no** adopta ningún otro componente (`C-03` a `C-11`) ni los tokens de la
sección 1: conserva su implementación visual actual, fuera del alcance de rediseño de
este spec (`RF-UI-002`).

## 3. Muestra estática de Recompensas (Presentación — `rewards/RecompensaEjemplo.kt`)

Único dato nuevo de este spec. **No es una entidad de dominio**: no tiene repositorio,
no se persiste, no participa de ninguna regla de negocio (puntaje, tope diario, etc.),
y no se expone fuera del paquete `presentation/rewards/` (`RF-UI-003b`).

```kotlin
// Vive únicamente en presentation/rewards/ — no en domain/model/
data class RecompensaEjemplo(
    val titulo: String,
    val categoria: CategoriaRecompensaEjemplo,
    val costoPuntos: Int,
    val imagenDrawableRes: Int, // recurso local, ver research.md §1
)

enum class CategoriaRecompensaEjemplo { TODOS, COMIDA, DESCUENTOS, PRODUCTOS }
```

| Campo | Origen | Notas |
|---|---|---|
| `titulo` | `especificacion-ui-app-eco.md` §7.2 | Las 6 recompensas de ejemplo del documento, con las erratas de copy ya corregidas (ver `spec.md` → Assumptions) |
| `categoria` | §7.2 "Categoría sugerida" | Enum cerrado de solo 4 valores, igual al de los chips de filtro (`C-04`) |
| `costoPuntos` | §7.2 "Costo" | Entero; se formatea con punto como separador de miles (`RF-UI-012`) |
| `imagenDrawableRes` | — | Recurso drawable local (placeholder), no URL (ver research.md §1) |

**Relación con `Usuario.puntosHistoricos`**: ninguna. El costo se muestra tal cual,
nunca se compara contra el saldo real del usuario para habilitar/deshabilitar
interacción (esa regla —"recompensa que cuesta más que el saldo"— está marcada "a
confirmar" en el documento de diseño §7.3 y por lo tanto fuera de alcance, `RF-UI-003`).

## 4. Estado de progreso de actividad (ya existente — solo lectura para la UI)

No es un dato nuevo; se documenta para dejar explícito cómo `C-06`/`S-03` derivan su
estado visual sin inventar un campo nuevo (`RF-UI-006`):

| Estado visual | Condición (ya calculada por el dominio existente) | Fuente |
|---|---|---|
| Sin iniciar (idle) | No existe `RegistroVerificacion` aprobado hoy para esa actividad, y (para Caminar) no hay seguimiento iniciado | `HistorialViewModel`/`ActividadDetalleViewModel`, `CaminataEnCursoStore` |
| En curso (in_progress) | Caminar: seguimiento iniciado con `pasosRegistrados < metaPasos` | `VerificacionCaminataUiState` |
| Completada (completed) | Existe `RegistroVerificacion` con `resultado = APROBADO` para esa actividad hoy, o (Caminar) `pasosRegistrados ≥ metaPasos` | `RegistroVerificacion.consumeTopeDiario()` ya existente |
