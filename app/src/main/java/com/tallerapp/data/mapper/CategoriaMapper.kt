package com.tallerapp.data.mapper

import com.tallerapp.data.local.CategoriaEntity
import com.tallerapp.domain.model.Categoria

fun CategoriaEntity.toDomain(): Categoria = Categoria(
    id = id,
    tipo = tipo,
    nombre = nombre,
    color = color,
    icono = icono,
    orden = orden,
    activo = activo,
    presupuestoCentavos = presupuestoCentavos,
)

fun Categoria.toEntity(): CategoriaEntity = CategoriaEntity(
    id = id,
    tipo = tipo,
    nombre = nombre,
    color = color,
    icono = icono,
    orden = orden,
    activo = activo,
    presupuestoCentavos = presupuestoCentavos,
)
