package com.tallerapp.domain.model

/**
 * Estado de cobro, independiente del estado de reparación (Frozen Spec 8).
 * En la Fase 2 solo se inicializa y se muestra; cambia en la Fase 4 (Cobros).
 */
enum class EstadoCobro(val etiqueta: String) {
    PENDIENTE_DE_COBRO("Pendiente de cobro"),
    COBRADO("Cobrado"),
}
