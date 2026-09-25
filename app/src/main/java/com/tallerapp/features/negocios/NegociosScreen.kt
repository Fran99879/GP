package com.tallerapp.features.negocios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.NegocioActual
import com.tallerapp.domain.model.Negocio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegociosScreen(viewModel: NegociosViewModel, onBack: () -> Unit) {
    val negocios by viewModel.negocios.collectAsStateWithLifecycle()
    val actual by viewModel.actual.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editando by remember { mutableStateOf<Negocio?>(null) }
    var creando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Negocios") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Cada negocio guarda sus propios movimientos, deudas y recurrentes. Tocá uno para cambiar el negocio activo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tus negocios", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { creando = true }) { Text("+ Nuevo") }
            }

            negocios.forEach { n ->
                val seleccionado = n.id == actual
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { NegocioActual.set(context, n.id) },
                    colors = if (seleccionado) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        CardDefaults.cardColors()
                    },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (seleccionado) "🏪" else "🏬", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(n.nombre, fontWeight = FontWeight.SemiBold)
                            if (seleccionado) {
                                Text(
                                    "Activo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        TextButton(onClick = { editando = n }) { Text("Renombrar") }
                    }
                }
            }
        }
    }

    if (creando) {
        NegocioDialog(
            titulo = "Nuevo negocio",
            inicial = "",
            onCerrar = { creando = false },
            onGuardar = { viewModel.crear(it); creando = false },
        )
    }

    editando?.let { n ->
        NegocioDialog(
            titulo = "Renombrar negocio",
            inicial = n.nombre,
            onCerrar = { editando = null },
            onGuardar = { viewModel.renombrar(n.id, it); editando = null },
        )
    }
}

@Composable
private fun NegocioDialog(
    titulo: String,
    inicial: String,
    onCerrar: () -> Unit,
    onGuardar: (String) -> Unit,
) {
    var nombre by remember { mutableStateOf(inicial) }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(enabled = nombre.isNotBlank(), onClick = { onGuardar(nombre.trim()) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } },
    )
}
