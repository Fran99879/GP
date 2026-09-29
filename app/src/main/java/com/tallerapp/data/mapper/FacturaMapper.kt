package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.FacturaEntity
import com.tallerapp.data.local.FacturaItemEntity
import com.tallerapp.domain.model.Factura
import com.tallerapp.domain.model.ItemFactura

fun FacturaEntity.toDomain(items: List<ItemFactura> = emptyList()): Factura = Factura(
    id = id,
    numero = numero,
    cliente = cliente,
    documento = documento,
    fecha = fecha,
    descuentoPct = descuentoPct,
    subtotalCentavos = subtotalCentavos,
    totalCentavos = totalCentavos,
    notas = notas,
    cuenta = cuenta,
    ingresoId = ingresoId,
    createdAt = createdAt,
    items = items,
)

fun Factura.toEntity(): FacturaEntity = FacturaEntity(
    id = id,
    negocioId = NegocioActual.value,
    numero = numero,
    cliente = cliente,
    documento = documento,
    fecha = fecha,
    descuentoPct = descuentoPct,
    subtotalCentavos = subtotalCentavos,
    totalCentavos = totalCentavos,
    notas = notas,
    cuenta = cuenta,
    ingresoId = ingresoId,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
)

fun FacturaItemEntity.toDomain(): ItemFactura = ItemFactura(
    id = id,
    productoId = productoId,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitCentavos = precioUnitCentavos,
    descuentoPct = descuentoPct,
    subtotalCentavos = subtotalCentavos,
)

/** [facturaId] lo completa el DAO al emitir, cuando ya conoce el id de la cabecera. */
fun ItemFactura.toEntity(facturaId: Long = 0): FacturaItemEntity = FacturaItemEntity(
    id = id,
    facturaId = facturaId,
    productoId = productoId,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitCentavos = precioUnitCentavos,
    descuentoPct = descuentoPct,
    subtotalCentavos = subtotalCentavos,
)
