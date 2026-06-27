package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Ingreso

/**
 * Operaciones atómicas de cobro (Frozen Spec 9.3/9.4). Garantizan que el ingreso y el
 * estado de cobro del trabajo se actualicen juntos o no se actualicen (Arquitectura RT-1).
 */
interface CobroRepository {
    /** Inserta el ingreso del cobro y marca el trabajo como Cobrado. Devuelve el id del ingreso. */
    suspend fun registrarCobro(ingreso: Ingreso, trabajoId: Long): Long

    /** Elimina el ingreso del cobro y devuelve el trabajo a Pendiente de cobro. */
    suspend fun anularCobro(trabajoId: Long): Boolean
}
