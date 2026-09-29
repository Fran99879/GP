package com.tallerapp.features.contactos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Contacto
import com.tallerapp.domain.usecase.EliminarContactoUseCase
import com.tallerapp.domain.usecase.GuardarContactoUseCase
import com.tallerapp.domain.usecase.ObservarContactosPorTipoUseCase
import com.tallerapp.domain.usecase.ResultadoGuardarContacto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que la pantalla de contactos tiene que avisar. */
sealed interface AvisoContacto {
    /** La ficha completa es Pro. */
    data object RequierePro : AvisoContacto

    data class NombreRepetido(val existente: Contacto) : AvisoContacto

    data class Error(val mensaje: String) : AvisoContacto
}

/**
 * Clientes o proveedores, según el [tipo] con el que se construya: la pantalla es la misma
 * y el listado cambia de fuente. Los contactos marcados como "ambos" aparecen en las dos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContactosViewModel(
    val tipo: String,
    observarContactos: ObservarContactosPorTipoUseCase,
    private val guardarContacto: GuardarContactoUseCase,
    private val eliminarContacto: EliminarContactoUseCase,
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val contactos: StateFlow<List<Contacto>> = _busqueda
        .flatMapLatest { texto -> observarContactos(tipo, texto) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _aviso = MutableStateFlow<AvisoContacto?>(null)
    val aviso: StateFlow<AvisoContacto?> = _aviso.asStateFlow()

    /** Contacto abierto para editar; null cuando no hay ficha en pantalla. */
    private val _abierto = MutableStateFlow<Contacto?>(null)
    val abierto: StateFlow<Contacto?> = _abierto.asStateFlow()

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    fun abrir(contacto: Contacto?) {
        _abierto.value = contacto
    }

    fun guardar(contacto: Contacto, onGuardado: () -> Unit = {}) = viewModelScope.launch {
        when (val r = guardarContacto(contacto)) {
            is ResultadoGuardarContacto.Guardado -> onGuardado()
            ResultadoGuardarContacto.RequierePro -> _aviso.value = AvisoContacto.RequierePro
            ResultadoGuardarContacto.NombreVacio ->
                _aviso.value = AvisoContacto.Error("Ponele un nombre al contacto.")
            is ResultadoGuardarContacto.NombreRepetido ->
                _aviso.value = AvisoContacto.NombreRepetido(r.existente)
        }
    }

    fun eliminar(contacto: Contacto) = viewModelScope.launch {
        eliminarContacto(contacto.id)
    }

    fun descartarAviso() {
        _aviso.value = null
    }
}
