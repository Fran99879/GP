package com.tallerapp.data.repository

import com.tallerapp.data.local.TrabajoDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Implementación del contrato de Trabajos sobre Room (Arquitectura §1.1, capa de datos). */
class TrabajoRepositoryImpl(private val dao: TrabajoDao) : TrabajoRepository {

    override fun observarTodos(): Flow<List<Trabajo>> =
        dao.observarTodos().map { lista -> lista.map { it.toDomain() } }

    override fun observarBusqueda(query: String): Flow<List<Trabajo>> =
        dao.buscar(query).map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Long): Trabajo? = dao.obtener(id)?.toDomain()

    override suspend fun crear(trabajo: Trabajo): Long = dao.insertar(trabajo.toEntity())

    override suspend fun actualizar(trabajo: Trabajo) = dao.actualizar(trabajo.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
