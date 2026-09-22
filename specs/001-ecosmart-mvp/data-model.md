# Data Model: EcoSmart — Sistema Completo (MVP)

**Fase**: 1 (Design & Contracts) | **Fecha**: 2026-09-20 | **Plan**: [plan.md](./plan.md)

Extraído de las Key Entities de `spec.md` y de las reglas de negocio de sus
Historias de Usuario y Requisitos Funcionales. Las entidades de **Dominio**
son clases ricas (Principio III): encapsulan sus propias reglas, no son
simples bolsas de datos. Su mapeo a **Room** vive en una clase `*RoomEntity`
+ `*Mapper` separada, para que el dominio no dependa de anotaciones de Room
(Principio IV).

---

## 1. Enums (Value Objects — Principio VI)

```kotlin
enum class CategoriaActividad { RECICLAR, REUTILIZAR, CAMINAR }

enum class ResultadoVerificacion { APROBADO, RECHAZADO, INDETERMINADO }

enum class EstadoPermiso { NO_SOLICITADO, OTORGADO, DENEGADO }

enum class TipoPermiso { GALERIA, CAMARA, GPS, PODOMETRO }

// Corrección post-QA 2026-09-22 (RF-063): los 48 barrios oficiales de CABA,
// elegidos en un desplegable — reemplaza el campo de dirección de texto
// libre. `nombreVisible` es la etiqueta mostrada en la UI y también la
// clave de coincidencia contra `PuntoVerde.barrio` (RF-018).
enum class Barrio(val nombreVisible: String) {
    AGRONOMIA("Agronomía"), ALMAGRO("Almagro"), BALVANERA("Balvanera"),
    BARRACAS("Barracas"), BELGRANO("Belgrano"), BOEDO("Boedo"),
    CABALLITO("Caballito"), CHACARITA("Chacarita"), COGHLAN("Coghlan"),
    COLEGIALES("Colegiales"), CONSTITUCION("Constitución"), FLORES("Flores"),
    FLORESTA("Floresta"), LA_BOCA("La Boca"), LA_PATERNAL("La Paternal"),
    LINIERS("Liniers"), MATADEROS("Mataderos"), MONTE_CASTRO("Monte Castro"),
    MONSERRAT("Monserrat"), NUEVA_POMPEYA("Nueva Pompeya"), NUNEZ("Núñez"),
    PALERMO("Palermo"), PARQUE_AVELLANEDA("Parque Avellaneda"),
    PARQUE_CHACABUCO("Parque Chacabuco"), PARQUE_CHAS("Parque Chas"),
    PARQUE_PATRICIOS("Parque Patricios"), PUERTO_MADERO("Puerto Madero"),
    RECOLETA("Recoleta"), RETIRO("Retiro"), SAAVEDRA("Saavedra"),
    SAN_CRISTOBAL("San Cristóbal"), SAN_NICOLAS("San Nicolás"),
    SAN_TELMO("San Telmo"), VELEZ_SARSFIELD("Vélez Sarsfield"),
    VERSALLES("Versalles"), VILLA_CRESPO("Villa Crespo"),
    VILLA_DEL_PARQUE("Villa del Parque"), VILLA_DEVOTO("Villa Devoto"),
    VILLA_GENERAL_MITRE("Villa General Mitre"), VILLA_LUGANO("Villa Lugano"),
    VILLA_LURO("Villa Luro"), VILLA_ORTUZAR("Villa Ortúzar"),
    VILLA_PUEYRREDON("Villa Pueyrredón"), VILLA_REAL("Villa Real"),
    VILLA_RIACHUELO("Villa Riachuelo"), VILLA_SANTA_RITA("Villa Santa Rita"),
    VILLA_SOLDATI("Villa Soldati"), VILLA_URQUIZA("Villa Urquiza");

    companion object {
        fun desdeNombreVisible(nombre: String): Barrio? =
            entries.find { it.nombreVisible.equals(nombre.trim(), ignoreCase = true) }
    }
}

enum class NivelUsuario(val puntosMinimos: Int) {
    SEMILLA(0),
    BROTE(500),
    PLANTA(1500),
    ARBOL(3500);

    companion object {
        fun desdePuntaje(puntos: Int): NivelUsuario =
            entries.sortedByDescending { it.puntosMinimos }
                .first { puntos >= it.puntosMinimos }
    }
}
```

