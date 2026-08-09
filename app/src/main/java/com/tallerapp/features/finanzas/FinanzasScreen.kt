package com.tallerapp.features.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.features.finanzas.components.MovimientoRow

private enum class TipoMov { INGRESO, EGRESO }

private data class PendienteEliminar(val tipo: TipoMov, val id: Long)

/**
 * Hub de Finanzas: caja del día, ingresos y gastos de hoy con edición/anulación (V-7),
 * y accesos para registrar nuevos movimientos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen(
    viewModel: FinanzasViewModel,
    onBack: () -> Unit,
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
    onEditarIngreso: (Long) -> Unit,
    onEditarEgreso: (Long) -> Unit,
) {
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val ingresos by viewModel.ingresos.collectAsStateWithLifecycle()
    val egresos by viewModel.egresos.collectAsStateWithLifecycle()
    var pendiente by remember { mutableStateOf<PendienteEliminar?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanzas") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Hoy", style = MaterialTheme.typography.titleMedium)
                    FilaResumen("Ingresos del día", Dinero.formatear(resumen.ingresosCentavos))
                    FilaResumen("Gastos del día", Dinero.formatear(resumen.gastosCentavos))
                    FilaResumen("Balance del día", Dinero.formatear(resumen.gananciaCentavos))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("+ Ingreso", onNuevoIngreso, modifier = Modifier.weight(1f))
                PrimaryButton("− Gasto", onNuevoGasto, modifier = Modifier.weight(1f))
            }

            Text("Ingresos de hoy", style = MaterialTheme.typography.titleMedium)
            if (ingresos.isEmpty()) {
                Text("Sin ingresos hoy", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                ingresos.forEach { ingreso ->
                    val esHoy = Fechas.esHoy(ingreso.fechaRegistro)
                    MovimientoRow(
                        titulo = ingreso.concepto,
                        subtitulo = "${ingreso.metodo.etiqueta} · ${Fechas.formatear(ingreso.fecha)}",
                        monto = Dinero.formatear(ingreso.montoCentavos),
                        montoColor = Ingreso,
                        permiteEditar = esHoy,
                        permiteAnular = esHoy,
                        onEditar = { onEditarIngreso(ingreso.id) },
                        onEliminar = { pendiente = PendienteEliminar(TipoMov.INGRESO, ingreso.id) },
                    )
                }
            }

            Text("Gastos de hoy", style = MaterialTheme.typography.titleMedium)
            if (egresos.isEmpty()) {
                Text("Sin gastos hoy", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                egresos.forEach { egreso ->
                    val esHoy = Fechas.esHoy(egreso.fechaRegistro)
                    MovimientoRow(
                        titulo = egreso.concepto,
                        subtitulo = "${egreso.categoria.etiqueta} · ${Fechas.formatear(egreso.fecha)}",
                        monto = Dinero.formatear(egreso.montoCentavos),
                        montoColor = Gasto,
                        permiteEditar = esHoy,
                        permiteAnular = esHoy,
                        onEditar = { onEditarEgreso(egreso.id) },
                        onEliminar = { pendiente = PendienteEliminar(TipoMov.EGRESO, egreso.id) },
                    )
                }
            }
        }
    }

    pendiente?.let { p ->
        AlertDialog(
            onDismissRequest = { pendiente = null },
            title = { Text("Anular movimiento") },
            text = { Text("¿Anular este movimiento? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    when (p.tipo) {
                        TipoMov.INGRESO -> viewModel.eliminarIngreso(p.id)
                        TipoMov.EGRESO -> viewModel.eliminarEgreso(p.id)
                    }
                    pendiente = null
                }) { Text("Anular") }
            },
            dismissButton = {
                TextButton(onClick = { pendiente = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(etiqueta)
        Text(valor, fontWeight = FontWeight.Bold)
    }
}
