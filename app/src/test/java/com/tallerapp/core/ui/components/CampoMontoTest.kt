package com.tallerapp.core.ui.components

import com.tallerapp.core.util.Dinero
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * El campo de importe tiene que dar el mismo monto se escriba o se pegue. Ver la regla de los
 * puntos de miles en [Dinero.parsearACentavos].
 */
class CampoMontoTest {

    /** Lo que termina guardado: el campo filtra y el dominio parsea. */
    private fun centavos(texto: String, anterior: String = ""): Long? =
        Dinero.parsearACentavos(filtrarMonto(texto, anterior))

    @Test
    fun `al tipear los puntos siguen siendo el decimal`() {
        assertEquals("1", filtrarMonto("1", ""))
        assertEquals("1.", filtrarMonto("1.", "1"))
        assertEquals("1.5", filtrarMonto("1.5", "1."))
        assertEquals("1.50", filtrarMonto("1.50", "1.5"))
        // Un dígito de más después de los dos decimales se descarta, no salta a 1.500.
        assertEquals("1.50", filtrarMonto("1.500", "1.50"))
        assertEquals(150L, centavos("1.500", "1.50"))
    }

    @Test
    fun `al tipear los miles los pone el formato visual`() {
        assertEquals("25000", filtrarMonto("25000", "2500"))
        assertEquals(2500000L, centavos("25000", "2500"))
    }

    @Test
    fun `al pegar el punto de miles vale como miles`() {
        assertEquals("1500", filtrarMonto("1.500"))
        assertEquals(150000L, centavos("1.500"))

        assertEquals("1200000", filtrarMonto("1.200.000"))
        assertEquals(120000000L, centavos("1.200.000"))
    }

    @Test
    fun `al pegar con coma decimal se respetan los centavos`() {
        assertEquals("1500.50", filtrarMonto("1.500,50"))
        assertEquals(150050L, centavos("1.500,50"))
    }

    @Test
    fun `al pegar un decimal con punto sigue siendo decimal`() {
        assertEquals("1500.50", filtrarMonto("1500.50"))
        assertEquals(150050L, centavos("1500.50"))
    }

    @Test
    fun `al pegar se descarta el simbolo y los espacios`() {
        assertEquals("25000", filtrarMonto("$ 25.000"))
        assertEquals(2500000L, centavos("$ 25.000"))
    }

    @Test
    fun `al pegar texto sin numeros queda vacio`() {
        assertEquals("", filtrarMonto("abc"))
        assertEquals(null, centavos("abc"))
    }

    @Test
    fun `el monto guardado vuelve al campo sin cambiar`() {
        val texto = Dinero.centavosAEntrada(150050L)
        assertEquals(150050L, centavos(texto))
    }
}
