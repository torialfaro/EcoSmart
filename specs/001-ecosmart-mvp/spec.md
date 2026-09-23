# Feature Specification: EcoSmart — Sistema Completo (MVP)

**Feature Branch**: `001-ecosmart-mvp`

**Created**: 2026-09-20

**Status**: Draft

**Input**: User description: "Especificación de Requisitos del Sistema EcoSmart. Detallar qué debe hacer la aplicación nativa para Android EcoSmart, tomando como marco inmutable constitution.md, organizada en 5 épicas (Autenticación/Preferencias, Home/Contenido, Verificación/Puntuación, Perfil/Historial, Permisos del Dispositivo), con historias de usuario priorizadas (P1/P2/P3), criterios de aceptación Given-When-Then, requisitos funcionales RF-001 a RF-045, requisitos no funcionales RNF-001 a RNF-005, casos borde y registro de ambigüedades detectadas."

## Clarifications

### Session 2026-09-20

- Q: ¿Cuáles son los umbrales de puntos para cada nivel de usuario? → A: 4 niveles fijos por puntaje histórico acumulado: Semilla (0–499), Brote (500–1.499), Planta (1.500–3.499) y Árbol (3.500+); el nivel nunca hace descender ni consumir el puntaje.
- Q: ¿Cómo se convierten los pasos del podómetro a metros para la regla de puntaje de caminata? → A: 1 paso = 0,75 m (133 pasos ≈ 100 m); los puntos se otorgan solo por bloques completos de 100 m, sin fracciones por metros/pasos remanentes.
- Q: ¿Un resultado "Indeterminado" de EcoGPT consume el tope diario de fotos de esa categoría? → A: No. Solo los resultados "Aprobado" descuentan del tope diario; "Indeterminado" pide una nueva foto (mejor luz/encuadre) sin penalizar los intentos diarios restantes.
- Q: ¿Cómo se obtienen y actualizan los datos de Puntos Verdes de CABA, y con qué radio se filtran? → A: Dataset estático empaquetado con la app, con sincronización en segundo plano cuando hay conexión; se muestran únicamente los puntos dentro de un radio de 3 km de la dirección de perfil (o coordenadas GPS si el permiso está activo).
- Q: ¿Existen roles de usuario distintos (administrador, moderador) dentro de la app móvil? → A: No. Existe un único rol, "EcoCiudadano"; toda la validación de Reciclar/Reutilizar se delega en EcoGPT, sin moderación humana dentro de la app.
- Q: ¿Las actividades del catálogo tienen restricciones de horario o de "stock" además del tope diario? → A: No. Todas las actividades están disponibles los 365 días del año sin restricción horaria; la única limitación es el tope diario por categoría, que se reinicia a las 00:00:00 hora local del dispositivo.
- Q: ¿Cómo se evita que un usuario reutilice la misma fotografía para sumar puntos repetidamente en la misma o distinta actividad? → A: Cada verificación exige una fotografía nueva capturada o seleccionada en el momento; EcoGPT analiza la imagen enviada para detectar si es idéntica a una ya aprobada previamente por el mismo usuario y rechaza el envío en ese caso.
- Q: Caso hipotético — si un atacante filtra toda la base de datos, ¿qué mecanismo debe proteger las contraseñas para que no puedan recuperarse en texto plano? → A: Cifrado reversible con clave (formato JWK/JWE), en lugar de un hash unidireccional. Decisión explícita del proyecto, con la salvedad de seguridad indicada en Assumptions.
- Q: Caso hipotético — si ese mismo atacante también descarga la base de datos completa, ¿dónde debe vivir la clave de cifrado para que no la obtenga junto con los datos? → A: En un almacén de claves/secretos separado de la base de datos de usuarios (nunca en la misma base de datos ni embebida en el código).
- Q: Caso hipotético — un usuario envía una foto para verificar una actividad y EcoGPT tarda en responder. ¿Cuánto tiempo máximo debe esperar el sistema antes de mostrar un error de timeout? → A: 30 segundos.
- Q: Caso hipotético — un usuario intenta registrarse con la contraseña "12345678" (8 caracteres, solo números). ¿Qué criterio mínimo debe exigir el sistema? → A: Mínimo 8 caracteres combinando letras y números; "12345678" se rechaza por ser solo números.
- Q: Caso hipotético — un usuario camina exactamente 266 pasos (2 bloques de 133 pasos, pero solo 199,5 m reales). ¿Cuántos puntos suma: 50 (por metros reales) o 100 (por bloques de pasos)? → A: 100 puntos; el bloque de puntaje se calcula por cada 133 pasos directamente registrados por el podómetro, no por los metros convertidos.
- Q: Caso hipotético — un usuario nunca abrió el diálogo de permiso de Cámara, luego lo deniega, y más tarde lo deniega "para siempre". ¿Cuántos estados distintos necesita modelar el sistema? → A: 3 estados (NO_SOLICITADO / OTORGADO / DENEGADO); la denegación permanente no requiere un estado adicional, ya la cubre el Edge Case correspondiente.
- Q: ¿Qué número concreto reemplaza a "varios días consecutivos" en el escenario de ejemplo de US11? → A: 3 días (no cambia la regla de negocio de racha, ya definida en RF-038/RF-039; solo aclara el ejemplo).

### Session 2026-09-22 — Correcciones post-QA manual

Ronda de correcciones detectadas al probar el MVP en un dispositivo real (Android Studio), no ambigüedades de diseño sino defectos/omisiones de la implementación respecto de RF ya definidos, más 2 decisiones de negocio nuevas:

- Q: Los campos de contraseña de Login/Registro mostraban el texto ingresado en claro, sin ocultarlo. ¿Cuál es el comportamiento correcto? → A: Deben estar ocultos por defecto (no visibles a primera instancia), con una opción explícita para mostrarlos temporalmente a pedido del usuario. Nuevo requisito: RF-060.
- Q: La sesión no se conservaba entre aperturas de la app — el usuario debía loguearse cada vez, pese a que RF-010 pide "autenticado hasta que cierre sesión explícitamente". ¿Cuál es el comportamiento correcto? → A: La sesión DEBE persistir entre reinicios de la app (no solo en memoria del proceso); el usuario solo vuelve a loguearse si cierra sesión explícitamente. RF-010 se reafirma como requisito de persistencia real, no solo de "no pedir contraseña en cada pantalla dentro de la misma sesión de proceso". Nuevo requisito: RF-061.
- Q: Desde la Home no había ningún atajo hacia el perfil del usuario. ¿Cómo debe accederse? → A: Un ícono circular con la inicial del usuario, arriba a la derecha de la Home, que lleva directo al perfil. Nuevo requisito: RF-062.
- Q: El campo "dirección" del registro era texto libre, sin relación con los datos reales de Puntos Verdes de CABA. ¿Cómo debe capturarse, y para qué se usa? → A: Debe ser un desplegable cerrado con los 48 barrios oficiales de la Ciudad de Buenos Aires (no texto libre); ese valor reemplaza al radio GPS de 3 km como mecanismo de búsqueda de Puntos Verdes — se buscan los Puntos Verdes del barrio elegido, no los más cercanos por coordenadas. RF-018 y RF-053 quedan revisados; nuevo requisito: RF-063. El filtro por radio GPS (`PuntoVerde.estaDentroDelRadio`, Haversine) se conserva en el dominio por si se retoma un filtro geográfico más adelante, pero deja de ser el mecanismo activo.
- Q: El botón "Realizar" de una actividad de Reciclar/Reutilizar cerraba la app en vez de abrir el formulario de verificación con cámara. ¿Cuál era la causa y cuál es el comportamiento correcto? → A: Defecto de implementación, no ambigüedad de requisito: la pantalla de verificación de foto nunca chequeaba el permiso de Cámara antes de intentar usarla (`CameraX.bindToLifecycle` lanzaba `SecurityException` sin capturar si el permiso no estaba otorgado). RF-044/RF-045 ya exigían bloquear el formulario y mostrar la guía en ese caso — la corrección conecta esa regla, ya modelada en el dominio (`PermisoDispositivo.bloqueaFormulario`), en el punto de uso real. Nuevo requisito explícito: RF-064 (verificación de permiso previa a cualquier acceso a Cámara/Podómetro, sin excepción).

### Session 2026-09-22 (continuación) — Mapa de Puntos Verdes y Perfil consolidado

Segunda ronda de correcciones detectadas en la misma prueba manual, más 3 decisiones de diseño nuevas:

