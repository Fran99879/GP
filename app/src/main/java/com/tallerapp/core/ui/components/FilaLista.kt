package com.tallerapp.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Fila de listado: algo a la izquierda, texto en el medio, y a la derecha un valor con sus botones.
 *
 * **El problema que resuelve.** En un `Row`, los hijos sin `weight` se miden primero y se quedan con
 * el ancho que quieran; al que tiene `weight` le toca lo que sobra, que puede ser casi nada. Cuando
 * eso pasa, Compose no corta el texto: lo parte **letra por letra**, y un nombre se lee en vertical
 * ("Her / man / o"). Se vio en un teléfono real con el tamaño de pantalla grande de Android.
 *
 * Ojo: lo dispara el **tamaño de pantalla** (densidad), no el de la letra. Un teléfono de 1080 px a
 * 560 dpi tiene 308 dp de ancho contra los 411 dp del mismo teléfono en su densidad normal. Por eso
 * no alcanza con mirar `fontScale`.
 *
 * **Cómo lo resuelve.** Dos reglas, sin puntos de corte por ancho, así todos los teléfonos ven el
 * mismo diseño:
 *
 * 1. El valor y los botones van en una columna a la derecha, uno debajo del otro. Esa columna mide
 *    lo que mida el más ancho de los dos, no la suma. Donde antes se iban `$ 150.000,00` + `Editar`
 *    + `✕` en fila, ahora ocupan poco más que el monto solo.
 * 2. Los textos llevan tope de renglones y `…` al final. Aunque el espacio quede corto, se lee
 *    "Herman…" y no una columna de letras.
 *
 * @param inicio casillero, emoji o lo que vaya pegado al borde izquierdo.
 * @param valor el número de la fila (un importe, una cantidad). Se muestra en negrita.
 * @param extra avisos debajo del subtítulo, dentro de la columna de texto.
 * @param acciones los botones de la fila. Van bajo el valor.
 */
@Composable
fun FilaLista(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    tachado: Boolean = false,
    colores: CardColors? = null,
    inicio: @Composable (() -> Unit)? = null,
    valor: String? = null,
    valorColor: Color = Color.Unspecified,
    extra: @Composable ColumnScope.() -> Unit = {},
    acciones: @Composable RowScope.() -> Unit = {},
) {
    val card: @Composable (@Composable () -> Unit) -> Unit = { contenido ->
        if (colores != null) {
            Card(modifier = modifier.fillMaxWidth(), colors = colores) { contenido() }
        } else {
            Card(modifier = modifier.fillMaxWidth()) { contenido() }
        }
    }

    card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            inicio?.invoke()

            Column(modifier = Modifier.weight(1f)) {
                TextoFila(
                    titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (tachado) TextDecoration.LineThrough else null,
                )
                if (subtitulo != null) {
                    TextoFila(
                        subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                    )
                }
                extra()
            }

            Column(horizontalAlignment = Alignment.End) {
                if (valor != null) {
                    Text(
                        valor,
                        fontWeight = FontWeight.Bold,
                        color = valorColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, content = acciones)
            }
        }
    }
}

/**
 * Texto de una fila de listado: nunca se parte letra por letra, corta con `…`.
 *
 * Es el tope que hace que una fila angosta se degrade de forma legible. Para texto suelto fuera de
 * una fila no hace falta: ahí el renglón puede seguir abajo sin problema.
 */
@Composable
fun TextoFila(
    texto: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textDecoration: TextDecoration? = null,
    maxLines: Int = 2,
) {
    Text(
        texto,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textDecoration = textDecoration,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
