package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.model.RepartoPago
import com.tallerapp.testutil.FakeCobroRepository
import com.tallerapp.testutil.FakeIngresoRepository
import com.tallerapp.testutil.FakeTrabajoRepository
import com.tallerapp.testutil.trabajoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnularCobroUseCaseTest {

    private val trabajos = FakeTrabajoRepository()
    private val ingresos = FakeIngresoRepository()
    private val cobros = FakeCobroRepository(trabajos)
    private val anular = AnularCobroUseCase(trabajos, ingresos, cobros)

    @Test
    fun `anula un cobro de hoy y revierte el trabajo`() = runTest {
        // Trabajo cobrado hoy: ingreso con fechaRegistro = ahora.
        val ingresoId = ingresos.crear(
            Ingreso(
                id = 0,
                montoCentavos = 100_000,
                concepto = "Cobro",
                metodo = MetodoPago.EFECTIVO,
                reparto = null as RepartoPago?,
                fecha = 0L,
                fechaRegistro = System.currentTimeMillis(),
                origen = OrigenIngreso.COBRO_DE_TRABAJO,
                trabajoId = 1L,
            ),
        )
        val id = trabajos.crear(
            trabajoDe(EstadoReparacion.ENTREGADO, cobro = EstadoCobro.COBRADO, cobroId = ingresoId),
        )

        val ok = anular(id)

        assertTrue(ok)
        assertEquals(EstadoCobro.PENDIENTE_DE_COBRO, trabajos.obtener(id)!!.estadoCobro)
    }

    @Test
    fun `no anula un cobro de otro dia`() = runTest {
        val ayer = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        val ingresoId = ingresos.crear(
            Ingreso(
                id = 0,
                montoCentavos = 100_000,
                concepto = "Cobro",
                metodo = MetodoPago.EFECTIVO,
                reparto = null,
                fecha = 0L,
                fechaRegistro = ayer,
                origen = OrigenIngreso.COBRO_DE_TRABAJO,
                trabajoId = 1L,
            ),
        )
        val id = trabajos.crear(
            trabajoDe(EstadoReparacion.ENTREGADO, cobro = EstadoCobro.COBRADO, cobroId = ingresoId),
        )

        val ok = anular(id)

        assertFalse(ok)
        assertEquals(EstadoCobro.COBRADO, trabajos.obtener(id)!!.estadoCobro)
    }
}
