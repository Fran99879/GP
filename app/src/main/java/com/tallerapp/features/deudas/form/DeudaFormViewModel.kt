package com.tallerapp.features.deudas.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.usecase.DeudaResultado
import com.tallerapp.domain.usecase.EditarDeudaUseCase
import com.tallerapp.domain.usecase.ObtenerDeudaUseCase
import com.tallerapp.domain.usecase.RegistrarDeudaUseCase
import com.tallerapp.domain.validation.DeudaErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeudaFormState(
    val nombre: String = "",
    val monto: String = "",
    val nota: String = "",
    val fecha: Long = Fechas.hoyInicioMillis(),
    val errores: DeudaErrores = DeudaErrores(),
    val titulo: String = "Nueva deuda",
    val procesando: Boolean = false,
    val guardadoOk: Boolean = false,
)

class DeudaFormViewModel(
    private val registrarDeuda: RegistrarDeudaUseCase,
    private val editarDeuda: EditarDeudaUseCase,
    private val obtenerDeuda: ObtenerDeudaUseCase,
    private val deudaId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(DeudaFormState())
    val state: StateFlow<DeudaFormState> = _state.asStateFlow()

    private val esEdicion: Boolean = deudaId != null

    init {
        if (deudaId != null) cargar(deudaId)
    }

    private fun cargar(id: Long) {
        viewModelScope.launch {
            val d = obtenerDeuda(id) ?: return@launch
            _state.update {
                it.copy(
                    nombre = d.nombre,
                    monto = Dinero.centavosAEntrada(d.montoCentavos),
                    nota = d.nota,
                    fecha = d.fecha,
                    titulo = "Editar deuda",
                )
            }
        }
    }

    fun onNombreChange(v: String) = _state.update { it.copy(nombre = v) }
    fun onMontoChange(v: String) = _state.update { it.copy(monto = v) }
    fun onNotaChange(v: String) = _state.update { it.copy(nota = v) }
    fun onFechaChange(v: Long) = _state.update { it.copy(fecha = v) }

    fun guardar() {
        if (_state.value.procesando) return
        val s = _state.value
        val montoCentavos = Dinero.parsearACentavos(s.monto)
        _state.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val resultado = if (esEdicion) {
                editarDeuda(deudaId!!, s.nombre, montoCentavos, s.fecha, s.nota)
            } else {
                registrarDeuda(s.nombre, montoCentavos, s.fecha, s.nota)
            }
            when (resultado) {
                is DeudaResultado.Exito -> _state.update { it.copy(guardadoOk = true) }
                is DeudaResultado.Invalido ->
                    _state.update { it.copy(errores = resultado.errores, procesando = false) }
            }
        }
    }
}
