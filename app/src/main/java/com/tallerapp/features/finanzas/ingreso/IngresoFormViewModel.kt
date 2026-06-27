package com.tallerapp.features.finanzas.ingreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.RepartoPago
import com.tallerapp.domain.usecase.EditarIngresoUseCase
import com.tallerapp.domain.usecase.IngresoResultado
import com.tallerapp.domain.usecase.ObtenerIngresoUseCase
import com.tallerapp.domain.usecase.RegistrarIngresoUseCase
import com.tallerapp.domain.validation.IngresoErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngresoFormState(
    val monto: String = "",
    val concepto: String = "",
    val metodo: MetodoPago? = null,
    val efectivo: String = "",
    val transferencia: String = "",
    val tarjeta: String = "",
    val mercadoPago: String = "",
    val fecha: Long = Fechas.hoyInicioMillis(),
    val editable: Boolean = true,
    val errores: IngresoErrores = IngresoErrores(),
    val titulo: String = "Nuevo Ingreso",
    val guardadoOk: Boolean = false,
) {
    val esMixto: Boolean get() = metodo == MetodoPago.PAGO_MIXTO
}

class IngresoFormViewModel(
    private val registrarIngreso: RegistrarIngresoUseCase,
    private val editarIngreso: EditarIngresoUseCase,
    private val obtenerIngreso: ObtenerIngresoUseCase,
    private val ingresoId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(IngresoFormState())
    val state: StateFlow<IngresoFormState> = _state.asStateFlow()

    private val esEdicion: Boolean = ingresoId != null

    init {
        if (ingresoId != null) cargar(ingresoId)
    }

    private fun cargar(id: Long) {
        viewModelScope.launch {
            val i = obtenerIngreso(id) ?: return@launch
            _state.update {
                it.copy(
                    monto = Dinero.centavosAEntrada(i.montoCentavos),
                    concepto = i.concepto,
                    metodo = i.metodo,
                    efectivo = i.reparto?.efectivoCentavos?.let(Dinero::centavosAEntrada).orEmpty(),
                    transferencia = i.reparto?.transferenciaCentavos?.let(Dinero::centavosAEntrada).orEmpty(),
                    tarjeta = i.reparto?.tarjetaCentavos?.let(Dinero::centavosAEntrada).orEmpty(),
                    mercadoPago = i.reparto?.mercadoPagoCentavos?.let(Dinero::centavosAEntrada).orEmpty(),
                    fecha = i.fecha,
                    editable = Fechas.esHoy(i.fechaRegistro),
                    titulo = "Editar Ingreso",
                )
            }
        }
    }

    fun onMontoChange(v: String) = _state.update { it.copy(monto = v) }
    fun onConceptoChange(v: String) = _state.update { it.copy(concepto = v) }
    fun onMetodoChange(v: MetodoPago) = _state.update { it.copy(metodo = v) }
    fun onEfectivoChange(v: String) = _state.update { it.copy(efectivo = v) }
    fun onTransferenciaChange(v: String) = _state.update { it.copy(transferencia = v) }
    fun onTarjetaChange(v: String) = _state.update { it.copy(tarjeta = v) }
    fun onMercadoPagoChange(v: String) = _state.update { it.copy(mercadoPago = v) }
    fun onFechaChange(v: Long) = _state.update { it.copy(fecha = v) }

    fun guardar() {
        val s = _state.value
        val montoCentavos = Dinero.parsearACentavos(s.monto)
        val reparto = if (s.esMixto) {
            RepartoPago(
                efectivoCentavos = aCentavos(s.efectivo),
                transferenciaCentavos = aCentavos(s.transferencia),
                tarjetaCentavos = aCentavos(s.tarjeta),
                mercadoPagoCentavos = aCentavos(s.mercadoPago),
            )
        } else null

        viewModelScope.launch {
            val resultado = if (esEdicion) {
                editarIngreso(ingresoId!!, montoCentavos, s.concepto, s.metodo, reparto, s.fecha)
            } else {
                registrarIngreso(montoCentavos, s.concepto, s.metodo, reparto, s.fecha)
            }
            when (resultado) {
                is IngresoResultado.Exito -> _state.update { it.copy(guardadoOk = true) }
                is IngresoResultado.Invalido -> _state.update { it.copy(errores = resultado.errores) }
            }
        }
    }

    /** Texto vacío cuenta como 0; texto inválido también, la validación atrapa el total. */
    private fun aCentavos(texto: String): Long =
        if (texto.isBlank()) 0L else Dinero.parsearACentavos(texto) ?: 0L
}
