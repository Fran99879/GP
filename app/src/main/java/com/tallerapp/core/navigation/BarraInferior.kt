package com.tallerapp.core.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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
                label = { Text(destino.etiqueta) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(),
            )
        }
    }
}
