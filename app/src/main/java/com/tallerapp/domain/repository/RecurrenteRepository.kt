package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Recurrente
import kotlinx.coroutines.flow.Flow

interface RecurrenteRepository {
    fun observar(): Flow<List<Recurrente>>
    suspend fun activos(): List<Recurrente>
    suspend fun crear(recurrente: Recurrente): Long
    suspend fun actualizar(recurrente: Recurrente)
    suspend fun marcarGenerado(id: Long, ym: String)
    suspend fun eliminar(id: Long)
}
