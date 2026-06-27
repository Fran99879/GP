package com.tallerapp.domain.model

/**
 * Origen de un ingreso (Frozen Spec 5.2). En la Fase 3 solo se usa MANUAL;
 * COBRO_DE_TRABAJO lo generará el módulo de Cobros (Fase 4).
 */
enum class OrigenIngreso {
    MANUAL,
    COBRO_DE_TRABAJO,
}
