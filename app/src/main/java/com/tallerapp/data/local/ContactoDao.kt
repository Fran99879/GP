package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query("SELECT nombre FROM contacto ORDER BY nombre COLLATE NOCASE")
    fun observar(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(entity: ContactoEntity)
}
