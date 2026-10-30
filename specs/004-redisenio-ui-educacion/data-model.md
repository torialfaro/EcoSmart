# Data Model (Fase 1): Reconstrucción de la Sección Educación

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Research**: [research.md](./research.md)

Este módulo **no agrega entidades de dominio, tablas Room ni documentos Firestore**. Todo lo que sigue es
modelo de Presentación o contrato de un archivo de solo lectura.

## 1. Contrato del archivo del catálogo

Archivo: `app/src/main/assets/educacion/lecciones.json`. Esquema formal en
[contracts/lecciones.schema.json](./contracts/lecciones.schema.json).

```json
{
  "lecciones": [
    {
      "id": "circuito-reciclaje-caba",
      "titulo": "El circuito del reciclaje en CABA",
      "categoria": "RECICLAR",
      "duracionMinutos": 4,
      "resumen": "Seguí el recorrido de una botella: de tu casa a un producto nuevo, en 5 pasos ilustrados.",
      "introduccion": "Cada envase que separás hace un recorrido largo antes de volver a ser útil. Mirá cómo es, paso a paso.",
      "ilustracionPortada": "il_leccion_circuito_portada",
      "descripcionPortada": "Botella, lata y envase de cartón junto a un contenedor verde de reciclaje",
      "pasos": [
        { "titulo": "Separá en casa", "texto": "Vaciá y enjuagá tus envases de plástico, vidrio, metal y cartón. Tienen que estar limpios y secos.", "ilustracion": "il_leccion_circuito_paso1", "descripcionIlustracion": "Envases limpios clasificados en cuatro contenedores de colores" },
        { "titulo": "Llevalos al contenedor verde", "texto": "Dejalos en un contenedor verde de tu barrio o llevalos a un Punto Verde.", "ilustracion": "il_leccion_circuito_paso2", "descripcionIlustracion": "Persona llevando una bolsa al contenedor verde" },
        { "titulo": "Los recolectan", "texto": "Un camión especial pasa por los contenedores y junta solo los materiales reciclables.", "ilustracion": "il_leccion_circuito_paso3", "descripcionIlustracion": "Camión recolector junto a un contenedor" },
        { "titulo": "Se clasifican por material", "texto": "En un centro de selección se separan por tipo y se prensan en fardos.", "ilustracion": "il_leccion_circuito_paso4", "descripcionIlustracion": "Cinta transportadora con fardos de materiales" },
        { "titulo": "Vuelven a ser algo útil", "texto": "Con ese material se fabrican productos nuevos: envases, ropa, mobiliario urbano y más.", "ilustracion": "il_leccion_circuito_paso5", "descripcionIlustracion": "Botella nueva y un banco de plaza hechos con material reciclado" }
      ],
      "tip": { "titulo": "Tip para que cuente", "texto": "Un envase con restos de comida puede ensuciar el resto del material. Enjuagalo antes." },
      "textoCierre": "Llevá tus envases limpios a un Punto Verde"
    }
  ]
}
```

Los textos de ejemplo son ilustrativos y se validan antes de publicarse (Assumptions del spec).

## 2. Modelos de Presentación (`presentation/education/LeccionUi.kt`)

| Entidad | Campo | Tipo | Regla de validación (parser) |
|---|---|---|---|
| **LeccionUi** | `id` | `String` | No vacío; único (se conserva la primera ocurrencia) |
| | `titulo` | `String` | No vacío |
| | `categoria` | `CategoriaActividad` (enum existente) | Debe coincidir con `RECICLAR` / `REUTILIZAR` / `CAMINAR`; si no, se descarta la lección |
| | `duracionMinutos` | `Int` | > 0 |
| | `resumen` | `String` | No vacío |
| | `introduccion` | `String` | No vacío |
| | `ilustracionPortada` | `String?` | Nombre de drawable; se resuelve al renderizar (falla → fondo pastel) |
| | `descripcionPortada` | `String` | Descripción para lector de pantalla; si falta, texto por defecto derivado del título |
| | `pasos` | `List<PasoLeccionUi>` | ≥ 1 elemento |
| | `tip` | `TipLeccionUi?` | Opcional; si está presente, `titulo` y `texto` no vacíos |
| | `textoCierre` | `String?` | Opcional; si falta o está vacío se usa "Poné en práctica lo que aprendiste" |
| | `cantidadPasos` | `Int` (derivado) | `pasos.size`; **no** se lee del archivo |
| **PasoLeccionUi** | `titulo`, `texto` | `String` | No vacíos |
| | `ilustracion` | `String?` | Igual que la portada |
| | `descripcionIlustracion` | `String` | Descripción accesible |
| **TipLeccionUi** | `titulo`, `texto` | `String` | No vacíos |

`numero` y `total` de cada paso ("Paso N de M") se derivan del índice y de `pasos.size` al renderizar.

## 3. Resultado del catálogo y estados de UI

```text
sealed class ResultadoCatalogo
├── Cargado(lecciones: List<LeccionUi>)   # al menos una lección válida
├── Vacio                                  # archivo válido sin lecciones
└── Error                                  # ausente / ilegible / todas inválidas
```

