package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Factura
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de facturas. Filtra por el negocio activo. */
interface FacturaRepository {
    fun observar(): Flow<List<Factura>>

    /** Factura con sus ítems, o null si no existe. */
    suspend fun obtener(id: Long): Factura?

    /** Cuántas facturas emitió el negocio activo (para proponer el próximo número). */
    suspend fun contar(): Int

    /** Guarda cabecera e ítems en una transacción y devuelve el id. */
    suspend fun emitir(factura: Factura): Long

    suspend fun asociarIngreso(id: Long, ingresoId: Long?)
    suspend fun eliminar(id: Long)
}