- Q: Caso hipotético — un usuario abre "Puntos Verdes" y no ve ninguno, sin entender por qué. ¿Qué proveedor de mapas se usa para mostrarlos visualmente, dado que el proyecto es local-first y evita dependencias externas con fricción de configuración (research.md §0)? → A: OpenStreetMap vía `osmdroid` — gratis, sin API key ni cuenta de facturación, coherente con el resto de las decisiones técnicas del proyecto. Se descarta Google Maps por requerir una API key de Google Cloud Console con facturación habilitada. Nuevo requisito: RF-065.
- Q: Caso hipotético — dos usuarios eligen el mismo barrio en su perfil; uno vive a 200 m de un Punto Verde real de un barrio vecino, el otro a 4 km de cualquier Punto Verde dentro de su propio barrio. Con RF-063 (coincidencia por barrio elegido, no por coordenadas), ambos verían exactamente los mismos puntos. ¿Se implementa detección geográfica real (polígonos oficiales de barrio + GPS) o se mantiene la coincidencia por barrio elegido, solo ampliando el dataset de ejemplo a los 48 barrios? → A: Se mantiene la coincidencia por barrio de RF-063 (no se agrega detección por polígonos); se amplía el dataset de ejemplo de Puntos Verdes para cubrir los 48 barrios de `Barrio` (antes cubría solo 6), mostrados en un mapa centrado en el promedio de las coordenadas de los puntos encontrados. La precisión geográfica real queda fuera de alcance del MVP — ver Assumptions. Nuevo requisito: RF-066.
- Q: Caso hipotético — un usuario toca "Cámara" y después quiere tocar "Galería" para elegir una foto existente en cambio, pero no logra tocar el botón porque la vista previa de la cámara lo tapa visualmente. ¿Cuál es la causa y el comportamiento correcto? → A: Defecto de implementación, no ambigüedad: `PreviewView` de CameraX en su modo de implementación por defecto (`PERFORMANCE`) usa un `SurfaceView`, que se compone en una capa de hardware separada y se dibuja por encima de cualquier otra vista de Compose sin importar el orden declarado en el layout. El comportamiento correcto es que los botones "Cámara"/"Galería" sean siempre tocables, nunca tapados por la vista previa. Nuevo requisito explícito: RF-067 (modo de implementación `COMPATIBLE` para que la vista previa respete el orden de composición normal).
- Q: Hoy "Perfil" (RF-036 a RF-040) solo muestra puntos/nivel/racha/%, y los datos de cuenta (nombre, email, barrio, teléfono, categorías de interés — RF-007) viven en una pantalla de edición separada que nunca se conectó a la navegación, por lo que son inalcanzables. ¿Se fusiona todo en una sola pantalla editable, o se mantienen dos pantallas (una de solo lectura con todos los datos, y un botón "Editar" hacia la pantalla de edición ya construida)? → A: Dos pantallas separadas. "Perfil" pasa a mostrar TODOS los datos del usuario (cuenta + métricas de actividad), de solo lectura, con un botón "Editar perfil" que navega a la pantalla de edición existente (que finalmente se conecta a la navegación). Nuevo requisito: RF-068.
- Q: ¿Debe haber algún saludo visible junto al ícono de perfil de la Home (RF-062), o alcanza con el ícono solo? → A: Debe mostrarse el texto "Hola, {nombreUsuario}" junto al ícono circular, arriba a la derecha de la Home. Nuevo requisito: RF-069.

### Session 2026-09-22 (continuación 2) — Rediseño visual y pasos del día

Ronda de refinamiento visual solicitada tras probar el MVP, con 3 imágenes de referencia adjuntas (una app "Pasito" con fondo verde bosque, tarjetas color crema y acentos ámbar/naranja de bordes muy redondeados; un swatch abstracto de paleta/formas; y una pantalla de ejemplo de Login/Signup con botón "Sign in with Google", divisor "or sign in with", campos etiquetados, toggle de contraseña, "Forgot Password" y botón grande en pastilla), más 2 decisiones de diseño nuevas:

- Q: Caso hipotético — un usuario tiene el permiso de Podómetro otorgado y caminó 4.200 pasos hoy; otro usuario nunca otorgó ese permiso. ¿Dónde debe verse "clara y visiblemente" la cantidad de pasos del día, y qué debe pasar si el permiso no está activo? → A: Dentro de la pantalla de Perfil, como una tarjeta destacada con el color de acento (no en la Home). Si el permiso de Podómetro no está otorgado, el dato no se muestra (no se muestra "0" ni ningún placeholder). Nuevo requisito: RF-070.
- Q: Las 3 imágenes de referencia usan una paleta azul en el ejemplo de Login/Signup, distinta de la paleta verde/ámbar del resto de la app (paleta ya elegida por RF-070 y el resto del rediseño). ¿El Login/Signup adopta el azul del ejemplo, o mantiene la paleta verde/ámbar propia de EcoSmart? → A: Mantiene la paleta verde/ámbar propia de EcoSmart, por consistencia visual con el resto de la app; del ejemplo se adopta solo la estructura del layout (botón de Google arriba, divisor "o iniciá sesión con", campos etiquetados, toggle de contraseña, botón principal en pastilla), no sus colores. Nuevo requisito: RF-071.

Además, sin necesidad de pregunta adicional (aplica directamente el estilo visual de las 3 imágenes de referencia a toda la app): paleta verde bosque/crema/ámbar con bordes fuertemente redondeados en tarjetas y botones, y jerarquía visual consistente (título > tarjeta destacada > detalle) en todas las pantallas. Nuevo requisito: RF-072.

### Session 2026-09-23 — Backend real de EcoGPT

Al probar el flujo completo (US8/US9: elegir actividad → realizarla →
verificación por IA → sumar puntos → ver progreso), toda verificación de
foto fallaba con "Sin conexión a internet". Causa raíz (no ambigüedad de
diseño): `ECOGPT_BASE_URL` apuntaba por defecto a
`https://api.ecogpt.example/v1/`, un dominio reservado por RFC 2606 que
nunca resuelve — "EcoGPT" nunca tuvo un backend real implementado, solo un
contrato (`contracts/openapi.yaml`) diseñado contra un proveedor de
terceros hipotético que jamás se contrató. 2 decisiones de negocio nuevas
sobre cómo resolverlo:

- Q: ¿Se implementa un backend propio real detrás de EcoGPT (proxy hacia
  una IA de verdad), se llama a una IA directamente desde el cliente
  Android sin intermediario, o se deja un mock local solo para demos? → A:
  Backend propio real (Opción A, la más completa): un servicio HTTP propio
  que recibe la foto + descripción del cliente y llama a la API de Claude
  (Anthropic, con visión) para comparar ambas contra
  `Actividad.resultadoEsperado` en una sola llamada multimodal, devolviendo
  el veredicto Aprobado/Rechazado/Indeterminado. Se descarta llamar a la
  IA directamente desde el cliente por exponer una API key de pago dentro
  del APK. Nuevo requisito: RF-073. Ver research.md §2.1 y
  `backend/README.md`.
- Q: ¿Con qué stack se construye ese backend y dónde se despliega? → A:
  Python + FastAPI (sobre la alternativa recomendada de Kotlin + Ktor, que
  hubiera mantenido un único lenguaje en el repo, mayormente por
  familiaridad con el ecosistema Python para integrar SDKs de IA),
  desplegado en Render (free tier). Riesgo aceptado y documentado: el free
  tier de Render duerme el servicio tras inactividad, y la primera
  request tras dormir puede superar el timeout fijo de 30s del cliente
  Android (RNF-008/SC-008), mostrando un falso "Timeout" aunque el backend
  esté sano. Nuevo requisito: RF-074 (ver Assumptions).

### Session 2026-09-23 (continuación) — Proveedor de IA sin costo

El backend de EcoGPT (RF-073) se había implementado con Claude/Anthropic,
una API paga. El usuario aclaró un requisito de negocio no capturado
antes: este es un proyecto escolar sin presupuesto, y debe funcionar
íntegramente sin que el usuario tenga que pagar por ningún servicio de IA.

- Q: Caso hipotético — la noche antes de la entrega, se hace la demo
  final subiendo 30 fotos seguidas sin haber cargado nunca una tarjeta de
  crédito en ningún lado. ¿Qué proveedor de IA con capa 100% gratuita
  reemplaza a Claude/Anthropic detrás del backend? → A: Gemini (Google AI
  Studio, capa gratuita) — no pide tarjeta de crédito, cuota diaria
  generosa en los modelos Flash, y soporte nativo de visión (imagen +
  texto en una sola llamada), igual que ya estaba armado con Claude. Se
  descartó OpenRouter (modelos ":free") por catálogo de modelos gratuitos
  con visión menos estable en el tiempo. RF-073 se actualiza para reflejar
  esto: reemplaza "Requisito derivado" implícito de que el proveedor de IA
  debe tener una capa de uso sin costo. Ver research.md §2.1 (corrección
  post-QA ronda 2) y `backend/README.md`.

## User Scenarios & Testing *(mandatory)*

