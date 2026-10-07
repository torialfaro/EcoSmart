---

description: "Task list template for feature implementation"
---

# Tasks: Persistencia Remota de Datos de Usuario en Firestore

**Input**: Design documents from `specs/002-firestore-datos-usuario/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Tests**: Incluidos. `constitution.md` Principio VIII exige tests para toda regla de
negocio crítica (puntaje, topes diarios, racha), y el Constitution Check de `plan.md`
marcó explícitamente este punto como "pendiente de `/speckit.tasks`". Como esas reglas
ahora viven en el backend de confianza (research.md §1) y el control de acceso crítico
vive en Reglas de Seguridad de Firestore (RF-D010/RF-D014), los tests cubren ambas capas.

**Organization**: Tareas agrupadas por historia de usuario (spec.md), en el mismo orden
de prioridad (US1-US3 = P1, US4 = P2, US5 = P3).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario a la que pertenece (US1-US5)
- Las rutas de archivo son las reales del repositorio (ver `plan.md` § Project Structure)

## Path Conventions

- Cliente Android: `app/src/main/kotlin/com/ecosmart/...` (módulo único `:app`, sin cambios de spec 001)
- Backend: `backend/app/...` (mismo servicio FastAPI de EcoGPT, extendido)
- Tests Android: `app/src/test/kotlin/...` (unitarios) y `app/src/androidTest/kotlin/...` (instrumentados)
- Tests backend: `backend/tests/...`
- Infraestructura de Firebase compartida: raíz del repo (`firestore.rules`, `firebase.json`)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependencias y configuración de proyecto para que Firebase esté disponible
en ambos lados (cliente y backend), sin lógica de negocio todavía.

- [X] T001 Agregar `firebase-bom`, `firebase-auth-ktx`, `firebase-firestore-ktx` y
      `play-services-auth` a `gradle/libs.versions.toml` (sección `[versions]`/`[libraries]`)
- [X] T002 Declarar el plugin `com.google.gms.google-services` en `build.gradle.kts`
      (raíz, `apply false`) y aplicarlo en `app/build.gradle.kts`, agregando las
      dependencias de T001
- [X] T003 [P] Agregar `app/google-services.json` a `.gitignore` (nunca se commitea, ver
      quickstart.md §2) y crear `app/google-services.json.example` como plantilla
      documentada
- [X] T004 [P] Agregar `firebase-admin` a `backend/requirements.txt`
- [X] T005 [P] Documentar las variables de entorno nuevas
      (`FIREBASE_SERVICE_ACCOUNT_JSON`, `FIRESTORE_EMULATOR_HOST`) en `backend/README.md`,
      consistente con quickstart.md §5
- [X] T006 [P] Crear `firebase.json` en la raíz del repo configurando los emuladores de
      Firestore y Authentication (quickstart.md §4)
- [X] T007 [P] Copiar `specs/002-firestore-datos-usuario/contracts/firestore.rules` a
      `firestore.rules` en la raíz del repo (fuente real desplegable, quickstart.md §3)

**Checkpoint**: proyecto compila con las dependencias de Firebase; emuladores arrancan
con `firebase emulators:start --only firestore,auth`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura compartida por todas las historias — Hilt providers,
cliente del backend de confianza, verificación de ID Token, mappers de Firestore. Ninguna
historia de usuario puede empezar hasta que esta fase esté completa.

**⚠️ CRITICAL**: No continuar a la Fase 3 sin completar esta fase.

- [X] T008 [P] Implementar `FirebaseAuthModule` (Hilt, provee `FirebaseAuth`) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/firebase/FirebaseAuthModule.kt`
- [X] T009 [P] Implementar `FirestoreModule` (Hilt, provee `FirebaseFirestore`) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/firebase/FirestoreModule.kt`
- [X] T010 [P] Implementar `firebase_admin_setup.py` (inicialización del Admin SDK +
      dependencia FastAPI `verificar_id_token` que extrae el `uid` verificado) en
      `backend/app/firebase_admin_setup.py` (research.md §3)
- [X] T011 [US-foundation] Montar el router de los 3 endpoints nuevos de
      `contracts/openapi.yaml` (`/registros-verificacion/reciclar-reutilizar`,
      `/registros-verificacion/caminar`, `/migracion/subir-datos-locales`) como esqueleto
      en `backend/app/puntos.py`, montado en `backend/app/main.py` (depende de T010)
- [X] T011a [US-foundation] Reimplementar la regla de tope diario por categoría
      (RF-032/033/035 de spec 001, equivalente a `AplicarTopeDiario`) en
      `backend/app/puntos.py` (RF-D013) — depende de T011
- [X] T011b [US-foundation] Reimplementar el cálculo de racha y nivel
      (RF-038/039/040/046/047 de spec 001, equivalente a `Usuario.sumarPuntos()`/
      `registrarActividadAprobadaHoy()`/`nivel()`) en `backend/app/puntos.py` (RF-D013) —
      depende de T011
- [X] T011c [US-foundation] Reimplementar la detección de fotos duplicadas por hash
      perceptual (RF-058/059 de spec 001, equivalente a `RegistroVerificacion.esDuplicadoDe()`)
      en `backend/app/puntos.py` (RF-D013) — depende de T011
- [X] T011d [P] Test de paridad: los mismos casos de tope diario/racha/nivel/duplicado ya
      cubiertos por los tests de dominio de `001-ecosmart-mvp` producen el mismo
      resultado en la reimplementación Python (T011a-T011c), en
      `backend/tests/test_paridad_reglas_negocio.py` — evita que las dos implementaciones
      (Kotlin en spec 001, Python en este módulo) diverjan silenciosamente (Principio V)
- [X] T011e [P] Implementar `DispositivoIdProvider` (identificador estable de
      instalación, `Settings.Secure.ANDROID_ID` persistido una vez) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/firebase/DispositivoIdProvider.kt`
      — usado por T037 (caminata) y T050 (permisos)
