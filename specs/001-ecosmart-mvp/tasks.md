# Tasks: EcoSmart — Sistema Completo (MVP)

**Input**: Documentos de diseño de `specs/001-ecosmart-mvp/` (`plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/openapi.yaml`, `quickstart.md`)

**Prerequisites**: `plan.md` ✅, `spec.md` ✅, `research.md` ✅, `data-model.md` ✅, `contracts/openapi.yaml` ✅

**Tests**: incluidas para las reglas de negocio críticas exigidas por el Principio VIII de `constitution.md` y por `quickstart.md` §6 (cálculo de puntos, topes diarios, nivel, racha, duplicados, timeout). No se generan tests exhaustivos de cada RF individual, para no violar el Principio VII (pragmatismo).

**Organization**: las tareas están agrupadas en **5 módulos**, cada uno correspondiente a una Épica de `spec.md` (p. ej. el Módulo 1 es "Login/Auth" = Épica 1). Dentro de cada módulo, las tareas llevan la etiqueta `[USx]` de la historia de usuario a la que pertenecen, para trazabilidad fina además de la agrupación por módulo.

## Formato: `[ID] [P?] [Story?] Descripción (archivo) (Depende de: ...)`

- **[P]**: se puede ejecutar en paralelo (archivo distinto, sin dependencias pendientes)
- **[USx]**: historia de usuario de `spec.md` a la que pertenece la tarea
- **(Depende de: Txxx)**: se agrega explícitamente cuando la tarea requiere que otra(s) estén terminadas antes
- Todas las rutas son relativas a la raíz del repositorio, bajo `app/src/main/kotlin/com/ecosmart/...` (ver estructura completa en `plan.md` § Project Structure)

## Mapa de Módulos → Épicas → Historias de Usuario

| Módulo | Épica (`spec.md`) | Historias de Usuario | Fase |
|---|---|---|---|
| — | Setup | — | Fase 1 |
| — | Foundational (compartido) | — | Fase 2 |
| **1. Login / Autenticación** | Épica 1 | US1, US2, US3 | Fase 3 |
| **2. Permisos del Dispositivo** | Épica 5 | US13 | Fase 4 |
| **3. Home y Contenido** | Épica 2 | US4, US5, US6, US7 | Fase 5 |
| **4. Verificación y Puntuación** | Épica 3 | US8, US9, US10 | Fase 6 |
| **5. Perfil e Historial** | Épica 4 | US11, US12 | Fase 7 |
| — | Polish | — | Fase 8 |

> El Módulo 2 (Permisos) se implementa antes que Home/Verificación aunque
> `spec.md` lo liste como Épica 5, porque US13 bloquea en la práctica los
> formularios de Cámara/Podómetro/GPS de los módulos 3 y 4 (razón
> documentada en la propia US13 de `spec.md`).

---

## Fase 1: Setup

**Propósito**: inicialización del proyecto Android.

- [X] T001 Crear estructura de proyecto Android Gradle (`app/build.gradle.kts`, `settings.gradle.kts`, `build.gradle.kts` raíz) per `plan.md` § Project Structure
- [X] T002 [P] Agregar dependencias core a `app/build.gradle.kts` (Jetpack Compose, Hilt, Room, Retrofit/OkHttp, WorkManager, CameraX, Play Services Location, Nimbus JOSE+JWT, `androidx.security.crypto`)
- [X] T003 [P] Configurar `BuildConfig` (`ECOGPT_API_KEY`, `ECOGPT_BASE_URL`, `PUNTOS_VERDES_BASE_URL`) leyendo de `local.properties` en `app/build.gradle.kts`, per `quickstart.md` §3
- [X] T004 [P] Configurar ktlint/detekt en `app/build.gradle.kts` y `build.gradle.kts` raíz
- [X] T005 Declarar permisos (Cámara, Galería, Ubicación, Reconocimiento de Actividad/Podómetro) en `app/src/main/AndroidManifest.xml`
- [X] T006 [P] Crear `EcoSmartApplication` (`@HiltAndroidApp`) en `app/src/main/kotlin/com/ecosmart/app/EcoSmartApplication.kt`

---

## Fase 2: Foundational (bloqueante — compartido por todos los módulos)

**Propósito**: infraestructura y tipos base que TODOS los módulos necesitan.

**⚠️ CRÍTICO**: ningún módulo (Fase 3+) puede empezar hasta completar esta fase.

