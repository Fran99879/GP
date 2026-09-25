package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Negocio
import kotlinx.coroutines.flow.Flow

interface NegocioRepository {
    fun observar(): Flow<List<Negocio>>
    suspend fun crear(nombre: String): Long
    suspend fun renombrar(id: Long, nombre: String)

    /** Crea el negocio por defecto ("Personal") si la tabla está vacía (instalación nueva). */
    suspend fun asegurarInicial()
}
