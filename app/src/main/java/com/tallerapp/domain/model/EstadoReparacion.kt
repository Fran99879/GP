package com.tallerapp.domain.model

/** Estados del ciclo de reparación (Frozen Spec 7.1). */
enum class EstadoReparacion(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_REPARACION("En reparación"),
    ESPERANDO_REPUESTOS("Esperando repuestos"),
    TERMINADO("Terminado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado"),
}