- [X] T007 [P] Crear enum `CategoriaActividad` en `app/src/main/kotlin/com/ecosmart/domain/valueobject/CategoriaActividad.kt`
- [X] T008 [P] Crear enum `ResultadoVerificacion` en `app/src/main/kotlin/com/ecosmart/domain/valueobject/ResultadoVerificacion.kt`
- [X] T009 [P] Crear enums `EstadoPermiso` y `TipoPermiso` en `app/src/main/kotlin/com/ecosmart/domain/valueobject/Permiso.kt`
- [X] T010 [P] Crear enum `NivelUsuario` con `desdePuntaje()` (RF-046/RF-047) en `app/src/main/kotlin/com/ecosmart/domain/valueobject/NivelUsuario.kt`
- [X] T011 [P] Crear value classes de ID (`UsuarioId`, `ActividadId`, `RegistroVerificacionId`, `PuntoVerdeId`) en `app/src/main/kotlin/com/ecosmart/domain/valueobject/Ids.kt`
- [X] T012 Crear `AppDatabase` (Room) con las 5 `@Entity` declaradas (stub) en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/AppDatabase.kt` (Depende de: T007, T008, T009)
- [X] T013 [P] Crear módulo Hilt `DatabaseModule` (provee `AppDatabase` y DAOs) en `app/src/main/kotlin/com/ecosmart/infrastructure/di/DatabaseModule.kt` (Depende de: T012)
- [X] T014 Crear `JweGestorClaves` (genera/recupera la clave JWK protegida por Android Keystore, RNF-007) en `app/src/main/kotlin/com/ecosmart/infrastructure/security/JweGestorClaves.kt`
- [X] T015 Crear `CifradorContrasena` (cifra/descifra JWE con Nimbus JOSE+JWT, RNF-006) en `app/src/main/kotlin/com/ecosmart/infrastructure/security/CifradorContrasena.kt` (Depende de: T014)
- [X] T016 [P] Crear módulo Hilt `SecurityModule` en `app/src/main/kotlin/com/ecosmart/infrastructure/di/SecurityModule.kt` (Depende de: T014, T015)
- [X] T017 Crear `EcoGptClient` (interfaz Retrofit + `OkHttpClient` con `callTimeout = 30s`, RNF-008) per `contracts/openapi.yaml` en `app/src/main/kotlin/com/ecosmart/infrastructure/network/EcoGptClient.kt`
- [X] T018 [P] Crear módulo Hilt `NetworkModule` (Retrofit/OkHttp, API key desde `BuildConfig`) en `app/src/main/kotlin/com/ecosmart/infrastructure/di/NetworkModule.kt` (Depende de: T017)
- [X] T019 Crear scaffold de navegación `EcoSmartNavHost` (rutas vacías por módulo) en `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt`
- [X] T020 [P] Crear theming compartido de Compose (tono ameno/no punitivo, RNF-001) en `app/src/main/kotlin/com/ecosmart/presentation/theme/Theme.kt`

**Checkpoint**: infraestructura lista — los 5 módulos pueden empezar (en el orden de la tabla, o en paralelo si hay más de una persona).

---

## Fase 3: Módulo 1 — Login / Autenticación (Épica 1: US1, US2, US3) 🎯 MVP

**Goal**: un usuario puede registrarse (email/contraseña o Google), elegir sus categorías de interés, y editar su perfil/contraseña después.

**Independent Test**: registrar una cuenta nueva con el checkbox de categorías → cerrar sesión → volver a iniciar sesión → editar el teléfono desde el perfil y verificar que persiste.

- [X] T021 [P] [US1] Crear entidad `Usuario` (`sumarPuntos`, `registrarActividadAprobadaHoy`, `nivel()`) en `app/src/main/kotlin/com/ecosmart/domain/model/Usuario.kt` (Depende de: T007, T010, T011)
- [X] T022 [P] [US1] Crear interfaz `UsuarioRepository` en `app/src/main/kotlin/com/ecosmart/domain/repository/UsuarioRepository.kt` (Depende de: T021)
- [X] T023 [US1] Crear `UsuarioRoomEntity` + `UsuarioMapper` en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/usuario/UsuarioRoomEntity.kt` y `UsuarioMapper.kt` (Depende de: T012, T021)
- [X] T024 [US1] Crear `UsuarioDao` (`findByEmail`, `insert`, `update`) en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/usuario/UsuarioDao.kt` (Depende de: T023)
- [X] T025 [US1] Implementar `UsuarioRepositoryImpl` (Room + `CifradorContrasena`) en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/UsuarioRepositoryImpl.kt` (Depende de: T015, T022, T024)
- [X] T026 [US1] Implementar caso de uso `RegistrarUsuario` (RF-001 a RF-006) en `app/src/main/kotlin/com/ecosmart/application/auth/RegistrarUsuario.kt` (Depende de: T025)
- [X] T027 [P] [US1] Implementar caso de uso `IniciarSesionConGoogle` (RF-002) en `app/src/main/kotlin/com/ecosmart/application/auth/IniciarSesionConGoogle.kt` (Depende de: T025)
- [X] T028 [US1] Implementar caso de uso `EditarPerfil` (RF-007 a RF-009) en `app/src/main/kotlin/com/ecosmart/application/auth/EditarPerfil.kt` (Depende de: T025)
- [X] T029 [US1] Implementar caso de uso `CambiarContrasena` (RF-008, RNF-006) en `app/src/main/kotlin/com/ecosmart/application/auth/CambiarContrasena.kt` (Depende de: T025)
- [X] T030 [US1] Crear `RegistroViewModel` en `app/src/main/kotlin/com/ecosmart/presentation/auth/RegistroViewModel.kt` (Depende de: T026, T027)
- [X] T031 [P] [US1] Crear `RegistroScreen` (Compose, checkbox de categorías RF-005/006) en `app/src/main/kotlin/com/ecosmart/presentation/auth/RegistroScreen.kt` (Depende de: T030)
- [X] T032 [US1] Crear `PerfilEdicionViewModel` en `app/src/main/kotlin/com/ecosmart/presentation/auth/PerfilEdicionViewModel.kt` (Depende de: T028, T029)
- [X] T033 [P] [US1] Crear `PerfilEdicionScreen` (Compose) en `app/src/main/kotlin/com/ecosmart/presentation/auth/PerfilEdicionScreen.kt` (Depende de: T032)
- [X] T034 [P] [US1] Test unitario de `RegistrarUsuario` (rechaza email duplicado, aplica política de contraseña RF-004) en `app/src/test/kotlin/com/ecosmart/application/auth/RegistrarUsuarioTest.kt` (Depende de: T026)
- [X] T035 [P] [US1] Test unitario de `Usuario.sumarPuntos()` / `nivel()` / racha en `app/src/test/kotlin/com/ecosmart/domain/model/UsuarioTest.kt` (Depende de: T021)

