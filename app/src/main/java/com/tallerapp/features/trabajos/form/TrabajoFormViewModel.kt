package com.tallerapp.features.trabajos.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.domain.usecase.CrearTrabajoUseCase
import com.tallerapp.domain.usecase.EditarTrabajoUseCase
import com.tallerapp.domain.usecase.GuardarResultado
import com.tallerapp.domain.usecase.ObtenerTrabajoUseCase
import com.tallerapp.domain.validation.TrabajoErrores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del formulario de alta/edición de trabajo. */
data class TrabajoFormState(
    val cliente: String = "",
    val telefono: String = "",
    val patente: String = "",
    val marca: String = "",
    val modelo: String = "",
    val servicio: ServicioRealizado? = null,
    val problema: String = "",
    val diagnostico: String = "",
    val precio: String = "",
    val precioEditable: Boolean = true,
    val errores: TrabajoErrores = TrabajoErrores(),
    val titulo: String = "Nuevo Trabajo",
    val procesando: Boolean = false,
    val guardadoId: Long? = null,
)

/**
 * ViewModel del formulario. Sirve tanto para alta (trabajoId = null) como para edición.
 * Toda la validación ocurre en los casos de uso; aquí solo se reflejan los errores.
 */
class TrabajoFormViewModel(
    private val crearTrabajo: CrearTrabajoUseCase,
    private val editarTrabajo: EditarTrabajoUseCase,
    private val obtenerTrabajo: ObtenerTrabajoUseCase,
    private val trabajoId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(TrabajoFormState())
    val state: StateFlow<TrabajoFormState> = _state.asStateFlow()

    private val esEdicion: Boolean = trabajoId != null

    init {
        if (trabajoId != null) cargar(trabajoId)
    }

    private fun cargar(id: Long) {
        viewModelScope.launch {
            val t = obtenerTrabajo(id) ?: return@launch
            _state.update {
                it.copy(
                    cliente = t.cliente,
                    telefono = t.telefono.orEmpty(),
                    patente = t.patente.orEmpty(),
                    marca = t.marca,
                    modelo = t.modelo,
                    servicio = t.servicio,
                    problema = t.problema.orEmpty(),
                    diagnostico = t.diagnostico.orEmpty(),
                    precio = Dinero.centavosAEntrada(t.precioCentavos),
                    precioEditable = t.estadoCobro != EstadoCobro.COBRADO,
                    titulo = "Editar Trabajo",
                )
            }
        }
    }

    fun onClienteChange(v: String) = _state.update { it.copy(cliente = v) }
    fun onTelefonoChange(v: String) = _state.update { it.copy(telefono = v) }
    fun onPatenteChange(v: String) = _state.update { it.copy(patente = v) }
    fun onMarcaChange(v: String) = _state.update { it.copy(marca = v) }
    fun onModeloChange(v: String) = _state.update { it.copy(modelo = v) }
    fun onServicioChange(v: ServicioRealizado) = _state.update { it.copy(servicio = v) }
    fun onProblemaChange(v: String) = _state.update { it.copy(problema = v) }
    fun onDiagnosticoChange(v: String) = _state.update { it.copy(diagnostico = v) }
    fun onPrecioChange(v: String) = _state.update { it.copy(precio = v) }

    fun guardar() {
        if (_state.value.procesando) return
        val s = _state.value
        val precioCentavos = Dinero.parsearACentavos(s.precio)
        _state.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val resultado = if (esEdicion) {
                editarTrabajo(
                    id = trabajoId!!,
                    cliente = s.cliente,
                    telefono = s.telefono,
                    patente = s.patente,
                    marca = s.marca,
                    modelo = s.modelo,
                    servicio = s.servicio,
                    problema = s.problema,
                    diagnostico = s.diagnostico,
                    precioCentavos = precioCentavos,
                )
            } else {
                crearTrabajo(
                    cliente = s.cliente,
                    telefono = s.telefono,
                    patente = s.patente,
                    marca = s.marca,
                    modelo = s.modelo,
                    servicio = s.servicio,
                    problema = s.problema,
                    diagnostico = s.diagnostico,
                    precioCentavos = precioCentavos,
                )
            }
            when (resultado) {
                is GuardarResultado.Exito -> _state.update { it.copy(guardadoId = resultado.id) }
                is GuardarResultado.Invalido ->
                    _state.update { it.copy(errores = resultado.errores, procesando = false) }
            }
        }
    }
}
