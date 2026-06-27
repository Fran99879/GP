package com.tallerapp.data.repository

import com.tallerapp.data.local.EgresoDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.repository.EgresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EgresoRepositoryImpl(private val dao: EgresoDao) : EgresoRepository {

    override fun observarRango(inicio: Long, fin: Long): Flow<List<Egreso>> =
        dao.observarRango(inicio, fin).map { lista -> lista.map { it.toDomain() } }

    override fun sumaRango(inicio: Long, fin: Long): Flow<Long> = dao.sumaRango(inicio, fin)

    override suspend fun obtener(id: Long): Egreso? = dao.obtener(id)?.toDomain()

    override suspend fun crear(egreso: Egreso): Long = dao.insertar(egreso.toEntity())

    override suspend fun actualizar(egreso: Egreso) = dao.actualizar(egreso.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
