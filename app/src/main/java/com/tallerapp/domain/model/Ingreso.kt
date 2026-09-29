package com.tallerapp.domain.model

/**
 * Ingreso de dinero (Frozen Spec 5.2). Importe en centavos (AD-7).
 *
 * @param cuenta medio por el que entró la plata; única respuesta desde v13. Antes convivía
 *   con un enum `MetodoPago` que preguntaba casi lo mismo.
 * @param fecha fecha contable del movimiento (para caja/reportes, RN-2/RN-4).
 * @param fechaRegistro instante de creación; gobierna la edición/anulación del día (V-7).
 * @param trabajoId trabajo asociado; solo cuando origen = COBRO_DE_TRABAJO (Fase 4).
 */
data class Ingreso(
    val id: Long = 0,
    val montoCentavos: Long,
    val concepto: String,
    val cuenta: String = "Efectivo",
    val fecha: Long,
    val fechaRegistro: Long,
    val origen: OrigenIngreso,
    val trabajoId: Long?,
)
