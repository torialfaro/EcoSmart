"""Backend de confianza — otorgamiento de puntos (spec 002-firestore-datos-usuario).

Único componente autorizado a escribir `puntosHistoricos`, `rachaActual`, `nivel` de
`usuarios/{uid}` y el `resultado`/`puntosOtorgados` de cada `registrosVerificacion`
(RF-D014). El cliente Android nunca escribe esos campos directamente; las Reglas de
Seguridad de Firestore (`firestore.rules`) además los rechazan como segunda capa de
defensa.

Las reglas de negocio de acá (tope diario, racha, nivel, duplicados) son una
**reimplementación en Python** de las ya existentes en el dominio Kotlin de
`001-ecosmart-mvp` (`EstrategiaDePuntaje`, `Usuario`, `RegistroVerificacion`) — no un
"traslado sin reescritura" (ver plan.md § Constitution Check III, corrección de
`/speckit.analyze`). `tests/test_paridad_reglas_negocio.py` (T011d) prueba que ambas
implementaciones coinciden en los mismos casos.
"""

from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException
from firebase_admin import firestore
from google.cloud.firestore_v1.base_query import FieldFilter

from .firebase_admin_setup import eliminar_credencial_auth, obtener_firestore, verificar_id_token
from .schemas_puntos import (
    RegistroVerificacionOtorgado,
    SolicitudCaminar,
    SolicitudMigracion,
    SolicitudPasosDelDia,
    SolicitudRegistroReciclarReutilizar,
)

router = APIRouter()

# --- T011a: tope diario por categoría (RF-032/033/035 de spec 001, RF-D013) ---------

PASOS_POR_BLOQUE = 133
PUNTOS_POR_BLOQUE_CAMINATA = 50
PUNTOS_FOTO_RECICLAR = 50
TOPE_DIARIO_RECICLAR = 1
PUNTOS_FOTO_REUTILIZAR = 100
TOPE_DIARIO_REUTILIZAR = 5

_TOPES_DIARIOS = {"RECICLAR": TOPE_DIARIO_RECICLAR, "REUTILIZAR": TOPE_DIARIO_REUTILIZAR, "CAMINAR": None}


def tope_diario(categoria: str) -> int | None:
    """`None` significa sin tope (Caminar no tiene uno) — equivalente a
    `EstrategiaDePuntaje.topeDiario` de spec 001."""
    return _TOPES_DIARIOS[categoria]


