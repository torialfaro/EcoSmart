package com.ecosmart.domain.model

import com.ecosmart.domain.valueobject.EstadoPermiso
import com.ecosmart.domain.valueobject.TipoPermiso
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermisoDispositivoTest {

    @Test
    fun `bloquea el formulario si el permiso requerido esta denegado`() {
        val permiso = PermisoDispositivo(TipoPermiso.CAMARA, EstadoPermiso.DENEGADO)

        assertTrue(permiso.bloqueaFormulario(TipoPermiso.CAMARA))
    }

    @Test
    fun `bloquea el formulario si el permiso requerido no fue solicitado`() {
        val permiso = PermisoDispositivo(TipoPermiso.GPS, EstadoPermiso.NO_SOLICITADO)

        assertTrue(permiso.bloqueaFormulario(TipoPermiso.GPS))
    }

    @Test
    fun `no bloquea el formulario si el permiso requerido esta otorgado`() {
        val permiso = PermisoDispositivo(TipoPermiso.CAMARA, EstadoPermiso.OTORGADO)

        assertFalse(permiso.bloqueaFormulario(TipoPermiso.CAMARA))
    }

    @Test
    fun `no bloquea un formulario que depende de un permiso distinto`() {
        // US13 AC3: denegar Podómetro no debe afectar una actividad que solo depende de Cámara.
        val permisoPodometro = PermisoDispositivo(TipoPermiso.PODOMETRO, EstadoPermiso.DENEGADO)

        assertFalse(permisoPodometro.bloqueaFormulario(TipoPermiso.CAMARA))
    }
}
