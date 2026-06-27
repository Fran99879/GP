package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.repository.TrabajoRepository
import com.tallerapp.domain.validation.TrabajoValidator

/**
 * Crea un trabajo nuevo (Frozen Spec 9.1). Valida (V-1), normaliza la patente (V-4) e
 * inicializa estado de reparación = Pendiente y estado de cobro = Pendiente de cobro.
 */
class CrearTrabajoUseCase(private val repository: TrabajoRepository) {

    suspend operator fun invoke(
        cliente: String,
        telefono: String?,
        patente: String?,
        marca: String,
        modelo: String,
        servicio: ServicioRealizado?,
        problema: String?,
        diagnostico: String?,
        precioCentavos: Long?,
    ): GuardarResultado {
        val errores = TrabajoValidator.validar(cliente, marca, modelo, servicio, precioCentavos)
        if (!errores.esValido) return GuardarResultado.Invalido(errores)

        val trabajo = Trabajo(
            id = 0,
            cliente = cliente.trim(),
            telefono = telefono?.trim()?.ifBlank { null },
            patente = TrabajoValidator.normalizarPatente(patente),
            marca = marca.trim(),
            modelo = modelo.trim(),
            servicio = servicio!!,
            fechaIngreso = System.currentTimeMillis(),
            estadoReparacion = EstadoReparacion.PENDIENTE,
            estadoCobro = EstadoCobro.PENDIENTE_DE_COBRO,
            problema = problema?.trim()?.ifBlank { null },
            diagnostico = diagnostico?.trim()?.ifBlank { null },
            precioCentavos = precioCentavos!!,
            cobroId = null,
        )
        return GuardarResultado.Exito(repository.crear(trabajo))
    }
}
