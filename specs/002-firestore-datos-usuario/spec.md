# Feature Specification: Persistencia Remota de Datos de Usuario en Firestore

**Feature Branch**: `002-firestore-datos-usuario`

**Created**: 2026-10-01

**Status**: Draft

**Input**: User description: "Genera un spec nuevo para gestionar el almacenamiento de los
datos del usuario para que ahora no vivan únicamente dentro del dispositivo sino que se
almacenen en Firestore mediante Firebase. Nada debería guardarse en el dispositivo sino en
Firebase para que ante la desinstalación del sistema el usuario pueda volver a acceder a su
cuenta. Los datos delicados como contraseñas no pueden ser conocidos por la BDD así que
tienen que estar protegidos con un hash irreversible. Todo lo que ahora se guarda en el
dispositivo debería hacerlo en Firestore. Los registros deben sincronizarse entre
dispositivos y mantenerse al desinstalar la app o borrar sus datos."

## Relación con la Especificación 001 (`001-ecosmart-mvp`)

Este módulo **no reemplaza** a `specs/001-ecosmart-mvp/`: reemplaza únicamente su
mecanismo de **persistencia** (de local-first con Room/SharedPreferences a
remoto-autoritativo con Firestore/Firebase Authentication). Todas las historias de
usuario, reglas de negocio, entidades de dominio y requisitos funcionales de puntaje,
topes diarios, racha, niveles, verificación por IA y permisos de dispositivo definidos en
spec 001 **se mantienen sin cambios**; lo único que cambia es **dónde y cómo** se guarda
el resultado de esas reglas.

Mapeo explícito de lo que se migra (ver `001-ecosmart-mvp/data-model.md` §2 y §4):

| Dato (spec 001) | Hoy vive en (spec 001) | Pasa a vivir en (este spec) |
|---|---|---|
| `Usuario` (perfil completo: email, nombre, apellido, nombre de usuario, barrio, teléfono, categorías de interés, puntos históricos, racha, última actividad aprobada) | `UsuarioRoomEntity` (Room, tabla `usuarios`) | Documento Firestore `usuarios/{uid}` |
| Contraseña (`ContrasenaCifrada`, JWE — RNF-006/RNF-007) | `UsuarioRoomEntity.contrasenaCifradaJwe` + clave en Android Keystore | Firebase Authentication (hash unidireccional gestionado por el proveedor; no vive en Firestore) |
| Sesión activa (RF-010/RF-061) | `SesionUsuario` (SharedPreferences) | Estado de sesión de Firebase Authentication (persistente, multi-dispositivo) |
| `RegistroVerificacion` (historial de Reciclar/Reutilizar/Caminar) | `RegistroVerificacionRoomEntity` (Room) | Subcolección Firestore `usuarios/{uid}/registrosVerificacion/{id}` |
| Pasos del día (RF-070, RF-081) | `PasosDelDiaRepositoryImpl` (SharedPreferences `pasos_del_dia`) | Campo `pasosHoy` + `fecha` en `usuarios/{uid}` (ver Edge Cases sobre el cálculo local de línea base) |
| Caminata en curso (RF-082) | `CaminataEnCursoStore` (SharedPreferences `caminata_en_curso`) | Documento Firestore `usuarios/{uid}/caminataEnCurso` (singleton) |
| `PermisoDispositivo` (estado de permisos — RF-044/045/064) | `PermisoDispositivoRoomEntity` (Room) | Reflejado en `usuarios/{uid}/permisosDispositivo/{dispositivoId}_{tipo}`, un documento por dispositivo (el estado real del permiso es siempre decidido por el sistema operativo del dispositivo en uso) |

Lo que **no** cambia de lugar porque no es "dato de usuario" sino catálogo/dataset
compartido de solo lectura (RF-011, RF-052, RF-065/066): `Actividad` (catálogo) y
`PuntoVerde` (dataset de CABA) permanecen fuera de alcance de este spec.

## Clarifications

### Session 2026-10-01

- Q: Caso hipotético — un atacante compromete el proyecto de Firebase y descarga la
  colección `usuarios` completa de Firestore. Spec 001 (RNF-006/RNF-007) eligió
  explícitamente cifrado reversible JWE/JWK (no hash) para la contraseña, con la clave en
  un almacén separado. ¿Se mantiene ese mismo esquema dentro de Firestore, o cambia? → A:
  Cambia. La contraseña no se guarda en Firestore bajo ninguna forma (ni plana, ni
  cifrada, ni hasheada): el login por email/contraseña se delega íntegramente a **Firebase
  Authentication**, que almacena un hash unidireccional (no reversible, algoritmo
  gestionado por el proveedor) en un almacén de identidad separado de Firestore, fuera del
  alcance de la app y de cualquier regla de seguridad de Firestore. Esto satisface de forma
  más estricta el requisito original ("la BDD no puede conocer la contraseña") que un
  esquema JWK/JWE reversible, porque ni siquiera un hash queda accesible desde Firestore.
  Reemplaza, para este módulo en adelante, la decisión de RNF-006/RNF-007 de spec 001
  (ver enmienda de `constitution.md` v2.0.0).
