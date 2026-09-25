package com.tallerapp.features.negocios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.core.NegocioActual
import com.tallerapp.domain.model.Negocio
import com.tallerapp.domain.usecase.CrearNegocioUseCase
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
) : ViewModel() {

    val negocios: StateFlow<List<Negocio>> = observarNegocios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Id del negocio actualmente seleccionado (compartido por toda la app). */
    val actual: StateFlow<Long> = NegocioActual.id

    fun crear(nombre: String) = viewModelScope.launch { crearNegocio(nombre) }
    fun renombrar(id: Long, nombre: String) = viewModelScope.launch { renombrarNegocio(id, nombre) }
}
