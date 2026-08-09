package com.tallerapp.features.deudas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.usecase.EliminarDeudaUseCase
import com.tallerapp.domain.usecase.MarcarDeudaCobradaUseCase
import com.tallerapp.domain.usecase.ObservarDeudasUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista de deudas a favor ("quién te debe"), con marcar cobrada y eliminar. */
class DeudasViewModel(
    observarDeudas: ObservarDeudasUseCase,
    private val marcarCobradaUC: MarcarDeudaCobradaUseCase,
    private val eliminarUC: EliminarDeudaUseCase,
) : ViewModel() {

    val deudas: StateFlow<List<Deuda>> =
        observarDeudas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun marcarCobrada(id: Long, cobrada: Boolean) =
        viewModelScope.launch { marcarCobradaUC(id, cobrada) }

    fun eliminar(id: Long) = viewModelScope.launch { eliminarUC(id) }
}
