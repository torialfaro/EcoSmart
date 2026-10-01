<!--
  SYNC IMPACT REPORT
  ==================
  Version change: 1.0.0 → 2.0.0 (MAJOR — redefinición incompatible de principio existente)
  Reason: El proyecto incorpora un segundo módulo de trabajo (spec 002,
  `specs/002-firestore-datos-usuario/`) que mueve la fuente de verdad de los
  datos de usuario de "local-first" (Room/SharedPreferences, spec 001) a
  "remoto-autoritativo" (Firebase/Firestore + Firebase Authentication), para
  que la cuenta, el historial y el puntaje sobrevivan a la desinstalación y
  se sincronicen entre dispositivos. Esto redefine, de forma incompatible
  hacia atrás, el Principio IV (rol de la capa de Infraestructura) y la
  sección "Stack Tecnológico" tal como estaban definidos en v1.0.0.

  Modified principles:
    - IV. Separación Estricta de Capas → bullet "Infraestructura" ampliado:
      Firebase/Firestore y Firebase Authentication se suman como
      integraciones de Infraestructura; Room/SharedPreferences dejan de ser
      la fuente de verdad de datos de usuario y pasan a cumplir, a lo sumo,
      un rol de caché/estado efímero de UI.

  Added sections/content:
    - Stack Tecnológico: bullet "Persistencia" reescrito (reemplaza a
      "Persistencia local") para declarar a Firestore como base de datos
      autoritativa y remota de los datos de usuario.
    - Stack Tecnológico: bullet "Autenticación" ampliado para delegar el
      almacenamiento de contraseñas a Firebase Authentication (hash
      unidireccional gestionado por el proveedor, nunca visible para
      Firestore ni para la app), en reemplazo del esquema JWE reversible de
      RNF-006/RNF-007 de spec 001 para este módulo en adelante.

  Removed sections: None.

  Templates requiring updates:
    ✅ .specify/memory/constitution.md — this file (updated)
    ⚠  .specify/templates/plan-template.md — el próximo `/speckit.plan` de
         spec 002 DEBE completar el "Constitution Check" validando el nuevo
         rol de Firestore/Firebase Auth como Infraestructura autoritativa.
    ✅ .specify/templates/spec-template.md — sin cambio estructural necesario.
    ✅ .specify/templates/tasks-template.md — sin cambio estructural necesario.

  Known ambiguities: ninguna nueva introducida por esta enmienda; ver
  `specs/002-firestore-datos-usuario/spec.md` → Clarifications para las
  decisiones tomadas en este módulo (reemplazo de JWE por Firebase Auth,
  manejo de datos device-bound, migración de datos locales existentes).

  Follow-up TODOs: None.
-->

# EcoSmart Constitution

## Core Principles

### I. La Especificación Manda sobre la Implementación (NON-NEGOTIABLE)

Ninguna línea de código o funcionalidad DEBE programarse si no está previamente trazada
y aprobada en una historia de usuario o requisito formal producido en las fases de
especificación. Cualquier desvío respecto del alcance documentado requiere modificar
la especificación antes de comenzar la implementación. Todo pull request que introduzca
funcionalidad sin historia de usuario asociada DEBE rechazarse.

**Rationale**: evita el desarrollo de funcionalidades no validadas y mantiene la
trazabilidad completa entre requerimiento e implementación, algo crítico en un proyecto
académico evaluado por entregables de especificación.

### II. Restricción Estricta de Fases (NON-NEGOTIABLE)

Durante las fases de especificación (`/speckit.specify`), aclaración (`/speckit.clarify`),
checklist (`/speckit.checklist`), planificación (`/speckit.plan`) y generación de tareas
(`/speckit.tasks`) el equipo DEBE producir o actualizar únicamente los documentos
correspondientes a esa fase. Está prohibido escribir o modificar código fuente de la
aplicación durante estas fases. La implementación ocurre exclusivamente durante
`/speckit.implement`.

**Rationale**: mezclar especificación e implementación produce decisiones técnicas
prematuras que no pasan por el proceso de revisión acordado.

### III. Modelado de Dominio con POO Real

Las entidades de dominio (por ejemplo `Usuario`, `Actividad`, `RegistroCaminata`,
`RegistroReciclaje`, `RegistroReutilizacion`, `Perfil`, `PuntoVerde`) DEBEN encapsular
sus propias reglas de negocio. Están prohibidas las clases anémicas (solo getters/setters)
cuando exista una regla de negocio clara que deba vivir en la entidad: por ejemplo, una
`Actividad` sabe si puede repetirse hoy dado su tope diario; un `Usuario` sabe calcular
su racha de días consecutivos; un `RegistroCaminata` sabe si la meta fue alcanzada.

