package com.tallerapp.features.finanzas.egreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.CategoriaEgreso
import com.tallerapp.domain.usecase.EditarEgresoUseCase
import com.tallerapp.domain.usecase.EgresoResultado
import com.tallerapp.domain.usecase.ObtenerEgresoUseCase
import com.tallerapp.domain.usecase.RegistrarEgresoUseCase
import com.tallerapp.domain.validation.EgresoErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EgresoFormState(
    val monto: String = "",
    val categoria: CategoriaEgreso? = null,
    val concepto: String = "",
    val fecha: Long = Fechas.hoyInicioMillis(),
    val editable: Boolean = true,
    val errores: EgresoErrores = EgresoErrores(),
    val titulo: String = "Nuevo Gasto",
    val procesando: Boolean = false,
    val guardadoOk: Boolean = false,
)

class EgresoFormViewModel(
    private val registrarEgreso: RegistrarEgresoUseCase,
    private val editarEgreso: EditarEgresoUseCase,
    private val obtenerEgreso: ObtenerEgresoUseCase,
    private val egresoId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(EgresoFormState())
    val state: StateFlow<EgresoFormState> = _state.asStateFlow()

    private val esEdicion: Boolean = egresoId != null

    init {
        if (egresoId != null) cargar(egresoId)
    }

    private fun cargar(id: Long) {
        viewModelScope.launch {
            val e = obtenerEgreso(id) ?: return@launch
            _state.update {
                it.copy(
                    monto = Dinero.centavosAEntrada(e.montoCentavos),
                    categoria = e.categoria,
                    concepto = e.concepto,
                    fecha = e.fecha,
                    editable = Fechas.esHoy(e.fechaRegistro),
                    titulo = "Editar Gasto",
                )
            }
        }
    }

    fun onMontoChange(v: String) = _state.update { it.copy(monto = v) }
    fun onCategoriaChange(v: CategoriaEgreso) = _state.update { it.copy(categoria = v) }
    fun onConceptoChange(v: String) = _state.update { it.copy(concepto = v) }
    fun onFechaChange(v: Long) = _state.update { it.copy(fecha = v) }

    fun guardar() {
        if (_state.value.procesando) return
        val s = _state.value
        val montoCentavos = Dinero.parsearACentavos(s.monto)
        _state.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val resultado = if (esEdicion) {
                editarEgreso(egresoId!!, montoCentavos, s.categoria, s.concepto, s.fecha)
            } else {
                registrarEgreso(montoCentavos, s.categoria, s.concepto, s.fecha)
            }
            when (resultado) {
                is EgresoResultado.Exito -> _state.update { it.copy(guardadoOk = true) }
                is EgresoResultado.Invalido ->
                    _state.update { it.copy(errores = resultado.errores, procesando = false) }
            }
        }
    }
}
