package com.tallerapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los mismos casos corren en la app de escritorio (`PreciosTests` de `PuraTests.cs`): el total de una
 * factura se recalcula en el dominio y se guarda, así que el entero tiene que coincidir en las dos.
 */
class PreciosTest {

    @Test
    fun `descuento redondea el medio hacia arriba`() {
        assertEquals(0L, Precios.descuento(10000L, 0.0))
        assertEquals(1000L, Precios.descuento(10000L, 10.0))
        assertEquals(10000L, Precios.descuento(10000L, 150.0)) // el porcentaje se recorta a 100
        assertEquals(0L, Precios.descuento(0L, 50.0))
        assertEquals(0L, Precios.descuento(-500L, 50.0))       // importe no positivo: no hay descuento
        assertEquals(33L, Precios.descuento(333L, 10.0))       // 33,3 -> 33
        assertEquals(26L, Precios.descuento(255L, 10.0))       // 25,5 -> 26
    }

    @Test
    fun `con descuento resta el descuento`() {
        assertEquals(9000L, Precios.conDescuento(10000L, 10.0))
        assertEquals(10000L, Precios.conDescuento(10000L, 0.0))
        assertEquals(0L, Precios.conDescuento(10000L, 100.0))
    }

    @Test
    fun `subtotal redondea el bruto y despues descuenta`() {
        assertEquals(3000L, Precios.subtotal(2.0, 1500L, 0.0))
        assertEquals(2700L, Precios.subtotal(2.0, 1500L, 10.0))
        assertEquals(500L, Precios.subtotal(0.5, 999L, 0.0))   // 499,5 -> 500: primero se redondea el bruto
        assertEquals(400L, Precios.subtotal(1.5, 333L, 20.0))  // bruto 499,5 -> 500; menos 20% = 100
        assertEquals(0L, Precios.subtotal(0.0, 1500L, 0.0))
    }

    @Test
    fun `el producto calcula precio final y aviso de stock bajo`() {
        val p = Producto(nombre = "Yerba", precioCentavos = 250000L, descuentoPct = 10.0, stock = 12.0, stockMinimo = 3.0)
        assertEquals(25000L, p.descuentoCentavos)
        assertEquals(225000L, p.precioFinalCentavos)
        assertTrue(p.tieneDescuento)
        assertFalse(p.stockBajo)

        // Sin stock mínimo no hay aviso: 0 significa "no me controles el stock".
        assertFalse(Producto(nombre = "A", stock = 0.0).stockBajo)
        assertTrue(Producto(nombre = "B", stock = 3.0, stockMinimo = 3.0).stockBajo)
    }
}
