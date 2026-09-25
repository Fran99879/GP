package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Cliente/proveedor frecuente (para autocompletar en deudas). */
@Entity(tableName = "contacto", indices = [Index(value = ["nombre"], unique = true)])
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val createdAt: Long,
)