- Q: El estado de los permisos de dispositivo (Cámara/Galería/GPS/Podómetro, RF-044/045)
  es, por diseño de Android, algo que el sistema operativo decide por cada instalación —
  no existe una API para "restaurar" un permiso como otorgado en un dispositivo nuevo. Con
  el requisito "nada debería guardarse en el dispositivo", ¿qué se sincroniza? → A: Se
  sincroniza el estado de permisos a Firestore solo con fines informativos/históricos (p.
  ej. para mostrarle al usuario en qué dispositivos usó la app con qué permisos); el
  comportamiento funcional de bloqueo de formularios (RF-044/045/064) sigue
  evaluando el permiso real del sistema operativo del dispositivo actual en cada uso, ya
  que no puede ser de otra manera dado un límite físico de la plataforma Android.
- Q: El conteo de "pasos de hoy" (RF-070/RF-081) depende de una lectura directa del sensor
  físico `TYPE_STEP_COUNTER` del dispositivo (que cuenta pasos acumulados desde el último
  reinicio de ese equipo en particular). ¿Esa lectura también se elimina del dispositivo? →
  A: El valor resultante ("pasos de hoy") se escribe en Firestore en cada actualización, y
  es ese valor remoto el que se muestra en el Perfil (RF-070). El cálculo de la línea base
  del sensor (offset necesario para convertir "conteo acumulado desde el reinicio" en
  "pasos de hoy") sigue siendo una operación puramente local y efímera, porque depende de
  hardware físico distinto en cada dispositivo — no es un dato de negocio, es un detalle de
  implementación del sensor, y se recalcula sin pérdida de información funcional ante una
  reinstalación.
- Q: ¿Qué pasa con los usuarios que ya tenían una cuenta creada bajo spec 001 (datos solo
  en Room, sin ningún documento en Firestore todavía) al actualizar a la versión de la app
  que incluye este módulo? → A: Migración única y automática: la primera vez que esa
  instalación abre la app tras la actualización, si detecta datos locales de Room sin
  documento equivalente en Firestore, los sube una sola vez (perfil, historial completo,
  racha, puntos) y recién entonces Firestore pasa a ser la fuente de verdad; from ese
  momento, los datos locales dejan de leerse para decisiones de negocio.
- Q: El SDK cliente de Firestore mantiene, por diseño, una caché local en el dispositivo
  para soportar lectura/escritura sin conexión — lo cual técnicamente "guarda algo en el
  dispositivo". ¿Esto contradice el requisito "nada debería guardarse en el dispositivo"? →
  A: No se considera una violación del requisito: es una caché técnica gestionada
  enteramente por el SDK de Firestore (no una base de datos propia de la app como Room),
  se reconstruye automáticamente desde el servidor, y se elimina junto con la app al
  desinstalarla sin pérdida de datos (el servidor conserva la copia autoritativa). La
  distinción relevante para este spec es: ninguna tabla/almacén propio de la app
  (Room, SharedPreferences) puede ser la única copia de un dato de usuario.
- Q: Hoy (spec 001) el dominio Kotlin calcula `puntosHistoricos`, `rachaActual` y
  `nivel()` en el propio dispositivo y los escribe en Room, un almacén local confiable
  por definición. Al mover esto a Firestore, ¿quién queda autorizado a escribir esos
  campos: el cliente Android directamente (protegido solo por Reglas de Seguridad de
  Firestore), o un componente de servidor de confianza? → A: Un componente de servidor de
  confianza (Cloud Functions, o una extensión del backend EcoGPT ya existente en Render,
  usando credenciales de Firebase Admin SDK) es el **único** autorizado a escribir
  `puntosHistoricos`, `rachaActual`, `nivel` y el campo `puntosOtorgados`/`resultado` de
  cada `RegistroVerificacion`. El cliente Android nunca escribe esos campos directamente;
  las Reglas de Seguridad de Firestore DEBEN rechazar cualquier intento del cliente de
  escribirlos. Esto aplica a las **tres** categorías (Caminar, Reciclar, Reutilizar), no
  solo a las verificadas por IA: para Caminar, que hoy (RF-008/RF-009 de spec 001) se
  resuelve 100% en el dispositivo comparando el podómetro contra la meta sin ninguna
  llamada de red, este módulo agrega la obligación de que el resultado de esa comparación
  se envíe también a ese componente de servidor de confianza para que sea él quien
  otorgue los puntos — ver RF-D014/RF-D015.
