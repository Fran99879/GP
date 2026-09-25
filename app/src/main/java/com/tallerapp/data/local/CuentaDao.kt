package com.tallerapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Proyección de suma agrupada por cuenta. */
data class SumaPorCuenta(val cuenta: String, val total: Long)

@Dao
interface CuentaDao {

    @Query("SELECT * FROM cuenta WHERE activo = 1 ORDER BY orden, nombre")
    fun observar(): Flow<List<CuentaEntity>>

    @Query("SELECT * FROM cuenta ORDER BY orden, nombre")
    suspend fun listar(): List<CuentaEntity>

    @Insert
    suspend fun insertar(entity: CuentaEntity): Long

    @Update
    suspend fun actualizar(entity: CuentaEntity)

    @Query("SELECT nombre FROM cuenta WHERE id = :id")
    suspend fun nombreDe(id: Long): String?

    @Query("UPDATE ingreso SET cuenta = :nuevo WHERE cuenta = :viejo")
    suspend fun renombrarEnIngresos(viejo: String, nuevo: String)

    @Query("UPDATE egreso SET cuenta = :nuevo WHERE cuenta = :viejo")
    suspend fun renombrarEnEgresos(viejo: String, nuevo: String)

    @Query("SELECT cuenta AS cuenta, COALESCE(SUM(montoCentavos),0) AS total FROM ingreso GROUP BY cuenta")
    suspend fun sumaIngresosPorCuenta(): List<SumaPorCuenta>

    @Query("SELECT cuenta AS cuenta, COALESCE(SUM(montoCentavos),0) AS total FROM egreso GROUP BY cuenta")
    suspend fun sumaEgresosPorCuenta(): List<SumaPorCuenta>

    @Query("DELETE FROM cuenta WHERE id = :id")
    suspend fun eliminar(id: Long)
}
