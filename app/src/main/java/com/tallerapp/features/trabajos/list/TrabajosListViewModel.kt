package com.tallerapp.features.trabajos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.usecase.ObservarTrabajosUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Estado de la lista de trabajos con búsqueda reactiva por patente/cliente (RN-10).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrabajosListViewModel(
    observarTrabajos: ObservarTrabajosUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val trabajos: StateFlow<List<Trabajo>> =
        _query
            .flatMapLatest { observarTrabajos(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(nuevo: String) {
        _query.value = nuevo
    }
}
