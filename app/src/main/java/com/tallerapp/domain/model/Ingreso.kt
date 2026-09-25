package com.tallerapp.domain.model

/**
 * Ingreso de dinero (Frozen Spec 5.2). Importe en centavos (AD-7).
 *
 * @param fecha fecha contable del movimiento (para caja/reportes, RN-2/RN-4).
 * @param fechaRegistro instante de creación; gobierna la edición/anulación del día (V-7).
 * @param reparto detalle del pago mixto; null si el método no es PAGO_MIXTO.
 * @param trabajoId trabajo asociado; solo cuando origen = COBRO_DE_TRABAJO (Fase 4).
 */
data class Ingreso(
    val id: Long = 0,
    val montoCentavos: Long,
    val concepto: String,
    val metodo: MetodoPago,
    val cuenta: String = "Efectivo",
    val reparto: RepartoPago?,
    val fecha: Long,
    val fechaRegistro: Long,
    val origen: OrigenIngreso,
    val trabajoId: Long?,
)
