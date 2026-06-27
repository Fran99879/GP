package com.tallerapp.domain.model

/** Categorías de egreso (Frozen Spec 5.6). */
enum class CategoriaEgreso(val etiqueta: String) {
    REPUESTOS("Repuestos"),
    HERRAMIENTAS("Herramientas"),
    COMBUSTIBLE("Combustible"),
    SERVICIOS_E_IMPUESTOS("Servicios e impuestos"),
    OTROS("Otros"),
}