- Q: SC-D002 exige reflejar un cambio en menos de 10 segundos "con conexión estable", y
  las Acceptance Scenarios de US2 aceptan tanto "volver a primer plano" como "refrescar"
  como disparadores válidos. ¿La sincronización entre dispositivos debe ser en tiempo real
  (listener permanente tipo `onSnapshot`) o bajo demanda (recarga al entrar a la pantalla o
  pull-to-refresh manual)? → A: Bajo demanda, sin listener permanente: la app recarga los
  datos remotos al entrar o volver a una pantalla, o mediante un gesto explícito de
  refresco (pull-to-refresh). Se descarta un listener en tiempo real constante por
  simplicidad y porque consume más cuota de lecturas de Firestore, relevante dado el límite
  diario gratuito ya discutido. Ver RF-D017.
- Q: `caminataEnCurso` es un documento singleton por usuario y "pasos de hoy" se actualiza
  en cada lectura del podómetro. Con Firestore compartido entre dispositivos, ¿qué evita
  que dos dispositivos con la misma cuenta inicien dos caminatas a la vez, o que sus
  escrituras de "pasos de hoy" se pisen entre sí? → A: El mismo componente de servidor de
  confianza de la pregunta anterior arbitra `caminataEnCurso` y `pasosHoy` mediante una
  transacción atómica de Firestore: si ya existe una caminata activa, rechaza el intento de
  iniciar una segunda (consistente con RF-082 de spec 001, ahora aplicado entre
  dispositivos y no solo dentro de uno); las actualizaciones de progreso de pasos se
  aplican también de forma transaccional para que no se pisen entre sí. Ver
  RF-D007/RF-D008 actualizados.
- Q: La capa gratuita "Spark" de Firebase tiene un límite diario de lecturas/escrituras
  compartido entre **todos** los usuarios del proyecto (no por usuario); si se agota,
  Firestore rechaza operaciones para todos hasta el reinicio diario de cuota. ¿Qué debe ver
  el usuario si una lectura/escritura es rechazada por esta causa? → A: El mismo
  tratamiento ya definido para "sin conexión" (spec 001): se reintenta automáticamente más
  tarde sin que el usuario pierda progreso, sin un mensaje de error distinto que distinga
  esta causa de una falla de red real — para el usuario final la experiencia es la misma
  ("no se pudo sincronizar ahora"). Ver Edge Cases y Assumptions.

### Session 2026-10-01 (continuación) — Resolución de hallazgos de `checklists/quality.md`

- Q: RF-D006 exige que Room/SharedPreferences, si se usan, se limiten a una caché de
  lectura reconstruible; RF-D011 no aclaraba qué pasa con los datos locales viejos (de la
  era pre-Firestore) una vez migrados. ¿Se conservan como respaldo, se reconvierten en
  caché, o se borran? → A: Se borran una vez confirmada la migración exitosa a Firestore;
  el rol de caché de lectura de RF-D006 aplica hacia adelante (datos leídos de Firestore
  después de la migración), no al remanente de la era pre-Firestore, para que no quede
  ambigüedad sobre cuál es la fuente de verdad. Ver RF-D011 actualizado.
- Q: Spec 001 (RF-074/RF-079) ya documenta que el backend en Render free-tier "duerme"
  tras inactividad, mitigado con un ping a `/health` + 1 reintento automático para EcoGPT.
  Con RF-D015, Caminar pasa a depender del mismo tipo de componente de servidor para
  otorgar puntos — un flujo que hoy no tenía ninguna dependencia de red. ¿Se mitiga el
  mismo riesgo de cold-start para este nuevo flujo? → A: Sí, se aplica la misma mitigación
  ya validada en RF-079: ping best-effort al abrir la pantalla de Caminar + reintento
  automático, por consistencia con el mecanismo ya probado para el mismo tipo de backend.
  Ver RF-D015 actualizado.
- Q: Una clarificación anterior ya decía que el espejo de permisos serviría "para
  mostrarle al usuario en qué dispositivos usó la app con qué permisos", pero no se
  definió el esquema concreto. ¿Se registra por dispositivo, o alcanza con un estado
  global por tipo de permiso? → A: Se registra por dispositivo: un documento por
  combinación dispositivo+tipo de permiso, con estado y fecha de actualización, porque es
  la única forma de cumplir literalmente la promesa de "en qué dispositivos" — un estado
  global por tipo perdería esa información apenas el usuario use un segundo dispositivo.
  Ver RF-D009 y Key Entities actualizados.
