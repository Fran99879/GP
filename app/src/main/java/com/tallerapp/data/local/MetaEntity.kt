package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Meta de ahorro. */
@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val objetivoCentavos: Long,
    val actualCentavos: Long,
    val fechaObjetivo: Long?,
    val createdAt: Long,
)
