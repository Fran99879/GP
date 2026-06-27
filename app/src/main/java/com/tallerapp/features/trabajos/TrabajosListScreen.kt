package com.tallerapp.features.trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.features.trabajos.components.TrabajoCard
import com.tallerapp.features.trabajos.list.TrabajosListViewModel

/**
 * Lista de trabajos con búsqueda por patente/cliente (Frozen Spec 7, 11).
 * Cada tarjeta abre el detalle; el botón inferior crea un trabajo nuevo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajosListScreen(
    viewModel: TrabajosListViewModel,
    onBack: () -> Unit,
    onNuevoTrabajo: () -> Unit,
    onAbrirDetalle: (Long) -> Unit,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val trabajos by viewModel.trabajos.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trabajos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar por patente o cliente") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (trabajos.isEmpty()) {
                    Text(
                        text = if (query.isBlank()) "No hay trabajos registrados"
                        else "Sin resultados para \"$query\"",
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items = trabajos, key = { it.id }) { trabajo ->
                            TrabajoCard(trabajo = trabajo, onClick = { onAbrirDetalle(trabajo.id) })
                        }
                    }
                }
            }

            PrimaryButton("Nuevo Trabajo", onNuevoTrabajo)
        }
    }
}
