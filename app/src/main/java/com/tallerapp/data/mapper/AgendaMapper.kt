package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.AgendaEntity
import com.tallerapp.domain.model.AgendaItem

fun AgendaEntity.toDomain(): AgendaItem = AgendaItem(
    id = id,
    tipo = tipo,
    titulo = titulo,
    descripcion = descripcion,
    fecha = fecha,
    hora = hora,
    hecho = hecho,
    createdAt = createdAt,
)

fun AgendaItem.toEntity(): AgendaEntity = AgendaEntity(
    id = id,
    tipo = tipo,
    titulo = titulo,
    descripcion = descripcion,
    fecha = fecha,
    hora = hora,
    hecho = hecho,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
    negocioId = NegocioActual.value,
)