> `NivelUsuario.desdePuntaje` implementa directamente RF-046/RF-047: los
> umbrales están en el propio enum (no dispersos en un `when`), y como
> nunca decrece por diseño (se calcula siempre desde el puntaje histórico
> acumulado, que nunca disminuye), la regla "el nivel nunca baja" se
> cumple sin lógica adicional.

---

## 2. Entidades de Dominio

### 2.1 `Usuario`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `UsuarioId` (value class sobre `UUID`) | |
| `email` | `String` | único, validado (RF-003) |
| `contrasenaCifrada` | `ContrasenaCifrada` (value object: JWE compacto, ver §4) | nunca se expone como `String` plano |
| `nombre`, `apellido`, `nombreUsuario`, `telefono` | `String` | editables (RF-007) |
| `barrio` | `Barrio?` | editable (RF-007), elegido por desplegable (RF-063); `null` solo en cuentas creadas por Google que todavía no lo completaron — corrección post-QA 2026-09-22, reemplaza el `direccion: String` original |
| `categoriasDeInteres` | `Set<CategoriaActividad>` | preferencia inicial y editable (RF-005/RF-006/RF-009) |
| `puntosHistoricos` | `Int` | solo crece; base de `NivelUsuario` (RF-046/RF-047) |
| `rachaActual` | `Int` | días consecutivos con al menos 1 actividad aprobada |
| `ultimaActividadAprobadaEn` | `LocalDate?` | usada para recalcular racha (RF-038/RF-039) |
| `rol` | siempre `EcoCiudadano` (no hay campo — RF-054: único rol posible, no se modela como enum de 1 valor) | |

**Reglas de negocio encapsuladas**:
- `nivel(): NivelUsuario = NivelUsuario.desdePuntaje(puntosHistoricos)` (RF-040, RF-046).
- `sumarPuntos(cantidad: Int): Usuario` — devuelve una copia con
  `puntosHistoricos` incrementado; **nunca** expone un setter que permita
  decrementarlo (RF-047, "nunca DEBE descender").
- `registrarActividadAprobadaHoy(hoy: LocalDate): Usuario` — recalcula
  `rachaActual`: si `ultimaActividadAprobadaEn == hoy.minusDays(1)`
  incrementa la racha; si `== hoy` no cambia (ya contaba); en cualquier
  otro caso reinicia la racha a 1 (RF-038/RF-039).

### 2.2 `Actividad`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `ActividadId` | |
| `categoria` | `CategoriaActividad` | |
| `descripcionCorta` | `String` | mostrada en la tarjeta de Home (RF-013) |
| `pasosASeguir`, `resultadoEsperado` | `String` | visibles solo en el detalle (RF-014/RF-015) |
| `fotoReferencialUrl` | `String` | |
| `puntosBase` | `Int` | 50 (Reciclar/Caminar) o 100 (Reutilizar) |

**Reglas de negocio encapsuladas**:
- `estaDisponibleHoy(): Boolean = true` — por RF-056, toda actividad del
  catálogo está disponible 365 días sin restricción horaria; el método
  existe para que la UI nunca decida esto por su cuenta (aunque hoy
  siempre sea `true`, encapsula la regla si cambiara).
- La disponibilidad **para un usuario en particular** no vive en
  `Actividad` sino en el caso de uso `AplicarTopeDiario` (que combina
  `Actividad.categoria` con el conteo de `RegistroVerificacion` del día),
  para no acoplar la entidad Actividad al historial de un usuario.