Las historias de usuario (US) están agrupadas por épica del producto. Cada una es
independientemente testeable y priorizada según su aporte al MVP (P1 = crítica,
P2 = importante, P3 = deseable).

### Épica 1: Autenticación, Registro y Preferencias de Usuario

#### US1 - Registro de una cuenta nueva (Priority: P1)

Como persona interesada en adoptar hábitos sostenibles, quiero crear una cuenta en
EcoSmart con mi correo y contraseña, o mediante mi cuenta de Google, para poder
acceder a las actividades y empezar a sumar puntos.

**Why this priority**: sin una cuenta no existe ningún otro flujo del producto; es
el punto de entrada obligatorio del MVP.

**Independent Test**: se puede probar completando el formulario de registro (o el
flujo de Google) y verificando que la cuenta queda creada y el usuario autenticado,
sin depender de ninguna otra historia.

**Acceptance Scenarios**:

1. **Given** que la persona no tiene cuenta, **When** completa correo, contraseña,
   nombre, apellido, nombre de usuario, dirección y teléfono y confirma el registro,
   **Then** el sistema crea la cuenta y la deja autenticada.
2. **Given** que la persona elige "Continuar con Google", **When** autoriza el acceso
   con su cuenta de Google, **Then** el sistema crea la cuenta asociada a ese correo
   y la deja autenticada.
3. **Given** que el correo ingresado ya está registrado, **When** la persona intenta
   registrarse nuevamente con ese correo, **Then** el sistema rechaza el registro e
   informa que el correo ya está en uso.

---

#### US2 - Selección inicial de categorías de interés (Priority: P1)

Como usuario recién registrado, quiero elegir qué categorías de actividades me
interesan (Reciclar, Reutilizar, Caminar) para que la Home me muestre solo lo
relevante para mí.

**Why this priority**: condiciona directamente qué contenido ve el usuario desde
el primer uso; sin esto la Home no puede filtrar actividades.

**Independent Test**: se puede probar completando el checkbox de categorías durante
la configuración inicial y verificando que la preferencia queda guardada.

**Acceptance Scenarios**:

1. **Given** que el usuario configura su cuenta por primera vez, **When** marca
   "Reciclar" y "Caminar" en el checkbox de categorías, **Then** el sistema guarda
   esas dos categorías como preferencia activa.
2. **Given** que el usuario marca las tres categorías disponibles, **When** confirma
   la selección, **Then** el sistema las guarda marcadas individualmente (no como
   una única opción agregada "todas").
3. **Given** que el usuario no marca ninguna categoría, **When** intenta continuar,
   **Then** el sistema le impide avanzar y solicita seleccionar al menos una.

---

#### US3 - Edición de perfil, credenciales y preferencias (Priority: P2)

Como usuario ya registrado, quiero poder editar mis datos de perfil, mi contraseña
y mis categorías de interés en cualquier momento, para mantener mi cuenta
actualizada.

**Why this priority**: no bloquea el primer uso del producto, pero es necesaria
para la retención y confianza del usuario a mediano plazo.

**Independent Test**: se puede probar modificando un dato de perfil (p. ej. el
teléfono) o una categoría de interés desde la sesión activa y verificando que el
cambio persiste tras cerrar y reabrir la app.

**Acceptance Scenarios**:

1. **Given** que el usuario está autenticado, **When** edita su dirección y
   teléfono desde su perfil, **Then** el sistema guarda los nuevos valores y los
   refleja en las siguientes pantallas.
2. **Given** que el usuario está autenticado, **When** cambia su contraseña
   ingresando la actual y la nueva, **Then** el sistema actualiza la credencial y
   exige la nueva contraseña en el siguiente login.
3. **Given** que el usuario tenía marcada solo "Caminar", **When** agrega
   "Reciclar" desde la edición de preferencias, **Then** la Home pasa a mostrar
   también las actividades de Reciclar.

---

### Épica 2: Exploración, Home y Contenido Informativo

#### US4 - Ver actividades filtradas en la Home (Priority: P1)

Como usuario, quiero ver en la Home solo las actividades de las categorías que me
interesan, agrupadas y etiquetadas con claridad, para elegir rápido qué hacer.

**Why this priority**: es la pantalla principal de uso diario; sin ella el usuario
no puede descubrir actividades para sumar puntos.

**Independent Test**: se puede probar iniciando sesión con preferencias ya
configuradas y verificando que solo aparecen tarjetas de las categorías elegidas,
separadas por sección.

**Acceptance Scenarios**:

1. **Given** que el usuario tiene marcado solo "Reciclar", **When** abre la Home,
   **Then** solo ve la sección "Reciclar" con sus actividades disponibles.
2. **Given** que el usuario tiene marcadas las tres categorías, **When** abre la
   Home, **Then** ve tres secciones claramente etiquetadas, una por categoría.
3. **Given** que una tarjeta de actividad se muestra en la Home, **When** el
   usuario la observa sin ingresar al detalle, **Then** ve foto referencial,
   puntos a ganar, descripción corta y el botón "Realizarla", sin ver todavía los
   pasos ni el resultado esperado.

---

#### US5 - Ver el detalle de una actividad (Priority: P2)

Como usuario, quiero abrir el detalle de una actividad para conocer los pasos a
seguir y el resultado esperado antes de realizarla.

**Why this priority**: mejora la tasa de éxito en la verificación (menos rechazos
de IA por falta de información), pero el usuario podría igualmente intentar la
actividad sin leer el detalle.

**Independent Test**: se puede probar tocando una tarjeta desde la Home y
verificando que se muestran pasos y resultado esperado, ausentes en la tarjeta.

**Acceptance Scenarios**:

1. **Given** que el usuario toca una tarjeta de actividad en la Home, **When** se
   abre el detalle, **Then** el sistema muestra los pasos a seguir y el resultado
   esperado de esa actividad.

---

#### US6 - Consultar contenido educativo (Priority: P3)

Como usuario interesado en sostenibilidad, quiero ver artículos y videos sobre
cuidado del medio ambiente, organizados por categoría, para aprender más allá de
las actividades puntuables.

**Why this priority**: aporta valor educativo y de retención, pero no es
indispensable para el circuito de puntuación del MVP.

**Independent Test**: se puede probar abriendo la sección informativa y
verificando que el contenido está ordenado por categoría.

**Acceptance Scenarios**:

1. **Given** que el usuario abre la sección informativa, **When** filtra por una
   categoría, **Then** ve únicamente artículos y videos asociados a esa categoría.

---

#### US7 - Ver Puntos Verdes cercanos (Priority: P2)

Como usuario, quiero ver los puntos de reciclaje oficiales cercanos a mi
dirección, para saber dónde llevar físicamente mis materiales.

**Why this priority**: complementa directamente la actividad de Reciclar, aunque
el flujo de verificación por foto puede funcionar sin esta sección.

**Independent Test**: se puede probar abriendo la sección "Puntos Verdes
Cercanos" con una dirección de usuario configurada y verificando que se listan
ubicaciones basadas en fuentes oficiales de CABA.

**Acceptance Scenarios**:

1. **Given** que el usuario tiene una dirección configurada en su perfil,
   **When** abre "Puntos Verdes Cercanos", **Then** el sistema muestra los puntos
   de reciclaje oficiales más próximos a esa dirección.

---

### Épica 3: Verificación de Actividades y Sistema de Puntuación

#### US8 - Verificar una actividad de caminata (Priority: P1)

Como usuario, quiero que el sistema compare mi meta de caminata con los datos de
mi podómetro para que, si la cumplo, se apruebe automáticamente y sume puntos.

**Why this priority**: es uno de los tres mecanismos centrales de puntuación del
MVP y no depende de servicios externos de IA.

**Independent Test**: se puede probar proponiendo una meta, simulando el avance
del podómetro y verificando el resultado Aprobado/No aprobado según corresponda.

**Acceptance Scenarios**:

1. **Given** que el usuario acepta una meta de caminata, **When** presiona
   "Realizar" y el podómetro del dispositivo registra un avance igual o mayor a
   la meta, **Then** el sistema marca la actividad como Aprobada y otorga los
   puntos correspondientes.
2. **Given** que el usuario acepta una meta de caminata, **When** presiona
   "Realizar" y el podómetro registra un avance menor a la meta, **Then** el
   sistema informa que la meta aún no se cumplió, sin otorgar puntos, y permite
   reintentar más tarde.

---

#### US9 - Verificar una actividad de Reciclar o Reutilizar con IA (Priority: P1)

Como usuario, quiero subir una foto con mi descripción y que la IA (EcoGPT) la
evalúe, para que se aprueben o rechacen mis puntos de forma automática y justa.

**Why this priority**: es el mecanismo central de las dos categorías principales
de puntuación (Reciclar, Reutilizar); sin esta historia no hay MVP.

