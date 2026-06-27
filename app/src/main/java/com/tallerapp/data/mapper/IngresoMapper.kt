package com.tallerapp.data.mapper

import com.tallerapp.data.local.IngresoEntity
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.model.RepartoPago

fun IngresoEntity.toDomain(): Ingreso {
    val metodoPago = MetodoPago.valueOf(metodo)
    return Ingreso(
        id = id,
        montoCentavos = montoCentavos,
        concepto = concepto,
        metodo = metodoPago,
        reparto = if (metodoPago == MetodoPago.PAGO_MIXTO) {
            RepartoPago(
                efectivoCentavos = repEfectivo ?: 0,
                transferenciaCentavos = repTransferencia ?: 0,
                tarjetaCentavos = repTarjeta ?: 0,
                mercadoPagoCentavos = repMercadoPago ?: 0,
            )
        } else null,
        fecha = fecha,
        fechaRegistro = fechaRegistro,
        origen = OrigenIngreso.valueOf(origen),
        trabajoId = trabajoId,
    )
}

fun Ingreso.toEntity(): IngresoEntity = IngresoEntity(
    id = id,
    montoCentavos = montoCentavos,
    concepto = concepto,
    metodo = metodo.name,
    repEfectivo = reparto?.efectivoCentavos,
    repTransferencia = reparto?.transferenciaCentavos,
    repTarjeta = reparto?.tarjetaCentavos,
    repMercadoPago = reparto?.mercadoPagoCentavos,
    fecha = fecha,
    fechaRegistro = fechaRegistro,
    origen = origen.name,
    trabajoId = trabajoId,
)
