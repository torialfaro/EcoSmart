# Data Model: Persistencia Remota de Datos de Usuario en Firestore

**Fase**: 1 (Design & Contracts) | **Fecha**: 2026-10-01 | **Plan**: [plan.md](./plan.md)

Extraído de los Key Entities y Functional Requirements de `spec.md`. Equivalente remoto
del esquema de `001-ecosmart-mvp/data-model.md` §2 y §4 — las reglas de negocio de cada
entidad (racha, niveles, topes diarios, duplicados) **no cambian**; lo que cambia es
dónde vive cada dato y quién tiene permiso de escribirlo.

---

## 1. Colecciones y documentos de Firestore

```text
usuarios/{uid}                                  # uid = Firebase Authentication UID
├── (campos del documento, ver §2)
├── registrosVerificacion/{registroId}           # subcolección, ver §3
├── caminataEnCurso/{"actual"}                   # documento singleton, ver §4
└── permisosDispositivo/{dispositivoId}_{tipo}   # ver §5
```

No existe una colección raíz separada por entidad (p. ej. no hay `registrosVerificacion`
a nivel raíz): todo cuelga de `usuarios/{uid}` para que las Reglas de Seguridad de
RF-D010 puedan expresarse con una única regla recursiva
(`match /usuarios/{uid}/{document=**}`), en vez de una regla distinta por colección.

---

## 2. `usuarios/{uid}` (documento de perfil)

Equivalente remoto de `Usuario` (`001-ecosmart-mvp/data-model.md` §2.1).

| Campo | Tipo Firestore | Quién escribe | Notas |
|---|---|---|---|
| `email` | `string` | Firebase Authentication (espejo de solo lectura) | igual al email de la credencial; nunca editable desde este documento (RF-078 de spec 001) |
| `nombre`, `apellido`, `nombreUsuario`, `telefono` | `string` | Cliente Android | editables (RF-007 de spec 001) |
| `barrio` | `string \| null` | Cliente Android | nombre del enum `Barrio` (spec 001 §1), `null` si aún no se configuró |
| `categoriasDeInteres` | `array<string>` | Cliente Android | nombres del enum `CategoriaActividad` |
| `puntosHistoricos` | `number` (int) | **Solo backend de confianza** (RF-D014) | nunca decrece (regla de negocio ya definida en spec 001, inalterada) |
| `rachaActual` | `number` (int) | **Solo backend de confianza** (RF-D014) | |
| `nivel` | `string` | **Solo backend de confianza** (RF-D014) | nombre del enum `NivelUsuario`, recalculado junto con `puntosHistoricos` |
| `ultimaActividadAprobadaEn` | `string \| null` | **Solo backend de confianza** | ISO-8601 `LocalDate` |
| `pasosHoy` | `number` (int) | **Solo backend de confianza**, vía transacción (RF-D007) | ver §4 para la distinción con `caminataEnCurso` |
| `pasosHoyFecha` | `string` | **Solo backend de confianza** | fecha ISO-8601 del `pasosHoy` vigente, para detectar el corte diario |
| `creadoEn`, `actualizadoEn` | `timestamp` (servidor) | Firestore (`serverTimestamp()`) | nunca el reloj del dispositivo (ver Edge Case de conflicto por reloj local) |

**Reglas de negocio**: idénticas a `Usuario` de spec 001 (`nivel()`, `sumarPuntos()`,
`registrarActividadAprobadaHoy()`), implementadas ahora en el backend de confianza en vez
de en el cliente Android — ver `contracts/openapi.yaml` y Principio III/IV (el dominio
rico se traslada, no se duplica ni se reinventa).

---

## 3. `usuarios/{uid}/registrosVerificacion/{registroId}` (subcolección de historial)

Equivalente remoto de `RegistroVerificacion` (`001-ecosmart-mvp/data-model.md` §2.3).

| Campo | Tipo Firestore | Quién escribe | Notas |
|---|---|---|---|
| `actividadId` | `string` | Cliente Android (vía endpoint, nunca escritura directa de Firestore) | referencia al catálogo estático de Actividades (fuera de alcance, sigue local/empaquetado) |
| `categoria` | `string` | Cliente Android (vía endpoint) | nombre del enum `CategoriaActividad` |
| `fecha` | `string` | Cliente Android (vía endpoint) | ISO-8601 `LocalDate`, fecha local del dispositivo (RF-057 de spec 001, sin cambios) |
| `resultado` | `string` | **Solo backend de confianza** (RF-D014) | nombre del enum `ResultadoVerificacion` |
| `motivoIA` | `string \| null` | **Solo backend de confianza** | motivo devuelto por EcoGPT, reenviado por el cliente al endpoint nuevo |
| `puntosOtorgados` | `number` (int) | **Solo backend de confianza** (RF-D014) | 0 salvo `resultado == APROBADO` |
| `huellaImagen` | `string \| null` | Cliente Android (vía endpoint) | hash perceptual, usado por el backend para `esDuplicadoDe()` antes de otorgar puntos |
| `pasosRegistrados` | `number \| null` | Cliente Android (vía endpoint) | solo para `categoria == CAMINAR` |
| `creadoEn` | `timestamp` (servidor) | Firestore (`serverTimestamp()`) | usado para ordenar "últimos 3" (RF-041/042 de spec 001, ver RF-D003) |

