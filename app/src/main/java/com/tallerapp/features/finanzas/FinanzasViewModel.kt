package com.tallerapp.features.finanzas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.usecase.EliminarEgresoUseCase
import com.tallerapp.domain.usecase.EliminarIngresoUseCase
import com.tallerapp.domain.usecase.ObservarCategoriasUseCase
import com.tallerapp.domain.usecase.ObservarEgresosRangoUseCase
import com.tallerapp.domain.usecase.ObservarIngresosRangoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

/** Qué movimientos se muestran en la lista. */
enum class VistaMov { TODOS, INGRESOS, GASTOS }

/** Filtro de movimientos: rango de fechas, texto y qué lista se muestra. */
data class FiltroMov(
    // Arranca en el mes en curso, no en el día: lo que se quiere ver al abrir es cómo viene
    // el mes, y el movimiento de hoy igual aparece primero en la lista.
    val desde: Long = Fechas.rangoDelMesActual().first,
    val hasta: Long = Fechas.rangoDelMesActual().second - 86_400_000L,
    val texto: String = "",
    val vista: VistaMov = VistaMov.TODOS,
)

/** Hub de Finanzas con búsqueda y filtros por rango de fechas. */
@OptIn(ExperimentalCoroutinesApi::class)
class FinanzasViewModel(
    observarIngresosRango: ObservarIngresosRangoUseCase,
    observarEgresosRango: ObservarEgresosRangoUseCase,
    observarCategorias: ObservarCategoriasUseCase,
    private val eliminarIngresoUC: EliminarIngresoUseCase,
    private val eliminarEgresoUC: EliminarEgresoUseCase,
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroMov())
    val filtro: StateFlow<FiltroMov> = _filtro.asStateFlow()

    private fun finExclusivo(hasta: Long) = hasta + 86_400_000L // incluye el día "hasta"

    val ingresos: StateFlow<List<Ingreso>> =
        _filtro.flatMapLatest { f ->
            observarIngresosRango(f.desde, finExclusivo(f.hasta)).map { lista ->
                val filtrada = if (f.texto.isBlank()) lista
                else lista.filter {
                    it.concepto.contains(f.texto, true) || it.cuenta.contains(f.texto, true)
                }
                // Lo último arriba: primero por fecha del movimiento y, dentro del mismo día,
                // por orden de carga, así lo que se acaba de anotar queda a la vista.
                filtrada.sortedWith(
                    compareByDescending<Ingreso> { it.fecha }.thenByDescending { it.fechaRegistro },
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val egresos: StateFlow<List<Egreso>> =
        _filtro.flatMapLatest { f ->
            observarEgresosRango(f.desde, finExclusivo(f.hasta)).map { lista ->
                val filtrada = if (f.texto.isBlank()) lista
                else lista.filter {
                    it.concepto.contains(f.texto, true) || it.categoria.contains(f.texto, true) ||
                        it.cuenta.contains(f.texto, true)
                }
                filtrada.sortedWith(
                    compareByDescending<Egreso> { it.fecha }.thenByDescending { it.fechaRegistro },
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val iconosEgreso: StateFlow<Map<String, String>> =
        observarCategorias("egreso")
            .map { list -> list.associate { it.nombre to it.icono } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setDesde(millis: Long) = _filtro.update { it.copy(desde = millis, hasta = maxOf(it.hasta, millis)) }
    fun setHasta(millis: Long) = _filtro.update { it.copy(hasta = millis, desde = minOf(it.desde, millis)) }
    fun setTexto(texto: String) = _filtro.update { it.copy(texto = texto) }

    /** Tocar la vista activa vuelve a mostrar todo: el botón hace de interruptor. */
    fun setVista(vista: VistaMov) = _filtro.update {
        it.copy(vista = if (it.vista == vista) VistaMov.TODOS else vista)
    }

    fun filtrarHoy() = _filtro.update {
        it.copy(desde = Fechas.hoyInicioMillis(), hasta = Fechas.hoyInicioMillis())
    }

    fun filtrarEsteMes() {
        val (ini, fin) = Fechas.rangoDelMes(YearMonth.now())
        _filtro.update { it.copy(desde = ini, hasta = fin - 86_400_000L) }
    }

    fun eliminarIngreso(id: Long) = viewModelScope.launch { eliminarIngresoUC(id) }
    fun eliminarEgreso(id: Long) = viewModelScope.launch { eliminarEgresoUC(id) }
}