**Checkpoint**: Módulo 1 (Login) funcional y testeable de forma independiente.

---

## Fase 4: Módulo 2 — Permisos del Dispositivo (Épica 5: US13)

**Goal**: tras el login, la app solicita Galería/Cámara/GPS/Podómetro y guía al usuario si los rechaza.

**Independent Test**: denegar el permiso de Cámara → intentar abrir el formulario de verificación de Reciclar → verificar que se bloquea y se muestra la guía paso a paso.

- [X] T036 [P] [US13] Crear entidad `PermisoDispositivo` (`bloqueaFormulario`) en `app/src/main/kotlin/com/ecosmart/domain/model/PermisoDispositivo.kt` (Depende de: T009)
- [X] T037 [P] [US13] Crear interfaz `PermisoRepository` en `app/src/main/kotlin/com/ecosmart/domain/repository/PermisoRepository.kt` (Depende de: T036)
- [X] T038 [US13] Crear `PermisoDispositivoRoomEntity` + `PermisoDao` + Mapper en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/permiso/` (Depende de: T012, T036)
- [X] T039 [US13] Implementar `PermisoRepositoryImpl` en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/PermisoRepositoryImpl.kt` (Depende de: T037, T038)
- [X] T040 [US13] Implementar caso de uso `EvaluarEstadoPermiso` en `app/src/main/kotlin/com/ecosmart/application/permission/EvaluarEstadoPermiso.kt` (Depende de: T039)
- [X] T041 [US13] Crear `SolicitudPermisosViewModel` en `app/src/main/kotlin/com/ecosmart/presentation/permissions/SolicitudPermisosViewModel.kt` (Depende de: T040)
- [X] T042 [P] [US13] Crear `SolicitudPermisosScreen` (post-login, RF-043) en `app/src/main/kotlin/com/ecosmart/presentation/permissions/SolicitudPermisosScreen.kt` (Depende de: T041)
- [X] T043 [P] [US13] Crear `GuiaHabilitarPermisoScreen` (alerta explicativa, RF-045, tono RNF-001) en `app/src/main/kotlin/com/ecosmart/presentation/permissions/GuiaHabilitarPermisoScreen.kt` (Depende de: T041)
- [X] T044 [P] [US13] Test unitario de `PermisoDispositivo.bloqueaFormulario` en `app/src/test/kotlin/com/ecosmart/domain/model/PermisoDispositivoTest.kt` (Depende de: T036)

**Checkpoint**: Módulo 2 (Permisos) funcional; los módulos 3 y 4 ya pueden bloquear sus formularios correctamente.

---

## Fase 5: Módulo 3 — Home y Contenido (Épica 2: US4, US5, US6, US7)

**Goal**: el usuario ve su Home filtrada por categoría, entra al detalle de una actividad, consulta contenido educativo y ve Puntos Verdes cercanos.

**Independent Test**: con preferencias ya configuradas, abrir la Home → verificar que solo aparecen las categorías elegidas → tocar una tarjeta → verificar que el detalle muestra pasos y resultado esperado (ausentes en la tarjeta).