- Q: US5 dice que el usuario "confirma la eliminación de su cuenta", pero no aclaraba si
  el borrado es inmediato y definitivo o si existe un período de gracia para arrepentirse.
  ¿Cuál es el comportamiento correcto? → A: Borrado inmediato y definitivo apenas el
  usuario confirma: no hay período de gracia ni forma de recuperar la cuenta después de
  confirmar. La pantalla de confirmación previa a la acción es la única salvaguarda contra
  un borrado accidental. Ver RF-D012 y US5 actualizados.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recuperar la cuenta tras desinstalar la app (Priority: P1)

Como usuario que desinstaló EcoSmart (o cambió de celular), quiero poder volver a
instalarla, iniciar sesión con mi mismo email y contraseña (o Google), y encontrar mi
perfil, mi historial y mi puntaje exactamente como los dejé, para no perder mi progreso.

**Why this priority**: es la razón de ser de este módulo; sin esto, migrar a Firestore no
aporta ningún valor sobre el esquema local-first de spec 001.

**Independent Test**: se puede probar creando una cuenta, sumando puntos con al menos una
actividad aprobada, desinstalando la app, reinstalándola e iniciando sesión con las mismas
credenciales, verificando que el perfil, el historial y el puntaje coinciden exactamente
con los previos a la desinstalación.

**Acceptance Scenarios**:

1. **Given** un usuario con cuenta existente y actividades aprobadas, **When** desinstala
   la app y la reinstala en el mismo dispositivo, **Then** al iniciar sesión recupera su
   perfil completo, su historial de verificaciones y su puntaje sin pérdidas.
2. **Given** un usuario con cuenta existente, **When** instala la app en un dispositivo
   distinto al que usó originalmente e inicia sesión con las mismas credenciales, **Then**
   ve exactamente el mismo perfil, historial y puntaje que en el dispositivo original.
3. **Given** un usuario que nunca inició sesión en un dispositivo nuevo, **When** la app
   arranca sin sesión activa, **Then** no se muestra ningún dato de un usuario distinto ni
   datos residuales de instalaciones anteriores.

---

### User Story 2 - Sincronizar cambios entre dispositivos (Priority: P1)

Como usuario que usa EcoSmart en más de un dispositivo con la misma cuenta, quiero que una
actividad aprobada o un cambio de perfil hecho en un dispositivo se refleje en el otro, para
no tener puntajes o historiales distintos según el dispositivo que use.

**Why this priority**: es el segundo objetivo explícito del módulo (sincronización entre
dispositivos), junto con la persistencia ante desinstalación.

**Independent Test**: se puede probar iniciando sesión con la misma cuenta en dos
dispositivos (o dos instalaciones), aprobando una actividad en uno de ellos con conexión a
internet, y verificando que el otro refleja el nuevo puntaje e historial al reabrir o
refrescar la pantalla correspondiente.

**Acceptance Scenarios**:

1. **Given** la misma cuenta abierta en dos dispositivos con conexión a internet, **When**
   se aprueba una actividad en el Dispositivo A, **Then** el Dispositivo B refleja el nuevo
   puntaje, racha e historial al volver a primer plano o refrescar.
2. **Given** la misma cuenta abierta en dos dispositivos, **When** se edita el perfil
   (teléfono, barrio, categorías de interés) desde el Dispositivo A, **Then** el
   Dispositivo B muestra los datos actualizados la próxima vez que consulta el perfil.
3. **Given** un Dispositivo B sin conexión a internet en el momento del cambio, **When**
   recupera la conexión, **Then** termina reflejando el estado más reciente de la cuenta
   sin intervención manual del usuario.

---

### User Story 3 - Contraseñas fuera del alcance de la base de datos (Priority: P1)

Como responsable de la seguridad de EcoSmart, quiero que ninguna contraseña de usuario sea
recuperable ni visible desde la base de datos de la aplicación, para que una eventual
filtración de Firestore no exponga credenciales de acceso.

**Why this priority**: es un requisito de seguridad explícito y no negociable del pedido
original; sin esto, migrar a una base de datos externa (con una superficie de ataque
potencialmente mayor que un archivo local en el dispositivo) sería una regresión de
seguridad, no una mejora.

**Independent Test**: se puede probar inspeccionando los documentos de la colección
`usuarios` de Firestore de una cuenta de prueba y verificando que ningún campo contiene la
contraseña en texto plano, cifrada ni hasheada — la credencial completa vive únicamente en
Firebase Authentication, fuera de Firestore.

**Acceptance Scenarios**:

1. **Given** un usuario recién registrado con email y contraseña, **When** se inspecciona
   su documento en Firestore, **Then** no existe ningún campo de contraseña (ni plano, ni
   cifrado, ni hash) en ese documento.
2. **Given** un usuario que cambia su contraseña desde "Editar perfil" (RF-007 de spec
   001), **When** se completa el cambio, **Then** el nuevo valor queda únicamente en
   Firebase Authentication y el usuario puede loguearse con la nueva contraseña desde
   cualquier dispositivo.
