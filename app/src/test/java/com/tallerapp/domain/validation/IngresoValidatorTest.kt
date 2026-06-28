package com.tallerapp.domain.validation

import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.RepartoPago
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica V-2 (obligatorios) y V-6 (pago mixto). */
class IngresoValidatorTest {

    @Test
    fun `ingreso simple valido`() {
        val errores = IngresoValidator.validar(50_000, "Venta", MetodoPago.EFECTIVO, null)
        assertTrue(errores.esValido)
    }

    @Test
    fun `monto cero o concepto vacio o sin metodo es invalido`() {
        val errores = IngresoValidator.validar(0, "", null, null)
        assertFalse(errores.esValido)
        assertTrue(errores.monto != null)
        assertTrue(errores.concepto != null)
        assertTrue(errores.metodo != null)
    }

    @Test
    fun `pago mixto que suma el total es valido`() {
        val reparto = RepartoPago(efectivoCentavos = 30_000, transferenciaCentavos = 20_000)
        val errores = IngresoValidator.validar(50_000, "Cobro", MetodoPago.PAGO_MIXTO, reparto)
        assertNull(errores.reparto)
        assertTrue(errores.esValido)
    }

    @Test
    fun `pago mixto que no suma el total es invalido`() {
        val reparto = RepartoPago(efectivoCentavos = 30_000, transferenciaCentavos = 10_000)
        val errores = IngresoValidator.validar(50_000, "Cobro", MetodoPago.PAGO_MIXTO, reparto)
        assertTrue(errores.reparto != null)
    }

    @Test
    fun `pago mixto con un solo metodo es invalido`() {
        val reparto = RepartoPago(efectivoCentavos = 50_000)
        val errores = IngresoValidator.validar(50_000, "Cobro", MetodoPago.PAGO_MIXTO, reparto)
        assertTrue(errores.reparto != null)
    }
}
