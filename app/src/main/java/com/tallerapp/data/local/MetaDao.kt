package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {

    @Query("SELECT * FROM meta ORDER BY id")
    fun observar(): Flow<List<MetaEntity>>

    @Insert
    suspend fun insertar(entity: MetaEntity): Long

    @Update
    suspend fun actualizar(entity: MetaEntity)

    @Query("DELETE FROM meta WHERE id = :id")
    suspend fun eliminar(id: Long)
}
