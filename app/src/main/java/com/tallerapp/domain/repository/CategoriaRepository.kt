package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Categoria
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de categorías personalizables. */
interface CategoriaRepository {
    fun observar(tipo: String): Flow<List<Categoria>>
    suspend fun listar(tipo: String): List<Categoria>
    suspend fun crear(categoria: Categoria): Long
    /** Actualiza; si cambió el nombre, propaga a los movimientos de ese tipo. */
    suspend fun actualizar(categoria: Categoria)
    /** Elimina y reasigna sus movimientos a "Otros". No borra "Otros". Devuelve false si no se pudo. */
    suspend fun eliminar(id: Long): Boolean
}
