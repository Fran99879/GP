package com.tallerapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit

/**
 * Escala de texto del sistema a partir de la cual una caja de alto fijo se rompe: el texto se va a
 * dos renglones y el segundo queda tapado. Sirve de umbral para apilar una fila o para topear un
 * tamaño; mirando el mismo número en todos lados, la app cambia de forma de una sola vez.
 */
const val ESCALA_APILADO = 1.3f

/**
 * Hasta [tope] el texto acompaña el tamaño del sistema; de ahí no crece más.
 *
 * Es para los textos encerrados en una caja de alto fijo que no se puede agrandar: el título de la
 * barra superior y las etiquetas de la barra inferior. En el resto de la app el texto escala
 * entero, que es lo que el usuario pidió en Ajustes de Android.
 */
@Composable
fun conTopeDeEscala(tamanio: TextUnit, tope: Float = ESCALA_APILADO): TextUnit {
    val escala = LocalDensity.current.fontScale
    return if (escala > tope) tamanio * (tope / escala) else tamanio
}
