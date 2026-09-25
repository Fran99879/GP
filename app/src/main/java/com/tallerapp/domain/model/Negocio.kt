package com.tallerapp.domain.model

/** Un negocio/tienda del usuario (o "Personal"). */
data class Negocio(
    val id: Long = 0,
    val nombre: String,
    val createdAt: Long = 0,
)
