package com.tallerapp.features.finanzas.ingreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Cuenta
import com.tallerapp.domain.usecase.EditarIngresoUseCase
import com.tallerapp.domain.usecase.IngresoResultado
import com.tallerapp.domain.usecase.ObservarCuentasUseCase
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
    val cuenta: String? = null,
    val cuentas: List<Cuenta> = emptyList(),
    val fecha: Long = Fechas.hoyInicioMillis(),
    val editable: Boolean = true,
    val errores: IngresoErrores = IngresoErrores(),
    val titulo: String = "Nuevo Ingreso",
    val procesando: Boolean = false,
    val guardadoOk: Boolean = false,
)

class IngresoFormViewModel(
    private val registrarIngreso: RegistrarIngresoUseCase,
    private val editarIngreso: EditarIngresoUseCase,
    private val obtenerIngreso: ObtenerIngresoUseCase,
    private val observarCuentas: ObservarCuentasUseCase,
    private val ingresoId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(IngresoFormState())
    val state: StateFlow<IngresoFormState> = _state.asStateFlow()

    private val esEdicion: Boolean = ingresoId != null

    init {
        viewModelScope.launch {
            observarCuentas().collect { cts ->
                _state.update { st -> st.copy(cuentas = cts, cuenta = st.cuenta ?: cts.firstOrNull()?.nombre) }
            }
        }
        if (ingresoId != null) cargar(ingresoId)
    }

    private fun cargar(id: Long) {
        viewModelScope.launch {
            val i = obtenerIngreso(id) ?: return@launch
            _state.update {
                it.copy(
                    monto = Dinero.centavosAEntrada(i.montoCentavos),
                    concepto = i.concepto,
                    cuenta = i.cuenta,
                    fecha = i.fecha,
                    editable = Fechas.esHoy(i.fechaRegistro),
                    titulo = "Editar Ingreso",
                )
            }
        }
    }

    fun onMontoChange(v: String) = _state.update { it.copy(monto = v) }
    fun onConceptoChange(v: String) = _state.update { it.copy(concepto = v) }
    fun onCuentaChange(v: String) = _state.update { it.copy(cuenta = v) }
    fun onFechaChange(v: Long) = _state.update { it.copy(fecha = v) }

    fun guardar() {
        if (_state.value.procesando) return
        val s = _state.value
        val montoCentavos = Dinero.parsearACentavos(s.monto)

        _state.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val cuenta = s.cuenta ?: "Efectivo"
            val resultado = if (esEdicion) {
                editarIngreso(ingresoId!!, montoCentavos, s.concepto, cuenta, s.fecha)
            } else {
                registrarIngreso(montoCentavos, s.concepto, cuenta, s.fecha)
            }
            when (resultado) {
                is IngresoResultado.Exito -> _state.update { it.copy(guardadoOk = true) }
                is IngresoResultado.Invalido ->
                    _state.update { it.copy(errores = resultado.errores, procesando = false) }
            }
        }
    }
}
