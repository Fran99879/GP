package com.tallerapp.data.repository

import com.tallerapp.data.local.IngresoDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngresoRepositoryImpl(private val dao: IngresoDao) : IngresoRepository {

    override fun observarRango(inicio: Long, fin: Long): Flow<List<Ingreso>> =
        dao.observarRango(inicio, fin).map { lista -> lista.map { it.toDomain() } }

    override fun sumaRango(inicio: Long, fin: Long): Flow<Long> = dao.sumaRango(inicio, fin)

    override suspend fun obtener(id: Long): Ingreso? = dao.obtener(id)?.toDomain()

    override suspend fun crear(ingreso: Ingreso): Long = dao.insertar(ingreso.toEntity())

    override suspend fun actualizar(ingreso: Ingreso) = dao.actualizar(ingreso.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
