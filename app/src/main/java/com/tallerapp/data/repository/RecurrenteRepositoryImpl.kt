package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.RecurrenteDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Recurrente
import com.tallerapp.domain.repository.RecurrenteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class RecurrenteRepositoryImpl(private val dao: RecurrenteDao) : RecurrenteRepository {
    override fun observar(): Flow<List<Recurrente>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observar(ng) }.map { l -> l.map { it.toDomain() } }
    override suspend fun activos(): List<Recurrente> = dao.activos(NegocioActual.value).map { it.toDomain() }
    override suspend fun crear(recurrente: Recurrente): Long = dao.insertar(recurrente.toEntity())
    override suspend fun actualizar(recurrente: Recurrente) = dao.actualizar(recurrente.toEntity())
    override suspend fun marcarGenerado(id: Long, ym: String) = dao.marcarGenerado(id, ym)
    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