- [X] T012 [P] Implementar `FirebaseIdTokenInterceptor` (adjunta
      `Authorization: Bearer <idToken>` a cada request) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/firebase/FirebaseIdTokenInterceptor.kt`
      (depende de T008)
- [X] T013 Implementar `BackendConfianzaClient` (interfaz Retrofit de
      `contracts/openapi.yaml`) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/network/BackendConfianzaClient.kt`
- [X] T014 Agregar el provider de `BackendConfianzaClient` (Retrofit + OkHttp con
      `FirebaseIdTokenInterceptor`) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/di/NetworkModule.kt` (depende de
      T012, T013)
- [X] T015 [P] Implementar `UsuarioFirestoreMapper` (`usuarios/{uid}` ↔ `Usuario`,
      data-model.md §2) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/firestore/UsuarioFirestoreMapper.kt`
- [X] T016 [P] Implementar `RegistroVerificacionFirestoreMapper` (subcolección ↔
      `RegistroVerificacion`, data-model.md §3) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/firestore/RegistroVerificacionFirestoreMapper.kt`
- [X] T017 [P] Rules unit tests de RF-D010/RF-D014 contra el emulador de Firestore
      (usuario A no puede leer/escribir `usuarios/B`; ningún usuario puede escribir
      `puntosHistoricos`/`rachaActual`/`nivel`/`resultado`/`puntosOtorgados`; el Admin SDK
      sí puede) en `backend/tests/test_firestore_rules.py` (quickstart.md §7)

**Checkpoint**: Hilt provee `FirebaseAuth`/`FirebaseFirestore`/`BackendConfianzaClient`;
el backend valida ID Tokens y expone los 3 endpoints nuevos; las Reglas de Seguridad
están probadas contra el emulador. Las historias de usuario pueden empezar.

---

## Phase 3: User Story 1 - Recuperar la cuenta tras desinstalar la app (Priority: P1) 🎯 MVP

**Goal**: Un usuario que reinstala la app o cambia de dispositivo recupera su perfil,
historial y puntaje completos iniciando sesión con las mismas credenciales.

**Independent Test**: crear cuenta, aprobar 1 actividad, desinstalar, reinstalar, iniciar
sesión → verificar que perfil/historial/puntaje coinciden (ver quickstart.md §6).

### Tests for User Story 1

- [X] T018 [P] [US1] Test de integración: `UsuarioRepositoryImpl` escribe y lee
      `usuarios/{uid}` contra el emulador de Firestore, en
      `app/src/androidTest/kotlin/com/ecosmart/infrastructure/persistence/UsuarioRepositoryImplTest.kt`
- [X] T019 [P] [US1] Test de integración: `RegistroVerificacionRepositoryImpl` lee el
      historial completo de la subcolección `registrosVerificacion` contra el emulador,
      en
      `app/src/androidTest/kotlin/com/ecosmart/infrastructure/persistence/RegistroVerificacionRepositoryImplTest.kt`

### Implementation for User Story 1

- [X] T020 [US1] Reescribir `UsuarioRepositoryImpl` para leer/escribir
      `usuarios/{uid}` vía `FirebaseFirestore` + `UsuarioFirestoreMapper` (RF-D001,
      RF-D005), usando `FieldValue.serverTimestamp()` en `actualizadoEn` para que
      ediciones concurrentes desde dos dispositivos se resuelvan por última escritura
      según el reloj del servidor (Edge Case de spec.md), eliminando la dependencia de
      `UsuarioDao`/Room para lectura de negocio, en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/UsuarioRepositoryImpl.kt`
      (depende de T009, T015)
