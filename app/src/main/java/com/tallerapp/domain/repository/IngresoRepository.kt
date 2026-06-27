package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Ingreso
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de Ingresos (Frozen Spec 8.1). */
interface IngresoRepository {
    /** Ingresos cuya fecha contable cae en [inicio, fin). */
    fun observarRango(inicio: Long, fin: Long): Flow<List<Ingreso>>

    /** Suma de montos (centavos) en [inicio, fin); 0 si no hay. */
    fun sumaRango(inicio: Long, fin: Long): Flow<Long>

    suspend fun obtener(id: Long): Ingreso?
    suspend fun crear(ingreso: Ingreso): Long
    suspend fun actualizar(ingreso: Ingreso)
    suspend fun eliminar(id: Long)
}
