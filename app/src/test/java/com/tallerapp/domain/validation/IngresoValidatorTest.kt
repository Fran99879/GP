package com.tallerapp.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica V-2 (obligatorios). El pago mixto se retiró en v13 junto con `MetodoPago`. */
class IngresoValidatorTest {

    @Test
    fun `ingreso simple valido`() {
        val errores = IngresoValidator.validar(50_000, "Venta")
        assertTrue(errores.esValido)
    }

    @Test
    fun `monto cero o concepto vacio es invalido`() {
        val errores = IngresoValidator.validar(0, "")
        assertFalse(errores.esValido)
        assertTrue(errores.monto != null)
        assertTrue(errores.concepto != null)
    }

    @Test
    fun `monto nulo es invalido`() {
        val errores = IngresoValidator.validar(null, "Venta")
        assertFalse(errores.esValido)
        assertTrue(errores.monto != null)
    }
}