3. **Given** una filtración hipotética de la colección `usuarios` de Firestore, **When** un
   atacante la descarga completa, **Then** no obtiene ninguna contraseña ni hash de
   contraseña de ningún usuario.

---

### User Story 4 - Migración de cuentas existentes de spec 001 (Priority: P2)

Como usuario que ya tenía una cuenta creada bajo la versión local-first de EcoSmart (spec
001, datos solo en Room), quiero que al actualizar la app mis datos pasen automáticamente a
Firestore, para no tener que volver a registrarme ni perder mi historial previo.

**Why this priority**: sin esto, los usuarios existentes antes de este módulo perderían su
progreso al pasar a ser requerido el login remoto, lo cual es inaceptable, pero no bloquea
la posibilidad de probar el flujo completo con una cuenta nueva (US1-US3).

**Independent Test**: se puede probar con una instalación que ya tiene datos de Room
poblados (perfil + historial), actualizando la app a la versión con este módulo y
verificando que, tras el primer inicio de sesión, esos mismos datos aparecen reflejados en
Firestore sin duplicados ni pérdidas.

**Acceptance Scenarios**:

1. **Given** una instalación con datos locales de Room de una cuenta de spec 001, **When**
   se abre la app actualizada por primera vez e inicia sesión, **Then** el sistema sube el
   perfil y el historial completo a Firestore una única vez.
2. **Given** que la migración anterior ya se completó para una cuenta, **When** la app se
   vuelve a abrir, **Then** no se repite la subida ni se duplican registros de historial.

---

### User Story 5 - Eliminación de cuenta y sus datos (Priority: P3)

Como usuario que decide dejar de usar EcoSmart, quiero poder eliminar mi cuenta para que
mis datos dejen de existir en la base de datos remota.

**Why this priority**: es una expectativa razonable de cualquier sistema que ahora
almacena datos personales fuera del dispositivo del usuario, pero no bloquea el valor
central de recuperación/sincronización de las historias P1.

**Independent Test**: se puede probar eliminando una cuenta de prueba desde su configuración
y verificando que su documento y subcolecciones ya no existen en Firestore ni es posible
iniciar sesión con esas credenciales.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado, **When** confirma la eliminación de su cuenta, **Then**
   el sistema borra de forma inmediata y definitiva su documento de perfil, su historial y
   su credencial de Firebase Authentication, sin período de gracia ni posibilidad de
   recuperación posterior.
2. **Given** una cuenta ya eliminada, **When** alguien intenta iniciar sesión con esas
   credenciales, **Then** el sistema rechaza el intento como si la cuenta nunca hubiera
   existido.

---

### Edge Cases

- ¿Qué pasa si un usuario pierde la conexión a internet justo después de que una foto es
  **Aprobada** por EcoGPT (spec 001, US9) pero antes de que el registro se guarde en
  Firestore? El sistema debe reintentar automáticamente apenas vuelva la conexión, sin que
  el usuario pierda los puntos otorgados ni tenga que repetir la verificación.
- ¿Qué pasa si dos dispositivos con la misma cuenta editan el mismo campo de perfil (p. ej.
  el teléfono) en simultáneo, estando ambos sin conexión, y luego ambos recuperan la
  conexión? Se resuelve por "última escritura gana" según el reloj del servidor, no del
  dispositivo (evita inconsistencias por relojes locales desincronizados).
- ¿Qué pasa si un usuario intenta registrarse con un email ya usado en Firebase
  Authentication, pero no en Firestore (p. ej. cuenta eliminada a medias)? El registro se
  rechaza igual que en RF-003 de spec 001 (correo ya en uso).
- ¿Qué pasa si un usuario intenta leer o escribir el documento de otro usuario manipulando
  la app (p. ej. interceptando o alterando una request)? Las reglas de seguridad de
  Firestore deben rechazar cualquier lectura/escritura que no corresponda a la cuenta
  autenticada actual, sin excepción.
- ¿Qué pasa si falla la migración automática de datos locales (US4) a mitad de camino (p.
  ej. se corta la conexión mientras se sube el historial)? El sistema debe poder reanudar o
  reintentar la migración en el siguiente inicio de la app sin duplicar registros ya
  subidos.
- ¿Qué pasa si el usuario borra los datos de la app (Configuración de Android → Borrar
  datos) en vez de desinstalarla? Debe comportarse igual que una reinstalación: al volver a
  iniciar sesión, todos los datos se recuperan desde Firestore.
- ¿Qué pasa si Firestore rechaza una operación porque se agotó la cuota diaria gratuita
  compartida del proyecto (no un corte de conexión del usuario)? Se trata igual que "sin
  conexión": reintento automático posterior, mismo mensaje al usuario, sin pérdida de
  progreso (ver Clarifications).
