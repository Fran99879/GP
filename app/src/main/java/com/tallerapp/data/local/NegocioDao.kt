package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