| Estado de `EstadoEducacion` | Origen | Qué muestra Educación | Qué muestra Inicio |
|---|---|---|---|
| `Cargando` | Lectura en curso | Encabezado y barra inferior | La sección se omite |
| `Cargado` | `ResultadoCatalogo.Cargado` | Secciones por categoría con tarjetas | Hasta 2 tarjetas compactas |
| `Vacio` | `ResultadoCatalogo.Vacio` | "Todavía no hay lecciones disponibles" | La sección se omite |
| `Error` | `ResultadoCatalogo.Error` | "No pudimos cargar las lecciones" + "Reintentar" | La sección se omite |

Transiciones: `Error` → (`Reintentar` → `CatalogoLecciones.recargar()`) → `Cargando` → `Cargado`/`Vacio`/`Error`.

`DetalleLeccionViewModel`: `Cargando` → `Cargada(leccion)` | `NoEncontrada` (vuelve a la pantalla de origen).

## 4. Agrupación y orden

- Orden fijo de secciones: `RECICLAR`, `REUTILIZAR`, `CAMINAR` (lista explícita en Presentación; no se depende del orden del enum).
- Dentro de cada sección: orden del archivo. Las secciones sin lecciones no se renderizan.
- Inicio: las **2 primeras lecciones** del catálogo en orden del archivo (sin priorización por categoría).

## 5. Tokens nuevos o con alias (ver research.md §7)

| Token | Valor | Origen |
|---|---|---|
| `ColorPastelReutilizar` | `#FFE8BE` | nuevo |
| `ColorPastelReciclar` | `#E6F2DF` | alias de `ColorGreen100` |
| `ColorPastelCaminar` | `#E3F2F6` | nuevo |
| `ColorPastelAux1/2/3` | `#DCEFE6`, `#E4EEF3`, `#DFF0E6` | nuevos (disponibles para ilustraciones/chip Caminar) |
| `ColorTextoSecundarioFuerte` / `ColorTextoLectura` | `#3E4A40` / `#4A554C` | nuevos |
| `ColorTipFondo` / `ColorTipBorde` | `#FFF1D6` / `#F2CF8A` | alias de `ColorRecompensaCardBg` / `ColorRachaBorde` |
| `ColorTipTitulo` / `ColorTipTexto` | `#6B3300` / `#6B4A12` | alias de `ColorRachaTextoNumero` / `ColorRachaTextoMensaje` |
| `ColorChipReutilizarTexto` | `#8A5A00` | nuevo |
| `RadiusTarjetaLeccion` / `RadiusHoja` | 24 dp / 28 dp | nuevos |
| `SpaceGapTarjetaLeccion` / `SpaceSeccionEducacion` | 16 dp / 28 dp | nuevos |
| `TypeTituloEducacion` 30/36, `TypeTituloLeccion` 24/32, `TypeTituloTarjeta` 18/24, `TypeIntroLeccion` 15/22, `TypeResumen` 14/20, `TypeTextoPaso` 14/21 | Nunito Bold/Regular | nuevos |

Chip por categoría (`RF-ED-005`): Reciclar `#E6F2DF` / `#1F5E34`; Reutilizar `#FFF1D6` / `#8A5A00`; Caminar `#DFF0E6` / `#2E7D3E`.
Fondo de ilustración por categoría: Reciclar `#E6F2DF`, Reutilizar `#FFE8BE`, Caminar `#E3F2F6`.

## 6. Componentes (`theme/ComponentesLeccion.kt`)

| ID | Componente | Parámetros principales | Notas |
|---|---|---|---|
| `C-13` | `TarjetaLeccion` | `LeccionUi`, `onClick`, `compacta: Boolean` | Variante compacta para Inicio; un único elemento tocable |
| `C-14` | `ChipCategoria` | `CategoriaActividad`, `translucido: Boolean` | Píldora 28 dp |
| `C-15` | `PasoIlustrado` | `PasoLeccionUi`, `numero`, `total` | Radio 24; píldora "Paso N de M" |
| `C-16` | `TarjetaTip` | `TipLeccionUi` | Ícono `ic_bombilla` |
| `C-17` | `TarjetaLlamadoAccion` | `texto`, `onVerActividades` | Etiqueta "Ahora te toca a vos"; botón "Ver actividades" 48 dp |
| `C-18` | `EncabezadoLeccion` | `LeccionUi`, `onVolver` | Banda 208 + hoja crema radio 28 superpuesta 28 |
| — | `IlustracionLeccion` | `nombre`, `descripcion`, `categoria`, `modifier` | Resuelve el drawable por nombre; fallback pastel |
| — | `ConectorPaso` | — | Línea/chevron de 24 dp, decorativo |

## 7. Trazabilidad requisito → modelo

| Requisito | Elemento |
|---|---|
| RF-ED-003/004/005 | `LeccionUi.categoria`, agrupación (§4), chip y fondo por categoría (§5) |
| RF-ED-009/010/011 | `LeccionUi.pasos`, `tip`, `textoCierre`, `PasoLeccionUi` |
| RF-ED-013 | Selección de 2 primeras lecciones (§4) |
| RF-ED-014 | Contrato del archivo (§1) + validaciones (§2) |
| RF-ED-015 | `LeccionUi` sin campo de actividad ni de identificadores del catálogo |
| RF-ED-016/017 | `ilustracion*` por nombre; fallback en `IlustracionLeccion` |
| RF-ED-018 | Tokens (§5) |
