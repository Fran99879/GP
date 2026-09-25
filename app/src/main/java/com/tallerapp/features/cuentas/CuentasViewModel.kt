package com.tallerapp.features.cuentas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Cuenta
import com.tallerapp.domain.usecase.EliminarCuentaUseCase
import com.tallerapp.domain.usecase.GuardarCuentaUseCase
import com.tallerapp.domain.usecase.SaldosCuentasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CuentasViewModel(
    private val saldosCuentas: SaldosCuentasUseCase,
    private val guardarCuenta: GuardarCuentaUseCase,
    private val eliminarCuenta: EliminarCuentaUseCase,
) : ViewModel() {

    private val _cuentas = MutableStateFlow<List<Cuenta>>(emptyList())
    val cuentas: StateFlow<List<Cuenta>> = _cuentas.asStateFlow()

    init { refrescar() }

    private fun refrescar() = viewModelScope.launch { _cuentas.value = saldosCuentas() }

    fun guardar(cuenta: Cuenta) = viewModelScope.launch { guardarCuenta(cuenta); refrescar() }
    fun eliminar(id: Long) = viewModelScope.launch { eliminarCuenta(id); refrescar() }
}
