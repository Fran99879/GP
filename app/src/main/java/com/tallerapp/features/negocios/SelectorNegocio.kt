package com.tallerapp.features.negocios

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tallerapp.core.NegocioActual
import com.tallerapp.core.di.rememberAppContainer
import com.tallerapp.domain.model.Negocio

/**
 * Selector rápido de negocio para la barra superior (equivale al ComboBox 🏪 del escritorio).
 * Se oculta cuando hay un solo negocio, igual que el escritorio en plan Personal.
 */
@Composable
fun SelectorNegocioTopBar(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val context = LocalContext.current
    val negocios by produceState(initialValue = emptyList<Negocio>(), container) {
        container.observarNegocios().collect { value = it }
    }
    val actual by NegocioActual.id.collectAsState()
    var abierto by remember { mutableStateOf(false) }

    if (negocios.size <= 1) return

    val nombre = negocios.firstOrNull { it.id == actual }?.nombre ?: "Negocio"

    TextButton(onClick = { abierto = true }, modifier = modifier) {
        Text(
            "🏪 $nombre",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
    }
    DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
        negocios.forEach { n ->
            DropdownMenuItem(
                text = {
                    Text(
                        if (n.id == actual) "✓  ${n.nombre}" else n.nombre,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
                onClick = {
                    NegocioActual.set(context, n.id)
                    abierto = false
                },
            )
        }
    }
}

/** Nombre del negocio activo (para encabezados de documentos como el remito). */
@Composable
fun recordarNombreNegocioActual(): String {
    val container = rememberAppContainer()
    val negocios by produceState(initialValue = emptyList<Negocio>(), container) {
        container.observarNegocios().collect { value = it }
    }
    val actual by NegocioActual.id.collectAsState()
    return negocios.firstOrNull { it.id == actual }?.nombre ?: "Mis Finanzas"
}