- ¿Qué pasa si una escritura es rechazada por un motivo no transitorio (reglas de
  seguridad, error de validación del componente de servidor de confianza)? Tras un número
  acotado de reintentos, el sistema debe mostrar un error explícito con una acción concreta
  (reintentar manualmente o volver), no reintentar en silencio para siempre (ver
  Clarifications y RF-D016).

## Requirements *(mandatory)*

### Functional Requirements

- **RF-D001**: El sistema DEBE almacenar el perfil completo del usuario (email, nombre,
  apellido, nombre de usuario, barrio, teléfono, categorías de interés, puntos históricos,
  racha actual, última actividad aprobada — ver `Usuario` en
  `001-ecosmart-mvp/data-model.md` §2.1) en un documento remoto de Firestore, que es la
  única fuente de verdad para esos datos.
- **RF-D002**: El sistema DEBE delegar el registro, inicio de sesión y almacenamiento de
  credenciales (email/contraseña y Google) a Firebase Authentication. Ningún campo de
  contraseña (en texto plano, cifrado o hasheado) DEBE existir en ningún documento de
  Firestore.
- **RF-D003**: El sistema DEBE almacenar cada verificación de actividad (Reciclar,
  Reutilizar, Caminar — ver `RegistroVerificacion` en `001-ecosmart-mvp/data-model.md`
  §2.3) en Firestore, asociada de forma inequívoca al usuario autenticado que la generó.
  Las consultas sobre esta subcolección DEBEN soportar el orden y la paginación que ya
  exige spec 001 (últimas 3 + "ver más", RF-041/RF-042).
- **RF-D004**: El sistema DEBE escribir automáticamente en Firestore, sin acción manual del
  usuario, cualquier cambio de perfil o nueva verificación aprobada, en cuanto haya
  conexión a internet disponible. Esto aplica únicamente a los campos que el cliente tiene
  permitido escribir (todos salvo los restringidos por RF-D014). Reflejar ese cambio en
  **otro** dispositivo con la misma cuenta ocurre bajo demanda (al entrar o volver a la
  pantalla correspondiente, o mediante un gesto explícito de refresco), no mediante un
  listener permanente en tiempo real (ver RF-D017).
- **RF-D005**: El sistema DEBE permitir que un usuario recupere el 100% de su perfil,
  historial de verificaciones, puntaje y racha al iniciar sesión en cualquier dispositivo
  (incluida una reinstalación en el mismo dispositivo o una instalación en un dispositivo
  distinto), sin depender de ningún dato remanente en el dispositivo anterior.
- **RF-D006**: El sistema NO DEBE depender de Room ni de SharedPreferences como única copia
  de ningún dato de usuario cubierto por este spec (perfil, historial, racha, puntaje,
  caminata en curso, pasos del día). Room/SharedPreferences, si se usan, DEBEN limitarse a
  una caché de lectura reconstruible desde Firestore.
- **RF-D007**: El sistema DEBE sincronizar a Firestore el conteo de pasos del día (RF-070 de
  spec 001) en cada actualización, aplicada de forma transaccional por el componente de
  servidor de confianza (ver RF-D014) para que actualizaciones casi simultáneas desde dos
  dispositivos no se pisen entre sí, para que el valor esté disponible en cualquier
  dispositivo y sobreviva a una desinstalación; el cálculo de la línea base del sensor
  físico del podómetro permanece como detalle de implementación local (ver Clarifications).
  Este conteo es puramente informativo y no otorga puntos por sí solo — el otorgamiento de
  puntos de una caminata con meta cumplida se rige por RF-D015, un mecanismo distinto.
- **RF-D008**: El sistema DEBE sincronizar a Firestore el estado de la caminata en curso
  (RF-082 de spec 001: una sola caminata activa a la vez, vence a medianoche), de modo que
  el progreso sea visible desde cualquier dispositivo con la misma cuenta. El inicio de una
  caminata DEBE validarse mediante una transacción atómica del componente de servidor de
  confianza, que rechaza el intento si ya existe una caminata activa iniciada desde
  cualquier otro dispositivo de la misma cuenta.
- **RF-D009**: El sistema DEBE registrar en Firestore, con fines informativos/históricos,
  el estado de los permisos de dispositivo otorgados (RF-044/045/064 de spec 001), **por
  dispositivo** (identificador de instalación + tipo de permiso + fecha de actualización),
  para que el usuario pueda ver en qué dispositivos otorgó qué permisos. Esto es
  puramente informativo: la decisión funcional de bloquear un formulario siempre se basa
  en el permiso real del sistema operativo del dispositivo en uso en ese momento (ver
  Clarifications).
