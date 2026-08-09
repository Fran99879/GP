package com.tallerapp.domain.model

/**
 * Una deuda a favor: alguien que te debe dinero ("quién te debe").
 * Importe en centavos (AD-7).
 *
 * @param nombre quién debe.
 * @param montoCentavos cuánto debe.
 * @param fecha desde cuándo (fecha de la deuda).
 * @param nota detalle opcional (concepto).
 * @param cobrada true cuando ya se saldó.
 * @param fechaCobro instante en que se marcó como cobrada; null si sigue pendiente.
 * @param fechaRegistro instante de creación.
 */
data class Deuda(
    val id: Long = 0,
    val nombre: String,
    val montoCentavos: Long,
    val fecha: Long,
    val nota: String = "",
    val cobrada: Boolean = false,
    val fechaCobro: Long? = null,
    val fechaRegistro: Long = 0,
)
