package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EgresoDao {

    @Query("SELECT * FROM egreso WHERE fecha >= :inicio AND fecha < :fin ORDER BY fechaRegistro DESC")
    fun observarRango(inicio: Long, fin: Long): Flow<List<EgresoEntity>>

    @Query("SELECT COALESCE(SUM(montoCentavos), 0) FROM egreso WHERE fecha >= :inicio AND fecha < :fin")
    fun sumaRango(inicio: Long, fin: Long): Flow<Long>

    @Query("SELECT * FROM egreso WHERE id = :id")
    suspend fun obtener(id: Long): EgresoEntity?

    @Insert
    suspend fun insertar(entity: EgresoEntity): Long

    @Update
    suspend fun actualizar(entity: EgresoEntity)

    @Query("DELETE FROM egreso WHERE id = :id")
    suspend fun eliminar(id: Long)
}
