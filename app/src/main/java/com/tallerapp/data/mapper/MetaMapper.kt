package com.tallerapp.data.mapper

import com.tallerapp.data.local.MetaEntity
import com.tallerapp.domain.model.Meta

fun MetaEntity.toDomain(): Meta = Meta(
    id = id,
    nombre = nombre,
    objetivoCentavos = objetivoCentavos,
    actualCentavos = actualCentavos,
    fechaObjetivo = fechaObjetivo,
    createdAt = createdAt,
)

fun Meta.toEntity(): MetaEntity = MetaEntity(
    id = id,
    nombre = nombre,
    objetivoCentavos = objetivoCentavos,
    actualCentavos = actualCentavos,
    fechaObjetivo = fechaObjetivo,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
)
