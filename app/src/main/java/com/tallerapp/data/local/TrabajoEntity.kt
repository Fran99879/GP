package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro persistible de un trabajo (tabla local). Los enums se guardan como String.
 * Índices por estado, patente y cliente para dashboard, lista y búsqueda (MIP §12).
 */
@Entity(
    tableName = "trabajo",
    indices = [
        Index("estadoReparacion"),
        Index("patente"),
        Index("cliente"),
    ],
)
data class TrabajoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cliente: String,
    val telefono: String?,
    val patente: String?,
    val marca: String,
    val modelo: String,
    val servicio: String,
    val fechaIngreso: Long,
    val estadoReparacion: String,
    val estadoCobro: String,
    val problema: String?,
    val diagnostico: String?,
    val precioCentavos: Long,
    val cobroId: Long?,
)
