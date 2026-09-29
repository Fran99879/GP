package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM producto WHERE negocioId = :ng ORDER BY nombre COLLATE NOCASE")
    fun observar(ng: Long): Flow<List<ProductoEntity>>

    /** Busca por nombre o por código de barras; el código escaneado entra por acá también. */
    @Query(
        "SELECT * FROM producto WHERE negocioId = :ng " +
            "AND (nombre LIKE '%' || :texto || '%' OR codigoBarras LIKE '%' || :texto || '%') " +
            "ORDER BY nombre COLLATE NOCASE",
    )
    fun buscar(ng: Long, texto: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM producto WHERE id = :id")
    suspend fun obtener(id: Long): ProductoEntity?

    /** Coincidencia exacta de código, para resolver un escaneo sin ambigüedad. */
    @Query("SELECT * FROM producto WHERE negocioId = :ng AND codigoBarras = :codigo LIMIT 1")
    suspend fun porCodigo(ng: Long, codigo: String): ProductoEntity?

    @Query("SELECT COUNT(*) FROM producto WHERE negocioId = :ng")
    suspend fun contar(ng: Long): Int

    @Insert
    suspend fun insertar(entity: ProductoEntity): Long

    @Update
    suspend fun actualizar(entity: ProductoEntity)

    /**
     * Descuenta unidades vendidas. No baja de 0: un stock negativo no significa nada y
     * el usuario puede haber cargado el catálogo sin inventariar.
     */
    @Query("UPDATE producto SET stock = MAX(0, stock - :cantidad) WHERE id = :id")
    suspend fun descontarStock(id: Long, cantidad: Double)

    @Query("DELETE FROM producto WHERE id = :id")
    suspend fun eliminar(id: Long)
}
