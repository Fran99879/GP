package com.tallerapp.features.calculadora

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Lógica de una calculadora de ejecución inmediata (como la del teléfono).
 * Usa BigDecimal para evitar los errores de redondeo del punto flotante.
 * Internamente el separador decimal es "."; en pantalla se muestra como ",".
 */
class CalculadoraEstado {

    private var acumulado: BigDecimal? = null
    private var operador: String? = null
    private var entrada: String = "0"
    private var reemplazar: Boolean = true // el próximo dígito reinicia la entrada
    private var error: Boolean = false

    val pantalla: String get() = if (error) "Error" else formatear(entrada)
    val formula: String get() {
        val a = acumulado ?: return ""
        val op = operador ?: return ""
        return "${formatear(a.toPlainString())} $op"
    }

    fun pulsar(tecla: String) {
        if (error && tecla != "C") return
        when (tecla) {
            "C" -> limpiar()
            "⌫" -> borrar()
            "±" -> negar()
            "%" -> porcentaje()
            "+", "−", "×", "÷" -> aplicarOperador(tecla)
            "=" -> igual()
            "," -> decimal()
            else -> digito(tecla)
        }
    }

    private fun limpiar() {
        acumulado = null; operador = null; entrada = "0"; reemplazar = true; error = false
    }

    private fun digito(d: String) {
        if (reemplazar) { entrada = d; reemplazar = false }
        else if (entrada == "0") entrada = d
        else if (entrada.replace("-", "").replace(".", "").length < 12) entrada += d
    }

    private fun decimal() {
        if (reemplazar) { entrada = "0."; reemplazar = false }
        else if (!entrada.contains(".")) entrada += "."
    }

    private fun borrar() {
        if (reemplazar) return
        entrada = entrada.dropLast(1)
        if (entrada.isEmpty() || entrada == "-") entrada = "0"
    }

    private fun negar() {
        if (entrada == "0") return
        entrada = if (entrada.startsWith("-")) entrada.drop(1) else "-$entrada"
    }

    private fun porcentaje() {
        val actual = entrada.toBigDecimalOrNull() ?: return
        val base = acumulado
        val resultado = if (base != null && operador != null) {
            base.multiply(actual).divide(BigDecimal(100))
        } else {
            actual.divide(BigDecimal(100))
        }
        entrada = normalizar(resultado)
        reemplazar = true
    }

    private fun aplicarOperador(op: String) {
        val actual = entrada.toBigDecimalOrNull() ?: return
        if (operador != null && !reemplazar) {
            val r = calcular(acumulado, operador, actual) ?: return
            acumulado = r
            entrada = normalizar(r)
        } else {
            acumulado = actual
        }
        operador = op
        reemplazar = true
    }

    private fun igual() {
        val op = operador ?: return
        val actual = entrada.toBigDecimalOrNull() ?: return
        val r = calcular(acumulado, op, actual) ?: return
        entrada = normalizar(r)
        acumulado = null
        operador = null
        reemplazar = true
    }

    private fun calcular(a: BigDecimal?, op: String?, b: BigDecimal): BigDecimal? {
        if (a == null || op == null) return b
        return when (op) {
            "+" -> a.add(b)
            "−" -> a.subtract(b)
            "×" -> a.multiply(b)
            "÷" -> if (b.signum() == 0) { error = true; null }
                   else a.divide(b, 8, RoundingMode.HALF_UP)
            else -> b
        }
    }

    /** Quita ceros/puntos sobrantes y limita decimales a 8. */
    private fun normalizar(v: BigDecimal): String =
        v.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

    /** Formatea para mostrar: separador de miles con "." y decimal con ",". */
    private fun formatear(valor: String): String {
        val negativo = valor.startsWith("-")
        val sinSigno = valor.removePrefix("-")
        val partes = sinSigno.split(".")
        val entero = partes[0].ifEmpty { "0" }
        val agrupado = entero.reversed().chunked(3).joinToString(".").reversed()
        val decimal = if (partes.size > 1) ",${partes[1]}" else ""
        return (if (negativo) "-" else "") + agrupado + decimal
    }
}
