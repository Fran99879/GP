package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un negocio/tienda del usuario (o "Personal"). */
@Entity(tableName = "negocio")
data class NegocioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val createdAt: Long,
)
