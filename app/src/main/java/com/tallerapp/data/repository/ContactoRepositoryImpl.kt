package com.tallerapp.data.repository

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.ContactoDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Contacto
import com.tallerapp.domain.repository.ContactoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ContactoRepositoryImpl(private val dao: ContactoDao) : ContactoRepository {

    override fun observar(tipo: String, busqueda: String): Flow<List<Contacto>> =
        NegocioActual.id.flatMapLatest { ng ->
            if (busqueda.isBlank()) dao.observarPorTipo(ng, tipo)
            else dao.buscarPorTipo(ng, tipo, busqueda.trim())
        }.map { lista -> lista.map { it.toDomain() } }

    override fun observarNombres(): Flow<List<String>> =
        NegocioActual.id.flatMapLatest { ng -> dao.observarNombres(ng) }

    override suspend fun obtener(id: Long): Contacto? = dao.obtener(id)?.toDomain()

    override suspend fun porNombre(nombre: String): Contacto? =
        dao.porNombre(NegocioActual.value, nombre.trim())?.toDomain()

    override suspend fun crear(contacto: Contacto): Long = dao.insertar(contacto.toEntity())

    override suspend fun actualizar(contacto: Contacto) = dao.actualizar(contacto.toEntity())

    override suspend fun eliminar(id: Long) = dao.eliminarConVinculos(id)
}
