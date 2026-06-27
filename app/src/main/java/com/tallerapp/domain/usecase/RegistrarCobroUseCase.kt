package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.model.RepartoPago
import com.tallerapp.domain.repository.CobroRepository
import com.tallerapp.domain.repository.TrabajoRepository
import com.tallerapp.domain.validation.CobroErrores
import com.tallerapp.domain.validation.CobroValidator

/**
 * Registra el cobro de un trabajo entregado (Frozen Spec 9.3, RN-3).
 * - Solo si el estado de reparación es Entregado (V-5) y aún no fue cobrado.
 * - El monto es el precio del trabajo; genera un único ingreso con origen = Cobro.
 * - La operación (ingreso + marcar Cobrado) es atómica en el repositorio.
 */
class RegistrarCobroUseCase(
    private val trabajoRepository: TrabajoRepository,
    private val cobroRepository: CobroRepository,
) {

    suspend operator fun invoke(
        trabajoId: Long,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
    ): CobroResultado {
        val trabajo = trabajoRepository.obtener(trabajoId)
            ?: return CobroResultado.Invalido(CobroErrores(general = "El trabajo no existe"))

        if (trabajo.estadoReparacion != EstadoReparacion.ENTREGADO) {
            return CobroResultado.Invalido(
                CobroErrores(general = "El vehículo debe estar Entregado para cobrar"),
            )
        }
        if (trabajo.estadoCobro == EstadoCobro.COBRADO) {
            return CobroResultado.Invalido(CobroErrores(general = "El trabajo ya fue cobrado"))
        }

        val errores = CobroValidator.validar(trabajo.precioCentavos, metodo, reparto)
        if (!errores.esValido) return CobroResultado.Invalido(errores)

        val ingreso = Ingreso(
            id = 0,
            montoCentavos = trabajo.precioCentavos,
            concepto = "Cobro - ${trabajo.identificacion()}",
            metodo = metodo!!,
            reparto = if (metodo == MetodoPago.PAGO_MIXTO) reparto else null,
            fecha = Fechas.hoyInicioMillis(),
            fechaRegistro = System.currentTimeMillis(),
            origen = OrigenIngreso.COBRO_DE_TRABAJO,
            trabajoId = trabajo.id,
        )
        return CobroResultado.Exito(cobroRepository.registrarCobro(ingreso, trabajo.id))
    }
}