### 2.3 `RegistroVerificacion`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `RegistroVerificacionId` | |
| `usuarioId` | `UsuarioId` | |
| `actividadId` | `ActividadId` | |
| `categoria` | `CategoriaActividad` | denormalizado para consultas rápidas de tope diario |
| `fecha` | `LocalDate` | fecha local del dispositivo (RF-057) |
| `resultado` | `ResultadoVerificacion` | |
| `motivoIA` | `String?` | motivo devuelto por EcoGPT cuando `RECHAZADO`/`INDETERMINADO` (RF-029) |
| `puntosOtorgados` | `Int` | 0 salvo `resultado == APROBADO` |
| `huellaImagen` | `String?` | hash perceptual de la foto enviada (solo Reciclar/Reutilizar), ver `research.md` §3 |
| `pasosRegistrados` | `Int?` | solo para `categoria == CAMINAR` |

**Reglas de negocio encapsuladas**:
- `consumeTopeDiario(): Boolean = resultado == ResultadoVerificacion.APROBADO`
  — implementa RF-050: solo lo Aprobado descuenta el tope diario;
  Rechazado/Indeterminado no.
- `esDuplicadoDe(otro: RegistroVerificacion, umbralHamming: Int = 5): Boolean` —
  compara `huellaImagen` con la de otro registro `APROBADO` del mismo
  usuario (RF-058/RF-059).

### 2.4 `PuntoVerde`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `PuntoVerdeId` | |
| `nombre`, `direccion` | `String` | |
| `barrio` | `String` | tal como lo publica la fuente oficial de CABA (texto libre externo, no el enum `Barrio` del Usuario); es la clave de búsqueda activa (RF-018/RF-063) — agregado en la corrección post-QA 2026-09-22 |
| `latitud`, `longitud` | `Double` | |
| `categoriasQueAcepta` | `Set<String>` | tal como las publica la fuente oficial de CABA |

**Reglas de negocio encapsuladas**:
- `estaDentroDelRadio(origen: Coordenada, radioKm: Double = 3.0): Boolean` —
  distancia Haversine entre `origen` y `(latitud, longitud)` ≤ `radioKm`.
  Regla del diseño original (RF-053); ya no es el mecanismo activo de
  búsqueda de Puntos Verdes (reemplazado por coincidencia de `barrio`, ver
  `ObtenerPuntosVerdesDelBarrio`), pero se conserva en el dominio — sigue
  siendo válida y testeada (`PuntoVerdeTest`) por si se retoma un filtro
  geográfico más adelante.

### 2.5 `PermisoDispositivo`

| Campo | Tipo | Notas |
|---|---|---|
| `tipo` | `TipoPermiso` | |
| `estado` | `EstadoPermiso` | |

**Reglas de negocio encapsuladas**:
- `bloqueaFormulario(tipoRequerido: TipoPermiso): Boolean` — usado por los
  casos de uso de verificación para decidir si deben impedir el envío y
  disparar la guía explicativa (RF-044/RF-045).

---

## 3. Value Objects auxiliares

- `UsuarioId`, `ActividadId`, `RegistroVerificacionId`, `PuntoVerdeId`:
  `value class` sobre `UUID` — evita mezclar accidentalmente un ID de
  `Usuario` con uno de `Actividad` en la firma de un caso de uso.
- `Coordenada(latitud: Double, longitud: Double)` — usado por
  `PuntoVerde.estaDentroDelRadio` y por el resultado del
  `UbicacionProvider`.
- `ContrasenaCifrada(jweCompacto: String)` — envuelve el JWE serializado;
  el dominio nunca maneja la contraseña en texto plano ni el JWK (eso es
  responsabilidad exclusiva de `infrastructure/security`, ver
  `research.md` §4).

---

## 4. Mapeo a Room

Cada entidad de dominio tiene su contraparte `@Entity` en
`infrastructure/persistence/room`, más un `*Mapper` (`toDomain()` /
`toRoomEntity()`) — el dominio nunca importa `androidx.room.*`.

