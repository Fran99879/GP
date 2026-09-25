package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Meta
import com.tallerapp.domain.repository.MetaRepository
import kotlinx.coroutines.flow.Flow

class ObservarMetasUseCase(private val repository: MetaRepository) {
    operator fun invoke(): Flow<List<Meta>> = repository.observar()
}

class GuardarMetaUseCase(private val repository: MetaRepository) {
    suspend operator fun invoke(meta: Meta) {
        if (meta.id == 0L) repository.crear(meta) else repository.actualizar(meta)
    }
}

class EliminarMetaUseCase(private val repository: MetaRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}
