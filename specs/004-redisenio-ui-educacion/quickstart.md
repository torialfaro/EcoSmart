# Quickstart (Fase 1): Validación de la Sección Educación

**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

Guía para validar la implementación (después de `/speckit.implement`). Prerrequisitos de entorno
en la memoria del repo: JDK 21 para Gradle 8.9 y distribución de Gradle cacheada (no hay `gradlew`).

## 1. Compilar y correr tests

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
# Gradle 8.9 de la caché del usuario (ver gradle-wrapper.properties)
gradle :app:testDebugUnitTest          # parser, agrupación y suite existente (SC-ED-005)
gradle :app:connectedDebugAndroidTest  # Compose UI Test en dispositivo/emulador
gradle :app:ktlintCheck :app:detekt
```

Nunca importar `androidx.compose.foundation.layout.weight` (error de K2 documentado en la memoria del repo).

## 2. Validación manual (dispositivo o emulador, API 26+)

| # | Paso | Resultado esperado | Requisito |
|---|---|---|---|
| 1 | Abrir Educación desde la barra inferior | Sin segmento Videos/Artículos/IA, sin "Videos destacados"/"Ver todos"; sección "Reciclar" con la lección de ejemplo; Educación resaltada con píldora `#E6F2DF` | RF-ED-001/002/003/007 |
| 2 | Revisar encabezado | Título 30/36 verde `#0F4D2E`; hojita pegada al texto del subtítulo (también con 2 líneas); avatar con la inicial real del usuario; flecha de 48×48 | RF-ED-006 |
| 3 | Tocar la tarjeta | Se abre el detalle: portada, chips, título, introducción, 5 pasos "Paso N de 5" con conectores, tip y tarjeta de cierre; barra con Educación resaltada | RF-ED-009/010/011 |
| 4 | Volver desde el detalle (flecha y gesto del sistema) | Educación en la misma posición de scroll (probar agregando temporalmente lecciones al JSON para que haga falta scroll) | RF-ED-008 |
| 5 | "Ver actividades" | Se abre la pestaña Actividades en su estado habitual; atrás regresa a Inicio | RF-ED-012 |
| 6 | Abrir Inicio | "Contenido para vos" muestra tarjetas de lección compactas, ninguna tarjeta de video | RF-ED-013 |
| 7 | Tocar la tarjeta de Inicio | Detalle con **Inicio** resaltado en la barra; volver regresa a Inicio en su posición | RF-ED-007/008 |
| 8 | Flecha de volver del listado | Retrocede como el botón atrás (a Inicio si no hay pantalla previa) | RF-ED-006 |

## 3. Casos de datos (editar `assets/educacion/lecciones.json` temporalmente)

| Caso | Resultado esperado |
|---|---|
| `{"lecciones": []}` | Educación: "Todavía no hay lecciones disponibles"; Inicio sin la sección |
| JSON truncado o `lecciones` ausente | Educación: "No pudimos cargar las lecciones" + "Reintentar"; la app no se cierra |
| Una lección sin título o con `categoria` desconocida | Esa lección no aparece; las demás sí |
| Todas las lecciones inválidas | Estado de error |
| `ilustracion` con un nombre inexistente | Fondo pastel de la categoría en lugar del dibujo, sin fallar |
| Lección sin `tip` y sin `textoCierre` | Sin tip ni hueco; cierre con "Poné en práctica lo que aprendiste" |
| Lección con 3 y con 7 pasos | "Paso N de M" y conectores correctos |
| Agregar una lección usando vectores existentes | Aparece sin cambiar código (SC-ED-008) |
| `id` duplicado | Se conserva la primera |

## 4. Accesibilidad

1. Ajustes → Pantalla → Tamaño de fuente al máximo equivalente a 130%: ningún título, paso ni botón se corta; los bloques crecen.
2. TalkBack: cada tarjeta se lee como un único elemento (categoría, título, duración, pasos); las ilustraciones se leen con su descripción;
   destellos, conectores y hojita no se anuncian; los pasos se recorren en orden.
3. Contraste ≥ 4,5:1 (chips, texto `#3E4A40`/`#4A554C`, tip, texto blanco del cierre) con una herramienta de contraste.
4. Zonas táctiles ≥ 48×48 (flecha, botón circular de volver, tarjetas, "Ver actividades").
5. Confirmar que no hay animaciones ni transiciones en Educación y su detalle.

## 5. Regresiones

- Suite unitaria existente en verde (`SC-ED-005`).
- Otras pantallas (Actividades, Recompensas, Perfil, Puntos Verdes) sin cambios visuales; el resto de Inicio idéntico.
- `git diff --stat` no debe incluir archivos de `domain/`, `application/`, `infrastructure/` ni `backend/`.
