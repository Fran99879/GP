package com.tallerapp.core.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.tallerapp.core.ui.components.conTopeDeEscala

/**
 * Barra de navegación inferior con los destinos raíz (equivale a la barra lateral
 * persistente del escritorio, adaptada a la ergonomía del teléfono).
 */
@Composable
fun BarraInferior(rutaActual: String?, onNavegar: (String) -> Unit) {
    NavigationBar {
        DESTINOS_RAIZ.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = { onNavegar(destino.ruta) },
                icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                label = {
                    // La barra inferior reparte el ancho en partes iguales y tiene alto fijo: con
                    // el texto del sistema grande, "Movimientos" se partía en "Movimi / entos".
                    // El ícono y su contentDescription siguen diciendo lo mismo.
                    Text(
                        destino.etiqueta,
                        // Tope más bajo que el del resto: los cuatro destinos se reparten el ancho
                        // en partes iguales y "Movimientos" no entra entero ni a 1.3. Preferimos
                        // la palabra completa y chica antes que grande y cortada.
                        fontSize = conTopeDeEscala(
                            MaterialTheme.typography.labelMedium.fontSize,
                            tope = 1.1f,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(),
            )
        }
    }
}
