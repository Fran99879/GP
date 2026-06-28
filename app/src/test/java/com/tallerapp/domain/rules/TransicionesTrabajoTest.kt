package com.tallerapp.domain.rules

import com.tallerapp.domain.model.EstadoReparacion.CANCELADO
import com.tallerapp.domain.model.EstadoReparacion.ENTREGADO
import com.tallerapp.domain.model.EstadoReparacion.EN_REPARACION
import com.tallerapp.domain.model.EstadoReparacion.ESPERANDO_REPUESTOS
import com.tallerapp.domain.model.EstadoReparacion.PENDIENTE
import com.tallerapp.domain.model.EstadoReparacion.TERMINADO
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica la máquina de transiciones (Frozen Spec 7.2). */
class TransicionesTrabajoTest {

    @Test
    fun `pendiente puede ir a en reparacion, esperando repuestos o cancelado`() {
        assertTrue(TransicionesTrabajo.puedeTransicionar(PENDIENTE, EN_REPARACION))
        assertTrue(TransicionesTrabajo.puedeTransicionar(PENDIENTE, ESPERANDO_REPUESTOS))
        assertTrue(TransicionesTrabajo.puedeTransicionar(PENDIENTE, CANCELADO))
    }

    @Test
    fun `pendiente no puede ir directo a entregado`() {
        assertFalse(TransicionesTrabajo.puedeTransicionar(PENDIENTE, ENTREGADO))
    }

    @Test
    fun `solo terminado puede ir a entregado`() {
        assertTrue(TransicionesTrabajo.puedeTransicionar(TERMINADO, ENTREGADO))
        assertFalse(TransicionesTrabajo.puedeTransicionar(EN_REPARACION, ENTREGADO))
        assertFalse(TransicionesTrabajo.puedeTransicionar(ESPERANDO_REPUESTOS, ENTREGADO))
    }

    @Test
    fun `entregado y cancelado son terminales`() {
        assertTrue(TransicionesTrabajo.transicionesValidas(ENTREGADO).isEmpty())
        assertTrue(TransicionesTrabajo.transicionesValidas(CANCELADO).isEmpty())
    }
}
