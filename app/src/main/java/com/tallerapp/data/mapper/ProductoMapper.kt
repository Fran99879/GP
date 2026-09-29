package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.ProductoEntity
import com.tallerapp.domain.model.Producto

fun ProductoEntity.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    codigoBarras = codigoBarras,
    descripcion = descripcion,
    precioCentavos = precioCentavos,
    descuentoPct = descuentoPct,
    stock = stock,
    stockMinimo = stockMinimo,
    imagen = imagen,
    createdAt = createdAt,
)

fun Producto.toEntity(): ProductoEntity = ProductoEntity(
    id = id,
    // El negocio no viaja en el modelo de dominio: lo pone el mapper, igual que en agenda.
    negocioId = NegocioActual.value,
    nombre = nombre,
    codigoBarras = codigoBarras,
    descripcion = descripcion,
    precioCentavos = precioCentavos,
    descuentoPct = descuentoPct,
    stock = stock,
    stockMinimo = stockMinimo,
    imagen = imagen,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
)