def calcular_puntos(categoria: str, cantidad: int) -> int:
    """`cantidad` es la unidad propia de cada categoría: pasos para Caminar, 1 foto
    aprobada para Reciclar/Reutilizar — equivalente a `EstrategiaDePuntaje.calcularPuntos`.
    """
    if categoria == "CAMINAR":
        return (cantidad // PASOS_POR_BLOQUE) * PUNTOS_POR_BLOQUE_CAMINATA
    if categoria == "RECICLAR":
        return cantidad * PUNTOS_FOTO_RECICLAR
    if categoria == "REUTILIZAR":
        return cantidad * PUNTOS_FOTO_REUTILIZAR
    raise ValueError(f"Categoría desconocida: {categoria}")


# --- T011b: racha y nivel (RF-038/039/040/046/047 de spec 001, RF-D013) -------------

_NIVELES_POR_PUNTOS_MINIMOS = (
    ("ARBOL", 3500),
    ("PLANTA", 1500),
    ("BROTE", 500),
    ("SEMILLA", 0),
)


def calcular_nivel(puntos_historicos: int) -> str:
    """Equivalente a `NivelUsuario.desdePuntaje`: el nivel más alto cuyo umbral se alcanza."""
    for nivel, puntos_minimos in _NIVELES_POR_PUNTOS_MINIMOS:
        if puntos_historicos >= puntos_minimos:
            return nivel
    return "SEMILLA"  # pragma: no cover — inalcanzable, 0 ya cubre cualquier valor >= 0


def calcular_racha(racha_actual: int, ultima_actividad_aprobada_en: date | None, hoy: date) -> int:
    """Equivalente a `Usuario.registrarActividadAprobadaHoy`: mismo día no cambia,
    día consecutivo suma 1, cualquier otro caso reinicia a 1."""
    if ultima_actividad_aprobada_en == hoy:
        return racha_actual
    if ultima_actividad_aprobada_en == hoy - timedelta(days=1):
        return racha_actual + 1
    return 1


# --- T011c: detección de fotos duplicadas (RF-058/059 de spec 001, RF-D013) ---------

UMBRAL_HAMMING_POR_DEFECTO = 5


def distancia_hamming(hash_hex_a: str, hash_hex_b: str) -> int:
    return bin(int(hash_hex_a, 16) ^ int(hash_hex_b, 16)).count("1")


def es_duplicado(huella_propia: str | None, huella_otra: str | None, umbral: int = UMBRAL_HAMMING_POR_DEFECTO) -> bool:
    """Equivalente a `RegistroVerificacion.esDuplicadoDe`: sin huella de alguno de los
    dos lados, nunca son duplicados (p. ej. Caminar, que no tiene foto)."""
    if huella_propia is None or huella_otra is None:
        return False
    return distancia_hamming(huella_propia, huella_otra) <= umbral


# --- T011: endpoints (esqueleto; wiring completo de cada historia en su propia fase) -

@router.post("/registros-verificacion/reciclar-reutilizar", response_model=RegistroVerificacionOtorgado)
async def otorgar_puntos_reciclar_reutilizar(
    datos: SolicitudRegistroReciclarReutilizar,
    uid: str = Depends(verificar_id_token),
) -> RegistroVerificacionOtorgado:
    db = obtener_firestore()
    usuario_ref = db.collection("usuarios").document(uid)
    registros_ref = usuario_ref.collection("registrosVerificacion")

    transaction = db.transaction()

    @firestore.transactional
    def _procesar(transaction) -> RegistroVerificacionOtorgado:
        usuario_snap = usuario_ref.get(transaction=transaction)
        if not usuario_snap.exists:
            raise HTTPException(status_code=404, detail="Usuario no encontrado.")
        usuario = usuario_snap.to_dict() or {}

        tope = tope_diario(datos.categoria)
        if tope is not None:
            consulta = registros_ref.where(filter=FieldFilter("categoria", "==", datos.categoria)).where(
                filter=FieldFilter("fecha", "==", datos.fecha.isoformat())
            ).where(filter=FieldFilter("resultado", "==", "APROBADO"))
            usadas = sum(1 for _ in consulta.get(transaction=transaction))
            if usadas >= tope:
                raise HTTPException(status_code=409, detail="TOPE_DIARIO_ALCANZADO")

        if datos.resultado == "APROBADO":
            consulta_duplicados = registros_ref.where(
                filter=FieldFilter("categoria", "==", datos.categoria)
            ).where(filter=FieldFilter("resultado", "==", "APROBADO"))
            for doc in consulta_duplicados.get(transaction=transaction):
                if es_duplicado(datos.huellaImagen, doc.to_dict().get("huellaImagen")):
                    raise HTTPException(status_code=409, detail="FOTO_DUPLICADA")

        puntos = calcular_puntos(datos.categoria, 1) if datos.resultado == "APROBADO" else 0

        registro_ref = registros_ref.document()
        transaction.set(
            registro_ref,
            {
                "actividadId": datos.actividadId,
                "categoria": datos.categoria,
                "fecha": datos.fecha.isoformat(),
                "resultado": datos.resultado,
                "motivoIA": datos.motivoIA,
                "puntosOtorgados": puntos,
                "huellaImagen": datos.huellaImagen,
                "pasosRegistrados": None,
                "creadoEn": firestore.SERVER_TIMESTAMP,
            },
        )

        puntos_historicos = usuario.get("puntosHistoricos", 0)
        racha_actual = usuario.get("rachaActual", 0)
        if datos.resultado == "APROBADO":
            ultima_str = usuario.get("ultimaActividadAprobadaEn")
            ultima = date.fromisoformat(ultima_str) if ultima_str else None
            racha_actual = calcular_racha(racha_actual, ultima, datos.fecha)
            puntos_historicos += puntos
            transaction.update(
                usuario_ref,
                {
                    "puntosHistoricos": puntos_historicos,
                    "rachaActual": racha_actual,
                    "nivel": calcular_nivel(puntos_historicos),
                    "ultimaActividadAprobadaEn": datos.fecha.isoformat(),
                },
            )

        return RegistroVerificacionOtorgado(
            id=registro_ref.id,
            resultado=datos.resultado,
            puntosOtorgados=puntos,
            puntosHistoricosActualizados=puntos_historicos,
            rachaActual=racha_actual,
            nivel=calcular_nivel(puntos_historicos),
        )

    return _procesar(transaction)


@router.post("/registros-verificacion/caminar")
async def otorgar_puntos_caminar(
    datos: SolicitudCaminar,
    uid: str = Depends(verificar_id_token),
) -> dict:
    """RF-D007/RF-D008: arbitra `caminataEnCurso` (singleton `usuarios/{uid}/caminataEnCurso/actual`)
    de forma transaccional para que dos dispositivos de la misma cuenta no inicien dos
    caminatas a la vez. La respuesta no usa `response_model=RegistroVerificacionOtorgado`
    porque INICIAR/ACTUALIZAR_PROGRESO no tienen un registro de puntos que devolver — solo
    COMPLETAR lo incluye.
    """
    db = obtener_firestore()
    usuario_ref = db.collection("usuarios").document(uid)
    caminata_ref = usuario_ref.collection("caminataEnCurso").document("actual")
    hoy = date.today()
    hoy_iso = hoy.isoformat()

    if datos.accion == "INICIAR":
        transaction = db.transaction()

        @firestore.transactional
        def _iniciar(transaction):
            actual = (caminata_ref.get(transaction=transaction).to_dict()) or {}
            if actual.get("estado") == "EN_CURSO" and actual.get("fecha") == hoy_iso:
                raise HTTPException(status_code=409, detail="CAMINATA_ACTIVA_EN_OTRO_DISPOSITIVO")
            transaction.set(
                caminata_ref,
                {
                    "estado": "EN_CURSO",
                    "actividadId": datos.actividadId,
                    "metaPasos": datos.metaPasos,
                    "pasosLogrados": 0,
                    "fecha": hoy_iso,
                    "dispositivoIdIniciador": datos.dispositivoId,
                    "actualizadoEn": firestore.SERVER_TIMESTAMP,
                },
            )

        _iniciar(transaction)
        return {"estado": "EN_CURSO", "pasosLogrados": 0, "metaPasos": datos.metaPasos}

    if datos.accion == "ACTUALIZAR_PROGRESO":
        transaction = db.transaction()

        @firestore.transactional
        def _actualizar(transaction):
            actual = (caminata_ref.get(transaction=transaction).to_dict()) or {}
            if actual.get("estado") != "EN_CURSO" or actual.get("fecha") != hoy_iso:
                raise HTTPException(status_code=409, detail="NO_HAY_CAMINATA_ACTIVA")
            transaction.update(
                caminata_ref,
                {
                    "pasosLogrados": datos.pasosLogrados or 0,
                    "actualizadoEn": firestore.SERVER_TIMESTAMP,
                },
            )

        _actualizar(transaction)
        return {"estado": "EN_CURSO", "pasosLogrados": datos.pasosLogrados or 0, "metaPasos": datos.metaPasos}

    # accion == "COMPLETAR"
    transaction = db.transaction()

    @firestore.transactional
    def _completar(transaction):
        caminata_actual = (caminata_ref.get(transaction=transaction).to_dict()) or {}
        if caminata_actual.get("estado") == "COMPLETADA" and caminata_actual.get("fecha") == hoy_iso:
            raise HTTPException(status_code=409, detail="CAMINATA_YA_COMPLETADA")

        usuario_snap = usuario_ref.get(transaction=transaction)
        if not usuario_snap.exists:
            raise HTTPException(status_code=404, detail="Usuario no encontrado.")
        usuario = usuario_snap.to_dict() or {}

        puntos = calcular_puntos("CAMINAR", datos.pasosLogrados or 0)
        puntos_historicos = usuario.get("puntosHistoricos", 0) + puntos
        ultima_str = usuario.get("ultimaActividadAprobadaEn")
        ultima = date.fromisoformat(ultima_str) if ultima_str else None
        racha_actual = calcular_racha(usuario.get("rachaActual", 0), ultima, hoy)

        registro_ref = usuario_ref.collection("registrosVerificacion").document()
        transaction.set(
            registro_ref,
            {
                "actividadId": datos.actividadId,
                "categoria": "CAMINAR",
                "fecha": hoy_iso,
                "resultado": "APROBADO",
                "motivoIA": None,
                "puntosOtorgados": puntos,
                "huellaImagen": None,
                "pasosRegistrados": datos.pasosLogrados,
                "creadoEn": firestore.SERVER_TIMESTAMP,
            },
        )
        transaction.update(
            usuario_ref,
            {
                "puntosHistoricos": puntos_historicos,
                "rachaActual": racha_actual,
                "nivel": calcular_nivel(puntos_historicos),
                "ultimaActividadAprobadaEn": hoy_iso,
            },
        )
        transaction.set(
            caminata_ref,
            {
                "estado": "COMPLETADA",
                "actividadId": datos.actividadId,
                "metaPasos": datos.metaPasos,
                "pasosLogrados": datos.pasosLogrados,
                "fecha": hoy_iso,
                "actualizadoEn": firestore.SERVER_TIMESTAMP,
            },
        )
        return registro_ref.id, puntos, puntos_historicos, racha_actual

    registro_id, puntos, puntos_historicos, racha_actual = _completar(transaction)

    return {
        "id": registro_id,
        "resultado": "APROBADO",
        "puntosOtorgados": puntos,
        "puntosHistoricosActualizados": puntos_historicos,
        "rachaActual": racha_actual,
        "nivel": calcular_nivel(puntos_historicos),
    }


@router.patch("/pasos-del-dia")
async def actualizar_pasos_del_dia(
    datos: SolicitudPasosDelDia,
    uid: str = Depends(verificar_id_token),
) -> dict[str, str]:
    """RF-D007: puramente informativo, nunca otorga puntos (eso lo hace `/caminar` al
    completar una meta, RF-D015). Transaccional para que dos actualizaciones casi
    simultáneas de distintos dispositivos no se pisen: para la misma fecha se conserva el
    valor más alto ya escrito, para no retroceder el conteo con la línea base de sensor
    (100% local) de otro dispositivo."""
    db = obtener_firestore()
    usuario_ref = db.collection("usuarios").document(uid)
    fecha_iso = datos.fecha.isoformat()
    transaction = db.transaction()

    @firestore.transactional
    def _actualizar(transaction):
        actual = (usuario_ref.get(transaction=transaction).to_dict()) or {}
        if actual.get("pasosHoyFecha") == fecha_iso and actual.get("pasosHoy", 0) > datos.pasosHoy:
            return
        transaction.set(
            usuario_ref,
            {
                "pasosHoy": datos.pasosHoy,
                "pasosHoyFecha": fecha_iso,
                "actualizadoEn": firestore.SERVER_TIMESTAMP,
            },
            merge=True,
        )

    _actualizar(transaction)
    return {"estado": "ACTUALIZADO"}


@router.post("/migracion/subir-datos-locales")
async def migrar_datos_locales(
    datos: SolicitudMigracion,
    uid: str = Depends(verificar_id_token),
) -> dict[str, str]:
    """RF-D011: sube una única vez el perfil + historial de Room de una cuenta de spec 001.

    Idempotente por diseño, sin bandera separada: cada `RegistroMigracion.id` (PK local
    de Room) se reutiliza como ID del documento Firestore, así que repetir la subida con
    los mismos datos sobrescribe (`set()`) en vez de duplicar (`add()`). El documento de
    perfil también se escribe con `set(merge=True)`, idempotente por ser un único
    documento (no una lista).
    """
    db = obtener_firestore()
    usuario_ref = db.collection("usuarios").document(uid)
    perfil = datos.perfil

    ya_existe = usuario_ref.get().exists
    datos_usuario: dict = {
        "nombre": perfil.nombre,
        "apellido": perfil.apellido,
        "nombreUsuario": perfil.nombreUsuario,
        "barrio": perfil.barrio,
        "telefono": perfil.telefono,
        "categoriasDeInteres": perfil.categoriasDeInteres,
        "puntosHistoricos": perfil.puntosHistoricos,
        "rachaActual": perfil.rachaActual,
        "nivel": calcular_nivel(perfil.puntosHistoricos),
        "ultimaActividadAprobadaEn": (
            perfil.ultimaActividadAprobadaEn.isoformat() if perfil.ultimaActividadAprobadaEn else None
        ),
        "actualizadoEn": firestore.SERVER_TIMESTAMP,
    }
    if not ya_existe:
        datos_usuario["creadoEn"] = firestore.SERVER_TIMESTAMP
    usuario_ref.set(datos_usuario, merge=True)

    for registro in datos.historial:
        registro_ref = usuario_ref.collection("registrosVerificacion").document(f"migrado-{registro.id}")
        registro_ref.set(
            {
                "actividadId": registro.actividadId,
                "categoria": registro.categoria,
                "fecha": registro.fecha.isoformat(),
                "resultado": registro.resultado,
                "motivoIA": registro.motivoIA,
                "puntosOtorgados": registro.puntosOtorgados,
                "huellaImagen": registro.huellaImagen,
                "pasosRegistrados": registro.pasosRegistrados,
                "creadoEn": firestore.SERVER_TIMESTAMP,
            },
        )

    return {"estado": "MIGRADO"}


@router.delete("/cuenta")
async def eliminar_cuenta(uid: str = Depends(verificar_id_token)) -> dict[str, str]:
    """RF-D012: borrado inmediato y definitivo — documento de perfil, todas sus
    subcolecciones (registrosVerificacion, caminataEnCurso, permisosDispositivo) y la
    credencial de Firebase Authentication. Sin período de gracia (la pantalla de
    confirmación del cliente es la única salvaguarda, ver `spec.md` RF-D012)."""
    db = obtener_firestore()
    usuario_ref = db.collection("usuarios").document(uid)

    for subcoleccion in usuario_ref.collections():
        for documento in subcoleccion.stream():
            documento.reference.delete()
    usuario_ref.delete()

    eliminar_credencial_auth(uid)

    return {"estado": "ELIMINADA"}

