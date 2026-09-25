package com.tallerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Movimiento recurrente (sueldo, alquiler, suscripción). Se genera cada mes el día indicado. */
@Entity(tableName = "recurrente")
data class RecurrenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,               // "ingreso" | "egreso"
    val montoCentavos: Long,
    val concepto: String,
    val categoria: String,
    val cuenta: String,
    val diaMes: Int,                // 1..28
    val activo: Boolean,
    val ultimoGenerado: String,     // "yyyy-MM" del último mes generado ("" si nunca)
    val createdAt: Long,
    @ColumnInfo(defaultValue = "1") val negocioId: Long = 1,
)
