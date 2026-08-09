package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Deuda
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de Deudas a favor ("quién te debe"). */
interface DeudaRepository {
    fun observarTodas(): Flow<List<Deuda>>
    fun sumaPendiente(): Flow<Long>
    fun contarPendientes(): Flow<Int>
    suspend fun obtener(id: Long): Deuda?
    suspend fun crear(deuda: Deuda): Long
    suspend fun actualizar(deuda: Deuda)
    suspend fun eliminar(id: Long)
}
