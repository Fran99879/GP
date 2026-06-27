package com.tallerapp.domain.rules

import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.EstadoReparacion.CANCELADO
import com.tallerapp.domain.model.EstadoReparacion.ENTREGADO
import com.tallerapp.domain.model.EstadoReparacion.EN_REPARACION
import com.tallerapp.domain.model.EstadoReparacion.ESPERANDO_REPUESTOS
import com.tallerapp.domain.model.EstadoReparacion.PENDIENTE
import com.tallerapp.domain.model.EstadoReparacion.TERMINADO

/**
 * Máquina de transiciones de estado de reparación (Frozen Spec 7.2/7.3).
 * Único lugar donde se decide qué transiciones son válidas (regla V-9).
 * La UI consulta este objeto y solo ofrece destinos válidos; nunca decide por su cuenta.
 */
object TransicionesTrabajo {

    private val transiciones: Map<EstadoReparacion, Set<EstadoReparacion>> = mapOf(
        PENDIENTE to setOf(EN_REPARACION, ESPERANDO_REPUESTOS, CANCELADO),
        EN_REPARACION to setOf(ESPERANDO_REPUESTOS, TERMINADO, CANCELADO),
        ESPERANDO_REPUESTOS to setOf(EN_REPARACION, TERMINADO, CANCELADO),
        TERMINADO to setOf(ENTREGADO, EN_REPARACION),
        ENTREGADO to emptySet(),   // el cobro se maneja aparte (Fase 4)
        CANCELADO to emptySet(),   // estado terminal
    )

    /** Estados a los que se puede pasar desde [actual]. */
    fun transicionesValidas(actual: EstadoReparacion): Set<EstadoReparacion> =
        transiciones[actual].orEmpty()

    /** Indica si la transición [origen] → [destino] está permitida. */
    fun puedeTransicionar(origen: EstadoReparacion, destino: EstadoReparacion): Boolean =
        destino in transicionesValidas(origen)
}
