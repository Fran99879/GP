package com.tallerapp.domain.model

import kotlin.math.roundToLong

/**
 * Producto del catálogo del negocio.
 *
 * El descuento vive en el producto y el **precio final** se calcula, nunca se guarda:
 * guardar los dos deja lugar a que queden contradiciéndose. Las facturas sí congelan el
 * importe cobrado, porque ahí es historia.
 */
data class Producto(
    val id: Long = 0,
    val nombre: String,
    val codigoBarras: String = "",
    val descripcion: String = "",
    val precioCentavos: Long = 0,
    val descuentoPct: Double = 0.0,
    val stock: Double = 0.0,
    val stockMinimo: Double = 0.0,
    val imagen: String = "",
    val createdAt: Long = 0,
) {
    /** Cuánto se descuenta, en centavos. */
    val descuentoCentavos: Long get() = Precios.descuento(precioCentavos, descuentoPct)

    /** Precio que paga el cliente: lista menos descuento. */
    val precioFinalCentavos: Long get() = precioCentavos - descuentoCentavos

    val tieneDescuento: Boolean get() = descuentoPct > 0.0

    /** True cuando hay mínimo fijado y el stock ya lo alcanzó. */
    val stockBajo: Boolean get() = stockMinimo > 0.0 && stock <= stockMinimo
}

/** Cuentas de precios con descuento, en un solo lugar para que el PDF y la UI coincidan. */
object Precios {

    /** Descuento en centavos de aplicar [pct] % a [centavos]. Redondea al centavo. */
    fun descuento(centavos: Long, pct: Double): Long {
        if (pct <= 0.0 || centavos <= 0) return 0
        return (centavos * pct.coerceAtMost(100.0) / 100.0).roundToLong()
    }

    /** [centavos] con el [pct] % ya descontado. */
    fun conDescuento(centavos: Long, pct: Double): Long = centavos - descuento(centavos, pct)

    /** Importe de una línea: cantidad × precio unitario, con el descuento de la línea. */
    fun subtotal(cantidad: Double, precioUnitCentavos: Long, descuentoPct: Double): Long {
        val bruto = (cantidad * precioUnitCentavos).roundToLong()
        return conDescuento(bruto, descuentoPct)
    }
}