- [X] T021 [US1] Reescribir `RegistroVerificacionRepositoryImpl` para leer
      `usuarios/{uid}/registrosVerificacion` ordenado por `creadoEn` descendente (RF-D003)
      vía `FirebaseFirestore` + `RegistroVerificacionFirestoreMapper`, en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/RegistroVerificacionRepositoryImpl.kt`
      (depende de T009, T016)
- [X] T022 [US1] Actualizar `SesionUsuario` para exponer el `uid` del usuario autenticado
      de `FirebaseAuth.currentUser` en vez de `SharedPreferences` propias, en
      `app/src/main/kotlin/com/ecosmart/infrastructure/session/SesionUsuario.kt` (depende
      de T008)
- [X] T023 [US1] Actualizar `RepositoryModule` (Hilt) para que `UsuarioRepository`/
      `RegistroVerificacionRepository` sigan resolviendo a las implementaciones de T020/T021
      sin cambios de firma, en
      `app/src/main/kotlin/com/ecosmart/infrastructure/di/RepositoryModule.kt`
- [X] T024 [US1] Actualizar `PerfilViewModel`/`HistorialViewModel` para refrescar al
      volver a primer plano (ya exigido por RF-077 de spec 001, ahora respaldado por datos
      remotos) en
      `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilViewModel.kt` y
      `app/src/main/kotlin/com/ecosmart/presentation/profile/HistorialViewModel.kt`
- [X] T024a [US1] Reescribir `VerificarFotoConIA` para que, tras recibir el veredicto de
      EcoGPT, llame a `BackendConfianzaClient.otorgarPuntosReciclarReutilizar(...)` en vez
      de `aplicarTopeDiario.registrarResultado(...)` local (RF-D014/RF-D015 — sin esto,
      Reciclar/Reutilizar seguirían otorgando puntos localmente, bypasseando Firestore),
      en `app/src/main/kotlin/com/ecosmart/application/activity/VerificarFotoConIA.kt`
      (depende de T013, T014, T020)
- [X] T024b [US1] Test de integración: `VerificarFotoConIA` con veredicto APROBADO
      escribe el registro vía `BackendConfianzaClient` y nunca escribe
      `puntosHistoricos`/`rachaActual` directamente desde el cliente, en
      `app/src/androidTest/kotlin/com/ecosmart/application/activity/VerificarFotoConIATest.kt`
      (depende de T024a)

**Checkpoint**: la cuenta sobrevive a una desinstalación/reinstalación o a un dispositivo
nuevo, y las tres categorías (Caminar, Reciclar, Reutilizar) otorgan puntos exclusivamente
vía el backend de confianza — US1 es demostrable de forma independiente (MVP de este
módulo).

---

## Phase 4: User Story 3 - Contraseñas fuera del alcance de la base de datos (Priority: P1)

> Se implementa antes que US2 porque US1 ya depende de poder autenticarse — sin login
> funcionando no hay forma de validar US1 independientemente.

**Goal**: Ninguna contraseña es recuperable ni visible desde Firestore; el login se
delega íntegramente a Firebase Authentication.

**Independent Test**: inspeccionar `usuarios/{uid}` de una cuenta de prueba en Firebase
Console y confirmar que no existe ningún campo de contraseña (quickstart.md §6).

### Tests for User Story 3

- [X] T025 [P] [US3] Test unitario: `RegistrarUsuario`/`IniciarSesion` usan
      `FirebaseAuth.createUserWithEmailAndPassword`/`signInWithEmailAndPassword` y nunca
      construyen un `ContrasenaCifrada`/JWE, en
      `app/src/test/kotlin/com/ecosmart/application/auth/RegistrarUsuarioTest.kt` y
      `app/src/test/kotlin/com/ecosmart/application/auth/IniciarSesionTest.kt`
- [X] T026 [P] [US3] Test de integración: un documento `usuarios/{uid}` recién creado no
      contiene ningún campo de contraseña (SC-D003), en
      `app/src/androidTest/kotlin/com/ecosmart/infrastructure/persistence/UsuarioRepositoryImplTest.kt`
      (agregar caso a T018)

### Implementation for User Story 3

- [X] T027 [US3] Reescribir `RegistrarUsuario` para crear la cuenta vía
      `FirebaseAuth.createUserWithEmailAndPassword` y el documento inicial `usuarios/{uid}`
      (sin `contrasenaCifradaJwe`, RF-D002) en
      `app/src/main/kotlin/com/ecosmart/application/auth/RegistrarUsuario.kt` (depende de
      T008, T020)
- [X] T028 [US3] Reescribir `IniciarSesion` para usar
      `FirebaseAuth.signInWithEmailAndPassword` en vez de
      `UsuarioRepository.autenticar`/`CifradorContrasena`, en
      `app/src/main/kotlin/com/ecosmart/application/auth/IniciarSesion.kt`
- [X] T029 [US3] Reescribir `IniciarSesionConGoogle` para canjear el token de Google
      (vía `play-services-auth`) por una credencial de `GoogleAuthProvider` de Firebase
      Authentication (research.md §2), en
      `app/src/main/kotlin/com/ecosmart/application/auth/IniciarSesionConGoogle.kt`
- [X] T030 [US3] Reescribir `CambiarContrasena` para usar
      `FirebaseUser.updatePassword`, eliminando el uso de `CifradorContrasena`/JWE para
      este flujo, en `app/src/main/kotlin/com/ecosmart/application/auth/CambiarContrasena.kt`
- [X] T031 [US3] Actualizar `RegistroViewModel` para invocar los casos de uso
      reescritos (T027-T029) y manejar los nuevos tipos de error de Firebase
      Authentication (p. ej. email ya en uso, credenciales inválidas) en tono no punitivo
      (Principio IX), en `app/src/main/kotlin/com/ecosmart/presentation/auth/RegistroViewModel.kt`
- [X] T032 [US3] Eliminar el uso de `CifradorContrasena`/`ContrasenaCifrada` del flujo de
      autenticación de este módulo (queda sin referencias vivas tras T027-T030); no se
      borra la clase en sí hasta confirmar que `001-ecosmart-mvp` no la necesita en otro
      lado

**Checkpoint**: registro/login/cambio de contraseña funcionan 100% contra Firebase
Authentication; ningún campo de contraseña llega a Firestore.

---

## Phase 5: User Story 2 - Sincronizar cambios entre dispositivos (Priority: P1)

**Goal**: Un cambio de perfil o una actividad aprobada en un dispositivo se refleja en
otro dispositivo con la misma cuenta, bajo demanda (RF-D017, sin listener permanente).

**Independent Test**: misma cuenta en 2 dispositivos/emuladores, aprobar actividad en
uno, verificar que el otro refleja el cambio al volver a primer plano o refrescar
(quickstart.md §6).

### Tests for User Story 2

- [X] T033 [P] [US2] Test de integración: dos instancias de `UsuarioRepositoryImpl`
      (simulando 2 dispositivos) contra el mismo documento del emulador reflejan el mismo
      estado tras una recarga, en
      `app/src/androidTest/kotlin/com/ecosmart/infrastructure/persistence/UsuarioRepositoryImplTest.kt`
      (agregar caso)
- [X] T034 [P] [US2] Test de contrato: `POST /registros-verificacion/caminar` con
      `dispositivoId` distinto mientras ya hay una caminata activa responde `409`
      (RF-D008), en `backend/tests/test_puntos_endpoints.py`

### Implementation for User Story 2

- [X] T035 [US2] Implementar la recarga bajo demanda (al entrar/volver a primer plano,
      sin listener permanente — RF-D017) en `HomeViewModel`/`PerfilViewModel` en
      `app/src/main/kotlin/com/ecosmart/presentation/home/HomeViewModel.kt` y
      `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilViewModel.kt`
- [X] T036 [US2] Implementar el arbitraje transaccional de `caminataEnCurso`/`pasosHoy`
      (RF-D007/RF-D008) en el endpoint `/registros-verificacion/caminar` de
      `backend/app/puntos.py` (depende de T011)
- [X] T036b [US2] Implementar `PATCH /pasos-del-dia` en `backend/app/puntos.py` (RF-D007)
      — sincroniza el conteo diario de pasos de forma transaccional, independiente de
      cualquier caminata puntual con meta (distinto del endpoint `/caminar` de T036) —
      depende de T011
- [X] T037 [US2] Reescribir `RegistrarCaminata` para enviar `INICIAR`/
      `ACTUALIZAR_PROGRESO`/`COMPLETAR` al `BackendConfianzaClient` en vez de escribir
      directamente en `CaminataEnCursoStore`, en
      `app/src/main/kotlin/com/ecosmart/application/activity/RegistrarCaminata.kt`
      (depende de T011e, T013, T014)
- [X] T038 [US2] Implementar `CaminataEnCursoFirestoreMapper` (lectura de solo lectura del
      documento singleton `caminataEnCurso/actual` para reflejar el progreso en el
      dispositivo que no lo inició) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/firestore/CaminataEnCursoFirestoreMapper.kt`
