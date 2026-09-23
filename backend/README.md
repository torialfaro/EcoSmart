# Backend de EcoGPT

Implementa `POST /verificaciones` del contrato en
`../specs/001-ecosmart-mvp/contracts/openapi.yaml`: recibe la foto +
descripción que el cliente Android envía al verificar una actividad de
Reciclar/Reutilizar, y llama a la API de **Gemini** (Google AI Studio, con
visión, capa **100% gratuita** — sin tarjeta de crédito) para comparar la
imagen y la descripción del usuario contra el resultado esperado de la
actividad. Devuelve `APROBADO`, `RECHAZADO` o `INDETERMINADO` con un
motivo en tono alentador (nunca punitivo).

Reemplaza al placeholder `https://api.ecogpt.example/v1/` que usaba el
proyecto Android por defecto — ese dominio (reservado por RFC 2606) nunca
resolvía a ningún servidor real, por eso cada verificación fallaba con
"Sin conexión a internet" sin importar la conexión real del dispositivo.

## 1. Desarrollo local

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate        # PowerShell/Windows
# source .venv/bin/activate   # Linux/Mac

pip install -r requirements.txt
copy .env.example .env        # y completar GEMINI_API_KEY + ECOGPT_SHARED_API_KEY

uvicorn app.main:app --reload --port 8000
```

Probar con `curl` (reemplazando la ruta a una imagen real):

```bash
curl -X POST http://localhost:8000/verificaciones \
  -H "X-EcoGPT-Api-Key: el-mismo-valor-de-ECOGPT_SHARED_API_KEY" \
  -F "imagen=@./ejemplo.jpg" \
  -F "categoria=RECICLAR" \
  -F "resultadoEsperado=Una foto de residuos reciclables separados correctamente" \
  -F "descripcionUsuario=Separé botellas de plástico y las llevé al punto verde" \
  -F "huellasImagenesAprobadasPrevias="
```

## 2. Obtener una API key de Gemini (gratis, sin tarjeta de crédito)

1. Entrar a <https://aistudio.google.com/apikey> con una cuenta de Google.
2. **Create API key** → elegir o crear un proyecto de Google Cloud (no
   requiere billing habilitado para la capa gratuita).
3. Copiar la key generada a `GEMINI_API_KEY`.

La capa gratuita de Google AI Studio no pide tarjeta de crédito y alcanza
de sobra para un MVP académico (cuota diaria de miles de requests en los
modelos Flash). Los límites exactos de cuota pueden cambiar — ver
<https://ai.google.dev/gemini-api/docs/rate-limits> si algún request
empieza a devolver error 429.

## 3. Desplegar en Render (free tier)

1. Crear cuenta en <https://render.com> y conectar el repo de GitHub de EcoSmart.
2. **New → Blueprint**, apuntando a este repo — Render va a detectar
   `backend/render.yaml` automáticamente y va a pedir los 2 valores marcados
   `sync: false`: `GEMINI_API_KEY` y `ECOGPT_SHARED_API_KEY`.
   - Si preferís crearlo a mano en vez de con el Blueprint: **New → Web
     Service**, Root Directory = `backend`, Build Command =
     `pip install -r requirements.txt`, Start Command =
     `uvicorn app.main:app --host 0.0.0.0 --port $PORT`, y cargar las
     mismas 2 variables de entorno (`GEMINI_API_KEY`,
     `ECOGPT_SHARED_API_KEY`) en **Environment**.
3. Cuando termine el deploy, Render te da una URL pública tipo
   `https://ecosmart-ecogpt.onrender.com`.
4. Verificar que está vivo: `curl https://ecosmart-ecogpt.onrender.com/health`
   → `{"estado":"ok"}`.

### ⚠️ Riesgo del free tier de Render: cold start vs. timeout de 30s

El plan free de Render "duerme" el servicio tras ~15 minutos sin tráfico.
La primera request después de dormido puede tardar 30-50+ segundos en
responder mientras el servicio arranca — pero el cliente Android tiene un
`callTimeout` **fijo en 30 segundos** (RNF-008/SC-008, `NetworkModule.kt`),
así que la primera verificación del día puede fallar con "Timeout" aunque
el backend esté perfectamente sano, solo dormido. Mitigaciones posibles
(no implementadas todavía, quedan a tu criterio):

- Un ping periódico externo (p. ej. un cron gratuito tipo
  [cron-job.org](https://cron-job.org)) que golpee `/health` cada 10
  minutos para mantenerlo despierto.
- Pasar a un plan pago de Render sin sleep.
- Aumentar el timeout del cliente Android para este caso puntual (implica
  reabrir RNF-008/SC-008 en `spec.md`).

## 4. Conectar el proyecto Android al backend desplegado

En `local.properties` (raíz del proyecto, nunca se commitea):

```properties
ECOGPT_API_KEY=el-mismo-valor-de-ECOGPT_SHARED_API_KEY-en-Render
ECOGPT_BASE_URL=https://ecosmart-ecogpt.onrender.com/
```

La barra final en `ECOGPT_BASE_URL` es obligatoria (Retrofit lanza
`IllegalArgumentException` si falta — ver `CORRECCIÓN-005`/T105 en
`tasks.md`).