**Independent Test**: se puede probar subiendo una foto con descripción para una
actividad de Reciclar o Reutilizar y verificando cada una de las tres respuestas
posibles de la IA (Aprobado, Rechazado, Indeterminado).

**Acceptance Scenarios**:

1. **Given** que el usuario está en el formulario de verificación de una
   actividad de Reciclar o Reutilizar, **When** adjunta una foto (cámara o
   galería) y escribe su propia descripción, **Then** el sistema habilita el
   envío hacia EcoGPT.
2. **Given** que el sistema envió la foto y la descripción a EcoGPT, **When**
   la IA responde Aprobado, **Then** el sistema suma los puntos de esa actividad
   y muestra las opciones "Atrás" y "Ver progreso".
3. **Given** que el sistema envió la foto y la descripción a EcoGPT, **When**
   la IA responde Rechazado o Indeterminado, **Then** el sistema muestra el
   motivo del fallo y ofrece "Volver a intentar" o "Atrás", sin sumar puntos.
4. **Given** que el usuario ya usó su tope diario de fotos de una categoría,
   **When** un intento previo de ese mismo día fue Indeterminado, **Then**
   ese intento Indeterminado NO cuenta contra el tope diario (solo las fotos
   Aprobadas lo consumen).
5. **Given** que el usuario adjunta una foto idéntica a otra ya aprobada
   anteriormente por él mismo, **When** envía la verificación, **Then** el
   sistema rechaza el envío por duplicación, sin sumar puntos ni consumir el
   tope diario.

---

#### US10 - Aplicar topes diarios y acumular puntos (Priority: P1)

Como usuario, quiero que el sistema respete los límites diarios de cada
categoría al sumar mis puntos, para que la puntuación sea justa y consistente
con las reglas del programa.

**Why this priority**: protege la integridad del sistema de puntos, que es el
principal incentivo del producto.

**Independent Test**: se puede probar repitiendo una misma actividad varias
veces en el mismo día y verificando que, al alcanzar el tope, el sistema deja de
otorgar puntos y de permitir nuevos envíos para esa categoría.

**Acceptance Scenarios**:

1. **Given** que el usuario ya obtuvo 5 fotos aprobadas de Reutilizar en el día,
   **When** intenta enviar una sexta verificación de Reutilizar, **Then** el
   sistema le impide iniciar el envío e indica que alcanzó el tope diario.
2. **Given** que el usuario ya obtuvo 1 foto aprobada de Reciclar en el día,
   **When** intenta enviar una segunda verificación de Reciclar, **Then** el
   sistema le impide iniciar el envío e indica que alcanzó el tope diario.
3. **Given** que el usuario todavía no alcanzó el tope diario de una categoría,
   **When** completa una nueva verificación aprobada de esa categoría, **Then**
   el sistema suma los puntos correspondientes al total acumulado del usuario.

---

### Épica 4: Perfil, Métricas e Historial

#### US11 - Ver métricas del perfil (Priority: P2)

Como usuario, quiero ver mi puntaje total, mi participación por categoría, mi
racha de días consecutivos y mi nivel, para entender mi progreso general.

**Why this priority**: refuerza la motivación y retención, pero no bloquea el
circuito de generación de puntos del MVP.

**Independent Test**: se puede probar completando actividades en distintas
categorías y días, y verificando que el perfil refleja los totales, porcentajes,
racha y nivel correctos.

**Acceptance Scenarios**:

1. **Given** que el usuario completó actividades aprobadas en dos categorías
   distintas, **When** abre su perfil, **Then** ve el porcentaje de
   participación correspondiente a cada categoría.
2. **Given** que el usuario completó al menos una actividad aprobada por día
   durante 3 días consecutivos, **When** abre su perfil, **Then** ve su
   racha actual de días consecutivos.
3. **Given** que el usuario no completó ninguna actividad aprobada durante un
   día calendario completo, **When** vuelve a completar una actividad al día
   siguiente, **Then** el sistema reinicia el conteo de racha desde 1.

---

#### US12 - Consultar el historial de actividades (Priority: P3)

Como usuario, quiero ver un resumen de mis últimas actividades y poder
desplegar el historial completo, para revisar lo que hice.

**Why this priority**: es una funcionalidad de consulta que no afecta la
generación de puntos ni el flujo principal de uso diario.

**Independent Test**: se puede probar completando varias actividades y
verificando que el perfil muestra las 3 más recientes, con un botón "Ver más"
que despliega el listado completo.

**Acceptance Scenarios**:

1. **Given** que el usuario completó más de 3 actividades, **When** abre su
   perfil, **Then** ve únicamente las 3 actividades más recientes en el
   contenedor inicial del historial.
2. **Given** que el usuario está viendo el contenedor inicial del historial,
   **When** presiona "Ver más", **Then** el sistema despliega el listado
   completo de actividades realizadas.

---

### Épica 5: Gestión de Permisos del Dispositivo

#### US13 - Otorgar o gestionar permisos del dispositivo (Priority: P1)

Como usuario, quiero que la app me pida los permisos necesarios luego de
iniciar sesión, y que me explique cómo habilitarlos si los rechazo, para poder
usar todas las funciones sin confusión.

**Why this priority**: casi todas las actividades del MVP (caminata, foto,
Puntos Verdes) dependen de al menos un permiso de dispositivo; sin esta
historia el resto de las épicas queda bloqueado en la práctica.

**Independent Test**: se puede probar denegando un permiso específico (p. ej.
Cámara) y verificando que el sistema bloquea el formulario correspondiente y
muestra la guía paso a paso, sin afectar actividades que no dependen de ese
permiso.

**Acceptance Scenarios**:

1. **Given** que el usuario acaba de iniciar sesión por primera vez, **When**
   se le solicitan los permisos, **Then** el sistema pide explícitamente acceso
   a Galería, Cámara, GPS y Podómetro.
2. **Given** que el usuario denegó el permiso de Cámara, **When** intenta
   completar un formulario de verificación que requiere tomar una foto,
   **Then** el sistema le impide continuar y muestra una alerta explicativa con
   los pasos para habilitar el permiso desde la configuración del dispositivo.
3. **Given** que el usuario denegó el permiso de Podómetro pero otorgó el de
   Cámara, **When** intenta verificar una actividad de Reciclar, **Then** el
   sistema le permite continuar con normalidad, ya que esa actividad no
   depende del podómetro.

---

### Edge Cases

- **Tope diario superado**: si el usuario intenta iniciar una nueva
  verificación de una categoría cuyo tope diario ya fue alcanzado, el sistema
  DEBE impedir el inicio del formulario y mostrar un mensaje indicando el tope
  y cuándo se renueva (al día siguiente).
- **Corte de conexión durante el envío a EcoGPT**: si se pierde la conexión a
  internet mientras se envía la foto/prompt, el sistema DEBE informar el
  error de forma clara, conservar la foto y la descripción ya ingresadas, y
  permitir reintentar el envío sin duplicar el consumo del tope diario.
- **Lecturas anómalas del podómetro**: si el podómetro reporta un salto de
  pasos físicamente imposible en el intervalo verificado, el sistema DEBE
  rechazar esa lectura como inválida para la verificación y solicitar al
  usuario reintentar la actividad en condiciones normales, sin otorgar puntos
  por la lectura anómala.
- **Denegación permanente de permisos**: si el usuario deniega un permiso de
  forma permanente ("no volver a preguntar"), el sistema DEBE seguir
  mostrando la alerta explicativa con la guía paso a paso hacia la
  configuración del dispositivo cada vez que el usuario intente usar una
  función que dependa de ese permiso.
- **Imagen corrupta o formato no soportado**: si la imagen seleccionada desde
  la galería está corrupta o en un formato no soportado, el sistema DEBE
  rechazarla antes de enviarla a EcoGPT, informar el motivo y permitir elegir
  otra imagen, sin consumir el tope diario.
- **Sin Puntos Verdes en el radio de la dirección**: si no existen Puntos
  Verdes registrados dentro del radio de 3 km de la dirección del usuario,
  el sistema DEBE mostrar un mensaje indicando que no se encontraron puntos
  cercanos, en lugar de dejar la sección vacía sin explicación.
- **Fotografía duplicada**: si el usuario envía una fotografía idéntica a
  una ya aprobada anteriormente por él mismo, el sistema DEBE rechazar el
  envío, informar el motivo y solicitar una fotografía nueva, sin descontar
  el tope diario de esa categoría.
- **Timeout de EcoGPT**: si EcoGPT no responde dentro de los 30 segundos
  definidos en RNF-008, el sistema DEBE mostrar un error de timeout (no
  Rechazado ni Indeterminado), conservar la foto y la descripción ya
  ingresadas, y permitir reintentar el envío sin consumir el tope diario.

## Requirements *(mandatory)*

