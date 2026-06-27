package com.tallerapp.data.mapper

import com.tallerapp.data.local.TrabajoEntity
import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.domain.model.Trabajo

/** Conversión entre la entidad de dominio y el registro persistible. */

fun TrabajoEntity.toDomain(): Trabajo = Trabajo(
    id = id,
    cliente = cliente,
    telefono = telefono,
    patente = patente,
    marca = marca,
    modelo = modelo,
    servicio = ServicioRealizado.valueOf(servicio),
    fechaIngreso = fechaIngreso,
    estadoReparacion = EstadoReparacion.valueOf(estadoReparacion),
    estadoCobro = EstadoCobro.valueOf(estadoCobro),
    problema = problema,
    diagnostico = diagnostico,
    precioCentavos = precioCentavos,
    fechaEntrega = fechaEntrega,
    cobroId = cobroId,
)

fun Trabajo.toEntity(): TrabajoEntity = TrabajoEntity(
    id = id,
    cliente = cliente,
    telefono = telefono,
    patente = patente,
    marca = marca,
    modelo = modelo,
    servicio = servicio.name,
    fechaIngreso = fechaIngreso,
    estadoReparacion = estadoReparacion.name,
    estadoCobro = estadoCobro.name,
    problema = problema,
    diagnostico = diagnostico,
    precioCentavos = precioCentavos,
    fechaEntrega = fechaEntrega,
    cobroId = cobroId,
)