- **RF-D010**: El sistema DEBE rechazar, mediante reglas de seguridad del lado del
  servidor, cualquier intento de lectura o escritura de datos de un usuario que no sea el
  autenticado en ese momento (aislamiento entre cuentas distintas; para la restricción de
  campos específicos dentro del propio documento de un usuario, ver RF-D014).
- **RF-D011**: El sistema DEBE migrar automáticamente, una única vez por cuenta, los datos
  locales de Room de instalaciones existentes bajo spec 001 (perfil e historial) hacia
  Firestore, la primera vez que esa instalación inicie sesión tras incorporar este módulo,
  sin duplicar registros en inicios posteriores. Una vez confirmada la migración exitosa,
  el sistema DEBE borrar esos datos locales pre-Firestore (no reconvertirlos en caché ni
  conservarlos como respaldo); el rol de caché de lectura de RF-D006 aplica únicamente a
  datos leídos de Firestore después de ese punto.
- **RF-D012**: El sistema DEBE permitir que un usuario elimine su cuenta, lo cual incluye
  borrar su documento de perfil, todas sus subcolecciones (historial, caminata en curso,
  permisos) y su credencial de Firebase Authentication. El borrado DEBE ser inmediato y
  definitivo apenas el usuario confirma la acción, sin período de gracia ni posibilidad de
  recuperación posterior; la pantalla de confirmación previa es la única salvaguarda
  contra un borrado accidental.
- **RF-D013**: El sistema DEBE seguir aplicando, sin cambios, todas las reglas de negocio ya
  definidas en spec 001 sobre los datos migrados (tope diario por categoría RF-032/033/035,
  cálculo de racha RF-038/039, niveles RF-040/046/047, detección de fotos duplicadas
  RF-058/059); este spec solo cambia el medio de persistencia, no las reglas de negocio.
- **RF-D014**: El sistema NO DEBE permitir que el cliente Android escriba directamente los
  campos `puntosHistoricos`, `rachaActual`, `nivel` de `usuarios/{uid}`, ni el
  `resultado`/`puntosOtorgados` de ningún documento de `registrosVerificacion`. Esos campos
  DEBEN ser escritos exclusivamente por un componente de servidor de confianza; las Reglas
  de Seguridad de Firestore DEBEN rechazar cualquier escritura del cliente sobre ellos.
- **RF-D015**: Para la categoría Caminar, el sistema DEBE enviar el resultado de comparar
  el podómetro contra la meta (hoy resuelto enteramente en el dispositivo, RF-008/RF-009 de
  spec 001) a ese mismo componente de servidor de confianza, que es quien valida y otorga
  los puntos correspondientes — extiende a Caminar la misma garantía anti-manipulación que
  ya aplicaba naturalmente a Reciclar/Reutilizar por pasar siempre por EcoGPT. El sistema
  DEBE aplicar a este nuevo flujo la misma mitigación de cold-start ya definida en RF-079
  de spec 001 (ping best-effort al abrir la pantalla de Caminar + un reintento automático),
  ya que Caminar pasa a depender por primera vez de este tipo de backend.
- **RF-D016**: El sistema DEBE distinguir fallas transitorias (sin conexión, cuota diaria
  agotada — reintento automático indefinido sin mensaje de error, RF-D004) de fallas no
  transitorias (rechazo por Reglas de Seguridad, error de validación del componente de
  servidor de confianza). Para estas últimas, tras **1 reintento automático** (consistente
  con RF-079 de spec 001), el sistema DEBE mostrar un mensaje de error explícito con una
  acción concreta (reintentar manualmente o volver), en tono no punitivo (Principio IX de
  `constitution.md`), en vez de reintentar en silencio indefinidamente.
- **RF-D017**: El sistema NO DEBE depender de un listener de Firestore en tiempo real
  permanente para reflejar cambios entre dispositivos; DEBE recargar los datos remotos al
  entrar o volver a primer plano en una pantalla relevante (Home, Perfil, Historial), o
  ante un gesto explícito de refresco (pull-to-refresh) del usuario.

### Key Entities *(datos remotos)*

- **Documento de perfil (`usuarios/{uid}`)**: equivalente remoto de `Usuario` (spec 001),
  identificado por el `uid` de Firebase Authentication en lugar del `UsuarioId` local;
  conserva los mismos atributos y reglas de negocio (nivel, racha, suma de puntos), solo
  cambia su medio de persistencia.
- **Subcolección de historial (`usuarios/{uid}/registrosVerificacion`)**: equivalente
  remoto de `RegistroVerificacion` (spec 001), una entrada por verificación realizada.
- **Documento de caminata en curso (`usuarios/{uid}/caminataEnCurso`)**: equivalente remoto
  del estado hoy guardado en `CaminataEnCursoStore` (spec 001).