**Nota de escritura**: el cliente **nunca** crea el documento `registrosVerificacion`
directamente en Firestore — envía toda la evidencia (`actividadId`, `categoria`,
`fecha`, `huellaImagen`, `pasosRegistrados`) al endpoint correspondiente del backend de
confianza (`contracts/openapi.yaml`), que crea el documento completo (incluyendo
`resultado`/`puntosOtorgados`/`motivoIA`) y actualiza
`puntosHistoricos`/`rachaActual`/`nivel` del documento padre en una única transacción de
Firestore. Esto simplifica las Reglas de Seguridad (`contracts/firestore.rules` ya no
necesita permitir ninguna escritura de cliente sobre esta subcolección, ver §6).

**Índices compuestos requeridos**: `usuarioId` (impl. por la subcolección) + `categoria` +
`fecha` (para `contarAprobadosDelDia`, tope diario RF-032/033/035 de spec 001) y
`creadoEn` descendente (para `ultimosN`/`todos`, RF-D003).

---

## 4. `usuarios/{uid}/caminataEnCurso/actual` (documento singleton)

Equivalente remoto de `CaminataEnCursoStore` (spec 001). Un único documento de ID fijo
`"actual"` (no una subcolección con múltiples documentos), para que "una sola caminata
activa a la vez" (RF-082 de spec 001) sea una propiedad estructural (no se puede tener
dos documentos activos) reforzada además por la transacción atómica de RF-D008.

| Campo | Tipo Firestore | Quién escribe | Notas |
|---|---|---|---|
| `estado` | `string` | **Solo backend de confianza** | `"NINGUNA" \| "EN_CURSO" \| "COMPLETADA"` |
| `actividadId` | `string \| null` | **Solo backend de confianza** | |
| `metaPasos` | `number \| null` | **Solo backend de confianza** | |
| `pasosLogrados` | `number \| null` | **Solo backend de confianza** | |
| `fecha` | `string \| null` | **Solo backend de confianza** | ISO-8601 `LocalDate`, usada para vencer a medianoche (RF-082) |
| `dispositivoIdIniciador` | `string \| null` | **Solo backend de confianza** | informativo: qué dispositivo inició la caminata activa |
| `actualizadoEn` | `timestamp` (servidor) | Firestore (`serverTimestamp()`) | |

Este documento **no** otorga puntos por sí solo (ver RF-D007 vs. RF-D015 en `spec.md`):
al completarse (`pasosLogrados >= metaPasos`), el backend de confianza crea además el
`RegistroVerificacion` correspondiente (§3) en la misma transacción.

---

## 5. `usuarios/{uid}/permisosDispositivo/{dispositivoId}_{tipo}`

Equivalente informativo de `PermisoDispositivo` (spec 001). Un documento por combinación
dispositivo+tipo de permiso (ver Clarifications de `spec.md`, resolución de CHK004).

| Campo | Tipo Firestore | Quién escribe | Notas |
|---|---|---|---|
| `dispositivoId` | `string` | Cliente Android | identificador estable de instalación (`Settings.Secure.ANDROID_ID` o UUID generado una vez y persistido) |
| `tipo` | `string` | Cliente Android | nombre del enum `TipoPermiso` |
| `estado` | `string` | Cliente Android | nombre del enum `EstadoPermiso` |
| `actualizadoEn` | `timestamp` (servidor) | Firestore (`serverTimestamp()`) | |

El cliente Android **sí** puede escribir este documento directamente (a diferencia de
`puntosHistoricos`/`registrosVerificacion.resultado`): es puramente informativo, no
otorga puntos ni afecta ninguna regla de negocio (RF-D009), por lo que no requiere pasar
por el backend de confianza.

---

## 6. Resumen de permisos de escritura (insumo directo para `contracts/firestore.rules`)

| Documento / campo | Cliente Android | Backend de confianza (Admin SDK) |
|---|---|---|
| `usuarios/{uid}` — campos de perfil editable (nombre, barrio, teléfono, categorías) | ✅ solo su propio `uid` | ✅ |
| `usuarios/{uid}` — `puntosHistoricos`, `rachaActual`, `nivel`, `pasosHoy`, `pasosHoyFecha`, `ultimaActividadAprobadaEn` | ❌ (RF-D014) | ✅ |
| `registrosVerificacion/{id}` — todos los campos (`actividadId`, `categoria`, `fecha`, `huellaImagen`, `pasosRegistrados`, `resultado`, `puntosOtorgados`, `motivoIA`) | ❌ (el cliente envía la evidencia vía endpoint; nunca escribe el documento directamente, ver §3) | ✅ |
| `caminataEnCurso/actual` | ❌ (RF-D008: toda la escritura pasa por transacción del backend) | ✅ |
| `permisosDispositivo/{id}` | ✅ solo su propio `uid` | ✅ |
| Cualquier documento bajo `usuarios/{otroUid}` | ❌ siempre (RF-D010) | ✅ (Admin SDK bypassa las reglas) |

Las credenciales (contraseña) no aparecen en esta tabla porque **nunca** son un
documento de Firestore (RF-D002): viven exclusivamente en Firebase Authentication.
