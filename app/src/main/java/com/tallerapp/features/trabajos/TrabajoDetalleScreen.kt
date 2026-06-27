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
import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.Trabajo
import com.tallerapp.features.trabajos.detalle.TrabajoDetalleViewModel

/**
 * Detalle de un trabajo (Frozen Spec 7/8/9). Permite cambiar estado (un toque),
 * registrar/anular cobro cuando corresponde (Fase 4), editar y eliminar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajoDetalleScreen(
    viewModel: TrabajoDetalleViewModel,
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onRegistrarCobro: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmarAnular by remember { mutableStateOf(false) }

    // Recarga al volver (tras editar o registrar el cobro) para no mostrar datos viejos.
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

                    SeccionCobro(
                        trabajo = trabajo,
                        cobroAnulable = state.cobroAnulable,
                        onRegistrarCobro = onRegistrarCobro,
                        onAnularCobro = { mostrarConfirmarAnular = true },
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar datos")
                    }
                    OutlinedButton(
                        onClick = { mostrarConfirmarEliminar = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Eliminar trabajo")
                    }
                }
            }
        }
    }

    if (mostrarConfirmarEliminar) {
        ConfirmacionDialog(
            titulo = "Eliminar trabajo",
            mensaje = "¿Eliminar este trabajo? Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                mostrarConfirmarEliminar = false
                viewModel.eliminar()
            },
            onCancelar = { mostrarConfirmarEliminar = false },
        )
    }

    if (mostrarConfirmarAnular) {
        ConfirmacionDialog(
            titulo = "Anular cobro",
            mensaje = "¿Anular el cobro? El ingreso se eliminará y el trabajo volverá a pendiente de cobro.",
            textoConfirmar = "Anular",
            onConfirmar = {
                mostrarConfirmarAnular = false
                viewModel.anularCobro()
            },
            onCancelar = { mostrarConfirmarAnular = false },
        )
    }
}

@Composable
private fun SeccionCobro(
    trabajo: Trabajo,
    cobroAnulable: Boolean,
    onRegistrarCobro: () -> Unit,
    onAnularCobro: () -> Unit,
) {
    val puedeCobrar = trabajo.estadoReparacion == EstadoReparacion.ENTREGADO &&
        trabajo.estadoCobro == EstadoCobro.PENDIENTE_DE_COBRO

    if (!puedeCobrar && trabajo.estadoCobro != EstadoCobro.COBRADO) return

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Text("Cobro", style = MaterialTheme.typography.titleMedium)

    when {
        puedeCobrar -> OutlinedButton(onClick = onRegistrarCobro, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar cobro")
        }
        trabajo.estadoCobro == EstadoCobro.COBRADO -> {
            Text("Cobrado")
            if (cobroAnulable) {
                OutlinedButton(onClick = onAnularCobro, modifier = Modifier.fillMaxWidth()) {
                    Text("Anular cobro")
                }
            }
        }
    }
}

@Composable
private fun ConfirmacionDialog(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
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
