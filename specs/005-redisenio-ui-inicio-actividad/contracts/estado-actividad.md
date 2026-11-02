# Contrato: Estado diario de categorías y actividades

**Spec**: [../spec.md](../spec.md) | **Data model**: [../data-model.md](../data-model.md)

Contrato interno de Presentación entre `CargadorEstadoDiario` / `calcularEstadoDiario` y las
pantallas Inicio y Actividad individual. Es el equivalente de un contrato de API para este módulo.

## Entradas

| Entrada | Tipo | Origen |
|---|---|---|
| `registros` | `List<RegistroVerificacion>` | `RegistroVerificacionRepository.todos(usuarioId)` |
| `actividades` | `List<Actividad>` | `ActividadRepository` (ya cargado en `HomeViewModel`) |
| `hoy` | `LocalDate` | `LocalDate.now()` (mismo criterio que `AplicarTopeDiario`) |

## Salida

`EstadoDiarioUi` (ver data-model §1). Función pura, sin Android, sin efectos.

## Reglas

1. `aprobadosHoy(categoria)` = registros con `categoria`, `fecha == hoy` y `resultado == APROBADO`.
2. `EstadoCategoriaUi.hecha = aprobadosHoy(categoria) >= 1`.
3. `topeAlcanzado = tope != null && aprobadosHoy(categoria) >= tope`, con
   `tope = EstrategiaDePuntaje.para(categoria).topeDiario`.
4. `EstadoDiarioUi.categorias` incluye **todas** las `CategoriaActividad.entries`, estén o no en el
   catálogo, en orden Reciclar, Reutilizar, Caminar.
5. Estado de actividad con precedencia: `COMPLETADA_HOY` (registro aprobado de hoy con su
   `actividadId`) > `NO_DISPONIBLE_HOY` (`topeAlcanzado` de su categoría) > `PARA_HACER`.
6. Un registro con `actividadId` que no existe en `actividades` cuenta para la categoría (reglas 1–3)
   pero no marca a ninguna actividad.
7. Registros `RECHAZADO`/`INDETERMINADO` y de otros días no cuentan.

## Ejemplos

| Registros aprobados de hoy | Categorías hechas | Estados |
|---|---|---|
| ninguno | 0/3 | todas `PARA_HACER` |
| 1 de Reciclar (actividad A) | 1/3 | A `COMPLETADA_HOY`; otras de Reciclar `NO_DISPONIBLE_HOY`; resto `PARA_HACER` |
| 2 de Reutilizar (actividades B y C) | 1/3 | B y C `COMPLETADA_HOY`; otras de Reutilizar `PARA_HACER` (tope 5) |
| 5 de Reutilizar | 1/3 | las no hechas de Reutilizar `NO_DISPONIBLE_HOY` |
| 1 de Caminar (actividad D) | 1/3 | D `COMPLETADA_HOY`; otras de Caminar `PARA_HACER` (sin tope) |
| 1 de Reciclar con id inexistente | 1/3 | todas las de Reciclar `NO_DISPONIBLE_HOY` |

## Selección de tarjetas de Inicio

`seleccionarActividadesDeHoy(actividadesPorCategoria, estado)`: por categoría en orden fijo → primera
`PARA_HACER`; si no hay, primera `COMPLETADA_HOY`; si no hay, primera del catálogo; sin actividades,
se omite. Máximo una tarjeta por categoría.

## Botón por estado (`C-23`)

| Estado | Inicio (tarjeta) | Detalle |
|---|---|---|
| `PARA_HACER` | "Hacerla ahora" → abre el detalle | "¡Hacela ahora y sumá N puntos!" (cámara) → captura de foto |
| `COMPLETADA_HOY` | "Completada hoy" (sin acción de hacer) | Mensaje "¡Listo por hoy! Volvé mañana para sumar otra vez." |
| `NO_DISPONIBLE_HOY` | "Volvé mañana" (sin acción de hacer) | Mensaje equivalente al de completada, sin insignia de tilde |

Tocar una tarjeta en `COMPLETADA_HOY` o `NO_DISPONIBLE_HOY` sigue abriendo el detalle (solo lectura);
la captura de foto no se ofrece en esos estados.

## Errores

Si `todos()` falla: `estadoDiario = null` en Inicio (se oculta el bloque "Tu progreso de hoy" y las
tarjetas muestran `PARA_HACER`); en el detalle `estado = PARA_HACER`. El flujo de verificación valida
el tope contra el servidor (`AplicarTopeDiario`), por lo que no se puede superar el tope por un estado
local desactualizado.
