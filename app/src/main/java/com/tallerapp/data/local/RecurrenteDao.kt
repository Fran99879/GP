package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurrenteDao {

    @Query("SELECT * FROM recurrente WHERE negocioId = :ng ORDER BY tipo, diaMes, id")
    fun observar(ng: Long): Flow<List<RecurrenteEntity>>

    @Query("SELECT * FROM recurrente WHERE activo = 1 AND negocioId = :ng")
    suspend fun activos(ng: Long): List<RecurrenteEntity>

    @Insert
    suspend fun insertar(entity: RecurrenteEntity): Long

    @Update
    suspend fun actualizar(entity: RecurrenteEntity)

    @Query("UPDATE recurrente SET ultimoGenerado = :ym WHERE id = :id")
    suspend fun marcarGenerado(id: Long, ym: String)

    @Query("DELETE FROM recurrente WHERE id = :id")
    suspend fun eliminar(id: Long)
}
