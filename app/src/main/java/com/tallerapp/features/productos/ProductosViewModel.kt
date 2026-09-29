package com.tallerapp.features.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Producto
import com.tallerapp.domain.usecase.EliminarProductoUseCase
import com.tallerapp.domain.usecase.GuardarProductoUseCase
import com.tallerapp.domain.usecase.ObservarProductosUseCase
import com.tallerapp.domain.usecase.ProductoPorCodigoUseCase
import com.tallerapp.domain.usecase.ResultadoGuardarProducto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Avisos de una pantalla de productos, para mostrar en un diálogo o snackbar. */
sealed interface AvisoProducto {
    /** Guardar catálogo pide Pro. */
    data object RequierePro : AvisoProducto

    /** El código ya estaba en otro producto del negocio. */
    data class CodigoRepetido(val nombre: String) : AvisoProducto

    data class Error(val mensaje: String) : AvisoProducto

    /** Se escaneó un código que ya está cargado: la lista salta a ese producto. */
    data class YaExiste(val producto: Producto) : AvisoProducto
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductosViewModel(
    observarProductos: ObservarProductosUseCase,
    private val guardarProducto: GuardarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val productoPorCodigo: ProductoPorCodigoUseCase,
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    /** El catálogo se rearma con cada tecla de la búsqueda y con cada cambio de negocio. */
    val productos: StateFlow<List<Producto>> = _busqueda
        .flatMapLatest { texto -> observarProductos(texto) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _aviso = MutableStateFlow<AvisoProducto?>(null)
    val aviso: StateFlow<AvisoProducto?> = _aviso.asStateFlow()

    /** Producto que la pantalla debe abrir para editar (lo setea el escaneo). */
    private val _abrirProducto = MutableStateFlow<Producto?>(null)
    val abrirProducto: StateFlow<Producto?> = _abrirProducto.asStateFlow()

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    /**
     * Resuelve un código escaneado desde el listado: si ya existe, avisa y ofrece abrirlo;
     * si no, deja el código en la búsqueda para que el formulario lo arranque cargado.
     */
    fun escaneoEnListado(codigo: String) = viewModelScope.launch {
        val existente = productoPorCodigo(codigo)
        if (existente != null) {
            _aviso.value = AvisoProducto.YaExiste(existente)
        } else {
            _busqueda.value = codigo
        }
    }

    fun abrir(producto: Producto?) {
        _abrirProducto.value = producto
    }

    fun guardar(producto: Producto, onGuardado: () -> Unit = {}) = viewModelScope.launch {
        when (val r = guardarProducto(producto)) {
            is ResultadoGuardarProducto.Guardado -> onGuardado()
            ResultadoGuardarProducto.RequierePro -> _aviso.value = AvisoProducto.RequierePro
            is ResultadoGuardarProducto.CodigoRepetido ->
                _aviso.value = AvisoProducto.CodigoRepetido(r.existente.nombre)
            ResultadoGuardarProducto.NombreVacio ->
                _aviso.value = AvisoProducto.Error("Ponele un nombre al producto.")
        }
    }

    fun eliminar(producto: Producto) = viewModelScope.launch {
        eliminarProducto(producto.id)
    }

    fun descartarAviso() {
        _aviso.value = null
    }
}