- [X] T039 [US2] Manejar el error `409` de caminata activa en otro dispositivo con un
      mensaje no punitivo (Principio IX) en
      `app/src/main/kotlin/com/ecosmart/presentation/verification/VerificacionCaminataScreen.kt`

**Checkpoint**: cambios de perfil/actividad/caminata se reflejan entre dispositivos al
refrescar, sin listener en tiempo real, dentro de los 10s de SC-D002.

---

## Phase 6: User Story 4 - Migración de cuentas existentes de spec 001 (Priority: P2)

**Goal**: Las cuentas creadas bajo spec 001 (datos solo en Room) suben automáticamente su
perfil e historial a Firestore la primera vez que inician sesión tras esta actualización.

**Independent Test**: instalación con datos de Room poblados, actualizar a esta build,
iniciar sesión → verificar que Firestore queda poblado y Room se vacía (quickstart.md §6).

### Tests for User Story 4

- [X] T040 [P] [US4] Test de contrato: `POST /migracion/subir-datos-locales` es
      idempotente (una segunda llamada con los mismos datos no duplica el historial), en
      `backend/tests/test_puntos_endpoints.py`
- [X] T041 [P] [US4] Test unitario: el flujo de migración borra las tablas de Room
      únicamente después de una respuesta exitosa del backend, nunca antes, en
      `app/src/test/kotlin/com/ecosmart/infrastructure/migration/MigracionDatosLocalesTest.kt`

