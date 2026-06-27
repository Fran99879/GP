package com.tallerapp.domain.model

/**
 * Entidad de dominio: un trabajo = el ingreso de un vehículo al taller (Frozen Spec 5.1).
 * Modelo puro, sin dependencias de Android ni de persistencia.
 *
 * @param id 0 indica un trabajo aún no persistido.
 * @param precioCentavos importe a cobrar en centavos (Arquitectura AD-7, evita errores de redondeo).
 * @param fechaEntrega instante en que pasó a Entregado; null si aún no se entregó.
 * @param cobroId referencia al cobro asociado; null hasta la Fase 4.
 */
data class Trabajo(
    val id: Long = 0,
    val cliente: String,
    val telefono: String?,
    val patente: String?,
    val marca: String,
    val modelo: String,
    val servicio: ServicioRealizado,
    val fechaIngreso: Long,
    val estadoReparacion: EstadoReparacion,
    val estadoCobro: EstadoCobro,
    val problema: String?,
    val diagnostico: String?,
    val precioCentavos: Long,
    val fechaEntrega: Long? = null,
    val cobroId: Long? = null,
) {
    /**
     * Texto identificador para tarjetas y búsqueda (Frozen Spec C5):
     * usa la patente si existe; si no, "Marca Modelo — Cliente".
     */
    fun identificacion(): String =
        patente?.takeIf { it.isNotBlank() } ?: "$marca $modelo — $cliente"
}
