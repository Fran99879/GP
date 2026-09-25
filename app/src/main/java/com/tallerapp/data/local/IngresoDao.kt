package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {

    @Query("SELECT * FROM ingreso WHERE negocioId = :ng AND fecha >= :inicio AND fecha < :fin ORDER BY fechaRegistro DESC")
    fun observarRango(ng: Long, inicio: Long, fin: Long): Flow<List<IngresoEntity>>

    @Query("SELECT COALESCE(SUM(montoCentavos), 0) FROM ingreso WHERE negocioId = :ng AND fecha >= :inicio AND fecha < :fin")
    fun sumaRango(ng: Long, inicio: Long, fin: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(montoCentavos), 0) FROM ingreso WHERE fecha >= :inicio AND fecha < :fin")
    fun sumaRangoTodos(inicio: Long, fin: Long): Flow<Long>

    @Query(
        "SELECT negocioId, COALESCE(SUM(montoCentavos), 0) AS total FROM ingreso " +
            "WHERE fecha >= :inicio AND fecha < :fin GROUP BY negocioId",
    )
    fun totalesPorNegocio(inicio: Long, fin: Long): Flow<List<TotalPorNegocio>>

    @Query("SELECT * FROM ingreso WHERE id = :id")
    suspend fun obtener(id: Long): IngresoEntity?

    @Insert
    suspend fun insertar(entity: IngresoEntity): Long

    @Update
    suspend fun actualizar(entity: IngresoEntity)

    @Query("DELETE FROM ingreso WHERE id = :id")
    suspend fun eliminar(id: Long)
}
