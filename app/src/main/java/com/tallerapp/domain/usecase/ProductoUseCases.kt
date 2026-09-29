package com.tallerapp.domain.usecase

import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.domain.model.Producto
import com.tallerapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

/** Catálogo del negocio activo, filtrado por el texto de búsqueda (vacío = todo). */
class ObservarProductosUseCase(private val repository: ProductoRepository) {
    operator fun invoke(busqueda: String = ""): Flow<List<Producto>> = repository.buscar(busqueda)
}

class ObtenerProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Producto? = repository.obtener(id)
}

/** Resuelve un código escaneado: devuelve el producto ya cargado con ese código, o null. */
class ProductoPorCodigoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(codigo: String): Producto? =
        if (codigo.isBlank()) null else repository.porCodigo(codigo)
}

/** Resultado de guardar un producto. */
sealed interface ResultadoGuardarProducto {
    data class Guardado(val id: Long) : ResultadoGuardarProducto

    /** El catálogo es una función Pro (uso comercial, ver `PLANES.md`). */
    data object RequierePro : ResultadoGuardarProducto

    /** Ya existe otro producto con ese código de barras en el negocio. */
    data class CodigoRepetido(val existente: Producto) : ResultadoGuardarProducto

    data object NombreVacio : ResultadoGuardarProducto
}

/**
 * Crea o actualiza un producto.
 *
 * El código repetido se avisa, no se bloquea con un índice único: un escaneo en la cinta
 * no debe hacer fallar la operación, y el usuario decide si era el mismo artículo.
 */
class GuardarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(producto: Producto): ResultadoGuardarProducto {
        if (!EstadoPlan.esPro) return ResultadoGuardarProducto.RequierePro
        if (producto.nombre.isBlank()) return ResultadoGuardarProducto.NombreVacio

        val codigo = producto.codigoBarras.trim()
        if (codigo.isNotBlank()) {
            val existente = repository.porCodigo(codigo)
            if (existente != null && existente.id != producto.id) {
                return ResultadoGuardarProducto.CodigoRepetido(existente)
            }
        }

        val limpio = producto.copy(
            nombre = producto.nombre.trim(),
            codigoBarras = codigo,
            descripcion = producto.descripcion.trim(),
            descuentoPct = producto.descuentoPct.coerceIn(0.0, 100.0),
        )
        return if (limpio.id == 0L) {
            ResultadoGuardarProducto.Guardado(repository.crear(limpio))
        } else {
            repository.actualizar(limpio)
            ResultadoGuardarProducto.Guardado(limpio.id)
        }
    }
}

class EliminarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}
