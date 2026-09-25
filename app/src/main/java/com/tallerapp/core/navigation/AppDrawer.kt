package com.tallerapp.core.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Contenido del menú lateral (drawer), al estilo de la barra lateral del escritorio:
 * primero los destinos raíz, un separador, y luego los secundarios.
 */
@Composable
fun AppDrawer(rutaActual: String?, onNavegar: (String) -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mis Finanzas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Finanzas + Negocios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            DESTINOS_RAIZ.forEach { item -> ItemDrawer(item, rutaActual, onNavegar) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            DESTINOS_SECUNDARIOS.forEach { item -> ItemDrawer(item, rutaActual, onNavegar) }
        }
    }
}

@Composable
private fun ItemDrawer(item: DestinoNav, rutaActual: String?, onNavegar: (String) -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(item.icono, contentDescription = null) },
        label = { Text(item.etiqueta) },
        selected = rutaActual == item.ruta,
        onClick = { onNavegar(item.ruta) },
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
