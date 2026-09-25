package com.tallerapp.features.recurrentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.SelectorOpciones
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.Recurrente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrentesScreen(viewModel: RecurrentesViewModel, onBack: () -> Unit) {
    val recurrentes by viewModel.recurrentes.collectAsStateWithLifecycle()
    val categorias by viewModel.categoriasEgreso.collectAsStateWithLifecycle()
    val cuentas by viewModel.cuentas.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Recurrente?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurrentes") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Sueldo, alquiler, suscripciones… se cargan solos cada mes el día que elijas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { editando = Recurrente() }) { Text("+ Nuevo recurrente") }
            if (recurrentes.isEmpty()) {
                Text("Todavía no cargaste movimientos recurrentes. Creá uno (ej. Sueldo, Alquiler, una suscripción) y se va a cargar solo cada mes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            recurrentes.forEach { r ->
                Card(modifier = Modifier.fillMaxWidth().clickable { editando = r }) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.concepto, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${if (r.tipo == "ingreso") "Ingreso" else "Gasto"} · día ${r.diaMes}${if (r.activo) "" else " · (inactivo)"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            (if (r.tipo == "ingreso") "+" else "−") + Dinero.formatear(r.montoCentavos),
                            fontWeight = FontWeight.Bold,
                            color = if (r.tipo == "ingreso") Ingreso else Gasto,
                        )
                    }
                }
            }
        }
    }

    editando?.let { r ->
        RecurrenteDialog(
            inicial = r,
            categorias = categorias.map { it.nombre },
            cuentas = cuentas.map { it.nombre },
            onCerrar = { editando = null },
            onGuardar = { viewModel.guardar(it); editando = null },
            onEliminar = if (r.id != 0L) { { viewModel.eliminar(r.id); editando = null } } else null,
        )
    }
}

@Composable
private fun RecurrenteDialog(
    inicial: Recurrente,
    categorias: List<String>,
    cuentas: List<String>,
    onCerrar: () -> Unit,
    onGuardar: (Recurrente) -> Unit,
    onEliminar: (() -> Unit)?,
) {
    var tipo by remember { mutableStateOf(inicial.tipo) }
    var monto by remember { mutableStateOf(if (inicial.montoCentavos > 0) Dinero.centavosAEntrada(inicial.montoCentavos) else "") }
    var concepto by remember { mutableStateOf(inicial.concepto) }
    var categoria by remember { mutableStateOf(inicial.categoria.ifBlank { categorias.firstOrNull() ?: "Otros" }) }
    var cuenta by remember { mutableStateOf(inicial.cuenta.ifBlank { cuentas.firstOrNull() ?: "Efectivo" }) }
    var dia by remember { mutableStateOf(inicial.diaMes) }
    var activo by remember { mutableStateOf(inicial.activo) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (inicial.id == 0L) "Nuevo recurrente" else "Editar recurrente") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectorOpciones(
                    etiqueta = "Tipo", seleccionado = tipo, opciones = listOf("egreso", "ingreso"),
                    textoOpcion = { if (it == "ingreso") "Ingreso" else "Gasto" }, onSeleccion = { tipo = it },
                )
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") })
                if (tipo == "egreso" && categorias.isNotEmpty()) {
                    SelectorOpciones(etiqueta = "Categoría", seleccionado = categoria, opciones = categorias, textoOpcion = { it }, onSeleccion = { categoria = it })
                }
                if (cuentas.isNotEmpty()) {
                    SelectorOpciones(etiqueta = "Cuenta", seleccionado = cuenta, opciones = cuentas, textoOpcion = { it }, onSeleccion = { cuenta = it })
                }
                SelectorOpciones(etiqueta = "Día del mes (1 a 28)", seleccionado = dia, opciones = (1..28).toList(), textoOpcion = { it.toString() }, onSeleccion = { dia = it })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activo, onCheckedChange = { activo = it })
                    Text("Activo")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = concepto.isNotBlank() && (Dinero.parsearACentavos(monto) ?: 0L) > 0,
                onClick = {
                    onGuardar(
                        inicial.copy(
                            tipo = tipo, montoCentavos = Dinero.parsearACentavos(monto) ?: 0L,
                            concepto = concepto.trim(), categoria = categoria, cuenta = cuenta,
                            diaMes = dia, activo = activo,
                        ),
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (onEliminar != null) TextButton(onClick = onEliminar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        },
    )
}
