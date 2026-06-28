package com.tallerapp.domain.validation

import com.tallerapp.domain.model.ServicioRealizado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica V-1 (obligatorios) y V-4 (normalización de patente). */
class TrabajoValidatorTest {

    @Test
    fun `trabajo completo es valido`() {
        val errores = TrabajoValidator.validar("Juan", "Ford", "Focus", ServicioRealizado.FRENOS, 100_000)
        assertTrue(errores.esValido)
    }

    @Test
    fun `faltan obligatorios da errores por campo`() {
        val errores = TrabajoValidator.validar("", "", "", null, null)
        assertFalse(errores.esValido)
        assertTrue(errores.cliente != null)
        assertTrue(errores.marca != null)
        assertTrue(errores.modelo != null)
        assertTrue(errores.servicio != null)
        assertTrue(errores.precio != null)
    }

    @Test
    fun `precio negativo es invalido`() {
        val errores = TrabajoValidator.validar("Juan", "Ford", "Focus", ServicioRealizado.OTROS, -1)
        assertTrue(errores.precio != null)
    }

    @Test
    fun `normaliza patente a mayusculas sin espacios`() {
        assertEquals("AB123CD", TrabajoValidator.normalizarPatente(" ab 123 cd "))
    }

    @Test
    fun `patente vacia queda en null`() {
        assertNull(TrabajoValidator.normalizarPatente("   "))
        assertNull(TrabajoValidator.normalizarPatente(null))
    }
}
