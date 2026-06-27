package com.tallerapp.domain.model

/** Lista cerrada de servicios (Frozen Spec 5.4). Alimenta el reporte de Fase 6. */
enum class ServicioRealizado(val etiqueta: String) {
    CAMBIO_DE_ACEITE("Cambio de aceite"),
    FRENOS("Frenos"),
    EMBRAGUE("Embrague"),
    DISTRIBUCION("Distribución"),
    ELECTRICIDAD("Electricidad"),
    OTROS("Otros"),
}
