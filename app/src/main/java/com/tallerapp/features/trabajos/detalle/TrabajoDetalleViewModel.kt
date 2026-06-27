package com.tallerapp.features.trabajos.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.rules.TransicionesTrabajo
import com.tallerapp.domain.usecase.CambiarEstadoUseCase
import com.tallerapp.domain.usecase.EliminarTrabajoUseCase
import com.tallerapp.domain.usecase.ObtenerTrabajoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del detalle de un trabajo. */
data class TrabajoDetalleState(
    val cargando: Boolean = true,
    val trabajo: Trabajo? = null,
    val transiciones: List<EstadoReparacion> = emptyList(),
    val eliminado: Boolean = false,
)

class TrabajoDetalleViewModel(
    private val obtenerTrabajo: ObtenerTrabajoUseCase,
    private val cambiarEstadoUC: CambiarEstadoUseCase,
    private val eliminarTrabajo: EliminarTrabajoUseCase,
    private val trabajoId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(TrabajoDetalleState())
    val state: StateFlow<TrabajoDetalleState> = _state.asStateFlow()

    init {
        cargar()
    }

    /** Recarga el detalle (p. ej. al volver de la pantalla de edición). */
    fun recargar() = cargar()

    private fun cargar() {
        viewModelScope.launch {
            val t = obtenerTrabajo(trabajoId)
            _state.value = TrabajoDetalleState(
                cargando = false,
                trabajo = t,
                transiciones = t
                    ?.let { TransicionesTrabajo.transicionesValidas(it.estadoReparacion).toList() }
                    .orEmpty(),
            )
        }
    }

    fun cambiarEstado(destino: EstadoReparacion) {
        viewModelScope.launch {
            cambiarEstadoUC(trabajoId, destino)
            cargar()
        }
    }

    fun eliminar() {
        viewModelScope.launch {
            eliminarTrabajo(trabajoId)
            _state.update { it.copy(eliminado = true) }
        }
    }
}
