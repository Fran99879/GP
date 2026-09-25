package com.tallerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Registro persistible de una deuda a favor. Índice por estado para separar pendientes/cobradas. */
@Entity(tableName = "deuda", indices = [Index("cobrada")])
data class DeudaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val montoCentavos: Long,
    val fecha: Long,
    val nota: String,
    val cobrada: Boolean,
    val fechaCobro: Long?,
    val fechaRegistro: Long,
    val fechaLimite: Long? = null,
    @ColumnInfo(defaultValue = "1") val negocioId: Long = 1,
)
