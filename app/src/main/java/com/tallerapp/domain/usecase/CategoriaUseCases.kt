package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Categoria
import com.tallerapp.domain.repository.CategoriaRepository
import kotlinx.coroutines.flow.Flow

/** Observa las categorías activas de un tipo ("ingreso" | "egreso"). */
class ObservarCategoriasUseCase(private val repository: CategoriaRepository) {
    operator fun invoke(tipo: String): Flow<List<Categoria>> = repository.observar(tipo)
}

/** Crea o actualiza una categoría según tenga id. */
class GuardarCategoriaUseCase(private val repository: CategoriaRepository) {
    suspend operator fun invoke(categoria: Categoria) {
        if (categoria.id == 0L) repository.crear(categoria) else repository.actualizar(categoria)
    }
}

/** Elimina una categoría (reasigna sus movimientos a "Otros"). */
class EliminarCategoriaUseCase(private val repository: CategoriaRepository) {
    suspend operator fun invoke(id: Long): Boolean = repository.eliminar(id)
}