- **Credencial de autenticación (Firebase Authentication, fuera de Firestore)**: reemplaza a
  `ContrasenaCifrada`/JWE (spec 001) para este módulo; nunca es un documento de Firestore.
- **Registro de permisos (`usuarios/{uid}/permisosDispositivo/{dispositivoId}_{tipo}`)**:
  un documento por combinación dispositivo+tipo de permiso (`tipo`, `estado`,
  `actualizadoEn`), espejo informativo del estado de `PermisoDispositivo` (spec 001); no es
  la fuente de verdad funcional del bloqueo de formularios (ver RF-D009).
- **Componente de servidor de confianza (otorgamiento de puntos)**: Cloud Function o
  extensión del backend EcoGPT (Render), con credenciales de Firebase Admin SDK; único
  autorizado a escribir `puntosHistoricos`, `rachaActual`, `nivel` y el resultado de cada
  `RegistroVerificacion` (ver RF-D014/RF-D015); también arbitra `caminataEnCurso` y
  `pasosHoy` mediante transacciones atómicas de Firestore para resolver concurrencia entre
  dispositivos (ver RF-D007/RF-D008). El cliente Android solo lee estos campos. La elección
  concreta entre Cloud Function y extensión del backend EcoGPT se deja deliberadamente
  abierta para `/speckit.plan`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-D001**: El 100% de los usuarios que reinstalan la app o cambian de dispositivo
  recuperan su perfil, historial y puntaje completos al iniciar sesión con las mismas
  credenciales, sin ninguna pérdida de datos.
- **SC-D002**: Un cambio de perfil, una actividad aprobada, el estado de la caminata en
  curso o el conteo de pasos del día en un dispositivo se reflejan en otro dispositivo con
  la misma cuenta en menos de 10 segundos, con conexión estable en ambos.
- **SC-D003**: Una auditoría de la base de datos remota (inspección de todos los documentos
  de la colección `usuarios` y sus subcolecciones) no encuentra ningún campo de contraseña
  en texto plano, cifrado ni hasheado en el 100% de las cuentas.
- **SC-D004**: El 100% de las cuentas existentes bajo spec 001 (con datos previos solo en
  Room) conservan su perfil e historial completos tras su primera apertura de la app
  actualizada con este módulo.
- **SC-D005**: El 100% de las solicitudes de eliminación de cuenta resultan, dentro de
  **60 segundos**, en la desaparición completa y verificable del documento de perfil, sus
  subcolecciones y la credencial de Firebase Authentication asociada.
- **SC-D006**: El 100% de los intentos del cliente Android de escribir directamente
  `puntosHistoricos`, `rachaActual`, `nivel` o el `resultado`/`puntosOtorgados` de un
  `RegistroVerificacion` son rechazados por las Reglas de Seguridad de Firestore (RF-D014).

## Assumptions

- El proyecto de Firebase (Firestore + Firebase Authentication) se configura en la capa
  "free tier" (Spark), consistente con el presupuesto cero del proyecto ya documentado en
  spec 001 (`research.md` §2.1); si el volumen de uso excediera ese nivel, se reevaluará en
  una iteración posterior, fuera de alcance de este spec. Un agotamiento puntual de la
  cuota diaria compartida se trata como una falla de red transitoria más (ver Edge Cases),
  no como un caso de error distinto a nivel de UX.
- El dispositivo del usuario cuenta con conexión a internet al menos de forma intermitente;
  mientras no la tenga, la app puede operar en modo lectura/escritura en cola sobre la caché
  local del SDK de Firestore, pero no se considera "sincronizado" hasta que la conexión
  vuelve (ver Edge Cases).
- Cada usuario tiene una única cuenta activa por sesión (misma suposición que spec 001); el
  uso simultáneo de la misma cuenta en dos dispositivos es compatible con este spec, pero no
  se definen roles ni permisos distintos entre esos dispositivos.
- El catálogo de Actividades y el dataset de Puntos Verdes de CABA (spec 001) siguen
  gestionándose igual que hoy (datos compartidos de solo lectura, no datos de usuario) y
  quedan fuera de alcance de este spec.
- La migración automática de cuentas existentes (US4) asume que el usuario vuelve a iniciar
  sesión explícitamente al menos una vez tras la actualización; no hay forma de migrar
  cuentas de instalaciones que nunca vuelven a abrirse.
- La credencial de Firebase Admin SDK que usa el componente de servidor de confianza para
  escribir los campos restringidos (RF-D014) DEBE aprovisionarse y protegerse como secreto
  de servidor, de forma análoga a `ECOGPT_SHARED_API_KEY` en spec 001 (nunca embebida en el
  cliente Android ni en el repositorio).