```kotlin
@Entity(tableName = "usuarios")
data class UsuarioRoomEntity(
    @PrimaryKey val id: String,
    val email: String,
    val contrasenaCifradaJwe: String,   // JWE compacto — ver research.md §4
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val barrio: String?,                 // nombre del enum Barrio, o null si aún no se configuró
    val telefono: String,
    val categoriasDeInteres: String,     // CSV de CategoriaActividad, o tabla puente
    val puntosHistoricos: Int,
    val rachaActual: Int,
    val ultimaActividadAprobadaEn: String?, // ISO-8601 LocalDate
)

@Entity(
    tableName = "actividades",
)
data class ActividadRoomEntity(
    @PrimaryKey val id: String,
    val categoria: String,               // nombre del enum CategoriaActividad
    val descripcionCorta: String,
    val pasosASeguir: String,
    val resultadoEsperado: String,
    val fotoReferencialUrl: String,
    val puntosBase: Int,
)

@Entity(
    tableName = "registros_verificacion",
    foreignKeys = [
        ForeignKey(entity = UsuarioRoomEntity::class, parentColumns = ["id"], childColumns = ["usuarioId"]),
        ForeignKey(entity = ActividadRoomEntity::class, parentColumns = ["id"], childColumns = ["actividadId"]),
    ],
    indices = [Index("usuarioId"), Index("usuarioId", "categoria", "fecha")], // consulta de tope diario
)
data class RegistroVerificacionRoomEntity(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val actividadId: String,
    val categoria: String,
    val fecha: String,                   // ISO-8601 LocalDate, en huso horario local del dispositivo
    val resultado: String,               // nombre del enum ResultadoVerificacion
    val motivoIA: String?,
    val puntosOtorgados: Int,
    val huellaImagen: String?,
    val pasosRegistrados: Int?,
)

@Entity(tableName = "puntos_verdes")
data class PuntoVerdeRoomEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val direccion: String,
    val barrio: String,                  // texto de la fuente oficial de CABA (RF-063)
    val latitud: Double,
    val longitud: Double,
    val categoriasQueAcepta: String,     // CSV
)

@Entity(
    tableName = "permisos_dispositivo",
    primaryKeys = ["tipo"],
)
data class PermisoDispositivoRoomEntity(
    val tipo: String,                    // nombre del enum TipoPermiso
    val estado: String,                  // nombre del enum EstadoPermiso
)
```

**Nota sobre `contrasenaCifradaJwe`**: esta columna guarda el JWE
serializado (texto), NUNCA la contraseña en claro ni la clave JWK que la
cifra/descifra — esa clave vive exclusivamente en Android Keystore (ver
`research.md` §4), nunca en esta tabla ni en ninguna otra de Room
(RNF-007).

### DAOs (resumen de responsabilidades, sin código de implementación completo)

- `UsuarioDao`: CRUD + `findByEmail(email): UsuarioRoomEntity?` (RF-003,
  unicidad de email).
- `ActividadDao`: `findByCategorias(categorias: List<String>): Flow<List<ActividadRoomEntity>>`
  (RF-011, filtrado de Home).
- `RegistroVerificacionDao`:
  `contarAprobadosDelDia(usuarioId, categoria, fecha): Int` (base de
  `AplicarTopeDiario`, RF-032/RF-033/RF-035); `ultimosN(usuarioId, n=3)` y
  `todos(usuarioId)` (RF-041/RF-042); `huellasAprobadas(usuarioId, categoria)`
  (detección de duplicados, RF-058/RF-059).
- `PuntoVerdeDao`: `reemplazarTodos(lista)` (usado por el `Worker` de
  sincronización, RF-052) + consulta que trae todos los puntos para que el
  cálculo de radio (Haversine) se haga en memoria en el dominio, ya que
  SQLite no tiene una función de distancia geográfica nativa portable.
- `PermisoDispositivoDao`: `upsert(permiso)`, `observar(tipo): Flow<PermisoDispositivoRoomEntity?>`.

### Migraciones

Versión inicial `1`. Como es un MVP sin versión previa publicada, no hay
migraciones que definir todavía; `data-model.md` deja como convención que
cualquier cambio de esquema posterior a la primera release DEBE ir
acompañado de una `Migration` explícita (nunca `fallbackToDestructiveMigration`
en producción), consistente con el Principio VIII (no romper datos de
usuarios existentes sin una ruta de prueba).
