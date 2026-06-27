package com.tallerapp.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Utilidades de fecha. Usa la zona horaria del dispositivo (Frozen Spec 2.3). */
object Fechas {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val zona: ZoneId get() = ZoneId.systemDefault()

    fun formatear(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zona).toLocalDate().format(formato)

    /** Inicio del día de hoy (00:00 hora local), en millis. Fecha contable por defecto. */
    fun hoyInicioMillis(): Long =
        LocalDate.now(zona).atStartOfDay(zona).toInstant().toEpochMilli()

    /** Rango [inicio, fin) que cubre el día de hoy, para consultas "del día". */
    fun rangoDeHoy(): Pair<Long, Long> {
        val hoy = LocalDate.now(zona)
        val inicio = hoy.atStartOfDay(zona).toInstant().toEpochMilli()
        val fin = hoy.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
        return inicio to fin
    }

    /** Rango [inicio, fin) del mes calendario actual (RN-7), para reportes/dashboard. */
    fun rangoDelMesActual(): Pair<Long, Long> {
        val primerDia = LocalDate.now(zona).withDayOfMonth(1)
        val inicio = primerDia.atStartOfDay(zona).toInstant().toEpochMilli()
        val fin = primerDia.plusMonths(1).atStartOfDay(zona).toInstant().toEpochMilli()
        return inicio to fin
    }

    /** True si [epochMillis] cae en el día de hoy (para la regla V-7). */
    fun esHoy(epochMillis: Long): Boolean =
        Instant.ofEpochMilli(epochMillis).atZone(zona).toLocalDate() == LocalDate.now(zona)

    /** Convierte un millis local de inicio de día al millis de medianoche UTC (DatePicker). */
    fun aUtcMidnight(localMillis: Long): Long =
        Instant.ofEpochMilli(localMillis).atZone(zona).toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /** Convierte el millis de medianoche UTC que devuelve el DatePicker a inicio de día local. */
    fun deUtcMidnight(utcMillis: Long): Long =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay(zona).toInstant().toEpochMilli()
}
