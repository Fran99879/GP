package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Registro persistible de un egreso. Índice por fecha para las consultas del día. */
@Entity(tableName = "egreso", indices = [Index("fecha")])
data class EgresoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val montoCentavos: Long,
    val categoria: String,
    val concepto: String,
    val fecha: Long,
    val fechaRegistro: Long,
)
