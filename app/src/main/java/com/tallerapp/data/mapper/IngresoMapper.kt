package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.IngresoEntity
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.OrigenIngreso

fun IngresoEntity.toDomain(): Ingreso = Ingreso(
    id = id,
    montoCentavos = montoCentavos,
    concepto = concepto,
    cuenta = cuenta,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
    origen = OrigenIngreso.valueOf(origen),
    trabajoId = trabajoId,
)

fun Ingreso.toEntity(): IngresoEntity = IngresoEntity(
    id = id,
    montoCentavos = montoCentavos,
    concepto = concepto,
    cuenta = cuenta,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
    origen = origen.name,
    trabajoId = trabajoId,
    negocioId = NegocioActual.value,
)
