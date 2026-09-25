package com.tallerapp.features.categorias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.Categoria

private val Colores = listOf(
    "#4A7C59", "#3E8E6E", "#0A6E8C", "#023A5D", "#7A5AA6", "#C0544B",
    "#C25E7A", "#98643A", "#C99A6D", "#E09B22", "#5F6B76", "#8A8D91",
)
private val Emojis = listOf(
    "🍽️", "🚗", "🧾", "🏠", "⚕️", "🎉", "🛒", "💼", "✨", "💰", "📦", "🎓", "🐾", "👕", "📱", "☕",
)

private fun parseColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(viewModel: CategoriasViewModel, onBack: () -> Unit) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Categoria?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
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
            Seccion("Gastos", s.egresos, onNueva = { editando = Categoria(tipo = "egreso", nombre = "") }, onEditar = { editando = it })
            Spacer(Modifier.size(8.dp))
            Seccion("Ingresos", s.ingresos, onNueva = { editando = Categoria(tipo = "ingreso", nombre = "") }, onEditar = { editando = it })
        }
    }

    editando?.let { cat ->
        CategoriaDialog(
            inicial = cat,
            onCerrar = { editando = null },
            onGuardar = { viewModel.guardar(it); editando = null },
            onEliminar = if (cat.id != 0L && !cat.nombre.equals("Otros", true)) {
                { viewModel.eliminar(cat.id); editando = null }
            } else null,
        )
    }
}

@Composable
private fun Seccion(titulo: String, items: List<Categoria>, onNueva: () -> Unit, onEditar: (Categoria) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onNueva) { Text("+ Nueva") }
    }
    items.forEach { cat ->
        Card(modifier = Modifier.fillMaxWidth().clickable { onEditar(cat) }.padding(vertical = 2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(16.dp).background(parseColor(cat.color), CircleShape))
                Spacer(Modifier.size(10.dp))
                Text(cat.display, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CategoriaDialog(
    inicial: Categoria,
    onCerrar: () -> Unit,
    onGuardar: (Categoria) -> Unit,
    onEliminar: (() -> Unit)?,
) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var icono by remember { mutableStateOf(inicial.icono) }
    var color by remember { mutableStateOf(inicial.color) }
    var presupuesto by remember {
        mutableStateOf(if (inicial.presupuestoCentavos > 0) Dinero.centavosAEntrada(inicial.presupuestoCentavos) else "")
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (inicial.id == 0L) "Nueva categoría" else "Editar categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                Text("Ícono", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Emojis.forEach { em ->
                        Text(
                            em,
                            modifier = Modifier
                                .border(if (icono == em) 2.dp else 0.dp, MaterialTheme.colorScheme.primary)
                                .clickable { icono = em }
                                .padding(6.dp),
                        )
                    }
                }
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Colores.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(parseColor(hex), CircleShape)
                                .border(if (color == hex) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .clickable { color = hex },
                        )
                    }
                }
                if (inicial.tipo == "egreso") {
                    OutlinedTextField(
                        value = presupuesto,
                        onValueChange = { presupuesto = it },
                        label = { Text("Presupuesto mensual (opcional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank(),
                onClick = {
                    onGuardar(
                        inicial.copy(
                            nombre = nombre.trim(), icono = icono, color = color,
                            presupuestoCentavos = if (presupuesto.isBlank()) 0L else Dinero.parsearACentavos(presupuesto) ?: 0L,
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
