# Quickstart: Validación de la Reconstrucción Visual de la UI

Guía para correr y validar este módulo end-to-end. No incluye código de implementación
(ver `tasks.md`, generado por `/speckit.tasks`, para eso).

## Prerrequisitos

- Android Studio con el emulador o un dispositivo físico (`minSdk` 26+).
- Repositorio en el branch `003-reconstruccion-ui`, con `app/google-services.json`
  configurado (copiar desde `app/google-services.json.example` si hace falta, igual que
  en spec 001/002 — este módulo no cambia esa configuración).
- Ninguna variable de entorno ni servicio adicional: este módulo no agrega backend.

## 1. Compilar y correr la app

```powershell
./gradlew :app:assembleDebug
```

Instalar en el emulador/dispositivo desde Android Studio ("Run") o:

```powershell
./gradlew :app:installDebug
```

## 2. Validación visual pantalla por pantalla

Para cada pantalla, comparar lado a lado contra su sección de
`especificacion-ui-app-eco.md` (`SC-UI-002`):

| Pantalla | Cómo llegar | Sección a comparar |
|---|---|---|
| Inicio | Tab "Inicio" | §4 (S-01) |
| Actividades | Tab "Actividades" | §5 (S-02) |
| Caminar (detalle) | Tocar la tarjeta "Caminar" desde Inicio o Actividades | §6 (S-03) |
| Recompensas | Tab "Recompensas" | §7 (S-04) — recordar que usa datos de ejemplo fijos, ver `research.md` §1 |
| Educación | Tab "Educación" | §8 (S-05) |
| Perfil | Tab "Perfil" | §9 (S-06) |

Para cada una, verificar en particular:

- Colores/tipografía/espaciado coinciden con los tokens de `data-model.md` §1 (sin
  valores "sueltos" visualmente distintos).
- Los componentes compartidos (barra inferior, botón principal, chips, tarjetas) se ven
  idénticos entre pantallas distintas (`SC-UI-001`).
- La barra inferior resalta el tab correcto, incluido el caso especial de Caminar
  resaltando "Actividades" (`RF-UI-007`).

## 2bis. Puntos Verdes — nuevo acceso desde la barra inferior (sin rediseño visual)

A diferencia de la sección anterior, **no** se compara `PuntosVerdesScreen` contra
ninguna sección de `especificacion-ui-app-eco.md` (queda fuera del alcance de rediseño,
`RF-UI-002`/`RF-UI-002c`, research.md §9). Solo se valida que el acceso de navegación
funcione:

1. Desde cualquiera de las 6 pantallas principales, tocar el 6º ítem de la barra
   inferior (ícono de ubicación, entre Educación y Perfil) → se abre Puntos Verdes en
   1 solo toque (`SC-UI-006`).
2. La barra inferior sigue visible dentro de Puntos Verdes, con el ícono de ubicación
   resaltado como seleccionado (`RF-UI-002c`).
3. `PuntosVerdesScreen` conserva su apariencia e implementación actuales (mapa
   OpenStreetMap, datos por barrio — spec 001 `RF-018`/`RF-065`), sin ningún elemento
   del nuevo sistema de diseño (tokens de `data-model.md` §1 ni componentes `C-03` a
   `C-11`).
4. Con TalkBack activo, el ítem se lee como "Puntos Verdes" (`contentDescription`,
   `RF-UI-018`).

## 3. Validación funcional (no debe cambiar nada de esto)

Repetir, sin encontrar ninguna diferencia de comportamiento respecto de antes de este
módulo (`SC-UI-004`, `RF-UI-001`):

1. Completar una caminata hasta la meta → se otorgan los mismos puntos que antes.
2. Subir una foto de Reciclar/Reutilizar → el flujo de verificación por IA responde
   igual (Aprobado/Rechazado/Indeterminado), con el mismo tope diario.
3. Cerrar e instalar la app de nuevo con la misma cuenta → el perfil, historial y
   puntaje se recuperan igual que antes (spec 002, sin cambios).
4. Cualquier fila de menú de Perfil navega exactamente al mismo destino que antes,
   incluida "Mis actividades", que ahora abre `HistorialScreen` desde Perfil en vez de
   desde un tab propio (`research.md` §5).

## 4. Accesibilidad

- **Contraste**: usar el inspector de Accessibility Scanner (Android) o Layout
  Inspector + una herramienta de contraste sobre capturas de cada pantalla; ninguna
  debe caer por debajo de 4,5:1 (`RF-UI-015`).
- **Zonas táctiles**: con Layout Inspector, confirmar ≥48×48 dp en flecha de volver,
  chevrones, ítems de la barra inferior y chips (`RF-UI-016`).
- **Texto al 130%**: en Ajustes del sistema → Accesibilidad → Tamaño de fuente, llevar
  al máximo y recorrer las 6 pantallas verificando que ningún texto crítico se corte
  (`RF-UI-017`, `AC-20`).
- **Lector de pantalla (TalkBack)**: activarlo y recorrer una tarjeta de actividad y el
  anillo de Caminar, confirmando las lecturas de `RF-UI-018` (p. ej. "Caminar, 1200 de
  2000 metros, 50 puntos, en curso"; "60 por ciento").
- **Reducir movimiento**: activar la preferencia del sistema y confirmar que ninguna
  animación (anillo, barras, contador de puntos, transiciones) se reproduce —el estado
  final se muestra directamente (`RF-UI-019`).

## 5. Tests automatizados

```powershell
# Tests unitarios existentes (deben seguir en verde sin cambios — SC-UI-004)
./gradlew :app:testDebugUnitTest

# Nuevos tests de Compose UI de este módulo (ver research.md §7 / tasks.md)
./gradlew :app:connectedDebugAndroidTest
```

## 6. Criterios de salida

Este módulo se considera validado cuando:

- Los 6 ítems de la sección 2 de este documento no muestran diferencias perceptibles
  contra `especificacion-ui-app-eco.md` (`SC-UI-002`).
- Los 4 pasos de la sección 2bis confirman que Puntos Verdes es alcanzable en 1 toque
  desde la barra inferior, sin requerir ninguna comparación visual (`SC-UI-006`).
- Los 4 ítems de la sección 3 de este documento se comportan exactamente igual que
  antes de este módulo (`SC-UI-004`).
- La sección 4 (accesibilidad) no encuentra incumplimientos (`SC-UI-005`).
- Los comandos de la sección 5 terminan sin fallas.
