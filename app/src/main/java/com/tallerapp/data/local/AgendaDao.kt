package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {

    /** Entradas del negocio en [inicio, fin): ordenadas por fecha y hora. */
    @Query(
        "SELECT * FROM agenda WHERE negocioId = :ng AND fecha >= :inicio AND fecha < :fin " +
            "ORDER BY fecha, hora, id",
    )
    fun observarRango(ng: Long, inicio: Long, fin: Long): Flow<List<AgendaEntity>>

    @Query("SELECT * FROM agenda WHERE id = :id")
    suspend fun obtener(id: Long): AgendaEntity?

    @Insert
    suspend fun insertar(entity: AgendaEntity): Long

    @Update
    suspend fun actualizar(entity: AgendaEntity)

    @Query("UPDATE agenda SET hecho = :hecho WHERE id = :id")
    suspend fun marcarHecho(id: Long, hecho: Boolean)

    @Query("DELETE FROM agenda WHERE id = :id")
    suspend fun eliminar(id: Long)
}
