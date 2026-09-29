package com.tallerapp.features.facturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Factura
import com.tallerapp.domain.usecase.EliminarFacturaUseCase
import com.tallerapp.domain.usecase.ObservarFacturasUseCase
import com.tallerapp.domain.usecase.ObtenerFacturaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Listado de facturas emitidas del negocio activo, con el detalle bajo demanda. */
class FacturasViewModel(
    observarFacturas: ObservarFacturasUseCase,
    private val obtenerFactura: ObtenerFacturaUseCase,
    private val eliminarFactura: EliminarFacturaUseCase,
) : ViewModel() {

    /** El listado no trae los ítems: para eso está [abrir]. */
    val facturas: StateFlow<List<Factura>> = observarFacturas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _detalle = MutableStateFlow<Factura?>(null)
    val detalle: StateFlow<Factura?> = _detalle.asStateFlow()

    /** Carga la factura con sus líneas para ver el detalle o reimprimir el PDF. */
    fun abrir(id: Long) = viewModelScope.launch {
        _detalle.value = obtenerFactura(id)
    }

    fun cerrarDetalle() {
        _detalle.value = null
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        eliminarFactura(id)
        if (_detalle.value?.id == id) _detalle.value = null
    }
}
