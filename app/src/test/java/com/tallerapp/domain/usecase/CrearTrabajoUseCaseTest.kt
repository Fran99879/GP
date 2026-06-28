package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.testutil.FakeTrabajoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrearTrabajoUseCaseTest {

    private val repo = FakeTrabajoRepository()
    private val crear = CrearTrabajoUseCase(repo)

    @Test
    fun `crea trabajo en estado pendiente y pendiente de cobro`() = runTest {
        val resultado = crear(
            cliente = "Juan",
            telefono = null,
            patente = "ab123cd",
            marca = "Ford",
            modelo = "Focus",
            servicio = ServicioRealizado.FRENOS,
            problema = null,
            diagnostico = null,
            precioCentavos = 100_000,
        )

        assertTrue(resultado is GuardarResultado.Exito)
        val id = (resultado as GuardarResultado.Exito).id
        val trabajo = repo.obtener(id)!!
        assertEquals(EstadoReparacion.PENDIENTE, trabajo.estadoReparacion)
        assertEquals(EstadoCobro.PENDIENTE_DE_COBRO, trabajo.estadoCobro)
        assertEquals("AB123CD", trabajo.patente) // normalizada (V-4)
    }

    @Test
    fun `no crea trabajo si faltan obligatorios`() = runTest {
        val resultado = crear(
            cliente = "",
            telefono = null,
            patente = null,
            marca = "",
            modelo = "",
            servicio = null,
            problema = null,
            diagnostico = null,
            precioCentavos = null,
        )
        assertTrue(resultado is GuardarResultado.Invalido)
        assertTrue(repo.items.isEmpty())
    }
}
