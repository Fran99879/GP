package com.tallerapp.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DineroTest {

    @Test
    fun `parsea pesos con punto a centavos`() {
        assertEquals(150050L, Dinero.parsearACentavos("1500.50"))
    }

    @Test
    fun `parsea pesos con coma a centavos`() {
        assertEquals(150050L, Dinero.parsearACentavos("1500,50"))
    }

    @Test
    fun `rechaza texto vacio o invalido o negativo`() {
        assertNull(Dinero.parsearACentavos(""))
        assertNull(Dinero.parsearACentavos("abc"))
        assertNull(Dinero.parsearACentavos("-10"))
    }

    @Test
    fun `formatea centavos con separador de miles y dos decimales`() {
        assertEquals("$ 1.500,50", Dinero.formatear(150050L))
        assertEquals("$ 0,05", Dinero.formatear(5L))
        assertEquals("-$ 1.500,50", Dinero.formatear(-150050L))
    }
}
