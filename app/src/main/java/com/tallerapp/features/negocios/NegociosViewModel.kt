package com.tallerapp.features.negocios

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.NegocioActual
import com.tallerapp.domain.model.Negocio
import com.tallerapp.domain.usecase.CrearNegocioUseCase
import com.tallerapp.domain.usecase.EliminarNegocioUseCase
import com.tallerapp.domain.usecase.ObservarNegociosUseCase
import com.tallerapp.domain.usecase.RenombrarNegocioUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NegociosViewModel(
    observarNegocios: ObservarNegociosUseCase,
    private val crearNegocio: CrearNegocioUseCase,
    private val renombrarNegocio: RenombrarNegocioUseCase,
    private val eliminarNegocio: EliminarNegocioUseCase,
) : ViewModel() {

    val negocios: StateFlow<List<Negocio>> = observarNegocios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Id del negocio actualmente seleccionado (compartido por toda la app). */
    val actual: StateFlow<Long> = NegocioActual.id

    fun crear(nombre: String) = viewModelScope.launch { crearNegocio(nombre) }
    fun renombrar(id: Long, nombre: String) = viewModelScope.launch { renombrarNegocio(id, nombre) }

    /**
     * Elimina un negocio con todos sus datos. Si era el activo, pasa al primero que quede
     * para que la app nunca apunte a un negocio inexistente.
     */
    fun eliminar(context: Context, id: Long) = viewModelScope.launch {
        val eraElActivo = NegocioActual.value == id
        if (!eliminarNegocio(id)) return@launch
        if (eraElActivo) {
            negocios.value.firstOrNull { it.id != id }?.let { NegocioActual.set(context, it.id) }
        }
    }
}