- [X] T045 [P] [US4] Crear entidad `Actividad` (`estaDisponibleHoy`) en `app/src/main/kotlin/com/ecosmart/domain/model/Actividad.kt` (Depende de: T007)
- [X] T046 [P] [US4] Crear interfaz `ActividadRepository` en `app/src/main/kotlin/com/ecosmart/domain/repository/ActividadRepository.kt` (Depende de: T045)
- [X] T047 [US4] Crear `ActividadRoomEntity` + `ActividadDao` (`findByCategorias`) + Mapper en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/actividad/` (Depende de: T012, T045)
- [X] T048 [US4] Implementar `ActividadRepositoryImpl` + seed inicial del catálogo en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/ActividadRepositoryImpl.kt` (Depende de: T046, T047)
- [X] T049 [US4] Implementar caso de uso `ObtenerActividadesFiltradas` (RF-011/RF-012) en `app/src/main/kotlin/com/ecosmart/application/activity/ObtenerActividadesFiltradas.kt` (Depende de: T048)
- [X] T050 [US4] Crear `HomeViewModel` en `app/src/main/kotlin/com/ecosmart/presentation/home/HomeViewModel.kt` (Depende de: T049)
- [X] T051 [P] [US4] Crear `HomeScreen` (tarjetas por categoría, RF-013) en `app/src/main/kotlin/com/ecosmart/presentation/home/HomeScreen.kt` (Depende de: T050)
- [X] T052 [P] [US5] Crear `ActividadDetalleViewModel` (RF-014/RF-015) en `app/src/main/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleViewModel.kt` (Depende de: T048)
- [X] T053 [P] [US5] Crear `ActividadDetalleScreen` en `app/src/main/kotlin/com/ecosmart/presentation/activitydetail/ActividadDetalleScreen.kt` (Depende de: T052)
- [X] T054 [P] [US6] Crear `ContenidoEducativoViewModel` + `ContenidoEducativoScreen` (RF-016/RF-017) en `app/src/main/kotlin/com/ecosmart/presentation/home/ContenidoEducativoScreen.kt`
- [X] T055 [P] [US7] Crear entidad `PuntoVerde` (`estaDentroDelRadio`, Haversine) en `app/src/main/kotlin/com/ecosmart/domain/model/PuntoVerde.kt`
- [X] T056 [P] [US7] Crear interfaz `PuntoVerdeRepository` en `app/src/main/kotlin/com/ecosmart/domain/repository/PuntoVerdeRepository.kt` (Depende de: T055)
- [X] T057 [US7] Crear `PuntoVerdeRoomEntity` + `PuntoVerdeDao` (`reemplazarTodos`) + Mapper en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/puntoverde/` (Depende de: T012, T055)
- [X] T058 [US7] Crear `PuntosVerdesSyncClient` (Retrofit, `contracts/openapi.yaml` → `/puntos-verdes`) en `app/src/main/kotlin/com/ecosmart/infrastructure/network/PuntosVerdesSyncClient.kt` (Depende de: T018)
- [X] T059 [US7] Crear `PuntosVerdesSyncWorker` (WorkManager periódico, RF-052) en `app/src/main/kotlin/com/ecosmart/infrastructure/network/PuntosVerdesSyncWorker.kt` (Depende de: T057, T058)
- [X] T060 [US7] Implementar `PuntoVerdeRepositoryImpl` + `UbicacionProvider` (`FusedLocationProviderClient`) en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/PuntoVerdeRepositoryImpl.kt` y `app/src/main/kotlin/com/ecosmart/infrastructure/sensors/UbicacionProvider.kt` (Depende de: T056, T057)
- [X] T061 [US7] Implementar caso de uso `ObtenerPuntosVerdesCercanos` (radio 3 km, RF-018/RF-020/RF-053) en `app/src/main/kotlin/com/ecosmart/application/activity/ObtenerPuntosVerdesCercanos.kt` (Depende de: T060)
- [X] T062 [P] [US7] Crear `PuntosVerdesViewModel` + `PuntosVerdesScreen` en `app/src/main/kotlin/com/ecosmart/presentation/home/PuntosVerdesScreen.kt` (Depende de: T061)
- [X] T063 [P] [US4] Test unitario de `ObtenerActividadesFiltradas` (filtra por categorías del usuario) en `app/src/test/kotlin/com/ecosmart/application/activity/ObtenerActividadesFiltradasTest.kt` (Depende de: T049)
- [X] T064 [P] [US7] Test unitario de `PuntoVerde.estaDentroDelRadio` (3 km, Haversine) en `app/src/test/kotlin/com/ecosmart/domain/model/PuntoVerdeTest.kt` (Depende de: T055)

**Checkpoint**: Módulo 3 (Home/Contenido) funcional.

---

## Fase 6: Módulo 4 — Verificación y Puntuación (Épica 3: US8, US9, US10) 🎯 Núcleo del MVP

**Goal**: el usuario verifica caminatas (podómetro) y fotos de Reciclar/Reutilizar (EcoGPT), respetando topes diarios y sumando puntos.

**Independent Test**: completar una verificación de caminata que cumple la meta → verificar 50 pts/bloque de 133 pasos; enviar una foto de Reciclar → verificar los 3 veredictos posibles y el timeout a los 30 s; repetir hasta el tope diario → verificar el bloqueo.

