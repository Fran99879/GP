package com.tallerapp.features.recurrentes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Categoria
import com.tallerapp.domain.model.Cuenta
import com.tallerapp.domain.model.Recurrente
import com.tallerapp.domain.usecase.EliminarRecurrenteUseCase
import com.tallerapp.domain.usecase.GuardarRecurrenteUseCase
import com.tallerapp.domain.usecase.ObservarCategoriasUseCase
import com.tallerapp.domain.usecase.ObservarCuentasUseCase
import com.tallerapp.domain.usecase.ObservarRecurrentesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurrentesViewModel(
    observarRecurrentes: ObservarRecurrentesUseCase,
    observarCategorias: ObservarCategoriasUseCase,
    observarCuentas: ObservarCuentasUseCase,
    private val guardarRecurrente: GuardarRecurrenteUseCase,
    private val eliminarRecurrente: EliminarRecurrenteUseCase,
) : ViewModel() {

    val recurrentes: StateFlow<List<Recurrente>> =
        observarRecurrentes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoriasEgreso: StateFlow<List<Categoria>> =
        observarCategorias("egreso").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cuentas: StateFlow<List<Cuenta>> =
        observarCuentas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun guardar(rec: Recurrente) = viewModelScope.launch { guardarRecurrente(rec) }
    fun eliminar(id: Long) = viewModelScope.launch { eliminarRecurrente(id) }
}
