package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Trabajo
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistencia de Trabajos (Arquitectura §1, §4.1).
 * El dominio depende de esta interfaz; la implementación vive en la capa de datos.
 * Este contrato será consumido en solo lectura por Cobros (Fase 4) y Dashboard (Fase 5).
 */
interface TrabajoRepository {
    fun observarTodos(): Flow<List<Trabajo>>
    fun observarBusqueda(query: String): Flow<List<Trabajo>>
    suspend fun obtener(id: Long): Trabajo?
    suspend fun crear(trabajo: Trabajo): Long
    suspend fun actualizar(trabajo: Trabajo)
    suspend fun eliminar(id: Long)
}