- [X] T065 [P] [US8] [US9] [US10] Crear entidad `RegistroVerificacion` (`consumeTopeDiario`, `esDuplicadoDe`) en `app/src/main/kotlin/com/ecosmart/domain/model/RegistroVerificacion.kt` (Depende de: T008)
- [X] T066 [P] [US8] [US9] [US10] Crear interfaz `RegistroVerificacionRepository` en `app/src/main/kotlin/com/ecosmart/domain/repository/RegistroVerificacionRepository.kt` (Depende de: T065)
- [X] T067 [US8] [US9] [US10] Crear `RegistroVerificacionRoomEntity` + `RegistroVerificacionDao` (`contarAprobadosDelDia`, `ultimosN`, `huellasAprobadas`) + Mapper en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/room/verificacion/` (Depende de: T012, T065)
- [X] T068 [US8] [US9] [US10] Implementar `RegistroVerificacionRepositoryImpl` en `app/src/main/kotlin/com/ecosmart/infrastructure/persistence/RegistroVerificacionRepositoryImpl.kt` (Depende de: T066, T067)
- [X] T069 [P] Crear `EstrategiaDePuntaje` (Strategy: `CaminataPuntajeStrategy`, `FotoPuntajeStrategy`) en `app/src/main/kotlin/com/ecosmart/domain/model/EstrategiaDePuntaje.kt` (Depende de: T007)
- [X] T070 [US10] Implementar caso de uso `AplicarTopeDiario` (RF-032/033/034/035/056/057) en `app/src/main/kotlin/com/ecosmart/application/activity/AplicarTopeDiario.kt` (Depende de: T068)
- [X] T071 [P] [US8] Crear `PodometroProvider` (`TYPE_STEP_COUNTER`, filtro de lecturas anómalas) en `app/src/main/kotlin/com/ecosmart/infrastructure/sensors/PodometroProvider.kt`
- [X] T072 [US8] Implementar caso de uso `RegistrarCaminata` (RF-021 a RF-024, RF-031, RF-048, RF-049) en `app/src/main/kotlin/com/ecosmart/application/activity/RegistrarCaminata.kt` (Depende de: T069, T070, T071)
- [X] T073 [P] [US9] Crear `CamaraProvider` (CameraX) + `SelectorGaleria` (Photo Picker) en `app/src/main/kotlin/com/ecosmart/infrastructure/sensors/CamaraProvider.kt`
- [X] T074 [P] [US9] Implementar `CalculadorHuellaPerceptual` (dHash, `research.md` §3) en `app/src/main/kotlin/com/ecosmart/infrastructure/security/CalculadorHuellaPerceptual.kt`
- [X] T075 [US9] Implementar caso de uso `VerificarFotoConIA` (RF-025 a RF-030, RF-050, RF-051, RF-058, RF-059, timeout RNF-008) en `app/src/main/kotlin/com/ecosmart/application/activity/VerificarFotoConIA.kt` (Depende de: T069, T070, T073, T074, T017)
- [X] T076 [US9] Crear `ResultadoVerificacion.desde(respuestaEcoGpt)` (Factory) en `app/src/main/kotlin/com/ecosmart/infrastructure/network/EcoGptResponseMapper.kt` (Depende de: T017)
- [X] T077 [US8] Crear `VerificacionCaminataViewModel` + `VerificacionCaminataScreen` en `app/src/main/kotlin/com/ecosmart/presentation/verification/VerificacionCaminataScreen.kt` (Depende de: T072)
- [X] T078 [US9] Crear `VerificacionFotoViewModel` (maneja Aprobado/Rechazado/Indeterminado/Timeout) en `app/src/main/kotlin/com/ecosmart/presentation/verification/VerificacionFotoViewModel.kt` (Depende de: T075)
- [X] T079 [P] [US9] Crear `VerificacionFotoScreen` (formulario foto + descripción, RF-025/026) en `app/src/main/kotlin/com/ecosmart/presentation/verification/VerificacionFotoScreen.kt` (Depende de: T078)
- [X] T080 [P] [US9] Crear `ResultadoVerificacionScreen` (Aprobado/Rechazado/Indeterminado/Timeout, tono no punitivo RNF-001) en `app/src/main/kotlin/com/ecosmart/presentation/verification/ResultadoVerificacionScreen.kt` (Depende de: T078)
- [X] T081 [P] [US10] Test unitario de `AplicarTopeDiario` (5 Reutilizar/1 Reciclar; Indeterminado NO consume tope) en `app/src/test/kotlin/com/ecosmart/application/activity/AplicarTopeDiarioTest.kt` (Depende de: T070)
- [X] T082 [P] [US8] Test unitario de `RegistrarCaminata` (bloques de 133 pasos exactos, sin fracciones) en `app/src/test/kotlin/com/ecosmart/application/activity/RegistrarCaminataTest.kt` (Depende de: T072)
- [X] T083 [P] [US9] Test unitario de `VerificarFotoConIA` (timeout a los 30 s → error, no Rechazado/Indeterminado) en `app/src/test/kotlin/com/ecosmart/application/activity/VerificarFotoConIATest.kt` (Depende de: T075)
- [X] T084 [P] [US9] Test unitario de `RegistroVerificacion.esDuplicadoDe` (distancia de Hamming del hash perceptual) en `app/src/test/kotlin/com/ecosmart/domain/model/RegistroVerificacionTest.kt` (Depende de: T065)

**Checkpoint**: Módulo 4 (Verificación/Puntuación) funcional — el circuito central de puntos del MVP queda completo.

---

## Fase 7: Módulo 5 — Perfil e Historial (Épica 4: US11, US12)

**Goal**: el usuario ve sus métricas (puntos, % por categoría, racha, nivel) y su historial de actividades.

**Independent Test**: completar actividades en 2 categorías distintas y en 3 días consecutivos → abrir el perfil → verificar puntos, porcentaje por categoría, racha=3 y nivel correcto; abrir el historial → ver 3 recientes + "Ver más".

- [X] T085 [US11] Implementar caso de uso `CalcularMetricasPerfil` (puntos, % por categoría, racha, nivel — RF-036 a RF-040) en `app/src/main/kotlin/com/ecosmart/application/profile/CalcularMetricasPerfil.kt` (Depende de: T068, T025)
- [X] T086 [US12] Implementar caso de uso `ObtenerHistorial` (últimas 3 + completo — RF-041/RF-042) en `app/src/main/kotlin/com/ecosmart/application/profile/ObtenerHistorial.kt` (Depende de: T068)
- [X] T087 [US11] Crear `PerfilViewModel` en `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilViewModel.kt` (Depende de: T085)
- [X] T088 [P] [US11] Crear `PerfilScreen` (puntos, % por categoría, racha, nivel) en `app/src/main/kotlin/com/ecosmart/presentation/profile/PerfilScreen.kt` (Depende de: T087)
- [X] T089 [US12] Crear `HistorialViewModel` en `app/src/main/kotlin/com/ecosmart/presentation/profile/HistorialViewModel.kt` (Depende de: T086)
- [X] T090 [P] [US12] Crear `HistorialScreen` (3 recientes + botón "Ver más") en `app/src/main/kotlin/com/ecosmart/presentation/profile/HistorialScreen.kt` (Depende de: T089)
- [X] T091 [P] [US11] Test unitario de `CalcularMetricasPerfil` (racha se reinicia tras un día sin actividad; nivel por puntaje histórico, nunca desciende) en `app/src/test/kotlin/com/ecosmart/application/profile/CalcularMetricasPerfilTest.kt` (Depende de: T085)

**Checkpoint**: los 5 módulos cubren las 13 historias de usuario de `spec.md`.

---

## Fase 8: Polish & Cross-Cutting Concerns

**Propósito**: integración final y verificación transversal.

- [X] T092 [P] Conectar `EcoSmartNavHost` con las 5 pantallas de entrada de cada módulo y el flujo post-login → permisos → home en `app/src/main/kotlin/com/ecosmart/presentation/EcoSmartNavHost.kt` (Depende de: T031, T042, T051, T077, T088)
- [X] T093 [P] Test de integración con Room in-memory de `UsuarioDao`/`ActividadDao`/`RegistroVerificacionDao` en `app/src/androidTest/kotlin/com/ecosmart/infrastructure/persistence/RoomDaoTest.kt` (Depende de: T024, T047, T067)
- [X] T094 [P] Configurar `MockWebServer` para `EcoGptClient` en tests de integración (`quickstart.md` §7) en `app/src/androidTest/kotlin/com/ecosmart/infrastructure/network/EcoGptClientTest.kt` (Depende de: T017)
- [ ] T095 Ejecutar manualmente los 5 escenarios de validación de `quickstart.md` §5 de punta a punta — **pendiente**: requiere Android Studio + emulador/dispositivo, no ejecutable desde este entorno (ver nota de cierre de fase más abajo)
- [X] T096 [P] Revisar que ningún archivo bajo `app/src/main/kotlin/com/ecosmart/domain/` importe `android.*`/`androidx.*` (chequeo de capas, Principio IV)
- [X] T097 Limpieza de código y revisión de nombres contra el vocabulario de `spec.md`/`data-model.md`

---

## Fase 9: Correcciones Post-QA Manual (sesión 2026-09-22)

**Propósito**: corregir defectos y omisiones detectados al probar el MVP compilado en un dispositivo real, y cubrir 2 decisiones de negocio nuevas surgidas de esa prueba (RF-060 a RF-064, ver `spec.md` § Clarifications, sesión 2026-09-22).

- [X] T098 [P] Ocultar los campos de contraseña por defecto con opción de mostrar/ocultar (RF-060) — nuevo `CampoContrasena` reutilizable en `app/src/main/kotlin/com/ecosmart/presentation/auth/CamposCompartidos.kt`, usado en `RegistroScreen.kt` y `PerfilEdicionScreen.kt`
- [X] T099 Persistir la sesión autenticada entre reinicios de la app (RF-061) — `SesionUsuario` pasa de memoria pura a SharedPreferences en `app/src/main/kotlin/com/ecosmart/infrastructure/session/SesionUsuario.kt`; `MainActivity.kt` decide el `startDestination` según haya sesión guardada; nuevo botón "Cerrar sesión" en `PerfilScreen.kt`/`PerfilViewModel.kt` (Depende de: T030, T087)
- [X] T100 [P] Atajo al perfil desde la Home (RF-062) — avatar circular con la inicial del usuario en un `TopAppBar`, en `app/src/main/kotlin/com/ecosmart/presentation/home/HomeScreen.kt`/`HomeViewModel.kt` (Depende de: T050, T087)
- [X] T101 Reemplazar el campo de dirección de texto libre por un desplegable de barrios de CABA (RF-063) — nuevo enum `Barrio` (48 valores) en `app/src/main/kotlin/com/ecosmart/domain/valueobject/Barrio.kt`; `Usuario.direccion: String` pasa a `Usuario.barrio: Barrio?` (`Usuario.kt`, `UsuarioRoomEntity`/`UsuarioMapper`, `RegistrarUsuario`/`EditarPerfil`/`IniciarSesionConGoogle`); nuevo `SelectorBarrio` reutilizable en `CamposCompartidos.kt` (Depende de: T021, T023, T026, T028)
- [X] T102 Reemplazar la búsqueda de Puntos Verdes por radio GPS con búsqueda por barrio (RF-018/RF-063, deriva de T101) — `PuntoVerde` gana el campo `barrio: String` (`PuntoVerde.kt`, `PuntoVerdeRoomEntity`/`Mapper`, `PuntoVerdeDto`); nuevo caso de uso `ObtenerPuntosVerdesDelBarrio` reemplaza a `ObtenerPuntosVerdesCercanos` (eliminado); `PuntosVerdesScreen.kt`/`PuntosVerdesViewModel` ya no dependen de `UbicacionProvider`; `PuntoVerdeRepositoryImpl.sembrarSiEstaVacio()` siembra Puntos Verdes de ejemplo para que la búsqueda por barrio sea demostrable sin backend real (Depende de: T055, T060, T061, T101)
- [X] T103 Corregir el crash al tocar "Realizar" en Reciclar/Reutilizar (RF-064, el más importante de esta fase) — causa raíz: `VerificacionFotoScreen` invocaba `CameraX.bindToLifecycle` sin chequear el permiso de Cámara, lanzando `SecurityException` sin capturar; ahora verifica el permiso real del sistema operativo antes de tocar CameraX, pide el permiso una vez más si falta, y muestra `GuiaHabilitarPermisoScreen` (ya existente del Módulo 2) en vez de crashear si se rechaza; el mismo patrón se replica en `VerificacionCaminataScreen` para el permiso de Podómetro (`app/src/main/kotlin/com/ecosmart/presentation/verification/VerificacionFotoScreen.kt`/`VerificacionFotoViewModel.kt`, `VerificacionCaminataScreen.kt`) (Depende de: T075, T078, T079)
- [X] T104 [P] Actualizar tests existentes afectados por el cambio `Usuario.direccion` → `Usuario.barrio` (`UsuarioTest.kt`, `RegistrarUsuarioTest.kt`, `AplicarTopeDiarioTest.kt`, `CalcularMetricasPerfilTest.kt`, `RoomDaoTest.kt`) y por el nuevo campo `PuntoVerde.barrio` (`PuntoVerdeTest.kt`)

---

## Dependencias y Orden de Ejecución

### Dependencias entre Fases

- **Setup (Fase 1)**: sin dependencias — arranca de inmediato.
- **Foundational (Fase 2)**: depende de Setup — BLOQUEA los 5 módulos.
- **Módulos (Fase 3 a 7)**: todos dependen de Foundational.
  - Orden recomendado en solitario: 1 (Login) → 2 (Permisos) → 3 (Home) → 4 (Verificación) → 5 (Perfil), porque cada uno usa datos/servicios del anterior para probarse de punta a punta.
  - Con más de una persona, los módulos 1 y 2 pueden arrancar en paralelo apenas termina Foundational; 3 depende de 1 (necesita un `Usuario` para filtrar la Home); 4 depende de 2 y 3; 5 depende de 4 (necesita `RegistroVerificacion` para tener datos que mostrar).
- **Polish (Fase 8)**: depende de que los módulos que se quieran entregar estén completos.

### Dependencias entre Módulos

| Módulo | Depende de |
|---|---|
| 1. Login | Solo Foundational |
| 2. Permisos | Solo Foundational (independiente de Login, pero se usa post-login) |
| 3. Home y Contenido | Foundational + `Usuario` del Módulo 1 (para filtrar por categorías) |
| 4. Verificación y Puntuación | Foundational + Módulo 2 (permisos de Cámara/Podómetro) + Módulo 3 (se verifica una `Actividad` existente) |
| 5. Perfil e Historial | Foundational + Módulo 4 (necesita `RegistroVerificacion` para calcular métricas e historial) |

### Dentro de cada Módulo

- Entidad de dominio → interfaz de repositorio → Room Entity/DAO/Mapper → implementación del repositorio → caso(s) de uso → ViewModel → Screen (Compose)
- Los tests unitarios de un módulo pueden escribirse en paralelo a la UI de ese mismo módulo, una vez el caso de uso que testean está implementado

### Oportunidades de Paralelismo

- Todas las tareas `[P]` de Foundational (T007–T011, T013, T016, T018, T020) se pueden hacer en paralelo entre sí
- Dentro de un módulo, las entidades de dominio y los tests unitarios marcados `[P]` no comparten archivo y pueden hacerse en paralelo
- Los Módulos 1 y 2 pueden desarrollarse en paralelo por personas distintas apenas termina Foundational

---

## Ejemplo de Ejecución en Paralelo: Módulo 1 (Login)

```bash
# Lanzar en paralelo las tareas de dominio del Módulo 1:
Task: "Crear entidad Usuario en app/src/main/kotlin/com/ecosmart/domain/model/Usuario.kt"
Task: "Crear interfaz UsuarioRepository en app/src/main/kotlin/com/ecosmart/domain/repository/UsuarioRepository.kt"

