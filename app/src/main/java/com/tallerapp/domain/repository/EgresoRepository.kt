package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Egreso
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de Egresos (Frozen Spec 8.2). */
interface EgresoRepository {
    fun observarRango(inicio: Long, fin: Long): Flow<List<Egreso>>
    fun sumaRango(inicio: Long, fin: Long): Flow<Long>

    /** Egresos del rango de TODOS los negocios (vista combinada de Reportes). */
    fun observarRangoTodos(inicio: Long, fin: Long): Flow<List<Egreso>>

    /** Total del rango por cada negocio (comparativa entre negocios). */
    fun totalesPorNegocio(inicio: Long, fin: Long): Flow<Map<Long, Long>>
    suspend fun obtener(id: Long): Egreso?
    suspend fun crear(egreso: Egreso): Long
    suspend fun actualizar(egreso: Egreso)
    suspend fun eliminar(id: Long)
}