**Rationale**: un modelo rico evita que la lógica de negocio se disperse en servicios,
controladores o ViewModels, y mantiene el dominio como única fuente de verdad sobre las
reglas del juego (puntaje, topes, rachas, niveles).

### IV. Separación Estricta de Capas (Clean Architecture)

El sistema DEBE organizarse en las siguientes capas, con responsabilidades exclusivas
y sin mezclas, siguiendo una regla de dependencia unidireccional (UI → Presentación →
Aplicación → Dominio; la Infraestructura implementa interfaces definidas por Dominio
o Aplicación, nunca al revés):

- **Dominio**: reglas de negocio puras y modelos de entidades/objetos de valor. Sin
  dependencias del SDK de Android, Room, Retrofit ni ningún framework externo.
- **Aplicación / Servicios**: casos de uso de la aplicación (p. ej. `registrarCaminata`,
  `evaluarFotoConIA`, `actualizarPuntaje`, `calcularRacha`). Sin lógica de negocio propia;
  orquestan el dominio.
- **Infraestructura**: Firebase/Firestore y Firebase Authentication (fuente de verdad
  remota y autoritativa de los datos de usuario desde spec 002 — ver
  `specs/002-firestore-datos-usuario/`), Room/SharedPreferences (reservados, a partir de
  spec 002, a caché de lectura offline o estado efímero de UI — nunca la única copia de
  un dato de negocio), adaptadores a APIs nativas (Cámara, Galería, GPS/Ubicación,
  Podómetro) e integración con el proveedor de IA (EcoGPT). Sin lógica de negocio.
- **Presentación / ViewModel**: expone estado a la UI y traduce eventos de UI a casos
  de uso. Sin lógica de negocio ni acceso directo a Room o a las APIs de sensores.
- **Frontend (UI)**: pantallas nativas de Android. Sin lógica de negocio duplicada
  respecto del dominio.

**Rationale**: la separación facilita el testing unitario del dominio sin dependencias
de Android, y permite reemplazar infraestructura (p. ej. cambiar de proveedor de IA o
de mecanismo de persistencia) sin tocar las reglas de negocio.

### V. Calidad y Claridad de Código

Está prohibido: la lógica de negocio duplicada en más de un lugar, los métodos que
excedan aproximadamente 40 líneas, las validaciones de negocio ubicadas en controladores
o ViewModels, y el uso de `instanceof` (o `is` en Kotlin) para ramificar lógica de
negocio — en su lugar se DEBE usar polimorfismo, `sealed class` o `enum` con `when`
exhaustivo.

**Rationale**: mantiene el código predecible, reduce la deuda técnica y facilita que
múltiples colaboradores trabajen sobre el mismo dominio sin introducir regresiones.

### VI. Tipado y Valores Cerrados (Enums / Value Objects)

Todo conjunto cerrado y conocido de valores DEBE representarse como `enum class` o
*Value Object* de Kotlin, nunca como `String` libre ni constantes mágicas dispersas.
Aplica, como mínimo, a: categoría de actividad (`RECICLAR | REUTILIZAR | CAMINAR`),
resultado de verificación (`APROBADO | RECHAZADO | INDETERMINADO`), y estado de permiso
de dispositivo (`OTORGADO | DENEGADO | NO_SOLICITADO`).

**Rationale**: los enums son verificados en tiempo de compilación (exhaustividad en
`when`), documentan el vocabulario del dominio y evitan estados inválidos.

### VII. Diseño Pragmático (Patrones Solo si Simplifican)

Los patrones de diseño (Strategy, Factory, Observer, etc.) DEBEN aplicarse únicamente
cuando simplifican una solución concreta del proyecto. Está prohibido introducir un
patrón por convención, costumbre o currículum. Cada patrón incorporado DEBE justificarse
con el problema específico que resuelve, documentado en el plan técnico (`/speckit.plan`).

**Rationale**: la complejidad no justificada aumenta la curva de aprendizaje del equipo,
dificulta el debugging y no aporta valor al usuario final.

### VIII. Calidad de Pruebas

Toda regla de negocio importante y crítica DEBE estar cubierta por al menos un test
automatizado: unitario para reglas de dominio, de integración para repositorios y
adaptadores de infraestructura. Son obligatorios, como mínimo, tests de: cálculo de
puntos por tipo de actividad, aplicación de los topes diarios (5 fotos/día en
Reutilizar, 1 foto/día en Reciclar), transición de estado de verificación tras la
respuesta de la IA, comparación de meta de caminata contra el podómetro, y cálculo
de la racha de días consecutivos.

**Rationale**: los tests documentan el comportamiento esperado de la mecánica de
puntuación —el corazón del producto— y dan confianza para refactorizar sin romperla.

