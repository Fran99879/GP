package com.tallerapp.core.util

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Utilidades de dinero. El importe se maneja en centavos (entero) en todo el sistema
 * para evitar errores de redondeo (Arquitectura AD-7). Moneda única (Frozen Spec 2.3).
 */
object Dinero {

    /** Símbolo de moneda para mostrar (configurable). */
    var simbolo: String = "$"
    /** Moneda secundaria para el equivalente (ej. "US$"). */
    var simboloSecundario: String = "US$"
    /** Cuántas unidades de la principal equivalen a 1 de la secundaria (0 = desactivado). */
    var tasa: Double = 0.0
    var mostrarEquivalente: Boolean = false

    /** Convierte el texto del usuario a centavos. Devuelve null si es inválido o negativo. */
    fun parsearACentavos(texto: String): Long? {
        val limpio = texto.trim().replace(",", ".")
        if (limpio.isEmpty()) return null
        val valor = limpio.toDoubleOrNull() ?: return null
        if (valor < 0) return null
        return (valor * 100).roundToLong()
    }

    /** Representa centavos como texto editable para el formulario, ej. "1500.50". */
    fun centavosAEntrada(centavos: Long): String {
        val entero = centavos / 100
        val decimales = abs(centavos % 100)
        return "$entero.${decimales.toString().padStart(2, '0')}"
    }

    /** Formato de visualización con separador de miles, ej. "$ 1.500,50" o "-$ 1.500,50". */
    fun formatear(centavos: Long): String {
        val signo = if (centavos < 0) "-" else ""
        val abs = abs(centavos)
        val entero = abs / 100
        val decimales = (abs % 100).toString().padStart(2, '0')
        val enteroConMiles = entero.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "$signo$simbolo $enteroConMiles,$decimales"
    }

    /** Equivalente en la moneda secundaria, ej. "≈ US$ 12,50". "" si está desactivado o sin tasa. */
    fun equivalente(centavos: Long): String {
        if (!mostrarEquivalente || tasa <= 0) return ""
        val signo = if (centavos < 0) "-" else ""
        val valor = abs(centavos) / 100.0 / tasa
        val entero = valor.toLong()
        val dec = ((abs(valor) - abs(entero.toDouble())) * 100).roundToLong().toString().padStart(2, '0')
        val enteroConMiles = entero.toString().reversed().chunked(3).joinToString(".").reversed()
        return "≈ $signo$simboloSecundario $enteroConMiles,$dec"
    }
}
