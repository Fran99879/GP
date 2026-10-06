package com.tallerapp.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DineroTest {

    // Los mismos casos están en PuraTests.cs de la app de escritorio: la misma cadena tiene que dar
    // los mismos centavos en las dos apps (ver PLAN-ESCRITORIO-AL-DIA.md, punto A1).

    @Test
    fun `parsea pesos con punto a centavos`() {
        assertEquals(150050L, Dinero.parsearACentavos("1500.50"))
    }

    @Test
    fun `parsea pesos con coma a centavos`() {
        assertEquals(150050L, Dinero.parsearACentavos("1500,50"))
        assertEquals(150050L, Dinero.parsearACentavos("1.500,50"))
        assertEquals(5L, Dinero.parsearACentavos("0,05"))
        assertEquals(150000L, Dinero.parsearACentavos("1500"))
    }

    @Test
    fun `el punto es miles cuando el texto tiene forma de miles`() {
        assertEquals(150000L, Dinero.parsearACentavos("1.500"))
        assertEquals(1234567800L, Dinero.parsearACentavos("12.345.678"))
    }

    @Test
    fun `el punto es decimal cuando no tiene forma de miles`() {
        assertEquals(150L, Dinero.parsearACentavos("1.5"))
        assertEquals(123L, Dinero.parsearACentavos("1.23"))
        // La parte entera 0 nunca es miles: 0,999 redondea a 1,00.
        assertEquals(100L, Dinero.parsearACentavos("0.999"))
    }

    @Test
    fun `ignora espacios y redondea mas de dos decimales`() {
        assertEquals(0L, Dinero.parsearACentavos("0"))
        assertEquals(150050L, Dinero.parsearACentavos("1 500,50"))
        assertEquals(123457L, Dinero.parsearACentavos("1.234,567"))
    }

    @Test
    fun `rechaza texto vacio o invalido o negativo`() {
        assertNull(Dinero.parsearACentavos(""))
        assertNull(Dinero.parsearACentavos("abc"))
        assertNull(Dinero.parsearACentavos("-10"))
        assertNull(Dinero.parsearACentavos("1,5,0"))
        assertNull(Dinero.parsearACentavos("1.23.45"))
        assertNull(Dinero.parsearACentavos("1e5"))
        assertNull(Dinero.parsearACentavos("Infinity"))
        assertNull(Dinero.parsearACentavos("."))
    }

    @Test
    fun `la entrada editable vuelve al mismo monto`() {
        assertEquals("1500.50", Dinero.centavosAEntrada(150050L))
        assertEquals(150050L, Dinero.parsearACentavos(Dinero.centavosAEntrada(150050L)))
    }

    @Test
    fun `formatea centavos con separador de miles y dos decimales`() {
        assertEquals("$ 1.500,50", Dinero.formatear(150050L))
        assertEquals("$ 0,05", Dinero.formatear(5L))
        assertEquals("-$ 1.500,50", Dinero.formatear(-150050L))
    }
}
