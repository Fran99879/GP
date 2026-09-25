package com.tallerapp.features.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.AgendaItem
import com.tallerapp.domain.usecase.EliminarAgendaItemUseCase
import com.tallerapp.domain.usecase.GuardarAgendaItemUseCase
import com.tallerapp.domain.usecase.MarcarAgendaHechoUseCase
import com.tallerapp.domain.usecase.ObservarAgendaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * Agenda mensual: el calendario muestra el mes seleccionado y la lista, el día elegido.
 * Las entradas se filtran por el negocio activo (igual que el resto de la app).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModel(
    private val observarAgenda: ObservarAgendaUseCase,
    private val guardarAgendaItem: GuardarAgendaItemUseCase,
    private val marcarAgendaHecho: MarcarAgendaHechoUseCase,
    private val eliminarAgendaItem: EliminarAgendaItemUseCase,
) : ViewModel() {

    private val _mes = MutableStateFlow(YearMonth.now())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val _diaSeleccionado = MutableStateFlow(LocalDate.now())
    val diaSeleccionado: StateFlow<LocalDate> = _diaSeleccionado.asStateFlow()

    /** Todas las entradas del mes visible (para pintar los puntos del calendario). */
    val itemsDelMes: StateFlow<List<AgendaItem>> =
        _mes.flatMapLatest { ym ->
            val (inicio, fin) = Fechas.rangoDelMes(ym)
            observarAgenda(inicio, fin)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun mesAnterior() {
        _mes.value = _mes.value.minusMonths(1)
    }

    fun mesSiguiente() {
        _mes.value = _mes.value.plusMonths(1)
    }

    fun seleccionarDia(dia: LocalDate) {
        _diaSeleccionado.value = dia
        if (YearMonth.from(dia) != _mes.value) _mes.value = YearMonth.from(dia)
    }

    fun guardar(item: AgendaItem) = viewModelScope.launch { guardarAgendaItem(item) }
    fun marcarHecho(id: Long, hecho: Boolean) = viewModelScope.launch { marcarAgendaHecho(id, hecho) }
    fun eliminar(id: Long) = viewModelScope.launch { eliminarAgendaItem(id) }
}
