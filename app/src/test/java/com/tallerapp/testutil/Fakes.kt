package com.tallerapp.testutil

import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.DeudaRepository
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Fakes en memoria para probar los casos de uso sin Room ni Android. */

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

class FakeEgresoRepository : EgresoRepository {
    val items = linkedMapOf<Long, Egreso>()
    private var nextId = 1L

    override fun observarRango(inicio: Long, fin: Long): Flow<List<Egreso>> =
        flowOf(items.values.filter { it.fecha in inicio until fin })
    override fun sumaRango(inicio: Long, fin: Long): Flow<Long> =
        flowOf(items.values.filter { it.fecha in inicio until fin }.sumOf { it.montoCentavos })
    override suspend fun obtener(id: Long): Egreso? = items[id]
    override suspend fun crear(egreso: Egreso): Long {
        val id = nextId++
        items[id] = egreso.copy(id = id)
        return id
    }
    override suspend fun actualizar(egreso: Egreso) { items[egreso.id] = egreso }
    override suspend fun eliminar(id: Long) { items.remove(id) }
}

class FakeDeudaRepository : DeudaRepository {
    val items = linkedMapOf<Long, Deuda>()
    private var nextId = 1L

    override fun observarTodas(): Flow<List<Deuda>> = flowOf(items.values.toList())
    override fun sumaPendiente(): Flow<Long> =
        flowOf(items.values.filter { !it.cobrada }.sumOf { it.montoCentavos })
    override fun contarPendientes(): Flow<Int> =
        flowOf(items.values.count { !it.cobrada })
    override suspend fun obtener(id: Long): Deuda? = items[id]
    override suspend fun crear(deuda: Deuda): Long {
        val id = nextId++
        items[id] = deuda.copy(id = id)
        return id
    }
    override suspend fun actualizar(deuda: Deuda) { items[deuda.id] = deuda }
    override suspend fun eliminar(id: Long) { items.remove(id) }
}
