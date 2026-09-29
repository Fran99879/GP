package com.tallerapp.data.mapper

import com.tallerapp.core.NegocioActual
import com.tallerapp.data.local.ContactoEntity
import com.tallerapp.domain.model.Contacto

fun ContactoEntity.toDomain(): Contacto = Contacto(
    id = id,
    nombre = nombre,
    tipo = tipo,
    documento = documento,
    telefono = telefono,
    email = email,
    direccion = direccion,
    nota = nota,
    createdAt = createdAt,
)

fun Contacto.toEntity(): ContactoEntity = ContactoEntity(
    id = id,
    // El negocio no viaja en el modelo: lo pone el mapper, igual que en productos y agenda.
    negocioId = NegocioActual.value,
    nombre = nombre,
    tipo = tipo,
    documento = documento,
    telefono = telefono,
    email = email,
    direccion = direccion,
    nota = nota,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
)
