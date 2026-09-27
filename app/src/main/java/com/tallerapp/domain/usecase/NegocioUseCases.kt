package com.tallerapp.domain.usecase

import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.core.billing.LimitesPlan
import com.tallerapp.domain.model.Negocio
import com.tallerapp.domain.repository.NegocioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ObservarNegociosUseCase(private val repository: NegocioRepository) {
    operator fun invoke(): Flow<List<Negocio>> = repository.observar()
}

/** Resultado de intentar crear un negocio. */
sealed interface ResultadoCrearNegocio {
    data class Creado(val id: Long) : ResultadoCrearNegocio

    /** El plan Gratis ya llegó a su límite de negocios. */
    data object RequierePro : ResultadoCrearNegocio
}

/**
 * Crea un negocio. El plan Gratis admite [LimitesPlan.NEGOCIOS_GRATIS]; a partir de ahí
 * hace falta Pro (ver `PLANES.md`: uso personal gratis, uso comercial pago).
 */
class CrearNegocioUseCase(
    private val repository: NegocioRepository,
    private val observarNegocios: ObservarNegociosUseCase,
) {
    suspend operator fun invoke(nombre: String): ResultadoCrearNegocio {
        val cuantos = observarNegocios().first().size
        if (!EstadoPlan.esPro && cuantos >= LimitesPlan.NEGOCIOS_GRATIS) {
            return ResultadoCrearNegocio.RequierePro
        }
        return ResultadoCrearNegocio.Creado(repository.crear(nombre.trim()))
    }
}

class RenombrarNegocioUseCase(private val repository: NegocioRepository) {
    suspend operator fun invoke(id: Long, nombre: String) = repository.renombrar(id, nombre.trim())
}

class AsegurarNegocioInicialUseCase(private val repository: NegocioRepository) {
    suspend operator fun invoke() = repository.asegurarInicial()
}

/**
 * Elimina un negocio con todos sus datos. Nunca borra el último que queda:
 * la app siempre necesita al menos un negocio donde registrar.
 * Devuelve true si lo eliminó.
 */
class EliminarNegocioUseCase(
    private val repository: NegocioRepository,
    private val observarNegocios: ObservarNegociosUseCase,
) {
    suspend operator fun invoke(id: Long): Boolean {
        val negocios = observarNegocios().first()
        if (negocios.size <= 1) return false
        repository.eliminar(id)
        return true
    }
}
