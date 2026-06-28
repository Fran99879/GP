package com.tallerapp.testutil

import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.repository.CobroRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Fakes en memoria para probar los casos de uso sin Room ni Android. */

class FakeTrabajoRepository : TrabajoRepository {
    val items = linkedMapOf<Long, Trabajo>()
    private var nextId = 1L

    override fun observarTodos(): Flow<List<Trabajo>> = flowOf(items.values.toList())
    override fun observarBusqueda(query: String): Flow<List<Trabajo>> = flowOf(items.values.toList())
    override suspend fun obtener(id: Long): Trabajo? = items[id]
    override suspend fun crear(trabajo: Trabajo): Long {
        val id = nextId++
        items[id] = trabajo.copy(id = id)
        return id
    }
    override suspend fun actualizar(trabajo: Trabajo) { items[trabajo.id] = trabajo }
    override suspend fun eliminar(id: Long) { items.remove(id) }
}

class FakeIngresoRepository : IngresoRepository {
    val items = linkedMapOf<Long, Ingreso>()
    private var nextId = 1L

    override fun observarRango(inicio: Long, fin: Long): Flow<List<Ingreso>> =
        flowOf(items.values.filter { it.fecha in inicio until fin })
    override fun sumaRango(inicio: Long, fin: Long): Flow<Long> =
        flowOf(items.values.filter { it.fecha in inicio until fin }.sumOf { it.montoCentavos })
    override suspend fun obtener(id: Long): Ingreso? = items[id]
    override suspend fun crear(ingreso: Ingreso): Long {
        val id = nextId++
        items[id] = ingreso.copy(id = id)
        return id
    }
    override suspend fun actualizar(ingreso: Ingreso) { items[ingreso.id] = ingreso }
    override suspend fun eliminar(id: Long) { items.remove(id) }
}

/** Cobro fake que imita la transacción real: registra el ingreso y marca el trabajo. */
class FakeCobroRepository(
    private val trabajos: FakeTrabajoRepository,
) : CobroRepository {
    val ingresos = mutableListOf<Ingreso>()
    private var nextId = 1L

    override suspend fun registrarCobro(ingreso: Ingreso, trabajoId: Long): Long {
        val id = nextId++
        ingresos.add(ingreso.copy(id = id))
        trabajos.obtener(trabajoId)?.let {
            trabajos.actualizar(it.copy(estadoCobro = EstadoCobro.COBRADO, cobroId = id))
        }
        return id
    }

    override suspend fun anularCobro(trabajoId: Long): Boolean {
        val trabajo = trabajos.obtener(trabajoId) ?: return false
        val cobroId = trabajo.cobroId ?: return false
        ingresos.removeAll { it.id == cobroId }
        trabajos.actualizar(trabajo.copy(estadoCobro = EstadoCobro.PENDIENTE_DE_COBRO, cobroId = null))
        return true
    }
}

/** Construye un trabajo de prueba con valores por defecto razonables. */
fun trabajoDe(
    estado: EstadoReparacion,
    cobro: EstadoCobro = EstadoCobro.PENDIENTE_DE_COBRO,
    precioCentavos: Long = 100_000,
    fechaEntrega: Long? = null,
    cobroId: Long? = null,
) = Trabajo(
    id = 0,
    cliente = "Juan",
    telefono = null,
    patente = null,
    marca = "Ford",
    modelo = "Focus",
    servicio = ServicioRealizado.FRENOS,
    fechaIngreso = 0L,
    estadoReparacion = estado,
    estadoCobro = cobro,
    problema = null,
    diagnostico = null,
    precioCentavos = precioCentavos,
    fechaEntrega = fechaEntrega,
    cobroId = cobroId,
)
