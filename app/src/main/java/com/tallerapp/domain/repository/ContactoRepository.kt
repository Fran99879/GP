package com.tallerapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface ContactoRepository {
    fun observar(): Flow<List<String>>
    suspend fun agregar(nombre: String)
}
