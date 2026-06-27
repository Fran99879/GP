package com.tallerapp.domain.validation

import com.tallerapp.domain.model.ServicioRealizado

/** Errores de validación por campo del formulario de trabajo (Frozen Spec V-1). */
data class TrabajoErrores(
    val cliente: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val servicio: String? = null,
    val precio: String? = null,
) {
    val esValido: Boolean
        get() = listOf(cliente, marca, modelo, servicio, precio).all { it == null }
}

/**
 * Validaciones funcionales del trabajo. Reglas puras de dominio aplicadas en los
 * casos de uso (no solo en la UI), de modo que ninguna escritura las eluda.
 */
object TrabajoValidator {

    fun validar(
        cliente: String,
        marca: String,
        modelo: String,
        servicio: ServicioRealizado?,
        precioCentavos: Long?,
    ): TrabajoErrores = TrabajoErrores(
        cliente = if (cliente.isBlank()) "Ingresá el cliente" else null,
        marca = if (marca.isBlank()) "Ingresá la marca" else null,
        modelo = if (modelo.isBlank()) "Ingresá el modelo" else null,
        servicio = if (servicio == null) "Elegí el servicio realizado" else null,
        precio = if (precioCentavos == null || precioCentavos < 0) "Ingresá un precio válido" else null,
    )

    /** Normaliza la patente a MAYÚSCULAS sin espacios; null si queda vacía (V-4). */
    fun normalizarPatente(raw: String?): String? =
        raw?.trim()?.uppercase()?.replace(" ", "")?.ifBlank { null }
}
