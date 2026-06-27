package com.tallerapp.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tallerapp.core.util.Fechas

/**
 * Selector de fecha. Muestra la fecha elegida y abre un diálogo de calendario.
 * Trabaja con millis de inicio de día en hora local (Frozen Spec: Fecha editable).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaPicker(
    fechaMillis: Long,
    onFechaChange: (Long) -> Unit,
) {
    var abierto by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { abierto = true },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Fecha: ${Fechas.formatear(fechaMillis)}")
    }

    if (abierto) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = Fechas.aUtcMidnight(fechaMillis),
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { onFechaChange(Fechas.deUtcMidnight(it)) }
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = estado)
        }
    }
}
