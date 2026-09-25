package com.tallerapp.domain.repository

import com.tallerapp.domain.model.AgendaItem
import kotlinx.coroutines.flow.Flow

interface AgendaRepository {
    fun observarRango(inicio: Long, fin: Long): Flow<List<AgendaItem>>
    suspend fun crear(item: AgendaItem): Long
    suspend fun actualizar(item: AgendaItem)
    suspend fun marcarHecho(id: Long, hecho: Boolean)
    suspend fun eliminar(id: Long)
}
