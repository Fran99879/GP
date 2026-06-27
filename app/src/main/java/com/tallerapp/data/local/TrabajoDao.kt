package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Acceso a datos de Trabajos. La búsqueda usa LIKE sobre patente y cliente (RN-10). */
@Dao
interface TrabajoDao {

    @Query("SELECT * FROM trabajo ORDER BY fechaIngreso DESC")
    fun observarTodos(): Flow<List<TrabajoEntity>>

    @Query(
        """
        SELECT * FROM trabajo
        WHERE patente LIKE '%' || :query || '%'
           OR cliente LIKE '%' || :query || '%'
        ORDER BY fechaIngreso DESC
        """
    )
    fun buscar(query: String): Flow<List<TrabajoEntity>>

    @Query("SELECT * FROM trabajo WHERE id = :id")
    suspend fun obtener(id: Long): TrabajoEntity?

    @Insert
    suspend fun insertar(entity: TrabajoEntity): Long

    @Update
    suspend fun actualizar(entity: TrabajoEntity)

    @Query("DELETE FROM trabajo WHERE id = :id")
    suspend fun eliminar(id: Long)
}
