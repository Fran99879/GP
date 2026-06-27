package com.tallerapp.features.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.ReporteMensual
import com.tallerapp.domain.model.ResumenDelDia
import com.tallerapp.domain.usecase.ObservarReporteMensualUseCase
import com.tallerapp.domain.usecase.ObservarResumenDelDiaUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Reportes diario (reusa el resumen del día) y mensual (Frozen Spec 12.2/12.3). */
class ReportesViewModel(
    observarResumenDelDia: ObservarResumenDelDiaUseCase,
    observarReporteMensual: ObservarReporteMensualUseCase,
) : ViewModel() {

    val diario: StateFlow<ResumenDelDia> =
        observarResumenDelDia().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResumenDelDia(0, 0),
        )

    val mensual: StateFlow<ReporteMensual> =
        observarReporteMensual().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ReporteMensual(),
        )
}
