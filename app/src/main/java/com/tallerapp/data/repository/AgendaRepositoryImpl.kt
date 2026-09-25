package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.AgendaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.AgendaItem
import com.tallerapp.domain.repository.AgendaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaRepositoryImpl(private val dao: AgendaDao) : AgendaRepository {

    override fun observarRango(inicio: Long, fin: Long): Flow<List<AgendaItem>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observarRango(ng, inicio, fin) }
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun crear(item: AgendaItem): Long = dao.insertar(item.toEntity())

    override suspend fun actualizar(item: AgendaItem) = dao.actualizar(item.toEntity())

    override suspend fun marcarHecho(id: Long, hecho: Boolean) = dao.marcarHecho(id, hecho)

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
