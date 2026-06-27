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
 * Solo los movimientos editables (registrados hoy, V-7) son clickeables y muestran "Anular".
 */
@Composable
fun MovimientoRow(
    titulo: String,
    subtitulo: String,
    monto: String,
    editable: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    val base = Modifier.fillMaxWidth()
    Card(modifier = if (editable) base.clickable { onEditar() } else base) {
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
            if (editable) {
                TextButton(onClick = onEliminar) { Text("Anular") }
            }
        }
    }
}
