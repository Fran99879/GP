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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.billing.EstadoPlan

/**
 * Contenido del menú lateral (drawer), al estilo de la barra lateral del escritorio:
 * primero los destinos raíz, un separador, los secundarios, y al final el bloque de
 * herramientas comerciales, que se habilita al activarse Pro.
 */
@Composable
fun AppDrawer(rutaActual: String?, onNavegar: (String) -> Unit) {
    // El plan se observa: al confirmarse la compra el bloque se destraba solo, sin reiniciar.
    val plan by EstadoPlan.plan.collectAsStateWithLifecycle()

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

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            Text(
                if (plan.esPro) "TU NEGOCIO" else "TU NEGOCIO · PRO",
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DESTINOS_PRO.forEach { item ->
                ItemDrawer(
                    item = item,
                    rutaActual = rutaActual,
                    // Con el plan Gratis la fila lleva a Planes en vez de a la pantalla.
                    onNavegar = { ruta -> onNavegar(if (plan.esPro) ruta else Destination.PLANES) },
                    conCandado = !plan.esPro,
                )
            }
            if (!plan.esPro) {
                Text(
                    "Se habilita con Pro.",
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ItemDrawer(
    item: DestinoNav,
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    conCandado: Boolean = false,
) {
    NavigationDrawerItem(
        icon = { Icon(item.icono, contentDescription = null) },
        label = { Text(item.etiqueta) },
        badge = if (conCandado) ({ Text("🔒") }) else null,
        selected = rutaActual == item.ruta,
        onClick = { onNavegar(item.ruta) },
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