### IX. Experiencia de Usuario (UX) Amena y Motivadora

Las interfaces y los textos DEBEN priorizar la usabilidad y mantener un tono ameno,
amigable y tranquilo. Está prohibido un tono punitivo o culpabilizador: ante un
rechazo de la IA o un permiso denegado, los mensajes DEBEN explicar la causa en
términos claros y ofrecer una acción concreta (volver a intentar, ver la guía paso
a paso de permisos) en lugar de penalizar o culpar al usuario. El diseño de la
mecánica de puntos DEBE valorar la acción realizada por sobre la recompensa en sí.

**Rationale**: EcoSmart busca fomentar hábitos sostenibles a largo plazo; un tono
punitivo desmotiva la adopción continua, que es la métrica de éxito real del producto.

## Stack Tecnológico y Restricciones

- **Plataforma**: Android Nativo. Prohibido introducir frameworks multiplataforma
  (Flutter, React Native, KMP para UI) sin una nueva iteración de alcance.
- **Lenguaje**: Kotlin como lenguaje principal para dominio, aplicación, infraestructura
  y presentación. Java permitido únicamente donde la arquitectura por capas lo requiera
  (p. ej. interoperabilidad con librerías existentes).
- **Arquitectura**: Clean Architecture con las capas descritas en el Principio IV.
- **Persistencia**: Firestore (Firebase) es, a partir de spec 002, la base de datos
  autoritativa y remota de todos los datos de usuario (perfil, preferencias, historial
  de verificaciones, puntaje, racha). Ningún dato de negocio del usuario DEBE depender
  únicamente de una copia local para sobrevivir: Room y SharedPreferences quedan
  reservados a caché de lectura offline o estado efímero de UI, nunca como única fuente
  de verdad. El catálogo estático de Actividades y el dataset de Puntos Verdes de CABA
  (spec 001) no son "datos de usuario" y quedan fuera de este requisito.
- **Sensores y hardware**: APIs nativas de Android para Cámara, Galería, GPS/Ubicación
  y Podómetro (Step Counter / Sensor API). Todo acceso a estas APIs se realiza desde la
  capa de Infraestructura, nunca desde la UI o el dominio.
- **Inteligencia Artificial**: integración con **EcoGPT** para el análisis y validación
  de imágenes y descripciones de usuario en las actividades de Reciclar y Reutilizar.
  El cliente de EcoGPT vive en Infraestructura, detrás de una interfaz definida por la
  capa de Aplicación, de modo que el proveedor de IA pueda sustituirse sin afectar
  reglas de negocio.
- **Autenticación**: registro con email/contraseña o acceso con Google. Las credenciales
  y contraseñas NUNCA se almacenan ni transmiten en texto plano. A partir de spec 002, el
  almacenamiento de contraseñas se delega a Firebase Authentication (hash unidireccional
  gestionado por el proveedor, fuera de Firestore): ni la base de datos ni la app conocen
  ni pueden reconstruir la contraseña en texto plano. Esto reemplaza, para este módulo en
  adelante, el esquema de cifrado reversible JWE/JWK de RNF-006/RNF-007 (spec 001).

## Contexto del Producto y Reglas de Negocio

### A. Gestión de Usuarios y Autenticación

Registro mediante correo electrónico (mail, contraseña, nombre, apellido, nombre de
usuario, dirección y teléfono) o mediante Google. Todo el perfil y las credenciales de
login DEBEN ser editables por el usuario en cualquier momento desde su sesión activa.
Al registrarse o configurar la cuenta por primera vez, el usuario completa un checkbox
de categorías de interés (Reciclar, Reutilizar, Caminar); si selecciona todas, se
marcan individualmente. Esta preferencia se guarda y es editable posteriormente.

### B. Home y Visualización de Actividades

La Home muestra las actividades filtradas según las categorías seleccionadas por el
usuario, separadas por secciones con etiquetas claras. Cada tarjeta de actividad
muestra: foto referencial, puntos a ganar, descripción corta y botón "Realizarla". Los
pasos a seguir y el resultado esperado se visualizan únicamente al ingresar al detalle
de la actividad. Existe una sección informativa con artículos y videos sobre cuidado
del medio ambiente, ordenados por categoría (layout preparado para integración futura),
y una sección de Puntos Verdes Cercanos con información geográfica basada en fuentes
oficiales de la Ciudad de Buenos Aires.

### C. Mecánica de Verificación y Puntuación

- **Caminata (Podómetro)**: se proponen metas (p. ej. "caminar 10k hoy"). Al presionar
  "Realizar", el sistema compara la meta con los datos del podómetro del dispositivo;
  si coincide, se marca como **Aprobado**.
