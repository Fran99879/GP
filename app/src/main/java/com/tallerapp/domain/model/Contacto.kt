package com.tallerapp.domain.model

/** Para qué sirve un contacto. "ambos" aparece en las dos listas. */
object TipoContacto {
    const val CLIENTE = "cliente"
    const val PROVEEDOR = "proveedor"
    const val AMBOS = "ambos"

    val todos = listOf(CLIENTE, PROVEEDOR, AMBOS)

    fun etiqueta(tipo: String): String = when (tipo) {
        PROVEEDOR -> "Proveedor"
        AMBOS -> "Cliente y proveedor"
        else -> "Cliente"
    }

    fun icono(tipo: String): String = when (tipo) {
        PROVEEDOR -> "🚚"
        AMBOS -> "🔁"
        else -> "🧑"
    }
}

/**
 * Cliente o proveedor del negocio.
 *
 * Los datos de contacto son todos opcionales: en la práctica se carga un nombre al
 * facturar y el resto se completa después, o nunca.
 */
data class Contacto(
    val id: Long = 0,
    val nombre: String,
    val tipo: String = TipoContacto.CLIENTE,
    val documento: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val nota: String = "",
    val createdAt: Long = 0,
) {
    /** True si sirve para el listado de [tipo] pedido (los "ambos" sirven para los dos). */
    fun esDe(tipo: String): Boolean = this.tipo == tipo || this.tipo == TipoContacto.AMBOS

    /** Una línea con lo que haya cargado, para la ficha del listado. */
    val detalle: String
        get() = listOf(documento, telefono, email).filter { it.isNotBlank() }.joinToString(" · ")
}
