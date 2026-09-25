package com.tallerapp.features.remito

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tallerapp.core.export.Compartir
import com.tallerapp.core.export.ItemRemito
import com.tallerapp.core.export.Remito
import com.tallerapp.core.export.RemitoPdf
import com.tallerapp.core.ui.components.BotonGhost
import com.tallerapp.core.ui.components.FechaPicker
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.util.Dinero

/** Una fila editable del remito (texto crudo; se parsea al generar). */
private class FilaRemito(
    descripcion: String = "",
    cantidad: String = "1",
    precio: String = "",
) {
    var descripcion by mutableStateOf(descripcion)
    var cantidad by mutableStateOf(cantidad)
    var precio by mutableStateOf(precio)

    val cantidadNum: Double get() = cantidad.replace(',', '.').toDoubleOrNull() ?: 0.0
    val precioCentavos: Long get() = Dinero.parsearACentavos(precio) ?: 0L
    val subtotalCentavos: Long get() = Math.round(cantidadNum * precioCentavos)
    val valida: Boolean get() = descripcion.isNotBlank() && cantidadNum > 0 && precioCentavos > 0
}

/**
 * Remito: cliente, número, fecha e ítems (cantidad × precio), con generación de PDF.
 * Equivale a la RemitoWindow del escritorio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemitoScreen(negocioNombre: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var cliente by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(System.currentTimeMillis()) }
    val filas = remember { mutableStateListOf(FilaRemito()) }

    val itemsValidos = filas.filter { it.valida }
    val total = itemsValidos.sumOf { it.subtotalCentavos }
    val puedeGenerar = cliente.isNotBlank() && itemsValidos.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remito") },
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
            TituloPantalla("Nuevo remito")
            TextoMuted(negocioNombre)

            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cliente,
                        onValueChange = { cliente = it },
                        label = { Text("Cliente *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = { Text("N° (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FechaPicker(fechaMillis = fecha, onFechaChange = { fecha = it })
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TituloSeccion("Ítems", modifier = Modifier.weight(1f))
                TextButton(onClick = { filas.add(FilaRemito()) }) { Text("+ Agregar") }
            }

            filas.forEachIndexed { indice, fila ->
                TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fila.descripcion,
                            onValueChange = { fila.descripcion = it },
                            label = { Text("Descripción") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fila.cantidad,
                                onValueChange = { fila.cantidad = it },
                                label = { Text("Cantidad") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = fila.precio,
                                onValueChange = { fila.precio = it },
                                label = { Text("P. unitario") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextoMuted("Subtotal: ${Dinero.formatear(fila.subtotalCentavos)}")
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                if (filas.size > 1) {
                                    TextButton(onClick = { filas.removeAt(indice) }) {
                                        Text("Quitar", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            TarjetaApp(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                conBorde = false,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                        Dinero.formatear(total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (!puedeGenerar) {
                TextoMuted("Completá el cliente y al menos un ítem con cantidad y precio.")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGhost("Cancelar", onBack, modifier = Modifier.weight(1f))
                Button(
                    enabled = puedeGenerar,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val remito = Remito(
                            negocio = negocioNombre,
                            cliente = cliente.trim(),
                            numero = numero.trim(),
                            fechaMillis = fecha,
                            items = itemsValidos.map {
                                ItemRemito(it.descripcion.trim(), it.cantidadNum, it.precioCentavos)
                            },
                        )
                        val uri = RemitoPdf.generar(context, remito)
                        Compartir.archivo(context, uri, "application/pdf")
                    },
                ) { Text("Generar PDF") }
            }
        }
    }
}
