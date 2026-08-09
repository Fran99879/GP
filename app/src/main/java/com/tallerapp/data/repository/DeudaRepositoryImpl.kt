package com.tallerapp.data.repository

import com.tallerapp.data.local.DeudaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.repository.DeudaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeudaRepositoryImpl(private val dao: DeudaDao) : DeudaRepository {

    override fun observarTodas(): Flow<List<Deuda>> =
        dao.observarTodas().map { lista -> lista.map { it.toDomain() } }

    override fun sumaPendiente(): Flow<Long> = dao.sumaPendiente()

    override fun contarPendientes(): Flow<Int> = dao.contarPendientes()

    override suspend fun obtener(id: Long): Deuda? = dao.obtener(id)?.toDomain()

    override suspend fun crear(deuda: Deuda): Long = dao.insertar(deuda.toEntity())

    override suspend fun actualizar(deuda: Deuda) = dao.actualizar(deuda.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
