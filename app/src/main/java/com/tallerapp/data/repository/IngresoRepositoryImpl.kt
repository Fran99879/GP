package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.IngresoDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class IngresoRepositoryImpl(private val dao: IngresoDao) : IngresoRepository {

    override fun observarRango(inicio: Long, fin: Long): Flow<List<Ingreso>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observarRango(ng, inicio, fin) }
            .map { lista -> lista.map { it.toDomain() } }

    override fun sumaRango(inicio: Long, fin: Long): Flow<Long> =
        NegocioActual.id.flatMapLatest { ng -> dao.sumaRango(ng, inicio, fin) }

    override fun sumaRangoTodos(inicio: Long, fin: Long): Flow<Long> = dao.sumaRangoTodos(inicio, fin)

    override fun totalesPorNegocio(inicio: Long, fin: Long): Flow<Map<Long, Long>> =
        dao.totalesPorNegocio(inicio, fin).map { lista -> lista.associate { it.negocioId to it.total } }

    override suspend fun obtener(id: Long): Ingreso? = dao.obtener(id)?.toDomain()

    override suspend fun crear(ingreso: Ingreso): Long = dao.insertar(ingreso.toEntity())

    override suspend fun actualizar(ingreso: Ingreso) = dao.actualizar(ingreso.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
