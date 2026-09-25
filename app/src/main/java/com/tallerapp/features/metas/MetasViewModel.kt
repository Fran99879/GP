package com.tallerapp.features.metas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Meta
import com.tallerapp.domain.usecase.EliminarMetaUseCase
import com.tallerapp.domain.usecase.GuardarMetaUseCase
import com.tallerapp.domain.usecase.ObservarMetasUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetasViewModel(
    observarMetas: ObservarMetasUseCase,
    private val guardarMeta: GuardarMetaUseCase,
    private val eliminarMeta: EliminarMetaUseCase,
) : ViewModel() {

    val metas: StateFlow<List<Meta>> =
        observarMetas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun guardar(meta: Meta) = viewModelScope.launch { guardarMeta(meta) }
    fun eliminar(id: Long) = viewModelScope.launch { eliminarMeta(id) }
}
