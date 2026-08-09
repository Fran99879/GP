package com.tallerapp.features.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.export.Compartir
import com.tallerapp.core.export.CsvExporter
import com.tallerapp.core.export.PdfExporter
import com.tallerapp.core.export.ReporteContenido
import com.tallerapp.core.ui.components.BarrasIngresoGasto
import com.tallerapp.core.ui.components.GraficoDona
import com.tallerapp.core.ui.components.Porcion
import com.tallerapp.core.ui.theme.CategoriaColores
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas

/** Reporte mensual navegable con gráficos y exportación a PDF/Excel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    viewModel: ReportesViewModel,
    onBack: () -> Unit,
) {
    val mensual by viewModel.mensual.collectAsStateWithLifecycle()
    val mes by viewModel.mes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val etiquetaMes = Fechas.etiquetaMes(mes)
    val esMesActual = mes == Fechas.mesActual()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Selector de mes.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = viewModel::mesAnterior) { Text("◀", style = MaterialTheme.typography.titleLarge) }
                Text(etiquetaMes, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = viewModel::mesSiguiente, enabled = !esMesActual) {
                    Text(
                        "▶",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (esMesActual) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Resumen del mes.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Balance del mes", style = MaterialTheme.typography.labelLarge)
                    Text(
                        Dinero.formatear(mensual.gananciaCentavos),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    BarrasIngresoGasto(
                        ingresosCentavos = mensual.ingresosCentavos,
                        gastosCentavos = mensual.gastosCentavos,
                        colorIngreso = Ingreso,
                        colorGasto = Gasto,
                    )
                }
            }

            // Gráfico de gastos por categoría.
            Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium)
            if (mensual.gastosPorCategoria.isEmpty()) {
                Text("Sin gastos este mes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val porciones = mensual.gastosPorCategoria.mapIndexed { i, g ->
                            Porcion(
                                etiqueta = g.categoria.etiqueta,
                                valorCentavos = g.montoCentavos,
                                color = CategoriaColores[i % CategoriaColores.size],
                            )
                        }
                        GraficoDona(porciones = porciones)
                    }
                }
            }

            // Exportar.
            Text("Exportar $etiquetaMes", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val reporte = ReporteContenido.construir(mensual, etiquetaMes)
                        val uri = PdfExporter.generar(context, reporte)
                        Compartir.archivo(context, uri, "application/pdf")
                    },
                ) { Text("PDF") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val reporte = ReporteContenido.construir(mensual, etiquetaMes)
                        val uri = CsvExporter.generar(context, reporte)
                        Compartir.archivo(context, uri, "text/csv")
                    },
                ) { Text("Excel") }
            }
        }
    }
}
