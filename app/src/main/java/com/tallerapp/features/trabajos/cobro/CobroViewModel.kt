package com.tallerapp.features.trabajos.cobro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.RepartoPago
import com.tallerapp.domain.usecase.CobroResultado
import com.tallerapp.domain.usecase.ObtenerTrabajoUseCase
import com.tallerapp.domain.usecase.RegistrarCobroUseCase
import com.tallerapp.domain.validation.CobroErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CobroState(
    val cargando: Boolean = true,
    val identificacion: String = "",
    val montoCentavos: Long = 0,
    val metodo: MetodoPago? = null,
    val efectivo: String = "",
    val transferencia: String = "",
    val tarjeta: String = "",
    val mercadoPago: String = "",
    val errores: CobroErrores = CobroErrores(),
    val cobradoOk: Boolean = false,
) {
    val esMixto: Boolean get() = metodo == MetodoPago.PAGO_MIXTO
}

/** Registra el cobro de un trabajo entregado. El monto es fijo: el precio del trabajo. */
class CobroViewModel(
    private val obtenerTrabajo: ObtenerTrabajoUseCase,
    private val registrarCobro: RegistrarCobroUseCase,
    private val trabajoId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(CobroState())
    val state: StateFlow<CobroState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val t = obtenerTrabajo(trabajoId)
            _state.update {
                it.copy(
                    cargando = false,
                    identificacion = t?.identificacion().orEmpty(),
                    montoCentavos = t?.precioCentavos ?: 0,
                )
            }
        }
    }

    fun onMetodoChange(v: MetodoPago) = _state.update { it.copy(metodo = v) }
    fun onEfectivoChange(v: String) = _state.update { it.copy(efectivo = v) }
    fun onTransferenciaChange(v: String) = _state.update { it.copy(transferencia = v) }
    fun onTarjetaChange(v: String) = _state.update { it.copy(tarjeta = v) }
    fun onMercadoPagoChange(v: String) = _state.update { it.copy(mercadoPago = v) }

    fun confirmar() {
        val s = _state.value
        val reparto = if (s.esMixto) {
            RepartoPago(
                efectivoCentavos = aCentavos(s.efectivo),
                transferenciaCentavos = aCentavos(s.transferencia),
                tarjetaCentavos = aCentavos(s.tarjeta),
                mercadoPagoCentavos = aCentavos(s.mercadoPago),
            )
        } else null

        viewModelScope.launch {
            when (val r = registrarCobro(trabajoId, s.metodo, reparto)) {
                is CobroResultado.Exito -> _state.update { it.copy(cobradoOk = true) }
                is CobroResultado.Invalido -> _state.update { it.copy(errores = r.errores) }
            }
        }
    }

    private fun aCentavos(texto: String): Long =
        if (texto.isBlank()) 0L else Dinero.parsearACentavos(texto) ?: 0L
}
