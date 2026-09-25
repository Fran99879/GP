package com.tallerapp.data.repository

import com.tallerapp.data.local.NegocioDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.domain.model.Negocio
import com.tallerapp.domain.repository.NegocioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NegocioRepositoryImpl(private val dao: NegocioDao) : NegocioRepository {

    override fun observar(): Flow<List<Negocio>> =
        dao.observar().map { lista -> lista.map { it.toDomain() } }

    override suspend fun crear(nombre: String): Long =
        dao.insertar(
            com.tallerapp.data.local.NegocioEntity(
                nombre = nombre,
                createdAt = System.currentTimeMillis(),
            ),
        )

    override suspend fun renombrar(id: Long, nombre: String) = dao.renombrar(id, nombre)

    override suspend fun asegurarInicial() {
        if (dao.count() == 0) {
            dao.insertar(
                com.tallerapp.data.local.NegocioEntity(
                    nombre = "Personal",
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}
