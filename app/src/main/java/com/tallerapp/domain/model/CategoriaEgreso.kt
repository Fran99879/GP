package com.tallerapp.domain.model

/** Categorías de gasto para finanzas personales. */
enum class CategoriaEgreso(val etiqueta: String) {
    ALIMENTOS("Alimentos"),
    TRANSPORTE("Transporte"),
    SERVICIOS("Servicios e impuestos"),
    HOGAR("Hogar"),
    SALUD("Salud"),
    OCIO("Ocio"),
    OTROS("Otros"),
}
