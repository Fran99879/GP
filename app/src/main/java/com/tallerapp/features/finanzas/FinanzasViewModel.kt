package com.tallerapp.features.finanzas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.ResumenDelDia
import com.tallerapp.domain.usecase.EliminarEgresoUseCase
import com.tallerapp.domain.usecase.EliminarIngresoUseCase
import com.tallerapp.domain.usecase.ObservarEgresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarIngresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarResumenDelDiaUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Hub de Finanzas: resumen del día y movimientos de hoy con anulación (V-7). */
class FinanzasViewModel(
    observarResumen: ObservarResumenDelDiaUseCase,
    observarIngresos: ObservarIngresosDelDiaUseCase,
    observarEgresos: ObservarEgresosDelDiaUseCase,
    private val eliminarIngresoUC: EliminarIngresoUseCase,
    private val eliminarEgresoUC: EliminarEgresoUseCase,
) : ViewModel() {

    val resumen: StateFlow<ResumenDelDia> =
        observarResumen().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResumenDelDia(0, 0),
        )

    val ingresos: StateFlow<List<Ingreso>> =
        observarIngresos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val egresos: StateFlow<List<Egreso>> =
        observarEgresos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun eliminarIngreso(id: Long) = viewModelScope.launch { eliminarIngresoUC(id) }
    fun eliminarEgreso(id: Long) = viewModelScope.launch { eliminarEgresoUC(id) }
}
