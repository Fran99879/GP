package com.tallerapp.features.finanzas.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Fila de un movimiento (ingreso o egreso) en el hub de Finanzas.
 * - [permiteEditar]: la fila es clickeable para editar (movimientos manuales de hoy, V-7).
 * - [permiteAnular]: muestra el botón "Anular" (movimientos de hoy, incluidos cobros).
 * Los ingresos por cobro no se editan sueltos; solo se anulan (revierte el cobro).
 */
@Composable
fun MovimientoRow(
    titulo: String,
    subtitulo: String,
    monto: String,
    permiteEditar: Boolean,
    permiteAnular: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    val base = Modifier.fillMaxWidth()
    Card(modifier = if (permiteEditar) base.clickable { onEditar() } else base) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall)
            }
            Text(monto, fontWeight = FontWeight.Bold)
            if (permiteAnular) {
                TextButton(onClick = onEliminar) { Text("Anular") }
            }
        }
    }
}
