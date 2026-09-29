package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.FacturaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Factura
import com.tallerapp.domain.repository.FacturaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class FacturaRepositoryImpl(private val dao: FacturaDao) : FacturaRepository {

    /** El listado no trae los ítems: son varias consultas y la lista solo muestra totales. */
    override fun observar(): Flow<List<Factura>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observar(ng) }
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Long): Factura? {
        val cabecera = dao.obtener(id) ?: return null
        return cabecera.toDomain(dao.items(id).map { it.toDomain() })
    }

    override suspend fun contar(): Int = dao.contar(NegocioActual.value)

    override suspend fun emitir(factura: Factura): Long =
        dao.emitir(factura.toEntity(), factura.items.map { it.toEntity() })

    override suspend fun asociarIngreso(id: Long, ingresoId: Long?) =
        dao.asociarIngreso(id, ingresoId)

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
