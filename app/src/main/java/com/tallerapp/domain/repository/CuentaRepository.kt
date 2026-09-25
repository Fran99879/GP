package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Cuenta
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de cuentas / medios de pago. */
interface CuentaRepository {
    fun observar(): Flow<List<Cuenta>>
    suspend fun listar(): List<Cuenta>
    /** Cuentas con el saldo calculado (inicial + ingresos − gastos). */
    suspend fun saldos(): List<Cuenta>
    suspend fun crear(cuenta: Cuenta): Long
    suspend fun actualizar(cuenta: Cuenta)
    /** Elimina y reasigna sus movimientos a "Efectivo". No borra "Efectivo". */
    suspend fun eliminar(id: Long): Boolean
}
