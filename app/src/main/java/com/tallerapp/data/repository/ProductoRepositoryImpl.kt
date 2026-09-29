package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.ProductoDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Producto
import com.tallerapp.domain.repository.ProductoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoRepositoryImpl(private val dao: ProductoDao) : ProductoRepository {

    // flatMapLatest sobre NegocioActual: al cambiar de negocio la lista se rearma sola.
    override fun observar(): Flow<List<Producto>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observar(ng) }
            .map { lista -> lista.map { it.toDomain() } }

    override fun buscar(texto: String): Flow<List<Producto>> =
        NegocioActual.id.flatMapLatest { ng ->
            if (texto.isBlank()) dao.observar(ng) else dao.buscar(ng, texto.trim())
        }.map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtener(id: Long): Producto? = dao.obtener(id)?.toDomain()

    override suspend fun porCodigo(codigo: String): Producto? =
        dao.porCodigo(NegocioActual.value, codigo.trim())?.toDomain()

    override suspend fun crear(producto: Producto): Long = dao.insertar(producto.toEntity())

    override suspend fun actualizar(producto: Producto) = dao.actualizar(producto.toEntity())

    override suspend fun descontarStock(id: Long, cantidad: Double) =
        dao.descontarStock(id, cantidad)

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
