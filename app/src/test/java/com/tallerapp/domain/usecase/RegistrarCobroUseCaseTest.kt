package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.testutil.FakeCobroRepository
import com.tallerapp.testutil.FakeTrabajoRepository
import com.tallerapp.testutil.trabajoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrarCobroUseCaseTest {

    private val trabajos = FakeTrabajoRepository()
    private val cobros = FakeCobroRepository(trabajos)
    private val registrar = RegistrarCobroUseCase(trabajos, cobros)

    @Test
    fun `cobra un trabajo entregado y genera el ingreso`() = runTest {
        val id = trabajos.crear(trabajoDe(EstadoReparacion.ENTREGADO, precioCentavos = 80_000))

        val resultado = registrar(id, MetodoPago.EFECTIVO, null)

        assertTrue(resultado is CobroResultado.Exito)
        assertEquals(EstadoCobro.COBRADO, trabajos.obtener(id)!!.estadoCobro)
        assertEquals(1, cobros.ingresos.size)
        val ingreso = cobros.ingresos.first()
        assertEquals(80_000, ingreso.montoCentavos)
        assertEquals(OrigenIngreso.COBRO_DE_TRABAJO, ingreso.origen)
        assertEquals(id, ingreso.trabajoId)
    }

    @Test
    fun `no cobra si el trabajo no esta entregado`() = runTest {
        val id = trabajos.crear(trabajoDe(EstadoReparacion.TERMINADO))
        val resultado = registrar(id, MetodoPago.EFECTIVO, null)
        assertTrue(resultado is CobroResultado.Invalido)
        assertEquals(EstadoCobro.PENDIENTE_DE_COBRO, trabajos.obtener(id)!!.estadoCobro)
        assertTrue(cobros.ingresos.isEmpty())
    }

    @Test
    fun `no cobra dos veces el mismo trabajo`() = runTest {
        val id = trabajos.crear(trabajoDe(EstadoReparacion.ENTREGADO))
        registrar(id, MetodoPago.EFECTIVO, null)
        val segundo = registrar(id, MetodoPago.EFECTIVO, null)
        assertTrue(segundo is CobroResultado.Invalido)
        assertEquals(1, cobros.ingresos.size)
    }
}
