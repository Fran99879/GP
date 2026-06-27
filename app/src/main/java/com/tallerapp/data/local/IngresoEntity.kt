package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro persistible de un ingreso. El reparto del pago mixto se guarda en columnas
 * nullable (solo se completan cuando el método es PAGO_MIXTO). Índice por fecha para
 * las consultas de caja/del día.
 */
@Entity(tableName = "ingreso", indices = [Index("fecha")])
data class IngresoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val montoCentavos: Long,
    val concepto: String,
    val metodo: String,
    val repEfectivo: Long?,
    val repTransferencia: Long?,
    val repTarjeta: Long?,
    val repMercadoPago: Long?,
    val fecha: Long,
    val fechaRegistro: Long,
    val origen: String,
    val trabajoId: Long?,
)
