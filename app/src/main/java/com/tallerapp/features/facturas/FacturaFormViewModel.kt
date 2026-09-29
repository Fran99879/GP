package com.tallerapp.features.facturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Cuenta
import com.tallerapp.domain.model.Factura
import com.tallerapp.domain.model.ItemFactura
import com.tallerapp.domain.model.Precios
import com.tallerapp.domain.model.Producto
import com.tallerapp.domain.usecase.EmitirFacturaUseCase
import com.tallerapp.domain.usecase.ObservarCuentasUseCase
import com.tallerapp.domain.usecase.ObservarProductosUseCase
import com.tallerapp.domain.usecase.ObtenerFacturaUseCase
import com.tallerapp.domain.usecase.ProductoPorCodigoUseCase
import com.tallerapp.domain.usecase.ResultadoEmitirFactura
import com.tallerapp.domain.usecase.SiguienteNumeroFacturaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que la pantalla de facturación tiene que contarle al usuario. */
sealed interface AvisoFactura {
    /** Facturar es una función Pro. */
    data object RequierePro : AvisoFactura

    data class Error(val mensaje: String) : AvisoFactura

    /** El código escaneado no está en el catálogo. */
    data class CodigoDesconocido(val codigo: String) : AvisoFactura
}

/**
 * Carga de una factura.
 *
 * Los ítems viven acá y no en la pantalla porque el escaneo los agrega desde una corrutina:
 * si el estado estuviera en la composición, un escaneo con la pantalla recompuesta perdería
 * la línea.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FacturaFormViewModel(
    observarProductos: ObservarProductosUseCase,
    observarCuentas: ObservarCuentasUseCase,
    private val emitirFactura: EmitirFacturaUseCase,
    private val obtenerFactura: ObtenerFacturaUseCase,
    private val productoPorCodigo: ProductoPorCodigoUseCase,
    private val siguienteNumero: SiguienteNumeroFacturaUseCase,
) : ViewModel() {

    private val _busquedaProducto = MutableStateFlow("")
    val busquedaProducto: StateFlow<String> = _busquedaProducto.asStateFlow()

    /** Catálogo filtrado, para el buscador que agrega líneas. */
    val productos: StateFlow<List<Producto>> = _busquedaProducto
        .flatMapLatest { texto -> observarProductos(texto) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cuentas: StateFlow<List<Cuenta>> = observarCuentas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _items = MutableStateFlow<List<ItemFactura>>(emptyList())
    val items: StateFlow<List<ItemFactura>> = _items.asStateFlow()

    private val _numero = MutableStateFlow("")
    val numero: StateFlow<String> = _numero.asStateFlow()

    private val _aviso = MutableStateFlow<AvisoFactura?>(null)
    val aviso: StateFlow<AvisoFactura?> = _aviso.asStateFlow()

    /** Factura ya emitida, con su id: la pantalla la usa para el PDF. */
    private val _emitida = MutableStateFlow<Factura?>(null)
    val emitida: StateFlow<Factura?> = _emitida.asStateFlow()

    init {
        viewModelScope.launch { _numero.value = siguienteNumero() }
    }

    fun buscarProducto(texto: String) {
        _busquedaProducto.value = texto
    }

    fun cambiarNumero(valor: String) {
        _numero.value = valor
    }

    /** Agrega el producto como línea, con su descuento propio ya puesto. */
    fun agregar(producto: Producto, cantidad: Double = 1.0) {
        val existente = _items.value.indexOfFirst { it.productoId == producto.id }
        if (existente >= 0) {
            // Escanear dos veces el mismo artículo suma cantidad, como en una caja.
            cambiarCantidad(existente, _items.value[existente].cantidad + cantidad)
            return
        }
        _items.value = _items.value + ItemFactura(
            productoId = producto.id,
            descripcion = producto.nombre,
            cantidad = cantidad,
            precioUnitCentavos = producto.precioCentavos,
            descuentoPct = producto.descuentoPct,
        )
    }

    /** Línea escrita a mano, para lo que no está en el catálogo (un servicio, un envío). */
    fun agregarManual(descripcion: String, cantidad: Double, precioCentavos: Long, descuentoPct: Double) {
        if (descripcion.isBlank() || precioCentavos <= 0) {
            _aviso.value = AvisoFactura.Error("La línea necesita descripción y precio.")
            return
        }
        _items.value = _items.value + ItemFactura(
            productoId = null,
            descripcion = descripcion.trim(),
            cantidad = cantidad,
            precioUnitCentavos = precioCentavos,
            descuentoPct = descuentoPct.coerceIn(0.0, 100.0),
        )
    }

    /** Resuelve un código escaneado contra el catálogo y lo agrega a la factura. */
    fun escanear(codigo: String) = viewModelScope.launch {
        val producto = productoPorCodigo(codigo)
        if (producto == null) {
            _aviso.value = AvisoFactura.CodigoDesconocido(codigo)
        } else {
            agregar(producto)
        }
    }

    fun cambiarCantidad(indice: Int, cantidad: Double) {
        val nueva = cantidad.coerceAtLeast(0.0)
        _items.value = _items.value.mapIndexed { i, item ->
            if (i != indice) item else recalcular(item.copy(cantidad = nueva))
        }
    }

    fun cambiarDescuentoItem(indice: Int, pct: Double) {
        _items.value = _items.value.mapIndexed { i, item ->
            if (i != indice) item else recalcular(item.copy(descuentoPct = pct.coerceIn(0.0, 100.0)))
        }
    }

    fun quitar(indice: Int) {
        _items.value = _items.value.filterIndexed { i, _ -> i != indice }
    }

    /**
     * Emite la factura. [descontarStock] y [registrarIngreso] son decisión del usuario:
     * con los dos en false la factura queda solo como documento para imprimir.
     */
    fun emitir(
        cliente: String,
        documento: String,
        fecha: Long,
        descuentoPct: Double,
        notas: String,
        cuenta: String,
        descontarStock: Boolean,
        registrarIngreso: Boolean,
    ) = viewModelScope.launch {
        val lineas = _items.value
        val subtotal = lineas.sumOf { it.subtotalCentavos }
        val borrador = Factura(
            numero = _numero.value,
            cliente = cliente,
            documento = documento,
            fecha = fecha,
            descuentoPct = descuentoPct.coerceIn(0.0, 100.0),
            subtotalCentavos = subtotal,
            totalCentavos = Precios.conDescuento(subtotal, descuentoPct),
            notas = notas,
            cuenta = cuenta,
            items = lineas,
        )

        when (val r = emitirFactura(borrador, descontarStock, registrarIngreso)) {
            is ResultadoEmitirFactura.Emitida ->
                // Se relee de la base: así la pantalla tiene los ids definitivos para el PDF.
                _emitida.value = obtenerFactura(r.id) ?: borrador.copy(id = r.id)
            ResultadoEmitirFactura.RequierePro -> _aviso.value = AvisoFactura.RequierePro
            ResultadoEmitirFactura.SinItems ->
                _aviso.value = AvisoFactura.Error("Agregá al menos un producto o una línea.")
            ResultadoEmitirFactura.SinCliente ->
                _aviso.value = AvisoFactura.Error("Poné el nombre del cliente.")
        }
    }

    fun descartarAviso() {
        _aviso.value = null
    }

    /** El subtotal guardado en la línea se recalcula al tocar cantidad o descuento. */
    private fun recalcular(item: ItemFactura): ItemFactura = item.copy(
        subtotalCentavos = Precios.subtotal(item.cantidad, item.precioUnitCentavos, item.descuentoPct),
    )
}
