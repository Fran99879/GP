package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categoria WHERE tipo = :tipo AND activo = 1 ORDER BY orden, nombre")
    fun observar(tipo: String): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categoria WHERE tipo = :tipo ORDER BY orden, nombre")
    suspend fun listar(tipo: String): List<CategoriaEntity>

    @Insert
    suspend fun insertar(entity: CategoriaEntity): Long

    @Update
    suspend fun actualizar(entity: CategoriaEntity)

    @Query("SELECT nombre FROM categoria WHERE id = :id")
    suspend fun nombreDe(id: Long): String?

    @Query("UPDATE egreso SET categoria = :nuevo WHERE categoria = :viejo")
    suspend fun renombrarEnEgresos(viejo: String, nuevo: String)

    @Query("DELETE FROM categoria WHERE id = :id")
    suspend fun eliminar(id: Long)
}
