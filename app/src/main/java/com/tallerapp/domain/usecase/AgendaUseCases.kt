package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.AgendaItem
import com.tallerapp.domain.repository.AgendaRepository
import kotlinx.coroutines.flow.Flow

/** Entradas de agenda de un rango (normalmente el mes visible del calendario). */
class ObservarAgendaUseCase(private val repository: AgendaRepository) {
    operator fun invoke(inicio: Long, fin: Long): Flow<List<AgendaItem>> =
        repository.observarRango(inicio, fin)
}

class GuardarAgendaItemUseCase(private val repository: AgendaRepository) {
    suspend operator fun invoke(item: AgendaItem) {
        if (item.id == 0L) repository.crear(item) else repository.actualizar(item)
    }
}

class MarcarAgendaHechoUseCase(private val repository: AgendaRepository) {
    suspend operator fun invoke(id: Long, hecho: Boolean) = repository.marcarHecho(id, hecho)
}

class EliminarAgendaItemUseCase(private val repository: AgendaRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}
