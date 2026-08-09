package com.tallerapp.features.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.ReporteMensual
import com.tallerapp.domain.usecase.ObservarReporteMensualUseCase
import com.tallerapp.domain.usecase.ObservarResumenDelDiaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

/**
 * Reporte mensual navegable: permite ver el mes actual y meses anteriores
 * ("ver resúmenes viejos"). El reporte se recalcula al cambiar de mes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportesViewModel(
    observarResumenDelDia: ObservarResumenDelDiaUseCase,
    private val observarReporteMensual: ObservarReporteMensualUseCase,
) : ViewModel() {

    private val _mes = MutableStateFlow(Fechas.mesActual())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    /** True si estamos en el mes actual (no se puede avanzar más allá). */
    val esMesActual: Boolean get() = _mes.value == Fechas.mesActual()

    val diario = observarResumenDelDia().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.tallerapp.domain.model.ResumenDelDia(0, 0),
    )

    val mensual: StateFlow<ReporteMensual> =
        _mes.flatMapLatest { observarReporteMensual(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReporteMensual())

    fun mesAnterior() {
        _mes.value = _mes.value.minusMonths(1)
    }

    fun mesSiguiente() {
        val siguiente = _mes.value.plusMonths(1)
        if (!siguiente.isAfter(Fechas.mesActual())) _mes.value = siguiente
    }
}
