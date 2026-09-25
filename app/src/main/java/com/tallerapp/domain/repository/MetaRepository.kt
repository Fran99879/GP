package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Meta
import kotlinx.coroutines.flow.Flow

interface MetaRepository {
    fun observar(): Flow<List<Meta>>
    suspend fun crear(meta: Meta): Long
    suspend fun actualizar(meta: Meta)
    suspend fun eliminar(id: Long)
}
