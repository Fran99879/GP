package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Producto
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia del catálogo de productos. Filtra por el negocio activo. */
interface ProductoRepository {
    fun observar(): Flow<List<Producto>>

    /** Nombre o código de barras que contenga [texto]. Con texto vacío devuelve todo. */
    fun buscar(texto: String): Flow<List<Producto>>

    suspend fun obtener(id: Long): Producto?

    /** Producto con ese código exacto en el negocio activo, o null. */
    suspend fun porCodigo(codigo: String): Producto?

    suspend fun crear(producto: Producto): Long
    suspend fun actualizar(producto: Producto)
    suspend fun descontarStock(id: Long, cantidad: Double)
    suspend fun eliminar(id: Long)
}
