package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Cuenta
import com.tallerapp.domain.repository.CuentaRepository
import kotlinx.coroutines.flow.Flow

/** Observa las cuentas activas (para los selectores). */
class ObservarCuentasUseCase(private val repository: CuentaRepository) {
    operator fun invoke(): Flow<List<Cuenta>> = repository.observar()
}

/** Cuentas con saldo calculado (inicial + ingresos − gastos). */
class SaldosCuentasUseCase(private val repository: CuentaRepository) {
    suspend operator fun invoke(): List<Cuenta> = repository.saldos()
}

class GuardarCuentaUseCase(private val repository: CuentaRepository) {
    suspend operator fun invoke(cuenta: Cuenta) {
        if (cuenta.id == 0L) repository.crear(cuenta) else repository.actualizar(cuenta)
    }
}

class EliminarCuentaUseCase(private val repository: CuentaRepository) {
    suspend operator fun invoke(id: Long): Boolean = repository.eliminar(id)
}
