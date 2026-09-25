package com.tallerapp.domain.model

/** Categoría personalizable (con color e ícono). tipo = "ingreso" | "egreso". */
data class Categoria(
    val id: Long = 0,
    val tipo: String = "egreso",
    val nombre: String,
    val color: String = "#8A8D91",
    val icono: String = "📦",
    val orden: Int = 0,
    val activo: Boolean = true,
    val presupuestoCentavos: Long = 0,
) {
    val display: String get() = "$icono  $nombre"
}
