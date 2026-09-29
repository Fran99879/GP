package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NegocioDao {

    @Query("SELECT * FROM negocio ORDER BY id")
    fun observar(): Flow<List<NegocioEntity>>

    @Query("SELECT * FROM negocio ORDER BY id")
    suspend fun listar(): List<NegocioEntity>

    @Query("SELECT COUNT(*) FROM negocio")
    suspend fun count(): Int

    @Insert
    suspend fun insertar(entity: NegocioEntity): Long

    @Query("UPDATE negocio SET nombre = :nombre WHERE id = :id")
    suspend fun renombrar(id: Long, nombre: String)

    // --- Borrado en cascada (igual que el escritorio: el negocio se lleva sus datos) ---

    @Query("DELETE FROM ingreso WHERE negocioId = :ng")
    suspend fun borrarIngresos(ng: Long)

    @Query("DELETE FROM egreso WHERE negocioId = :ng")
    suspend fun borrarEgresos(ng: Long)

    @Query("DELETE FROM deuda WHERE negocioId = :ng")
    suspend fun borrarDeudas(ng: Long)

    @Query("DELETE FROM recurrente WHERE negocioId = :ng")
    suspend fun borrarRecurrentes(ng: Long)

    @Query("DELETE FROM agenda WHERE negocioId = :ng")
    suspend fun borrarAgenda(ng: Long)

    @Query("DELETE FROM producto WHERE negocioId = :ng")
    suspend fun borrarProductos(ng: Long)

    @Query("DELETE FROM contacto WHERE negocioId = :ng")
    suspend fun borrarContactos(ng: Long)

    @Query("DELETE FROM factura_item WHERE facturaId IN (SELECT id FROM factura WHERE negocioId = :ng)")
    suspend fun borrarItemsDeFacturas(ng: Long)

    @Query("DELETE FROM factura WHERE negocioId = :ng")
    suspend fun borrarFacturas(ng: Long)

    @Query("DELETE FROM negocio WHERE id = :id")
    suspend fun borrarNegocio(id: Long)

    /**
     * Elimina el negocio y TODOS sus datos en una sola transacción: si algo falla,
     * no queda a medias (datos huérfanos sin negocio, o al revés).
     */
    @Transaction
    suspend fun eliminarConDatos(ng: Long) {
        borrarIngresos(ng)
        borrarEgresos(ng)
        borrarDeudas(ng)
        borrarRecurrentes(ng)
        borrarAgenda(ng)
        // Los ítems primero: cuelgan de la factura y quedarían huérfanos.
        borrarItemsDeFacturas(ng)
        borrarFacturas(ng)
        // Las fotos de los productos quedan en filesDir: son unos pocos KB por archivo y
        // borrarlas necesita Context, que el DAO no tiene.
        borrarProductos(ng)
        borrarContactos(ng)
        borrarNegocio(ng)
    }
}
