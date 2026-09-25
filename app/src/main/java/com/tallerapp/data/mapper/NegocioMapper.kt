package com.tallerapp.data.mapper

import com.tallerapp.data.local.NegocioEntity
import com.tallerapp.domain.model.Negocio

fun NegocioEntity.toDomain(): Negocio = Negocio(
    id = id,
    nombre = nombre,
    createdAt = createdAt,
)

fun Negocio.toEntity(): NegocioEntity = NegocioEntity(
    id = id,
    nombre = nombre,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
)
