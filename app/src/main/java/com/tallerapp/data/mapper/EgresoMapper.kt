package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.EgresoEntity
import com.tallerapp.domain.model.Egreso

fun EgresoEntity.toDomain(): Egreso = Egreso(
    id = id,
    montoCentavos = montoCentavos,
    categoria = categoria,
    concepto = concepto,
    cuenta = cuenta,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
)

fun Egreso.toEntity(): EgresoEntity = EgresoEntity(
    id = id,
    montoCentavos = montoCentavos,
    categoria = categoria,
    concepto = concepto,
    cuenta = cuenta,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
    negocioId = NegocioActual.value,
)
