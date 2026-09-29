package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cabecera de una factura emitida (comprobante interno, no fiscal).
 *
 * Los importes quedan **congelados** al emitir: si mañana cambia el precio del producto,
 * la factura de ayer sigue diciendo lo que se cobró. Por eso el total y el subtotal se
 * guardan en vez de recalcularse desde el catálogo.
 *
 * `ingresoId` apunta al ingreso que se creó al emitir, cuando el usuario pidió registrarlo.
 * Es null si se emitió solo como documento.
 */
@Entity(
    tableName = "factura",
    indices = [
        Index(value = ["negocioId"], name = "ix_factura_negocio"),
        Index(value = ["fecha"], name = "ix_factura_fecha"),
    ],
)
data class FacturaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val negocioId: Long,
    val numero: String,
    val cliente: String,
    /** CUIT, DNI o lo que el usuario quiera anotar del cliente. Opcional. */
    val documento: String,
    val fecha: Long,
    /** Descuento aplicado al total, además de los de cada ítem. */
    val descuentoPct: Double,
    /** Suma de los ítems, ya con el descuento de cada uno. */
    val subtotalCentavos: Long,
    /** Subtotal menos el descuento general. Es lo que se cobra. */
    val totalCentavos: Long,
    val notas: String,
    /** Cuenta donde entró la plata, cuando se registró el ingreso. */
    val cuenta: String,
    val ingresoId: Long?,
    /** Cliente de la ficha, si se eligió uno. El nombre queda igual guardado en [cliente]. */
    val clienteId: Long? = null,
    val createdAt: Long,
)

/**
 * Una línea de la factura. `productoId` es solo trazabilidad: la descripción y el precio
 * se copian, así borrar un producto del catálogo no rompe las facturas ya emitidas.
 */
@Entity(
    tableName = "factura_item",
    indices = [Index(value = ["facturaId"], name = "ix_factura_item_factura")],
)
data class FacturaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facturaId: Long,
    val productoId: Long?,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitCentavos: Long,
    val descuentoPct: Double,
    val subtotalCentavos: Long,
)
