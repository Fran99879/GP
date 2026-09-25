package com.tallerapp.data.mapper

import com.tallerapp.data.local.CuentaEntity
import com.tallerapp.domain.model.Cuenta

fun CuentaEntity.toDomain(): Cuenta = Cuenta(
    id = id,
    nombre = nombre,
    icono = icono,
    saldoInicialCentavos = saldoInicialCentavos,
    orden = orden,
    activo = activo,
)

fun Cuenta.toEntity(): CuentaEntity = CuentaEntity(
    id = id,
    nombre = nombre,
    icono = icono,
    saldoInicialCentavos = saldoInicialCentavos,
    orden = orden,
    activo = activo,
)
