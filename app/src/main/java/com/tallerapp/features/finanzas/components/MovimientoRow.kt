package com.tallerapp.features.finanzas.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tallerapp.core.ui.components.FilaLista

/**
 * Fila de un movimiento (ingreso o gasto) en el hub de Finanzas.
 * - [permiteEditar]: la fila es clickeable para editar (movimientos de hoy, V-7).
 * - [permiteAnular]: muestra el botón "Anular" (movimientos de hoy).
 *
 * El acomodo lo resuelve [FilaLista], que es el mismo de todos los listados de la app.
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
    FilaLista(
        titulo = titulo,
        subtitulo = subtitulo,
        modifier = if (permiteEditar) Modifier.clickable { onEditar() } else Modifier,
        valor = monto,
        valorColor = montoColor,
        acciones = {
            if (permiteAnular) {
                TextButton(onClick = onEliminar) { Text("Anular") }
            }
        },
    )
}
