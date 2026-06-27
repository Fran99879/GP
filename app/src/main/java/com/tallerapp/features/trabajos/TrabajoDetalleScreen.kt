package com.tallerapp.features.trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.features.trabajos.detalle.TrabajoDetalleViewModel

/**
 * Detalle de un trabajo (Frozen Spec 7/8/9). Permite cambiar el estado con un toque
 * (solo transiciones válidas), editar y eliminar con confirmación. El cobro es Fase 4.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajoDetalleScreen(
    viewModel: TrabajoDetalleViewModel,
    onBack: () -> Unit,
    onEditar: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // Recarga al volver (p. ej. tras editar) para no mostrar datos obsoletos.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.recargar() }

    LaunchedEffect(state.eliminado) {
        if (state.eliminado) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del trabajo") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        val trabajo = state.trabajo
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                state.cargando -> Text("Cargando…")
                trabajo == null -> Text("El trabajo no existe")
                else -> {
                    DatosTrabajo(trabajo)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Cambiar estado", style = MaterialTheme.typography.titleMedium)
                    if (state.transiciones.isEmpty()) {
                        Text("Sin cambios de estado disponibles")
                    } else {
                        state.transiciones.forEach { destino ->
                            OutlinedButton(
                                onClick = { viewModel.cambiarEstado(destino) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Marcar como: ${destino.etiqueta}")
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar datos")
                    }
                    OutlinedButton(
                        onClick = { mostrarConfirmacion = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Eliminar trabajo")
                    }
                }
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar trabajo") },
            text = { Text("¿Eliminar este trabajo? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    viewModel.eliminar()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun DatosTrabajo(trabajo: Trabajo) {
    Text(trabajo.identificacion(), style = MaterialTheme.typography.titleLarge)
    Fila("Cliente", trabajo.cliente)
    trabajo.telefono?.let { Fila("Teléfono", it) }
    trabajo.patente?.let { Fila("Patente", it) }
    Fila("Vehículo", "${trabajo.marca} ${trabajo.modelo}")
    Fila("Servicio", trabajo.servicio.etiqueta)
    Fila("Ingreso", Fechas.formatear(trabajo.fechaIngreso))
    Fila("Estado", trabajo.estadoReparacion.etiqueta)
    Fila("Cobro", trabajo.estadoCobro.etiqueta)
    Fila("Precio", Dinero.formatear(trabajo.precioCentavos))
    trabajo.problema?.let { Fila("Problema", it) }
    trabajo.diagnostico?.let { Fila("Diagnóstico", it) }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
