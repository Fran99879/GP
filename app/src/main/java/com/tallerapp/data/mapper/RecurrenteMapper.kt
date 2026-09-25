package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.RecurrenteEntity
import com.tallerapp.domain.model.Recurrente

fun RecurrenteEntity.toDomain(): Recurrente = Recurrente(
    id = id, tipo = tipo, montoCentavos = montoCentavos, concepto = concepto,
    categoria = categoria, cuenta = cuenta, diaMes = diaMes, activo = activo,
    ultimoGenerado = ultimoGenerado, createdAt = createdAt,
)

fun Recurrente.toEntity(): RecurrenteEntity = RecurrenteEntity(
    id = id, tipo = tipo, montoCentavos = montoCentavos, concepto = concepto,
    categoria = categoria, cuenta = cuenta, diaMes = diaMes, activo = activo,
    ultimoGenerado = ultimoGenerado,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
    negocioId = NegocioActual.value,
)
