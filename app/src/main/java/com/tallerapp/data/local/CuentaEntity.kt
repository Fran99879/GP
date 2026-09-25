package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cuenta / medio de pago (Efectivo, Banco, MercadoPago…). Compartida. */
@Entity(tableName = "cuenta")
data class CuentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val icono: String,
    val saldoInicialCentavos: Long,
    val orden: Int,
    val activo: Boolean,
)
