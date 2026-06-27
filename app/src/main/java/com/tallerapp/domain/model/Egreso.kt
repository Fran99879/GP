package com.tallerapp.domain.model

/**
 * Egreso de dinero (Frozen Spec 5.3). Importe en centavos (AD-7). No se vincula a
 * trabajos (decisión 13.3).
 *
 * @param fecha fecha contable del movimiento.
 * @param fechaRegistro instante de creación; gobierna la edición/anulación del día (V-7).
 */
data class Egreso(
    val id: Long = 0,
    val montoCentavos: Long,
    val categoria: CategoriaEgreso,
    val concepto: String,
    val fecha: Long,
    val fechaRegistro: Long,
)