### Functional Requirements

**Autenticación, Registro, Perfil y Preferencias (RF-001 a RF-010)**

- **RF-001**: El sistema DEBE permitir crear una cuenta con correo electrónico,
  contraseña, nombre, apellido, nombre de usuario, dirección y teléfono.
- **RF-002**: El sistema DEBE permitir crear una cuenta mediante autenticación
  con Google.
- **RF-003**: El sistema DEBE validar que el correo electrónico tenga un
  formato válido y no esté previamente registrado antes de crear la cuenta.
- **RF-004**: El sistema DEBE exigir que la contraseña tenga al menos 8
  caracteres y combine letras y números; una contraseña compuesta
  únicamente por números o únicamente por letras DEBE rechazarse.
- **RF-005**: El sistema DEBE presentar, durante el registro o la primera
  configuración de la cuenta, un checkbox para elegir categorías de interés
  (Reciclar, Reutilizar, Caminar).
- **RF-006**: Si el usuario selecciona las tres categorías de interés, el
  sistema DEBE guardarlas marcadas individualmente.
- **RF-007**: El sistema DEBE permitir editar los datos de perfil (nombre,
  apellido, nombre de usuario, barrio — ver RF-063, teléfono) en cualquier
  momento desde la sesión activa.
- **RF-008**: El sistema DEBE permitir editar el correo electrónico y la
  contraseña de login en cualquier momento desde la sesión activa.
- **RF-009**: El sistema DEBE permitir editar las categorías de interés en
  cualquier momento después del registro inicial.
- **RF-010**: El sistema DEBE mantener al usuario autenticado hasta que cierre
  sesión explícitamente o su sesión expire.

**Home, Detalle de Actividades, Contenido Educativo y Puntos Verdes (RF-011 a RF-020)**

- **RF-011**: La Home DEBE mostrar únicamente actividades de las categorías de
  interés seleccionadas por el usuario.
- **RF-012**: La Home DEBE agrupar las actividades en secciones separadas por
  categoría, con etiquetas claras.
- **RF-013**: Cada tarjeta de actividad DEBE mostrar foto referencial, puntos
  a ganar, descripción corta y botón "Realizarla".
- **RF-014**: El sistema DEBE ocultar los pasos a seguir y el resultado
  esperado de una actividad hasta que el usuario ingrese a su detalle.
- **RF-015**: La vista de detalle de actividad DEBE mostrar los pasos a
  seguir y el resultado esperado.
- **RF-016**: El sistema DEBE ofrecer una sección informativa con artículos y
  videos sobre cuidado del medio ambiente, ordenados por categoría.
- **RF-017**: El layout de la sección informativa DEBE permitir incorporar
  nuevo contenido sin requerir un rediseño estructural.
- **RF-018**: El sistema DEBE mostrar una sección "Puntos Verdes de tu
  Barrio" con ubicaciones de reciclaje que coincidan con el barrio del
  perfil del usuario (RF-063), cargadas según el mecanismo definido en
  RF-052. *(Revisado en la sesión 2026-09-22: reemplaza la versión anterior
  basada en radio GPS de 3 km — ver RF-053.)*
- **RF-019**: Los datos de Puntos Verdes DEBEN originarse en fuentes
  oficiales de la Ciudad de Buenos Aires.
- **RF-020**: Si no existen Puntos Verdes en el barrio del usuario, el
  sistema DEBE informarlo explícitamente (ver Edge Cases).

**Verificación (Podómetro / EcoGPT), Estados de IA, Topes Diarios y Suma de Puntos (RF-021 a RF-035)**

- **RF-021**: El sistema DEBE permitir al usuario iniciar una actividad de
  caminata proponiendo o aceptando una meta.
- **RF-022**: Al presionar "Realizar" en una actividad de caminata, el
  sistema DEBE comparar el progreso del podómetro del dispositivo contra la
  meta propuesta.
- **RF-023**: Si el progreso del podómetro alcanza o supera la meta, el
  sistema DEBE marcar la actividad como Aprobada y otorgar los puntos
  correspondientes.
- **RF-024**: Si el progreso del podómetro no alcanza la meta, el sistema
  DEBE informarlo sin otorgar puntos, permitiendo reintentar la actividad más
  tarde.
- **RF-025**: Para las actividades de Reciclar y Reutilizar, el sistema DEBE
  permitir adjuntar una foto desde la Cámara o desde la Galería.
- **RF-026**: El sistema DEBE exigir una descripción propia del usuario junto
  con la foto antes de habilitar el envío de la verificación.
- **RF-027**: El sistema DEBE enviar a EcoGPT un prompt que combine la foto,
  el resultado esperado de la actividad y la descripción del usuario.
- **RF-028**: Si EcoGPT responde Aprobado, el sistema DEBE ofrecer las
  opciones "Atrás" (volver al inicio) y "Ver progreso".
- **RF-029**: Si EcoGPT responde Rechazado o Indeterminado, el sistema DEBE
  mostrar el motivo del fallo devuelto por la IA.
- **RF-030**: Ante un resultado Rechazado o Indeterminado, el sistema DEBE
  ofrecer las opciones "Volver a intentar" (reingresar foto/descripción) o
  "Atrás" (volver a la Home sin sumar puntos).
- **RF-031**: El sistema DEBE otorgar 50 puntos por cada bloque de 133
  pasos registrados por el podómetro (equivalente aproximado a 100 metros
  caminados, según RF-048 y RF-049).
- **RF-032**: El sistema DEBE otorgar 100 puntos por cada foto aprobada de
  Reutilizar, con un máximo de 5 fotos aprobadas por día.
- **RF-033**: El sistema DEBE otorgar 50 puntos por cada foto aprobada de
  Reciclar, con un máximo de 1 foto aprobada por día.
- **RF-034**: El sistema DEBE permitir repetir una actividad el mismo día
  mientras no se haya alcanzado su tope diario.
- **RF-035**: El sistema DEBE impedir el envío de una nueva verificación para
  una categoría cuyo tope diario ya fue alcanzado, informando el motivo.

**Historial, Racha, Niveles de Usuario y Alertas/Guías de Permisos (RF-036 a RF-045)**

- **RF-036**: El perfil DEBE mostrar el total de puntos acumulados por el
  usuario.
- **RF-037**: El perfil DEBE mostrar el porcentaje de participación por
  categoría sobre el total de actividades realizadas.
- **RF-038**: El perfil DEBE mostrar la racha de días consecutivos en que el
  usuario completó al menos una actividad aprobada.
- **RF-039**: El sistema DEBE interrumpir la racha si transcurre un día
  calendario completo sin ninguna actividad aprobada.
- **RF-040**: El perfil DEBE mostrar el nivel de usuario alcanzado según el
  puntaje histórico acumulado, con los umbrales definidos en RF-046.
- **RF-041**: El perfil DEBE mostrar un contenedor inicial con las últimas 3
  actividades recientes del historial.
- **RF-042**: El sistema DEBE ofrecer un botón "Ver más" que despliegue el
  historial completo de actividades.
- **RF-043**: Tras el login, el sistema DEBE solicitar permisos de Galería,
  Cámara, GPS y Podómetro.
- **RF-044**: Si el usuario deniega uno o más permisos, el sistema DEBE
  impedir completar los formularios de actividad que dependan de ese permiso.
- **RF-045**: Ante un permiso denegado, el sistema DEBE mostrar una alerta
  explicativa con una guía paso a paso para habilitarlo desde la
  configuración del dispositivo.

**Resolución de Ambigüedades y Decisiones de Negocio (RF-046 a RF-059)**

- **RF-046**: El sistema DEBE calcular el nivel de usuario según su puntaje
  histórico acumulado, con 4 niveles fijos: Semilla (0–499 puntos), Brote
  (500–1.499 puntos), Planta (1.500–3.499 puntos) y Árbol (3.500 puntos en
  adelante).
- **RF-047**: El nivel de usuario NUNCA DEBE descender ni consumir puntos al
  subir de nivel; depende exclusivamente del puntaje histórico acumulado.
- **RF-048**: El sistema DEBE convertir los pasos registrados por el
  podómetro a metros usando la equivalencia 1 paso = 0,75 metros (133 pasos
  ≈ 100 metros), únicamente con fines informativos/de visualización (p. ej.
  mostrar "caminaste X metros hoy").
- **RF-049**: El sistema DEBE otorgar puntos de caminata por cada bloque
  completo de 133 pasos registrados por el podómetro, contados
  directamente sobre los pasos (no sobre los metros convertidos por
  RF-048); los pasos remanentes que no completen un bloque de 133 NO
  otorgan puntos fraccionados.
- **RF-050**: Un resultado "Indeterminado" de EcoGPT NO DEBE descontar del
  tope diario de fotos de esa categoría (igual que "Rechazado"); solo las
  fotos con resultado "Aprobado" descuentan del tope diario.
