package com.tallerapp.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Formato de fechas para la UI. Usa la zona horaria del dispositivo (Frozen Spec 2.3). */
object Fechas {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun formatear(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formato)
}
