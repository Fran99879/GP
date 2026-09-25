package com.tallerapp.features.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Categoria
import com.tallerapp.domain.model.ReporteMensual
import com.tallerapp.domain.model.ResumenNegocio
import com.tallerapp.domain.usecase.MesEvolucion
import com.tallerapp.domain.usecase.ObservarCategoriasUseCase
import com.tallerapp.domain.usecase.ObservarComparativaNegociosUseCase
import com.tallerapp.domain.usecase.ObservarEvolucionUseCase
import com.tallerapp.domain.usecase.ObservarReporteMensualUseCase
import com.tallerapp.domain.usecase.ObservarResumenDelDiaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

/**
 * Reporte mensual navegable: permite ver el mes actual y meses anteriores
 * ("ver resúmenes viejos"). El reporte se recalcula al cambiar de mes.
 *
 * [todosLosNegocios] combina los datos de todos los negocios (como el check del escritorio).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportesViewModel(
    observarResumenDelDia: ObservarResumenDelDiaUseCase,
    private val observarReporteMensual: ObservarReporteMensualUseCase,
    observarCategorias: ObservarCategoriasUseCase,
    private val observarEvolucion: ObservarEvolucionUseCase,
    private val observarComparativaNegocios: ObservarComparativaNegociosUseCase,
) : ViewModel() {

    /** Categorías de gasto (para los presupuestos). */
    val categoriasEgreso: StateFlow<List<Categoria>> =
        observarCategorias("egreso").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mes = MutableStateFlow(Fechas.mesActual())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val _todosLosNegocios = MutableStateFlow(false)
    val todosLosNegocios: StateFlow<Boolean> = _todosLosNegocios.asStateFlow()

    /** True si estamos en el mes actual (no se puede avanzar más allá). */
    val esMesActual: Boolean get() = _mes.value == Fechas.mesActual()

    val diario = observarResumenDelDia().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.tallerapp.domain.model.ResumenDelDia(0, 0),
    )

    val mensual: StateFlow<ReporteMensual> =
        combine(_mes, _todosLosNegocios) { mes, todos -> mes to todos }
            .flatMapLatest { (mes, todos) -> observarReporteMensual(mes, todos) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReporteMensual())

    /** Evolución de los últimos 12 meses (termina en el mes seleccionado). */
    val evolucion: StateFlow<List<MesEvolucion>> =
        _mes.mapLatest { observarEvolucion(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Comparativa entre negocios del mes seleccionado (solo se muestra si hay más de uno). */
    val comparativa: StateFlow<List<ResumenNegocio>> =
        _mes.flatMapLatest { observarComparativaNegocios(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun mesAnterior() {
        _mes.value = _mes.value.minusMonths(1)
    }

    fun mesSiguiente() {
        val siguiente = _mes.value.plusMonths(1)
        if (!siguiente.isAfter(Fechas.mesActual())) _mes.value = siguiente
    }

    fun setTodosLosNegocios(valor: Boolean) {
        _todosLosNegocios.value = valor
    }
}
