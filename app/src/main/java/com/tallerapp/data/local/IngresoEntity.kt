package com.tallerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Registro persistible de un ingreso. Índice por fecha para las consultas de caja/del día. */
@Entity(tableName = "ingreso", indices = [Index("fecha")])
data class IngresoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val montoCentavos: Long,
    val concepto: String,
    @ColumnInfo(defaultValue = "Efectivo") val cuenta: String,
    @ColumnInfo(defaultValue = "1") val negocioId: Long = 1,
    val fecha: Long,
    val fechaRegistro: Long,
    val origen: String,
    val trabajoId: Long?,
)
