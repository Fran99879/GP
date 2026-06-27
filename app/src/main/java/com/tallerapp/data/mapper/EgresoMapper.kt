package com.tallerapp.data.mapper

import com.tallerapp.data.local.EgresoEntity
import com.tallerapp.domain.model.CategoriaEgreso
import com.tallerapp.domain.model.Egreso

fun EgresoEntity.toDomain(): Egreso = Egreso(
    id = id,
    montoCentavos = montoCentavos,
    categoria = CategoriaEgreso.valueOf(categoria),
    concepto = concepto,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
)

fun Egreso.toEntity(): EgresoEntity = EgresoEntity(
    id = id,
    montoCentavos = montoCentavos,
    categoria = categoria.name,
    concepto = concepto,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
)
