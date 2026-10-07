"""Test de paridad (T011d, spec 002-firestore-datos-usuario): los mismos casos ya
cubiertos por los tests de dominio Kotlin de `001-ecosmart-mvp`
(`UsuarioTest.kt`, `RegistroVerificacionTest.kt`) deben producir el mismo resultado en
esta reimplementación Python (`app/puntos.py`, T011a-T011c). Evita que ambas
implementaciones diverjan silenciosamente (Principio V de constitution.md).
"""

from datetime import date, timedelta

import pytest

from app.puntos import (
    calcular_nivel,
    calcular_puntos,
    calcular_racha,
    distancia_hamming,
    es_duplicado,
    tope_diario,
)

# --- Paridad con EstrategiaDePuntaje.kt ---------------------------------------------


def test_puntos_caminar_por_bloques_de_133_pasos():
    # 266 pasos = 2 bloques completos de 133 -> 100 puntos (RF-031/049, caso de spec 001).
    assert calcular_puntos("CAMINAR", 266) == 100
    # Pasos remanentes (< 133) no puntúan.
    assert calcular_puntos("CAMINAR", 199) == 50


def test_puntos_y_tope_reciclar():
    assert calcular_puntos("RECICLAR", 1) == 50
    assert tope_diario("RECICLAR") == 1


def test_puntos_y_tope_reutilizar():
    assert calcular_puntos("REUTILIZAR", 1) == 100
    assert tope_diario("REUTILIZAR") == 5


def test_caminar_no_tiene_tope_diario():
    assert tope_diario("CAMINAR") is None


# --- Paridad con UsuarioTest.kt -----------------------------------------------------


def test_nivel_se_deriva_del_puntaje_historico_segun_los_umbrales():
    assert calcular_nivel(0) == "SEMILLA"
    assert calcular_nivel(500) == "BROTE"
    assert calcular_nivel(1500) == "PLANTA"
    assert calcular_nivel(3500) == "ARBOL"


def test_racha_incrementa_en_dias_consecutivos():
    ayer = date(2026, 9, 20)
    hoy = date(2026, 9, 21)
    assert calcular_racha(racha_actual=3, ultima_actividad_aprobada_en=ayer, hoy=hoy) == 4


def test_racha_no_se_duplica_si_ya_se_registro_hoy():
    hoy = date(2026, 9, 21)
    assert calcular_racha(racha_actual=2, ultima_actividad_aprobada_en=hoy, hoy=hoy) == 2


def test_racha_se_reinicia_tras_un_dia_sin_actividad():
    hace_tres_dias = date(2026, 9, 18)
    hoy = date(2026, 9, 21)
    assert calcular_racha(racha_actual=5, ultima_actividad_aprobada_en=hace_tres_dias, hoy=hoy) == 1


def test_racha_primera_actividad_sin_historial_previo():
    hoy = date(2026, 9, 21)
    assert calcular_racha(racha_actual=0, ultima_actividad_aprobada_en=None, hoy=hoy) == 1


# --- Paridad con RegistroVerificacionTest.kt ----------------------------------------


def test_es_duplicado_si_la_distancia_de_hamming_esta_dentro_del_umbral():
    # "ff00" vs "ff01": difieren en 1 bit.
    assert es_duplicado("ff00", "ff01") is True


def test_no_es_duplicado_si_la_distancia_de_hamming_supera_el_umbral():
    # "0000" vs "ffff": difieren en los 16 bits.
    assert es_duplicado("0000", "ffff") is False


def test_nunca_es_duplicado_si_a_alguno_le_falta_la_huella():
    assert es_duplicado(None, "ff00") is False
    assert es_duplicado("ff00", None) is False


def test_el_mismo_hash_exacto_siempre_es_duplicado():
    assert es_duplicado("a1b2c3d4", "a1b2c3d4") is True


@pytest.mark.parametrize(
    ("hash_a", "hash_b", "bits_distintos"),
    [("ff00", "ff01", 1), ("0000", "ffff", 16), ("a1b2c3d4", "a1b2c3d4", 0)],
)
def test_distancia_hamming(hash_a: str, hash_b: str, bits_distintos: int):
    assert distancia_hamming(hash_a, hash_b) == bits_distintos
