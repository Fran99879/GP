package com.tallerapp.features.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Categoria
import com.tallerapp.domain.usecase.EliminarCategoriaUseCase
import com.tallerapp.domain.usecase.GuardarCategoriaUseCase
import com.tallerapp.domain.usecase.ObservarCategoriasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriasState(
    val egresos: List<Categoria> = emptyList(),
    val ingresos: List<Categoria> = emptyList(),
)

class CategoriasViewModel(
    private val observarCategorias: ObservarCategoriasUseCase,
    private val guardarCategoria: GuardarCategoriaUseCase,
    private val eliminarCategoria: EliminarCategoriaUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriasState())
    val state: StateFlow<CategoriasState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observarCategorias("egreso").collect { list -> _state.update { it.copy(egresos = list) } }
        }
        viewModelScope.launch {
            observarCategorias("ingreso").collect { list -> _state.update { it.copy(ingresos = list) } }
        }
    }

    fun guardar(cat: Categoria) = viewModelScope.launch { guardarCategoria(cat) }
    fun eliminar(id: Long) = viewModelScope.launch { eliminarCategoria(id) }
}