### Implementation for User Story 4

- [X] T042 [US4] Implementar el endpoint `/migracion/subir-datos-locales` (idempotente
      por `uid`, ver research.md §6) en `backend/app/puntos.py` (depende de T011)
- [X] T043 [US4] Implementar `MigracionDatosLocales` (lee Room vía los DAOs existentes,
      llama al endpoint de migración, y solo si responde `200` marca la bandera
      `migracion_firestore_completada` en SharedPreferences y borra las tablas de Room —
      RF-D011) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/migration/MigracionDatosLocales.kt`
      (depende de T013, T014)
- [X] T044 [US4] Disparar `MigracionDatosLocales` en el primer login posterior a esta
      actualización (punto de entrada único, antes de navegar a Home) en
      `app/src/main/kotlin/com/ecosmart/presentation/auth/RegistroViewModel.kt` (depende
      de T043)

**Checkpoint**: una cuenta de spec 001 migra una única vez, sin duplicar historial en
inicios posteriores, y Room deja de ser consultado tras la migración (RF-D006).

---

## Phase 7: User Story 5 - Eliminación de cuenta y sus datos (Priority: P3)

**Goal**: Un usuario puede eliminar su cuenta; el borrado es inmediato y definitivo, sin
período de gracia (RF-D012).

**Independent Test**: eliminar una cuenta de prueba, verificar en Firebase Console que el
documento, subcolecciones y la credencial de Authentication desaparecen dentro de los
60s de SC-D005 (quickstart.md §6).

### Tests for User Story 5

- [X] T045 [P] [US5] Test de contrato: eliminar cuenta borra `usuarios/{uid}` y todas sus
      subcolecciones (registrosVerificacion, caminataEnCurso, permisosDispositivo) y la
      credencial de Authentication, en `backend/tests/test_puntos_endpoints.py`

### Implementation for User Story 5

- [X] T046 [US5] Implementar el endpoint de eliminación de cuenta (borra documento +
      subcolecciones vía Admin SDK + `auth.delete_user(uid)`) en `backend/app/puntos.py`
      (depende de T010, T011)
- [X] T047 [US5] Agregar la opción "Eliminar cuenta" con pantalla de confirmación (única
      salvaguarda, RF-D012) en
      `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilScreen.kt` y
      `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilViewModel.kt`
      (depende de T014)

**Checkpoint**: todas las historias de usuario de `spec.md` quedan implementadas y
demostrables de forma independiente.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Mejoras transversales a todas las historias.

- [X] T048 [P] Implementar el manejo de fallas transitorias vs. no transitorias
      (RF-D016: 1 reintento automático, luego error explícito no punitivo) como un
      interceptor/wrapper reutilizable en
      `app/src/main/kotlin/com/ecosmart/infrastructure/network/BackendConfianzaClient.kt`
- [X] T049 [P] Agregar el ping best-effort de calentamiento (RF-079/RF-D015) a
      `/health` del backend antes de los flujos de Caminar, Reciclar y Reutilizar, en
      `app/src/main/kotlin/com/ecosmart/application/activity/RegistrarCaminata.kt` y
      `app/src/main/kotlin/com/ecosmart/application/activity/VerificarFotoConIA.kt`
- [X] T050 [P] Implementar la escritura informativa de `permisosDispositivo/{dispositivoId}_{tipo}`
      (RF-D009, el cliente escribe directamente) en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/PermisoRepositoryImpl.kt`
      (depende de T011e)
