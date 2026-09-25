package com.tallerapp.data.repository

import com.tallerapp.data.local.MetaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Meta
import com.tallerapp.domain.repository.MetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MetaRepositoryImpl(private val dao: MetaDao) : MetaRepository {
    override fun observar(): Flow<List<Meta>> = dao.observar().map { list -> list.map { it.toDomain() } }
    override suspend fun crear(meta: Meta): Long = dao.insertar(meta.toEntity())
    override suspend fun actualizar(meta: Meta) = dao.actualizar(meta.toEntity())
    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
