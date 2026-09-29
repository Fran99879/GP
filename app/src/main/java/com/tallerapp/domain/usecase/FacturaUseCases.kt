package com.tallerapp.domain.usecase

import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.domain.model.Factura
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.model.Precios
import com.tallerapp.domain.repository.FacturaRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

/** Facturas del negocio activo, de la más reciente a la más vieja. */
class ObservarFacturasUseCase(private val repository: FacturaRepository) {
    operator fun invoke(): Flow<List<Factura>> = repository.observar()
}

/** Factura con sus ítems (para ver el detalle o reimprimir el PDF). */
class ObtenerFacturaUseCase(private val repository: FacturaRepository) {
    suspend operator fun invoke(id: Long): Factura? = repository.obtener(id)
}

/**
 * Próximo número sugerido: correlativo por negocio, con cuatro dígitos ("0001").
 * Es una sugerencia editable; el usuario puede llevar su propia numeración.
 */
class SiguienteNumeroFacturaUseCase(private val repository: FacturaRepository) {
    suspend operator fun invoke(): String =
        (repository.contar() + 1).toString().padStart(4, '0')
}

/** Resultado de emitir una factura. */
sealed interface ResultadoEmitirFactura {
    data class Emitida(val id: Long, val ingresoId: Long?) : ResultadoEmitirFactura

    /** Facturar es una función Pro (uso comercial, ver `PLANES.md`). */
    data object RequierePro : ResultadoEmitirFactura

    data object SinItems : ResultadoEmitirFactura
    data object SinCliente : ResultadoEmitirFactura
}

/**
 * Emite una factura: guarda cabecera e ítems, y según lo que pida el usuario descuenta
 * stock y registra el cobro como ingreso.
 *
 * Los dos efectos son opcionales a propósito: hay quien factura para imprimir nada más, y
 * hay quien todavía no inventarió el stock. Con los dos en false la factura es solo un
 * documento.
 *
 * El total se recalcula acá, no se confía en el que venga de la pantalla: el descuento
 * general se aplica sobre la suma de los ítems (que ya traen el suyo).
 */
class EmitirFacturaUseCase(
    private val facturas: FacturaRepository,
    private val productos: ProductoRepository,
    private val ingresos: IngresoRepository,
) {
    suspend operator fun invoke(
        factura: Factura,
        descontarStock: Boolean,
        registrarIngreso: Boolean,
    ): ResultadoEmitirFactura {
        if (!EstadoPlan.esPro) return ResultadoEmitirFactura.RequierePro
        if (factura.items.isEmpty()) return ResultadoEmitirFactura.SinItems
        if (factura.cliente.isBlank()) return ResultadoEmitirFactura.SinCliente

        val subtotal = factura.items.sumOf { it.subtotalCentavos }
        val total = Precios.conDescuento(subtotal, factura.descuentoPct)
        val aGuardar = factura.copy(
            cliente = factura.cliente.trim(),
            numero = factura.numero.trim(),
            subtotalCentavos = subtotal,
            totalCentavos = total,
        )

        val id = facturas.emitir(aGuardar)

        if (descontarStock) {
            factura.items.forEach { item ->
                item.productoId?.let { productos.descontarStock(it, item.cantidad) }
            }
        }

        val ingresoId = if (registrarIngreso && total > 0) {
            val concepto = buildString {
                append("Factura")
                if (aGuardar.numero.isNotBlank()) append(" N° ${aGuardar.numero}")
                append(" — ${aGuardar.cliente}")
            }
            ingresos.crear(
                Ingreso(
                    montoCentavos = total,
                    concepto = concepto,
                    cuenta = aGuardar.cuenta.ifBlank { "Efectivo" },
                    fecha = aGuardar.fecha,
                    fechaRegistro = System.currentTimeMillis(),
                    origen = OrigenIngreso.MANUAL,
                    trabajoId = null,
                ),
            ).also { facturas.asociarIngreso(id, it) }
        } else {
            null
        }

        return ResultadoEmitirFactura.Emitida(id, ingresoId)
    }
}

/**
 * Borra una factura y sus líneas. No revierte el stock ni borra el ingreso asociado:
 * eso ya pasó en la realidad y se corrige desde Movimientos si hace falta.
 */
class EliminarFacturaUseCase(private val repository: FacturaRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}
