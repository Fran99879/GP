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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Fila de un movimiento (ingreso o gasto) en el hub de Finanzas.
 * - [permiteEditar]: la fila es clickeable para editar (movimientos de hoy, V-7).
 * - [permiteAnular]: muestra el botón "Anular" (movimientos de hoy).
 */
@Composable
fun MovimientoRow(
    titulo: String,
    subtitulo: String,
    monto: String,
    montoColor: Color,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(monto, fontWeight = FontWeight.Bold, color = montoColor)
            if (permiteAnular) {
                TextButton(onClick = onEliminar) { Text("Anular") }
            }
        }
    }
}
