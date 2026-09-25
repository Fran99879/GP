package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.DeudaEntity
import com.tallerapp.domain.model.Deuda

fun DeudaEntity.toDomain(): Deuda = Deuda(
    id = id,
    nombre = nombre,
    montoCentavos = montoCentavos,
    fecha = fecha,
    nota = nota,
    cobrada = cobrada,
    fechaCobro = fechaCobro,
    fechaRegistro = fechaRegistro,
    fechaLimite = fechaLimite,
)

fun Deuda.toEntity(): DeudaEntity = DeudaEntity(
    id = id,
    nombre = nombre,
    montoCentavos = montoCentavos,
    fecha = fecha,
    nota = nota,
    cobrada = cobrada,
    fechaCobro = fechaCobro,
    fechaRegistro = fechaRegistro,
    fechaLimite = fechaLimite,
    negocioId = NegocioActual.value,
)
