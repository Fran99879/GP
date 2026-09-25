package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.DeudaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.repository.DeudaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class DeudaRepositoryImpl(private val dao: DeudaDao) : DeudaRepository {

    override fun observarTodas(): Flow<List<Deuda>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observarTodas(ng) }
            .map { lista -> lista.map { it.toDomain() } }

    override fun sumaPendiente(): Flow<Long> =
        NegocioActual.id.flatMapLatest { ng -> dao.sumaPendiente(ng) }

    override fun contarPendientes(): Flow<Int> =
        NegocioActual.id.flatMapLatest { ng -> dao.contarPendientes(ng) }

    override suspend fun obtener(id: Long): Deuda? = dao.obtener(id)?.toDomain()

    override suspend fun crear(deuda: Deuda): Long = dao.insertar(deuda.toEntity())

    override suspend fun actualizar(deuda: Deuda) = dao.actualizar(deuda.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