- [X] T051 [P] Actualizar `PasosDelDiaRepositoryImpl` para sincronizar `pasosHoy` a
      Firestore llamando a `BackendConfianzaClient.actualizarPasosDelDia(...)` en cada
      actualización (RF-D007), conservando el cálculo de línea base del sensor como
      detalle 100% local, en
      `app/src/main/kotlin/com/ecosmart/infrastructure/sensors/PasosDelDiaRepositoryImpl.kt`
      (depende de T036b)
- [ ] T052 Ejecutar la guía completa de validación de `quickstart.md` (§6, §7, §8) de
      punta a punta contra el emulador antes de dar por cerrado el módulo
- [X] T052a [P] Marcar `@Deprecated` `UsuarioDao`/`RegistroVerificacionDao` y actualizar
      el comentario de `AppDatabase.kt` aclarando que quedan sin uso de negocio tras la
      migración (RF-D006: Room nunca llegó a implementarse como caché activo en este
      módulo, evitar código muerto sin señalizar — Principio V), en
      `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/AppDatabase.kt`
- [X] T053 [P] Actualizar `001-ecosmart-mvp/data-model.md` y
      `001-ecosmart-mvp/research.md` con una nota de "Superado por spec 002" en las
      secciones de Room/JWE que este módulo reemplaza, para que no describan un
      comportamiento ya no vigente

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias — puede empezar de inmediato
- **Foundational (Phase 2)**: depende de Setup — BLOQUEA todas las historias
- **US1 (Phase 3)**: depende de Foundational
- **US3 (Phase 4)**: depende de Foundational; **US1 depende de que US3 provea login
  funcional** para ser demostrable de punta a punta (ver nota al inicio de Phase 4) —
  implementar US3 antes o junto con US1, nunca después