- **RF-051**: Ante un resultado "Indeterminado", el sistema DEBE solicitar
  al usuario una nueva foto con mejor iluminación o encuadre, sin penalizar
  sus intentos diarios restantes.
- **RF-052**: El sistema DEBE cargar el listado de Puntos Verdes desde un
  conjunto de datos empaquetado con la aplicación, con actualización en
  segundo plano cuando haya conexión disponible.
- **RF-053** *(reemplazado el 2026-09-22 por RF-063 — se conserva el texto
  original a modo de historial)*: El sistema DEBE mostrar únicamente los
  Puntos Verdes ubicados dentro de un radio máximo de 3 kilómetros respecto
  de la dirección del perfil del usuario (o de sus coordenadas GPS si el
  permiso está activo).
- **RF-054**: El sistema DEBE ofrecer un único rol de usuario
  ("EcoCiudadano"); no existen perfiles administrativos ni de moderación
  dentro de la aplicación móvil.
- **RF-055**: Toda validación de las fotos de Reciclar y Reutilizar DEBE
  resolverse exclusivamente mediante la evaluación de EcoGPT, sin
  intervención de moderación humana dentro de la app.
- **RF-056**: Todas las actividades del catálogo DEBEN estar disponibles
  los 365 días del año, sin restricciones de horario ni de stock; no
  aplican límites adicionales a los topes diarios por categoría ya
  definidos en RF-034 y RF-035.
- **RF-057**: El contador de tope diario por categoría DEBE reiniciarse
  automáticamente a las 00:00:00 hora local del dispositivo del usuario.
- **RF-058**: Cada verificación de Reciclar o Reutilizar DEBE requerir una
  fotografía nueva, capturada o seleccionada en el momento del envío; no se
  permite reenviar una foto ya utilizada en un envío previo.
- **RF-059**: El sistema DEBE enviar cada imagen a EcoGPT para detectar si
  es idéntica o duplicada respecto de una imagen ya aprobada previamente
  por el mismo usuario, y DEBE rechazar el envío si detecta una
  duplicación.

**Correcciones Post-QA Manual (RF-060 a RF-064, sesión 2026-09-22)**

- **RF-060**: Los campos de contraseña (login, registro, cambio de
  contraseña) DEBEN mostrar el texto ingresado oculto por defecto, con una
  opción explícita para mostrarlo/ocultarlo a pedido del usuario; nunca en
  texto plano a primera vista.
- **RF-061**: La sesión autenticada DEBE persistir entre reinicios del
  proceso de la aplicación (no solo en memoria); el usuario solo vuelve a
  ver el formulario de login si cierra sesión explícitamente o si nunca
  inició sesión.
- **RF-062**: La Home DEBE mostrar un acceso directo al perfil del usuario
  (ícono circular con su inicial, arriba a la derecha), visible en todo
  momento sobre esa pantalla.
- **RF-063**: El barrio del usuario DEBE elegirse de un desplegable cerrado
  con los 48 barrios oficiales de la Ciudad de Buenos Aires (no como texto
  libre), tanto en el registro (RF-001) como en la edición de perfil
  (RF-007). Reemplaza a RF-053: la búsqueda de Puntos Verdes (RF-018) usa
  ese barrio como clave de coincidencia exacta, no un radio geográfico.
- **RF-064**: El sistema DEBE verificar el permiso del dispositivo
  correspondiente (Cámara para Reciclar/Reutilizar, Podómetro para
  Caminar) inmediatamente antes de cualquier intento de uso del sensor
  asociado, sin excepción — nunca DEBE intentarse el acceso al sensor sin
  ese chequeo previo. Si el permiso no está otorgado, DEBE bloquear la
  acción y mostrar la guía de habilitación (RF-044/RF-045) en lugar de
  fallar sin control.

**Correcciones Post-QA Manual, ronda 2 (RF-065 a RF-069, sesión 2026-09-22 continuación)**

- **RF-065**: La sección "Puntos Verdes" DEBE mostrar los resultados en
  formato de mapa (no solo listado de texto), usando un proveedor de mapas
  que no requiera API key ni cuenta de facturación (OpenStreetMap).
- **RF-066**: El dataset de ejemplo de Puntos Verdes DEBE cubrir los 48
  barrios del valor cerrado `Barrio` (uno como mínimo por barrio), para que
  la búsqueda por barrio (RF-063) devuelva resultados sin importar cuál
  elija el usuario. El mapa DEBE centrarse en el promedio de coordenadas de
  los puntos encontrados. No se exige coincidencia geográfica real
  (polígonos de barrio/GPS) — ver Assumptions.
- **RF-067**: La vista previa de la Cámara en el formulario de
  Reciclar/Reutilizar NUNCA DEBE tapar ni bloquear visualmente los botones
  "Cámara"/"Galería"; ambos DEBEN permanecer tocables en todo momento
  mientras la vista previa está activa.
- **RF-068**: La pantalla "Perfil" DEBE mostrar todos los datos del
  usuario: los datos de cuenta (nombre, apellido, nombre de usuario,
  correo, barrio, teléfono, categorías de interés — RF-007) además de las
  métricas de actividad (puntos, % por categoría, racha, nivel — RF-036 a
  RF-040), de solo lectura, con un botón "Editar perfil" que lleva a la
  pantalla de edición (US3).
- **RF-069**: Junto al ícono de acceso al perfil en la Home (RF-062) DEBE
  mostrarse el texto "Hola, {nombreUsuario}".

**Rediseño Visual y Pasos del Día (RF-070 a RF-072, sesión 2026-09-22 continuación 2)**

- **RF-070**: La pantalla "Perfil" (RF-068) DEBE mostrar, en una tarjeta
  destacada con el color de acento de la app, la cantidad de pasos
  registrados hoy (suma de pasos de actividades de Caminar Aprobadas con
  fecha de hoy), únicamente si el permiso de Podómetro está otorgado
  (`EstadoPermiso.OTORGADO`). Si el permiso no está otorgado, la tarjeta NO
  DEBE mostrarse (nunca "0" ni un placeholder vacío).
- **RF-071**: Las pantallas de Login y Registro DEBEN reestructurarse según
  el patrón: botón "Iniciar sesión con Google" en la parte superior, un
  divisor visual ("o iniciá sesión con"), campos de formulario etiquetados
  con ancho completo, alternador de visibilidad de contraseña (RF-060) y un
  botón principal de envío en forma de píldora (bordes totalmente
  redondeados). Los colores DEBEN seguir la paleta propia de la app
  (verde/ámbar — ver RF-072), no la paleta del ejemplo de referencia.
- **RF-072**: Toda la interfaz de la app DEBE usar una paleta verde
  bosque/crema/ámbar consistente (fondo verde oscuro o crema según el
  elemento, tarjetas en tono crema/superficie clara, acentos en ámbar) con
  bordes fuertemente redondeados en tarjetas y botones, manteniendo
  jerarquía visual clara (título de sección > tarjeta destacada > detalle
  secundario) en cada pantalla.

**Backend Real de EcoGPT (RF-073/RF-074, sesión 2026-09-23)**

- **RF-073**: El sistema DEBE contar con un backend propio (`backend/`,
  fuera del módulo Android) que implemente `POST /verificaciones`
  (`contracts/openapi.yaml`) como proxy hacia una API de IA con capacidad
  de visión real, comparando en una sola evaluación la imagen, la
  descripción del usuario y `Actividad.resultadoEsperado`, antes de
  devolver Aprobado/Rechazado/Indeterminado con un motivo en tono
  alentador (RNF-001). La API key del proveedor de IA NUNCA DEBE
  embeberse en el cliente Android ni en el APK — el backend es la única
  pieza que la conoce; el cliente solo usa un secreto compartido propio
  para autenticarse contra este backend (`X-EcoGPT-Api-Key`).
- **RF-074**: Dado que el backend (RF-073) puede desplegarse en un hosting
  de capa gratuita con "cold start" (el servicio se duerme tras
  inactividad), el sistema DEBE tolerar que la primera verificación tras
  un período de inactividad falle por timeout (RNF-008) sin tratarlo como
  un defecto de la app — el usuario ve el mismo mensaje de timeout
  existente y puede reintentar (RF-030), sin necesidad de un mensaje
  distinto para este caso.
- **RF-075**: El proveedor de IA detrás del backend de EcoGPT (RF-073)
  DEBE tener una capa de uso sin costo (sin requerir tarjeta de crédito
  del usuario), consistente con el alcance de un proyecto escolar sin
  presupuesto. El cliente de IA en `backend/app/ecogpt.py` vive detrás de
  la misma interfaz interna del backend (`verificar_con_ia`), de modo que
  el proveedor pueda sustituirse en el futuro sin afectar el contrato
  `POST /verificaciones` ni el resto de la app.

