package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeudaDao {

    /** Todas las deudas: primero las pendientes, luego las cobradas, más nuevas arriba. */
    @Query("SELECT * FROM deuda ORDER BY cobrada ASC, fecha DESC, id DESC")
    fun observarTodas(): Flow<List<DeudaEntity>>

    /** Suma de lo que te deben (solo pendientes). */
    @Query("SELECT COALESCE(SUM(montoCentavos), 0) FROM deuda WHERE cobrada = 0")
    fun sumaPendiente(): Flow<Long>

    /** Cantidad de deudas pendientes. */
    @Query("SELECT COUNT(*) FROM deuda WHERE cobrada = 0")
    fun contarPendientes(): Flow<Int>

    @Query("SELECT * FROM deuda WHERE id = :id")
    suspend fun obtener(id: Long): DeudaEntity?

    @Insert
    suspend fun insertar(entity: DeudaEntity): Long

    @Update
    suspend fun actualizar(entity: DeudaEntity)

    @Query("DELETE FROM deuda WHERE id = :id")
    suspend fun eliminar(id: Long)
}