- **US2 (Phase 5)**: depende de Foundational + US1 (necesita perfil/historial ya
  migrados a Firestore para tener algo que sincronizar)
- **US4 (Phase 6)**: depende de Foundational + US1 + US3 (la migración necesita que el
  login y la lectura de Firestore ya funcionen)
- **US5 (Phase 7)**: depende de Foundational + US3 (necesita autenticación funcionando
  para poder eliminar la cuenta autenticada)
- **Polish (Phase 8)**: depende de que todas las historias deseadas estén completas

### Parallel Opportunities

- Todas las tareas `[P]` de la Fase 1 pueden ejecutarse en paralelo
- Dentro de la Fase 2: T008/T009/T010/T012/T015/T016/T017 son paralelas entre sí (T011,
  T013, T014 tienen dependencias directas señaladas)
- US2, US4 y US5 pueden trabajarse en paralelo por desarrolladores distintos una vez que
  US1 y US3 estén completas (todas dependen de Foundational + US1/US3, no entre sí)

---

## Parallel Example: Foundational Phase

```bash
# Lanzar en paralelo tras completar Setup:
Task: "Implementar FirebaseAuthModule en app/.../infrastructure/firebase/FirebaseAuthModule.kt"
Task: "Implementar FirestoreModule en app/.../infrastructure/firebase/FirestoreModule.kt"
Task: "Implementar firebase_admin_setup.py en backend/app/firebase_admin_setup.py"
Task: "Implementar UsuarioFirestoreMapper en app/.../persistence/firestore/UsuarioFirestoreMapper.kt"
Task: "Implementar RegistroVerificacionFirestoreMapper en app/.../persistence/firestore/RegistroVerificacionFirestoreMapper.kt"
Task: "Rules unit tests en backend/tests/test_firestore_rules.py"
```

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 3)

1. Completar Fase 1: Setup
2. Completar Fase 2: Foundational (CRÍTICO — bloquea todas las historias)
3. Completar Fase 4: US3 (login/registro contra Firebase Authentication — sin esto, US1
   no es demostrable de punta a punta)
4. Completar Fase 3: US1 (recuperar cuenta)
5. **DETENER y VALIDAR**: probar US1+US3 de forma independiente (quickstart.md §6)
6. Demo/entrega si corresponde

### Entrega Incremental

1. Setup + Foundational → base lista
2. US3 + US1 → login real + recuperación de cuenta (MVP de este módulo)
3. US2 → sincronización entre dispositivos
4. US4 → migración de cuentas existentes de spec 001 (crítico antes de liberar a
   usuarios reales que ya tengan la app instalada)
5. US5 → eliminación de cuenta
6. Polish

---

## Notes

- `[P]` = archivos distintos, sin dependencias pendientes entre sí
- Cada historia de usuario es demostrable de forma independiente siguiendo
  `quickstart.md` §6
- Verificar que los tests fallan antes de implementar (T018-T019, T025-T026, T033-T034,
  T040-T041, T045)
- Hacer commit después de cada tarea o grupo lógico de tareas