- **Reciclaje y Reutilización (IA)**: el usuario sube una foto de galería o toma una
  foto con la cámara, agregando una descripción propia. El sistema envía a EcoGPT un
  prompt que combina la foto, el resultado esperado de la actividad y la descripción
  del usuario.
- **Respuestas de la IA**:
  - **Aprobado**: opción de volver al inicio ("Atrás") o ver el progreso ("Ver
    progreso").
  - **Rechazado / Indeterminado**: se muestra el motivo del fallo, con opción de
    "Volver a intentar" (reiniciar el ingreso) o "Atrás" (volver a la home sin sumar
    puntos).
- **Grilla de puntajes y límites diarios** (valores cerrados, ver Principio VI):
  - Caminar: 50 puntos cada 100 metros (o equivalente en pasos según regla interna).
  - Reutilizar: 100 puntos por foto, límite de 5 fotos por día.
  - Reciclar: 50 puntos por foto, límite de 1 foto por día.
  - Las actividades pueden repetirse diariamente respetando estos topes.

### D. Perfil de Usuario y Métricas

Muestra el total de puntos acumulados, porcentaje de participación por categoría,
racha de días consecutivos completando actividades, y el nivel de usuario.

> **Ambigüedad pendiente**: los umbrales numéricos de los niveles de usuario NO están
> definidos todavía. Por el Principio I, esta funcionalidad NO DEBE implementarse hasta
> que `/speckit.specify` o `/speckit.clarify` resuelvan esta ambigüedad y la incorporen
> a la especificación correspondiente.

El historial muestra un contenedor inicial con las últimas 3 actividades recientes y
un botón "Ver más" para desplegar la ventana completa del historial.

### E. Gestión de Permisos del Dispositivo

Tras el login, la app solicita permisos de Galería, Cámara, GPS y Podómetro. Si el
usuario rechaza algún permiso, se le impide completar los formularios de actividades
que lo requieran, mostrando una alerta explicativa con una guía paso a paso para
habilitarlo desde la configuración del dispositivo (consistente con el tono no punitivo
del Principio IX).

## Flujo de Desarrollo y Calidad

### Proceso de trabajo con SpecKit

1. `/speckit.constitution` — este documento; rige todas las fases siguientes.
2. `/speckit.specify` — produce `spec.md` con historias de usuario y requisitos.
3. `/speckit.clarify` — resuelve ambigüedades (incluida la de niveles de usuario);
   actualiza `spec.md`.
4. `/speckit.checklist` — genera checklist de calidad de la especificación.
5. `/speckit.plan` — produce `plan.md` y documentos de diseño técnico, incluyendo el
   Constitution Check contra los 9 principios de este documento.
6. `/speckit.tasks` — produce `tasks.md` con tareas por historia de usuario y capa.
7. `/speckit.implement` — única fase en la que se produce código fuente.
8. `/speckit.analyze` — análisis de consistencia entre spec, plan y tareas.

### Reglas de calidad

- Ningún PR se mergea sin los tests automatizados exigidos por el Principio VIII en
  verde.
- No se aprueba código que introduzca regresiones en tests existentes.
- Los cambios que agreguen un nuevo valor cerrado (categoría, estado) DEBEN modelarse
  como enum antes de mergear (Principio VI).
- Todo nuevo caso de uso de Aplicación DEBE tener su contraparte de test unitario de
  dominio si involucra una regla de puntaje, tope diario o racha.

## Governance

Esta constitución es el documento rector del proyecto EcoSmart. Tiene precedencia
sobre cualquier decisión técnica individual o práctica de equipo no documentada aquí.

**Procedimiento de enmienda**:
1. Proponer el cambio mediante un issue o PR describiendo la modificación y su
   justificación.
2. El cambio DEBE ser revisado por al menos otra persona del equipo distinta al autor
   antes de aplicarse.
3. Actualizar este documento con `/speckit.constitution`, indicando la enmienda.
4. Incrementar la versión según semver: MAJOR para cambios incompatibles de gobernanza
   o eliminación/redefinición de principios, MINOR para adición de principios o
   secciones, PATCH para aclaraciones y correcciones sin cambio semántico.
5. Registrar la fecha en `LAST_AMENDED_DATE`.

**Política de revisión**: esta constitución DEBE revisarse al inicio de cada nueva
iteración o feature. Las ambigüedades detectadas en el Sync Impact Report (al inicio
de este archivo) DEBEN resolverse en `/speckit.specify` o `/speckit.clarify` antes de
iniciar la implementación de la funcionalidad afectada.

**Version**: 2.0.0 | **Ratified**: 2026-09-20 | **Last Amended**: 2026-10-01
