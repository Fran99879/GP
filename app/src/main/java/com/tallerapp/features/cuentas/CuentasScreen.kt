package com.tallerapp.features.cuentas

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.Cuenta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentasScreen(viewModel: CuentasViewModel, onBack: () -> Unit) {
    val cuentas by viewModel.cuentas.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Cuenta?>(null) }
    val total = cuentas.sumOf { it.saldoCentavos }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuentas") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total disponible", style = MaterialTheme.typography.labelMedium)
                    Text(Dinero.formatear(total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tus cuentas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { editando = Cuenta(nombre = "") }) { Text("+ Nueva") }
            }

            cuentas.forEach { c ->
                Card(modifier = Modifier.fillMaxWidth().clickable { editando = c }) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(c.icono, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.size(12.dp))
                        Text(c.nombre, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(
                            Dinero.formatear(c.saldoCentavos),
                            fontWeight = FontWeight.Bold,
                            color = if (c.saldoCentavos < 0) Gasto else Ingreso,
                        )
                    }
                }
            }
        }
    }

    editando?.let { c ->
        CuentaDialog(
            inicial = c,
            onCerrar = { editando = null },
            onGuardar = { viewModel.guardar(it); editando = null },
            onEliminar = if (c.id != 0L && !c.nombre.equals("Efectivo", true)) {
                { viewModel.eliminar(c.id); editando = null }
            } else null,
        )
    }
}

private val Emojis = listOf("💵", "🏦", "💳", "💰", "🪙", "📱", "🏧", "🧾")

@Composable
private fun CuentaDialog(
    inicial: Cuenta,
    onCerrar: () -> Unit,
    onGuardar: (Cuenta) -> Unit,
    onEliminar: (() -> Unit)?,
) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var icono by remember { mutableStateOf(inicial.icono) }
    var saldo by remember {
        mutableStateOf(if (inicial.saldoInicialCentavos != 0L) Dinero.centavosAEntrada(inicial.saldoInicialCentavos) else "")
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (inicial.id == 0L) "Nueva cuenta" else "Editar cuenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Emojis.forEach { em ->
                        Text(em, modifier = Modifier.clickable { icono = em }.padding(4.dp))
                    }
                }
                OutlinedTextField(
                    value = saldo,
                    onValueChange = { saldo = it },
                    label = { Text("Saldo inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank(),
                onClick = {
                    onGuardar(
                        inicial.copy(
                            nombre = nombre.trim(),
                            icono = icono,
                            saldoInicialCentavos = if (saldo.isBlank()) 0L else Dinero.parsearACentavos(saldo) ?: 0L,
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
