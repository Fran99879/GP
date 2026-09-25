package com.tallerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Categoría personalizable de movimientos (compartida). tipo = "ingreso" | "egreso". */
@Entity(tableName = "categoria")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val nombre: String,
    val color: String,
    val icono: String,
    val orden: Int,
    val activo: Boolean,
    @ColumnInfo(defaultValue = "0") val presupuestoCentavos: Long = 0,
)
