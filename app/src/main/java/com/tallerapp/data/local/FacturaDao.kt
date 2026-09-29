package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FacturaDao {

    @Query("SELECT * FROM factura WHERE negocioId = :ng ORDER BY fecha DESC, id DESC")
    fun observar(ng: Long): Flow<List<FacturaEntity>>

    @Query("SELECT * FROM factura WHERE id = :id")
    suspend fun obtener(id: Long): FacturaEntity?

    @Query("SELECT * FROM factura_item WHERE facturaId = :facturaId ORDER BY id")
    suspend fun items(facturaId: Long): List<FacturaItemEntity>

    @Query("SELECT * FROM factura_item WHERE facturaId = :facturaId ORDER BY id")
    fun observarItems(facturaId: Long): Flow<List<FacturaItemEntity>>

    /**
     * Cuántas facturas lleva el negocio. Sirve para proponer el próximo número sin
     * depender del id de la base (que es global a la app, no por negocio).
     */
    @Query("SELECT COUNT(*) FROM factura WHERE negocioId = :ng")
    suspend fun contar(ng: Long): Int

    @Insert
    suspend fun insertarFactura(entity: FacturaEntity): Long

    @Insert
    suspend fun insertarItems(items: List<FacturaItemEntity>)

    /** Cabecera e ítems en una sola transacción: una factura sin líneas no existe. */
    @Transaction
    suspend fun emitir(factura: FacturaEntity, items: List<FacturaItemEntity>): Long {
        val id = insertarFactura(factura)
        insertarItems(items.map { it.copy(facturaId = id) })
        return id
    }

    @Query("UPDATE factura SET ingresoId = :ingresoId WHERE id = :id")
    suspend fun asociarIngreso(id: Long, ingresoId: Long?)

    @Query("DELETE FROM factura_item WHERE facturaId = :facturaId")
    suspend fun borrarItems(facturaId: Long)

    @Query("DELETE FROM factura WHERE id = :id")
    suspend fun borrarFactura(id: Long)

    /**
     * Borra la factura con sus líneas. **No** revierte el stock ni borra el ingreso:
     * la plata entró de verdad y la mercadería salió; anular eso a mano es decisión
     * del usuario en Movimientos.
     */
    @Transaction
    suspend fun eliminar(id: Long) {
        borrarItems(id)
        borrarFactura(id)
    }
}
