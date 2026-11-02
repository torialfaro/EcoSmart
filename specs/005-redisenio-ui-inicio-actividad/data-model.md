# Data Model (Presentación): Inicio y Actividad Individual

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Research**: [research.md](./research.md)

No hay entidades de dominio, tablas, colecciones ni endpoints nuevos. Todo lo de abajo son modelos
de **Presentación** (`presentation/estadodiario/`, `home/`, `activitydetail/`) o recursos de diseño.

## 1. Modelos de estado diario

```text
enum EstadoActividadUi { PARA_HACER, COMPLETADA_HOY, NO_DISPONIBLE_HOY }

EstadoCategoriaUi
  - categoria: CategoriaActividad            # enum de dominio, sin valores nuevos
  - hecha: Boolean                           # >= 1 verificación APROBADA hoy en la categoría
  - topeAlcanzado: Boolean                   # topeDiario != null && aprobadosHoy >= topeDiario

EstadoDiarioUi
  - categorias: List<EstadoCategoriaUi>      # una por CategoriaActividad.entries, en orden fijo
  - estadoPorActividad: Map<ActividadId, EstadoActividadUi>
  - total: Int                               # = categorias.size   (RF-IA-019b)
  - hechas: Int                              # = categorias.count { it.hecha }
```

**Reglas** (ver `contracts/estado-actividad.md`):

| Condición (en orden de precedencia) | Estado de la actividad |
|---|---|
| Existe registro APROBADO de hoy con su `actividadId` | `COMPLETADA_HOY` |
| Su categoría tiene `topeAlcanzado` | `NO_DISPONIBLE_HOY` |
| Otro caso | `PARA_HACER` |

**Fuentes (sin duplicar reglas)**: `RegistroVerificacion` (`actividadId`, `categoria`, `fecha`,
`resultado`) y `EstrategiaDePuntaje.para(categoria).topeDiario`.

## 2. Selección de tarjetas de Inicio

```text
ActividadInicioUi
  - actividad: Actividad                     # id, categoria, descripcionCorta, puntosBase …
  - estado: EstadoActividadUi
```

`seleccionarActividadesDeHoy(actividades: List<Actividad>, estado: EstadoDiarioUi) → List<ActividadInicioUi>`
(actividades en el orden del catálogo):
por categoría en orden fijo, la primera `PARA_HACER`; si no hay, la primera `COMPLETADA_HOY`; si no
hay, la primera del catálogo. Categorías sin actividades se omiten.

## 3. Estados de pantalla

**`HomeUiState`** (cambios sobre el actual):

| Campo | Cambio |
|---|---|
| `nombreUsuario`, `puntosHistoricos` | Se conservan; vienen de `UsuarioRepository` |
| `actividadesPorCategoria`, `todasLasActividades` | Se conservan; `todasLasActividades` ya no alimenta un contador |
| `pasosHoy` | Se conserva (Caminar medible) |
| `estadoDiario: EstadoDiarioUi?` | **Nuevo**. `null` mientras carga: no se muestra "0/3" ni columnas inventadas |
| `cargando` | Se conserva |

`ActividadItemUi` (con `progresoActual`/`meta` "0/1") se reemplaza por `ActividadInicioUi`.
Sin valores por defecto de ejemplo en ningún campo (RF-IA-019).

**`ActividadDetalleUiState`** (cambios):

| Campo | Cambio |
|---|---|
| `actividad`, `cargando`, `pasosHoy` | Se conservan (Caminar usa `pasosHoy`) |
| `estado: EstadoActividadUi` | **Nuevo**; por defecto `PARA_HACER` mientras carga |
| `pasos: List<String>` | **Nuevo**; `PasosActividadParser.parsear(actividad.pasosASeguir)` |
| `tip: String?` | **Nuevo**; `actividad.resultadoEsperado` o `null` si vacío |
| `leccionRelacionadaId: String?` | **Nuevo**; primera lección de la categoría (`CatalogoLecciones`), `null` si no hay |

## 4. Mapeo categoría → recursos (`CategoriaUi.kt`, `when` exhaustivo)

| Categoría | Logo (C-19) | Degradé de panel | Chip |
|---|---|---|---|
| Reciclar | `logo_categoria_reciclar` | `#E6F2DF` → `#CFE8C6` | Existente |
| Reutilizar | `logo_categoria_reutilizar` | `#FFF1D6` → `#FFE2A8` | Existente |
| Caminar | `logo_categoria_caminar` | `#DFF0E6` → `#BFE0CC` | Existente |

## 5. Tokens nuevos (RF-IA-021; solo aditivos en `Tokens.kt` / `Theme.kt`)

| Token | Valor |
|---|---|
| Panel Reciclar (inicio/fin) | `#E6F2DF`, `#CFE8C6` |
| Panel Reutilizar (inicio/fin) | `#FFF1D6`, `#FFE2A8` |
| Panel Caminar (inicio/fin) | `#DFF0E6`, `#BFE0CC` |
| Logos | `#3E8E4A`, `#1F5E34`, `#7DB574`, `#E9A23B` |
| Radios | 24 dp, 28 dp |
| Sombra de tarjeta | suave (elevación baja; sin animación) |

Los demás (verdes del encabezado, ámbar del tip, radios previos) se reutilizan de spec 003/004.

## 6. Trazabilidad componentes ↔ requisitos

| Componente | Archivo | Requisitos |
|---|---|---|
| `C-19 LogoCategoria` (44/76/120, atenuado, tilde) | `ComponentesInicioActividad.kt` | RF-IA-005, 006, 012, 014, 020 |
| `C-20 TarjetaProgresoDiario` | idem | RF-IA-005 |
| `C-21 TarjetaActividadConBoton` | idem | RF-IA-006 |
| `C-22 PanelActividad` | idem | RF-IA-012, 014 |
| `C-23 BotonLlamadoAccion` (3 estados) | idem | RF-IA-006, 013, 014 |
| `C-24 PasoNumerado` | idem | RF-IA-015 |
| `C-25 TarjetaEnlaceLeccion` | idem | RF-IA-017 |
| `BarraInferiorEcoSmart` (6 ítems) | `ComponentesEcoSmart.kt` | RF-IA-009 |
| `EncabezadoEcoSmart` (variante degradé, esquinas inferiores, avatar) | idem | RF-IA-003 |
| `TarjetaPuntos` (blanca, superpuesta, logo app) | idem o `ComponentesInicioActividad.kt` | RF-IA-004 |
| `TarjetaTip` (existente, spec 004) | `ComponentesLeccion.kt` | RF-IA-016 |

## 7. Datos que consume cada pantalla (regla RF-IA-019)

| Valor visible | Fuente |
|---|---|
| Nombre, inicial | `UsuarioRepository.buscarPorId` (`nombreUsuario`) |
| Puntos | `UsuarioRepository` (`puntosHistoricos`) |
| Progreso N/total y estado por categoría | `EstadoDiarioUi` |
| Título, categoría, puntos de cada actividad | `Actividad` (`descripcionCorta`, `categoria`, `puntosBase`) |
| Estado del botón | `EstadoActividadUi` |
| Pasos / tip | `Actividad.pasosASeguir` / `Actividad.resultadoEsperado` |
| Lección relacionada | `CatalogoLecciones.obtener()` (primera de la categoría) |
