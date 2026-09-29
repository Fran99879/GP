package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    /**
     * Contactos del negocio que sirven para [tipo]. "ambos" entra en las dos listas: por eso
     * el filtro compara contra el tipo pedido **y** contra "ambos", en vez de por igualdad.
     */
    @Query(
        "SELECT * FROM contacto WHERE negocioId = :ng AND (tipo = :tipo OR tipo = 'ambos') " +
            "ORDER BY nombre COLLATE NOCASE",
    )
    fun observarPorTipo(ng: Long, tipo: String): Flow<List<ContactoEntity>>

    @Query(
        "SELECT * FROM contacto WHERE negocioId = :ng AND (tipo = :tipo OR tipo = 'ambos') " +
            "AND (nombre LIKE '%' || :texto || '%' OR documento LIKE '%' || :texto || '%' " +
            "OR telefono LIKE '%' || :texto || '%') ORDER BY nombre COLLATE NOCASE",
    )
    fun buscarPorTipo(ng: Long, tipo: String, texto: String): Flow<List<ContactoEntity>>

    /** Solo los nombres, para el autocompletado de deudas que ya existía. */
    @Query("SELECT nombre FROM contacto WHERE negocioId = :ng ORDER BY nombre COLLATE NOCASE")
    fun observarNombres(ng: Long): Flow<List<String>>

    @Query("SELECT * FROM contacto WHERE id = :id")
    suspend fun obtener(id: Long): ContactoEntity?

    @Query("SELECT * FROM contacto WHERE negocioId = :ng AND nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun porNombre(ng: Long, nombre: String): ContactoEntity?

    /**
     * IGNORE porque el alta rápida (escribir un nombre al facturar o al anotar una deuda)
     * no debe fallar si el contacto ya estaba: el índice único por negocio lo resuelve.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(entity: ContactoEntity): Long

    @Update
    suspend fun actualizar(entity: ContactoEntity)

    @Query("DELETE FROM contacto WHERE id = :id")
    suspend fun eliminar(id: Long)

    /**
     * Al borrar un contacto, los documentos que lo referencian quedan con el nombre que ya
     * tenían guardado: se limpia solo el vínculo, nunca el historial.
     */
    @Query("UPDATE factura SET clienteId = NULL WHERE clienteId = :id")
    suspend fun desvincularFacturas(id: Long)

    @Query("UPDATE egreso SET proveedorId = NULL WHERE proveedorId = :id")
    suspend fun desvincularEgresos(id: Long)

    @Query("UPDATE deuda SET contactoId = NULL WHERE contactoId = :id")
    suspend fun desvincularDeudas(id: Long)

    @androidx.room.Transaction
    suspend fun eliminarConVinculos(id: Long) {
        desvincularFacturas(id)
        desvincularEgresos(id)
        desvincularDeudas(id)
        eliminar(id)
    }
}
