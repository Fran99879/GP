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

    /**
     * Convierte el texto del usuario a centavos. Devuelve null si es inválido o negativo.
     *
     * Regla única compartida con la app de escritorio (`Dinero.ParsearACentavos`), porque la misma
     * cadena tiene que valer lo mismo en las dos:
     * - La coma es siempre el decimal; si hay coma, los puntos son separadores de miles.
     * - Sin coma, un punto es decimal salvo que el texto tenga forma de miles: parte entera de 1 a 3
     *   dígitos que no empieza con 0, y todos los grupos siguientes de exactamente 3 dígitos. Así
     *   "1.500" = 1500 (convención es-AR) y "1500.50" = 1500,50.
     * - Dos o más comas, o varios puntos que no son miles, se rechazan.
     *
     * Lo que escribe el usuario en [com.tallerapp.core.ui.components.CampoMonto] nunca llega acá con
     * separador de miles (ese campo ya normaliza a un punto decimal), pero sí los campos de texto
     * libre como los del remito, y de ahí la regla completa.
     */
    fun parsearACentavos(texto: String): Long? {
        val limpio = texto.trim().replace(" ", "").replace(" ", "")
        if (limpio.isEmpty()) return null

        val comas = limpio.count { it == ',' }
        if (comas > 1) return null

        val normalizado = if (comas == 1) {
            limpio.replace(".", "").replace(',', '.')
        } else {
            val grupos = limpio.split('.')
            when {
                grupos.size == 1 -> limpio
                esSeparadorDeMiles(grupos) -> grupos.joinToString("")
                grupos.size == 2 -> limpio
                else -> return null
            }
        }

        // toDoubleOrNull aceptaría "1e5", "Infinity" o hexadecimales: acá solo dígitos y un punto.
        if (!FORMA_NUMERICA.matches(normalizado)) return null
        val valor = normalizado.toDoubleOrNull() ?: return null
        if (valor < 0) return null
        return (valor * 100).roundToLong()
    }

    private val FORMA_NUMERICA = Regex("""^-?(\d+(\.\d*)?|\.\d+)$""")

    /**
     * Saca los puntos de miles cuando el texto tiene forma de miles: "1.200.000" queda "1200000"
     * y "$ 25.000" queda "25000". Si los puntos no son miles ("1500.50") devuelve lo mismo.
     *
     * Lo usa [com.tallerapp.core.ui.components.CampoMonto] al pegar, así un importe copiado de un
     * resumen o de un mensaje vale lo mismo que uno escrito a mano. La regla es la de
     * [parsearACentavos], escrita una sola vez.
     */
    internal fun quitarSeparadorDeMiles(texto: String): String {
        val soloNumero = texto.filter { it.isDigit() || it == '.' || it == ',' }
        // Con coma de por medio, el decimal es la coma y todos los puntos son miles.
        if (soloNumero.contains(',')) return soloNumero.replace(".", "")

        val grupos = soloNumero.split('.')
        return if (esSeparadorDeMiles(grupos)) grupos.joinToString("") else soloNumero
    }

    /**
     * ¿Los puntos del texto son separadores de miles? Ver la regla en [parsearACentavos].
     * La parte entera no puede empezar con 0 para que "0.999" siga siendo un decimal.
     */
    private fun esSeparadorDeMiles(grupos: List<String>): Boolean {
        if (grupos.size < 2) return false

        val entera = grupos.first()
        if (entera.isEmpty() || entera.length > 3 || entera[0] == '0') return false
        if (!entera.all { it.isDigit() }) return false

        return grupos.drop(1).all { it.length == 3 && it.all { c -> c.isDigit() } }
    }

    /**
     * Representa centavos como texto editable para el formulario, ej. "1500.50".
     *
     * Usa punto a propósito: este texto es el **valor crudo** de
     * [com.tallerapp.core.ui.components.CampoMonto], que lo muestra formateado con coma y separador
     * de miles. El escritorio devuelve "1500,50" porque ahí el usuario ve el texto tal cual.
     */
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
