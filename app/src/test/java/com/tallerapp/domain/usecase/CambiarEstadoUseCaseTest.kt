package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.testutil.FakeTrabajoRepository
import com.tallerapp.testutil.trabajoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CambiarEstadoUseCaseTest {

    private val repo = FakeTrabajoRepository()
    private val cambiar = CambiarEstadoUseCase(repo)

    @Test
    fun `transicion valida actualiza el estado`() = runTest {
        val id = repo.crear(trabajoDe(EstadoReparacion.PENDIENTE))
        val ok = cambiar(id, EstadoReparacion.EN_REPARACION)
        assertTrue(ok)
        assertEquals(EstadoReparacion.EN_REPARACION, repo.obtener(id)!!.estadoReparacion)
    }

    @Test
    fun `transicion invalida se rechaza y no cambia nada`() = runTest {
        val id = repo.crear(trabajoDe(EstadoReparacion.PENDIENTE))
        val ok = cambiar(id, EstadoReparacion.ENTREGADO)
        assertFalse(ok)
        assertEquals(EstadoReparacion.PENDIENTE, repo.obtener(id)!!.estadoReparacion)
    }

    @Test
    fun `marcar entregado registra la fecha de entrega`() = runTest {
        val id = repo.crear(trabajoDe(EstadoReparacion.TERMINADO))
        assertNull(repo.obtener(id)!!.fechaEntrega)
        cambiar(id, EstadoReparacion.ENTREGADO)
        assertNotNull(repo.obtener(id)!!.fechaEntrega)
    }
}
