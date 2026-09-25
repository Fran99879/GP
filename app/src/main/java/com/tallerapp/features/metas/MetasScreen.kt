package com.tallerapp.features.metas

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.Meta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(viewModel: MetasViewModel, onBack: () -> Unit) {
    val metas by viewModel.metas.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Meta?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metas de ahorro") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButton(onClick = { editando = Meta(nombre = "", objetivoCentavos = 0) }) { Text("+ Nueva meta") }
            if (metas.isEmpty()) {
                Text("Todavía no cargaste metas. Creá una para empezar a seguir tu ahorro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            metas.forEach { m ->
                Card(modifier = Modifier.fillMaxWidth().clickable { editando = m }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val completa = m.actualCentavos >= m.objetivoCentavos
                        Text(
                            "${if (completa) "✅" else "🎯"}  ${m.nombre}",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "${Dinero.formatear(m.actualCentavos)} de ${Dinero.formatear(m.objetivoCentavos)}  ·  ${(m.fraccion * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LinearProgressIndicator(progress = { m.fraccion }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                    }
                }
            }
        }
    }

    editando?.let { m ->
        MetaDialog(
            inicial = m,
            onCerrar = { editando = null },
            onGuardar = { viewModel.guardar(it); editando = null },
            onEliminar = if (m.id != 0L) { { viewModel.eliminar(m.id); editando = null } } else null,
        )
    }
}

@Composable
private fun MetaDialog(inicial: Meta, onCerrar: () -> Unit, onGuardar: (Meta) -> Unit, onEliminar: (() -> Unit)?) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var objetivo by remember { mutableStateOf(if (inicial.objetivoCentavos > 0) Dinero.centavosAEntrada(inicial.objetivoCentavos) else "") }
    var actual by remember { mutableStateOf(if (inicial.actualCentavos > 0) Dinero.centavosAEntrada(inicial.actualCentavos) else "") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (inicial.id == 0L) "Nueva meta" else "Editar meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(
                    value = objetivo, onValueChange = { objetivo = it }, label = { Text("Objetivo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = actual, onValueChange = { actual = it }, label = { Text("Ya ahorrado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && (Dinero.parsearACentavos(objetivo) ?: 0L) > 0,
                onClick = {
                    onGuardar(
                        inicial.copy(
                            nombre = nombre.trim(),
                            objetivoCentavos = Dinero.parsearACentavos(objetivo) ?: 0L,
                            actualCentavos = if (actual.isBlank()) 0L else Dinero.parsearACentavos(actual) ?: 0L,
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
