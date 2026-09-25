package com.tallerapp.features.deudas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import com.tallerapp.core.ui.theme.Deuda
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Deuda as DeudaModel

/** Pantalla "Quién te debe": deudas a favor, pendientes arriba y cobradas abajo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeudasScreen(
    onOpenMenu: () -> Unit = {},
    viewModel: DeudasViewModel,
    onBack: () -> Unit,
    onNuevaDeuda: () -> Unit,
    onEditarDeuda: (Long) -> Unit,
) {
    val deudas by viewModel.deudas.collectAsStateWithLifecycle()
    val totalPendiente = deudas.filter { !it.cobrada }.sumOf { it.montoCentavos }
    var aEliminar by remember { mutableStateOf<DeudaModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quién me debe") },
                navigationIcon = { TextButton(onClick = onOpenMenu) { Text("☰") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaDeuda) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva deuda")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total pendiente de cobro", style = MaterialTheme.typography.labelLarge)
                    Text(
                        Dinero.formatear(totalPendiente),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (deudas.isEmpty()) {
                Text(
                    "Nadie te debe todavía. Tocá + para anotar una deuda.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(deudas, key = { it.id }) { deuda ->
                        DeudaRow(
                            deuda = deuda,
                            onToggleCobrada = { viewModel.marcarCobrada(deuda.id, it) },
                            onEditar = { onEditarDeuda(deuda.id) },
                            onEliminar = { aEliminar = deuda },
                        )
                    }
                }
            }
        }
    }

    aEliminar?.let { d ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("Eliminar deuda") },
            text = { Text("¿Eliminar la deuda de ${d.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(d.id)
                    aEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun DeudaRow(
    deuda: DeudaModel,
    onToggleCobrada: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Checkbox(checked = deuda.cobrada, onCheckedChange = onToggleCobrada)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    deuda.nombre,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (deuda.cobrada) TextDecoration.LineThrough else null,
                )
                val detalle = buildString {
                    append("Desde ${Fechas.formatear(deuda.fecha)}")
                    if (deuda.nota.isNotBlank()) append(" · ${deuda.nota}")
                }
                Text(
                    detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!deuda.cobrada && deuda.fechaLimite != null) {
                    val dias = ((deuda.fechaLimite!! - Fechas.hoyInicioMillis()) / 86_400_000L).toInt()
                    val aviso = when {
                        dias < 0 -> "⚠ Cobro vencido (hace ${-dias} día${if (-dias == 1) "" else "s"})"
                        dias == 0 -> "🔔 Hoy es la fecha para cobrar"
                        dias <= 7 -> "🔔 Falta${if (dias == 1) "" else "n"} $dias día${if (dias == 1) "" else "s"} para cobrar"
                        else -> ""
                    }
                    if (aviso.isNotEmpty()) {
                        Text(
                            aviso,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (dias < 0) com.tallerapp.core.ui.theme.Gasto else Deuda,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Text(
                Dinero.formatear(deuda.montoCentavos),
                fontWeight = FontWeight.Bold,
                color = if (deuda.cobrada) MaterialTheme.colorScheme.onSurfaceVariant else Deuda,
            )
            if (!deuda.cobrada) {
                TextButton(onClick = onEditar) { Text("Editar") }
            }
            TextButton(onClick = onEliminar) { Text("✕") }
        }
    }
}
