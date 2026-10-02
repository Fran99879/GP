package com.tallerapp.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptación a pantallas grandes (ROADMAP sección 12, paso 1).
 *
 * La app está escrita para un teléfono en vertical: una columna con todo a `fillMaxWidth()`.
 * En tablet, en Chromebook o en el teléfono acostado eso estira cada tarjeta a todo el ancho
 * y deja líneas de texto que cruzan la pantalla de punta a punta.
 *
 * El arreglo barato es limitar el ancho del contenido y centrarlo. En un teléfono no cambia
 * nada —la pantalla mide menos que el máximo—, así que es seguro aplicarlo en todas las
 * pantallas de una sola pasada.
 */

/**
 * Ancho máximo del contenido de una pantalla.
 *
 * 600 dp es el límite de "compact" de Material 3: por debajo estamos en un teléfono y esto no
 * hace nada; por encima el contenido deja de estirarse.
 */
val ANCHO_MAXIMO_CONTENIDO: Dp = 600.dp

/**
 * Limita el ancho del contenido y lo centra en el espacio disponible.
 *
 * Se aplica al contenedor raíz de la pantalla. Si la pantalla scrollea, va **después** de
 * `verticalScroll(...)`: el área que scrollea sigue ocupando todo el alto y lo que se limita
 * es el contenido.
 *
 * ```
 * Column(
 *     modifier = Modifier
 *         .padding(padding)
 *         .fillMaxSize()
 *         .verticalScroll(rememberScrollState())
 *         .anchoContenido()
 *         .padding(16.dp),
 * )
 * ```
 */
fun Modifier.anchoContenido(ancho: Dp = ANCHO_MAXIMO_CONTENIDO): Modifier = this
    .fillMaxWidth()
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = ancho)
