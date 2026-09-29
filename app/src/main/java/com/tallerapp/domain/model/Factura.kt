package com.tallerapp.domain.model

/** Una línea de factura, con su propio descuento. */
data class ItemFactura(
    val id: Long = 0,
    val productoId: Long? = null,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitCentavos: Long,
    val descuentoPct: Double = 0.0,
    /** Importe cobrado por la línea. Se congela al emitir; antes se calcula. */
    val subtotalCentavos: Long = Precios.subtotal(cantidad, precioUnitCentavos, descuentoPct),
)

/**
 * Factura emitida: comprobante interno del negocio, **no fiscal**. Sirve para cobrar,
 * imprimir y dejar registro; no reemplaza una factura de ARCA/AFIP.
 */
data class Factura(
    val id: Long = 0,
    val numero: String,
    val cliente: String,
    val documento: String = "",
    val fecha: Long,
    /** Descuento sobre el total, además del de cada ítem. */
    val descuentoPct: Double = 0.0,
    val subtotalCentavos: Long,
    val totalCentavos: Long,
    val notas: String = "",
    val cuenta: String = "",
    val ingresoId: Long? = null,
    /** Cliente de la ficha, cuando se eligió uno. [cliente] guarda el nombre igual. */
    val clienteId: Long? = null,
    val createdAt: Long = 0,
    val items: List<ItemFactura> = emptyList(),
) {
    /** Descuento general en centavos (el de los ítems ya está dentro del subtotal). */
    val descuentoCentavos: Long get() = subtotalCentavos - totalCentavos

    /** True si el importe entró a la caja como ingreso. */
    val registradaEnCaja: Boolean get() = ingresoId != null
}