### Non-Functional Requirements

- **RNF-001**: La interfaz DEBE ser intuitiva y amigable, con un tono
  motivador y ameno, evitando mensajes punitivos o culpabilizadores.
- **RNF-002**: La navegación DEBE permitir acceder a actividades, al perfil y
  a los Puntos Verdes en no más de 2 toques desde el menú principal.
- **RNF-003**: La disponibilidad y los límites diarios restantes (p. ej. "Te
  quedan 3 fotos de reutilización hoy") DEBEN ser visibles de forma clara e
  inequívoca antes de iniciar una actividad.
- **RNF-004**: La lógica de negocio y las reglas de puntuación DEBEN
  mantenerse totalmente desacopladas de las interfaces de usuario.
- **RNF-005**: Todas las validaciones importantes de negocio (límites de
  fotos, cumplimiento de la meta de podómetro, formato de entrada) DEBEN
  estar explicitadas formalmente como criterios de aceptación verificables.
- **RNF-006**: Las contraseñas de los usuarios DEBEN almacenarse cifradas
  mediante un mecanismo de cifrado reversible basado en claves (formato
  JWK/JWE); en ningún caso DEBEN almacenarse ni transmitirse en texto
  plano.
- **RNF-007**: La clave de cifrado utilizada para proteger las contraseñas
  DEBE gestionarse en un almacén de claves/secretos separado de la base de
  datos donde se guardan los usuarios, de modo que una filtración de esa
  base de datos, por sí sola, no permita descifrar las contraseñas.
- **RNF-008**: El sistema DEBE esperar como máximo 30 segundos la respuesta
  de EcoGPT a un envío de verificación; superado ese plazo, DEBE tratarlo
  como timeout (ver Edge Cases), no como un resultado Rechazado ni
  Indeterminado de la IA.

### Key Entities

- **Usuario**: representa a la persona registrada; incluye credenciales,
  datos de perfil (entre ellos el **Barrio**, un valor cerrado de los 48
  barrios de CABA — RF-063, reemplaza al campo de dirección de texto
  libre), categorías de interés, puntos totales, racha actual y nivel
  alcanzado. Posee un único rol posible, "EcoCiudadano"; el modelo no
  contempla roles administrativos ni de moderación dentro de la app móvil.
- **Actividad**: representa una actividad ofrecida por EcoSmart; incluye
  categoría, descripción corta, puntos a ganar, pasos a seguir y resultado
  esperado.
- **Registro de Verificación**: representa un intento de un usuario de
  completar una actividad en una fecha determinada; incluye la actividad
  asociada, el resultado (Aprobado / Rechazado / Indeterminado), el motivo
  informado por la IA cuando aplica, los puntos otorgados, y una referencia
  a la fotografía enviada (cuando aplica) utilizada por el sistema para
  detectar duplicados en envíos futuros del mismo usuario.
- **Categoría de Interés**: valor cerrado que agrupa las actividades y las
  preferencias del usuario (Reciclar, Reutilizar, Caminar).
- **Barrio**: valor cerrado con los 48 barrios oficiales de la Ciudad de
  Buenos Aires (RF-063); lo elige el Usuario en su perfil y es la clave de
  búsqueda de Puntos Verdes (RF-018).
- **Punto Verde**: representa una ubicación oficial de reciclaje de la
  Ciudad de Buenos Aires, con su posición geográfica y el barrio en el que
  se ubica (texto tal como lo publica la fuente oficial, no necesariamente
  idéntico en formato al valor cerrado `Barrio` del Usuario).
- **Permiso de Dispositivo**: valor cerrado con 3 estados posibles
  (`NO_SOLICITADO` / `OTORGADO` / `DENEGADO`) por cada tipo de permiso
  (Galería, Cámara, GPS, Podómetro) que un Usuario puede tener.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Una persona nueva puede completar el registro y la selección
  inicial de categorías de interés en menos de 3 minutos.
- **SC-002**: Un usuario puede completar la verificación de una actividad de
  Reciclar o Reutilizar (adjuntar foto, escribir descripción y recibir
  respuesta) en menos de 2 minutos de interacción, sin contar el tiempo de
  espera de la red.
- **SC-003**: El 100% de los intentos de verificación que exceden el tope
  diario de su categoría son bloqueados antes de generar puntos adicionales.
- **SC-004**: El 90% de los usuarios encuentra y completa su primera
  actividad desde la Home sin necesitar ayuda externa.
- **SC-005**: Un usuario puede acceder a su historial completo de
  actividades en no más de 2 toques desde el menú principal.
- **SC-006**: El 100% de los resultados Rechazado o Indeterminado de la IA
  muestran un motivo comprensible y al menos una acción disponible
  (reintentar o volver), sin dejar al usuario sin salida.
- **SC-007**: El sistema detecta y rechaza el 100% de los envíos con una
  fotografía idéntica a una ya aprobada previamente por el mismo usuario.
- **SC-008**: El 100% de las esperas de respuesta de EcoGPT que superan los
  30 segundos se resuelven como timeout explícito, sin dejar al usuario
  esperando indefinidamente ni consumir su tope diario.

## Assumptions

- Se asume que el dispositivo del usuario cuenta con sensor de podómetro y
  servicios de ubicación habilitados por el sistema operativo; si no los
  tiene, las actividades que dependan de ellos quedan fuera de alcance para
  ese dispositivo.
- Se asume conectividad a internet intermitente pero disponible para el
  envío a EcoGPT; el comportamiento sin conexión se limita al descripto en
  Edge Cases.
- **Riesgo aceptado explícitamente — backend de EcoGPT en hosting free
  tier (RF-073/RF-074)**: el backend real de EcoGPT (`backend/`) se
  despliega en Render free tier, que duerme el servicio tras inactividad;
  la primera verificación después de un período sin uso puede demorar más
  que el timeout fijo de 30s del cliente (RNF-008/SC-008) y mostrarse como
  un "Timeout" al usuario aunque el backend esté funcionando
  correctamente. Se asume que esto es aceptable para un MVP académico sin
  presupuesto de infraestructura; ver `backend/README.md` §3 para
  mitigaciones posibles (ping periódico externo, o plan pago sin sleep).
- El reinicio de los topes diarios ("un día") se calcula según la zona
  horaria del dispositivo del usuario, reiniciando a las 00:00:00 hora
  local (ver RF-057).
- Se asume que cada usuario tiene una única cuenta activa por sesión; no se
  contempla el uso simultáneo de múltiples cuentas en un mismo dispositivo
  para este alcance.
- Se asume que el dataset empaquetado de Puntos Verdes (RF-052) se
  actualiza en segundo plano cuando hay conexión; no se define en este
  alcance una periodicidad mínima garantizada de esa sincronización.
- **Alcance de precisión geográfica (RF-066)**: la búsqueda de Puntos
  Verdes por barrio (RF-063) es una coincidencia de texto contra el barrio
  elegido por el usuario en su perfil, no una detección geográfica real
  por coordenadas GPS ni por polígonos oficiales de límites de barrio. Un
  usuario que vive cerca del límite entre dos barrios puede no ver Puntos
  Verdes reales que están físicamente más cerca de él pero catalogados en
  el barrio vecino. Se asume que esto es aceptable para el MVP; una
  detección geográfica real (point-in-polygon con el dataset oficial de
  límites de barrio de CABA) queda fuera de alcance y documentada como
  mejora futura.
- **Riesgo aceptado explícitamente**: se optó por cifrado reversible
  (JWK/JWE, RNF-006) en lugar de un hash unidireccional (el estándar
  habitual para contraseñas) por decisión explícita del proyecto. Esto
  implica que, a diferencia de un hash, la seguridad de las contraseñas
  depende críticamente de que la clave de cifrado (RNF-007) nunca se filtre
  junto con la base de datos. Se recomienda revisar esta decisión en una
  futura iteración de seguridad.

## Registro de Ambigüedades

Las 4 ambigüedades originalmente detectadas quedaron **resueltas** en la sesión de
clarificación del 2026-09-20 (ver sección `## Clarifications`). Se conserva la
tabla como registro histórico de trazabilidad.

| ID | Ambigüedad detectada | Estado | Decisión final | Requisitos derivados |
|----|----------------------|--------|-----------------|------------------------|
| [AMBIGÜEDAD-001] | Umbrales del Nivel de Usuario: no estaban definidos los rangos numéricos de puntos requeridos para cada nivel. | ✅ Resuelto | 4 niveles por puntaje histórico acumulado: Semilla (0–499), Brote (500–1.499), Planta (1.500–3.499), Árbol (3.500+). El nivel nunca consume puntos. | RF-046, RF-047 |
| [AMBIGÜEDAD-002] | Conversión de Pasos a Metros: no estaba definido el estándar de conversión del podómetro a metros para la regla "50 pts / 100 m". | ✅ Resuelto | 1 paso = 0,75 m (133 pasos ≈ 100 m); los puntos se otorgan solo por bloques completos de 100 m, sin fracciones. | RF-031, RF-048, RF-049 |
| [AMBIGÜEDAD-003] | Impacto del Resultado "Indeterminado": no estaba aclarado si un intento Indeterminado consume o no el cupo de fotos diarias del usuario. | ✅ Resuelto | "Indeterminado" NO consume el tope diario (igual que "Rechazado"); solo "Aprobado" descuenta del tope. | RF-050, RF-051 |
| [AMBIGÜEDAD-004] | Sincronización de Puntos Verdes: no estaba definida la periodicidad ni el método de actualización de los datos geográficos de la fuente de CABA. | ✅ Resuelto | Dataset estático empaquetado con la app, con sincronización en segundo plano cuando hay conexión; radio de visualización de 3 km desde la dirección/GPS del usuario. | RF-052, RF-053 |
| [AMBIGÜEDAD-005] | Protección de Contraseñas: no estaba definido el mecanismo de almacenamiento seguro de contraseñas en la base de datos. | ✅ Resuelto | Cifrado reversible con clave en formato JWK/JWE (no hash unidireccional), con la clave de cifrado gestionada en un almacén de claves separado de la base de datos de usuarios. Riesgo residual documentado en Assumptions. | RNF-006, RNF-007 |
| [AMBIGÜEDAD-006] | Timeout de EcoGPT: no estaba definido un límite de tiempo de espera para la respuesta de la IA; SC-002 incluso excluía explícitamente el tiempo de red de su medición. | ✅ Resuelto | Límite de 30 segundos; superado ese plazo, el sistema trata el envío como timeout (no como Rechazado/Indeterminado), sin consumir el tope diario. | RNF-008, SC-008, Edge Case "Timeout de EcoGPT" |
| [AMBIGÜEDAD-007] | Política de Contraseña: RF-004 exigía una "política mínima de seguridad" sin cuantificarla. | ✅ Resuelto | Mínimo 8 caracteres combinando letras y números; se rechaza una contraseña compuesta solo por números o solo por letras. | RF-004 |
| [AMBIGÜEDAD-008] | Cálculo de Bloques de Caminata: no estaba definido si el bloque de 100 m/50 pts se calcula sobre metros acumulados (con redondeo) o sobre bloques fijos de pasos, dado que 133 pasos × 0,75 m = 99,75 m ≠ 100 m exactos. | ✅ Resuelto | El bloque de puntaje se cuenta directamente sobre los pasos (cada 133 pasos = 1 bloque = 50 puntos); la conversión a metros de RF-048 es solo informativa/de visualización. | RF-031, RF-048, RF-049 |
| [AMBIGÜEDAD-009] | Estados de Permiso de Dispositivo: no estaban formalizados como entidad/valor cerrado en Key Entities, pese a que US13 y RF-043–RF-045 dependen de ellos. | ✅ Resuelto | 3 estados: `NO_SOLICITADO` / `OTORGADO` / `DENEGADO` por tipo de permiso; la denegación permanente no requiere un 4º estado, ya la cubre el Edge Case correspondiente. | Key Entities → Permiso de Dispositivo |
| [AMBIGÜEDAD-010] | Ejemplo de Racha en US11: el escenario de aceptación decía "varios días consecutivos" sin cuantificar el ejemplo. | ✅ Resuelto | Se reemplazó por "3 días consecutivos" en el escenario; no altera la regla de negocio de racha (RF-038/RF-039). | US11 (escenario 2) |
| [CORRECCIÓN-001] | Campos de contraseña visibles en texto plano por defecto en Login/Registro (defecto de implementación, detectado en QA manual del 2026-09-22). | ✅ Resuelto | Ocultos por defecto, con opción explícita de mostrar/ocultar. | RF-060 |
| [CORRECCIÓN-002] | La sesión no persistía entre reinicios de la app pese a lo que pide RF-010 (defecto de implementación). | ✅ Resuelto | Persistencia en SharedPreferences; solo se pide login de nuevo tras un cierre de sesión explícito. | RF-061 |
| [CORRECCIÓN-003] | Sin atajo de navegación hacia el perfil desde la Home (omisión de diseño de UI, no cubierta explícitamente por ningún RF anterior). | ✅ Resuelto | Ícono circular con la inicial del usuario, arriba a la derecha de la Home. | RF-062 |
| [CORRECCIÓN-004] | El campo "dirección" del registro era texto libre sin relación real con los datos de Puntos Verdes, que se organizan por barrio. | ✅ Resuelto | Desplegable cerrado de 48 barrios de CABA; reemplaza el radio GPS de 3 km como mecanismo de búsqueda de Puntos Verdes. | RF-063 (reemplaza RF-053) |
| [CORRECCIÓN-005] | El botón "Realizar" de Reciclar/Reutilizar cerraba la app: `VerificacionFotoScreen` nunca chequeaba el permiso de Cámara antes de invocar CameraX (defecto de implementación — RF-044/RF-045 ya exigían el bloqueo, pero no estaba conectado en este punto de uso). | ✅ Resuelto | Chequeo de permiso real del sistema operativo antes de cualquier acceso a Cámara/Podómetro, con fallback a la guía de habilitación existente en vez de fallar sin control. | RF-064 |
| [CORRECCIÓN-006] | "Puntos Verdes" no mostraba ningún resultado para casi ningún barrio (dataset de ejemplo cubría solo 6 de 48 barrios) ni tenía formato de mapa. | ✅ Resuelto | Mapa con OpenStreetMap/osmdroid, sin API key; dataset de ejemplo ampliado a los 48 barrios. | RF-065, RF-066 |
| [CORRECCIÓN-007] | La vista previa de la Cámara tapaba visualmente los botones "Cámara"/"Galería" (defecto de implementación: `PreviewView` en modo `PERFORMANCE` usa `SurfaceView`, que se dibuja por encima de Compose sin respetar el orden del layout). | ✅ Resuelto | Modo de implementación `COMPATIBLE` en `PreviewView`. | RF-067 |
| [CORRECCIÓN-008] | "Perfil" solo mostraba métricas de actividad; los datos de cuenta vivían en una pantalla de edición nunca conectada a la navegación (inalcanzable). | ✅ Resuelto | "Perfil" ahora muestra todos los datos (cuenta + métricas); botón "Editar perfil" conecta la pantalla de edición existente. | RF-068 |
| [CORRECCIÓN-009] | Sin saludo visible junto al ícono de perfil de la Home (omisión de diseño de UI). | ✅ Resuelto | Texto "Hola, {nombreUsuario}" junto al ícono circular. | RF-069 |
| [CORRECCIÓN-010] | La interfaz no seguía ninguna paleta/forma consistente ni mostraba la cantidad de pasos del día en ningún lugar visible; Login/Registro no seguía un layout de referencia claro. | ✅ Resuelto | Paleta verde bosque/crema/ámbar con bordes redondeados en toda la app (`Theme.kt`); tarjeta destacada de "Pasos hoy" en Perfil, visible solo con permiso de Podómetro otorgado; Login/Registro reestructurados (Google arriba, divisor, campos etiquetados, botón en píldora) manteniendo la paleta propia de EcoSmart. | RF-070, RF-071, RF-072 |
| [CORRECCIÓN-011] | El flujo completo de verificación (US8/US9) fallaba siempre con "Sin conexión a internet": `ECOGPT_BASE_URL` apuntaba por defecto a un dominio RFC 2606 que nunca resuelve — "EcoGPT" nunca tuvo un backend real implementado, solo un contrato de terceros hipotético jamás contratado. | ✅ Resuelto | Backend propio en `backend/` (Python + FastAPI, desplegado en Render) que implementa `POST /verificaciones` como proxy hacia una API de IA con visión, comparando imagen + descripción del usuario contra `Actividad.resultadoEsperado`. Requiere configuración manual pendiente del usuario: desplegar el backend y completar `ECOGPT_API_KEY`/`ECOGPT_BASE_URL` en `local.properties` (ver `backend/README.md`). | RF-073, RF-074 |
| [CORRECCIÓN-012] | La primera versión del backend de EcoGPT (CORRECCIÓN-011) usaba Claude/Anthropic, una API paga — incompatible con que este es un proyecto escolar sin presupuesto para servicios de IA. | ✅ Resuelto | Se reemplazó el proveedor de IA por Gemini (Google AI Studio, capa gratuita sin tarjeta de crédito), manteniendo el mismo contrato `POST /verificaciones` y el mismo backend (`backend/app/ecogpt.py`); solo cambia el proveedor detrás de la interfaz interna. | RF-075 |
