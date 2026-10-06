package com.tallerapp.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * Título de la barra superior.
 *
 * `TopAppBar` tiene alto fijo: si el título se va a dos renglones, el segundo queda tapado y la
 * palabra se lee cortada ("Movimient" y nada más). Con el texto del sistema al 200 % eso pasaba en
 * todas las pantallas.
 *
 * Por eso el título crece hasta [ESCALA_APILADO] y de ahí no sube. No es ignorar la preferencia del
 * usuario: el nombre de la pantalla se repite como encabezado dentro del cuerpo, que sí escala
 * entero, así que la versión grande del texto sigue estando. [TextOverflow.Ellipsis] queda de red
 * para los títulos largos que vienen de datos, como "Factura N° 0001".
 */
@Composable
fun TituloBarra(texto: String) {
    val estilo = MaterialTheme.typography.titleLarge

    Text(
        texto,
        style = estilo,
        fontSize = conTopeDeEscala(estilo.fontSize),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