# Luego, en paralelo, las pantallas Compose (una vez listos sus ViewModels):
Task: "Crear RegistroScreen en app/src/main/kotlin/com/ecosmart/presentation/auth/RegistroScreen.kt"
Task: "Crear PerfilEdicionScreen en app/src/main/kotlin/com/ecosmart/presentation/auth/PerfilEdicionScreen.kt"

# Y los tests unitarios, en paralelo entre sí:
Task: "Test unitario de RegistrarUsuario en app/src/test/kotlin/com/ecosmart/application/auth/RegistrarUsuarioTest.kt"
Task: "Test unitario de Usuario en app/src/test/kotlin/com/ecosmart/domain/model/UsuarioTest.kt"
```

---

## Estrategia de Implementación

### MVP mínimo (flujo completo: elegir actividad → realizarla → la app la verifica → recibo puntos → veo mi progreso)

El MVP mínimo DEBE cubrir el flujo central de punta a punta, no solo el
circuito de puntos aislado. Eso requiere una porción mínima de los 5
módulos — no los 5 completos, pero tampoco solo 1, 2 y 4:

1. Completar Fase 1 (Setup) y Fase 2 (Foundational).
2. Completar Módulo 1 (Login) — sin esto no hay ningún flujo.
3. Completar Módulo 2 (Permisos) — bloquea Cámara/Podómetro si no está.
4. Completar Módulo 3 (Home y Contenido) **solo su porción mínima**: US4
   (Home filtrada — el paso *"elijo actividad"*) y US5 (detalle de la
   actividad — pasos a seguir y resultado esperado antes de realizarla).
   US6 (contenido educativo) y US7 (Puntos Verdes) son P3/P2 y pueden
   diferirse a la Entrega Incremental; no son necesarios para este flujo.
5. Completar Módulo 4 (Verificación y Puntuación) completo — es el paso
   *"la realizo → la app la verifica → recibo puntos"* (caminata vía
   podómetro o foto vía EcoGPT, con topes diarios y puntaje).
6. Completar Módulo 5 (Perfil e Historial) **solo su porción mínima**:
   US11 (puntos totales, % por categoría, racha, nivel — el paso *"veo mi
   progreso"*). US12 (historial completo con "Ver más") es P3 y puede
   diferirse.
7. **DETENER y VALIDAR**: un usuario puede registrarse, dar permisos,
   **elegir una actividad desde la Home (T051), realizarla (caminata o
   foto), ver que el sistema la verifica automáticamente (podómetro o
   EcoGPT, T072/T075), sumar los puntos correspondientes (T070), y ver su
   progreso actualizado en el perfil (puntos/racha/nivel, T088)** — el
   ciclo completo de punta a punta que define el valor central de
   EcoSmart, no solo un fragmento técnico del backend de puntuación.

### Entrega Incremental (orden completo recomendado)

1. Setup + Foundational → base lista.
2. Módulo 1 (Login) → probar independientemente → demo de registro/login.
3. Módulo 2 (Permisos) → probar independientemente → demo de solicitud y
   bloqueo de formularios.
4. Módulo 3 (Home y Contenido) → probar independientemente → demo de
   exploración de actividades y Puntos Verdes.
5. Módulo 4 (Verificación y Puntuación) → probar independientemente →
   demo del circuito completo de puntos (MVP funcional).
6. Módulo 5 (Perfil e Historial) → probar independientemente → demo de
   métricas y progreso.
7. Fase 8 (Polish) → integración final, tests de integración, checklist
   de capas, validación de `quickstart.md`.

### Estrategia de Equipo en Paralelo

Con 3+ personas, tras completar Foundational:

- Persona A: Módulo 1 (Login) → luego Módulo 3 (Home)
- Persona B: Módulo 2 (Permisos) → luego apoya Módulo 4
- Persona C: espera a que A y B avancen lo mínimo (entidades/repos) y arma
  Módulo 4 (Verificación) en cuanto Módulo 2 esté listo
- Módulo 5 se toma al final, cuando Módulo 4 ya produce datos reales

---

## Notas

- `[P]` = archivos distintos, sin dependencias pendientes entre sí
- `[USx]` mapea cada tarea a su historia de usuario para trazabilidad fina dentro del módulo
- `(Depende de: ...)` se agrega solo cuando existe una dependencia real de otra tarea; su ausencia significa que la tarea es de "punto de entrada" de su bloque
- Cada módulo debe quedar independientemente completable y testeable antes de pasar al siguiente (o, en equipo, antes de integrarlo en Fase 8)
- Verificar que los tests fallen antes de implementar, si se sigue TDD estricto para las tareas de test listadas
- Commitear después de cada tarea o grupo lógico de tareas
- Evitar: tareas vagas, conflictos de mismo archivo entre tareas `[P]`, dependencias cruzadas entre módulos que rompan su independencia de prueba
