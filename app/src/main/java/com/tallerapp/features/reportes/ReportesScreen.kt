package com.tallerapp.features.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.draw.clip
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
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.ui.theme.CategoriaColores
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas

/** Reporte mensual navegable con gráficos y exportación a PDF/Excel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    onOpenMenu: () -> Unit = {},
    viewModel: ReportesViewModel,
    onBack: () -> Unit,
) {
    val mensual by viewModel.mensual.collectAsStateWithLifecycle()
    val mes by viewModel.mes.collectAsStateWithLifecycle()
    val categorias by viewModel.categoriasEgreso.collectAsStateWithLifecycle()
    val evolucion by viewModel.evolucion.collectAsStateWithLifecycle()
    val comparativa by viewModel.comparativa.collectAsStateWithLifecycle()
    val todos by viewModel.todosLosNegocios.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val etiquetaMes = Fechas.etiquetaMes(mes)
    val esMesActual = mes == Fechas.mesActual()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                navigationIcon = { TextButton(onClick = onOpenMenu) { Text("☰") } },
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


            // Ver todos los negocios combinados (check del escritorio). Solo si hay más de uno.
            if (comparativa.size > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = todos,
                        onCheckedChange = viewModel::setTodosLosNegocios,
                    )
                    Text("Ver todos los negocios (combinado)")
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
                    val equiv = Dinero.equivalente(mensual.gananciaCentavos)
                    if (equiv.isNotEmpty()) {
                        Text(equiv, style = MaterialTheme.typography.bodySmall)
                    }
                    BarrasIngresoGasto(
                        ingresosCentavos = mensual.ingresosCentavos,
                        gastosCentavos = mensual.gastosCentavos,
                        colorIngreso = Ingreso,
                        colorGasto = Gasto,
                    )
                }
            }

            // Gráfico de gastos por categoría.
            TituloSeccion("Gastos por categoría")
            if (mensual.gastosPorCategoria.isEmpty()) {
                Text("Sin gastos este mes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    val porciones = mensual.gastosPorCategoria.mapIndexed { i, g ->
                        Porcion(
                            etiqueta = g.categoria,
                            valorCentavos = g.montoCentavos,
                            color = CategoriaColores[i % CategoriaColores.size],
                        )
                    }
                    GraficoDona(porciones = porciones)
                }
            }

            // Evolución de los últimos 12 meses.
            if (evolucion.isNotEmpty()) {
                TituloSeccion("Evolución (últimos 12 meses)")
                val prev = evolucion.getOrNull(evolucion.size - 2)?.gastosCentavos ?: 0L
                val act = evolucion.last().gastosCentavos
                val textoEvolucion = when {
                    prev == 0L && act == 0L -> "Sin gastos el mes pasado ni este mes."
                    prev == 0L -> "Empezaste a registrar gastos este mes."
                    else -> {
                        val pct = kotlin.math.abs((act - prev) * 100.0 / prev).toInt()
                        "Gastaste $pct% ${if (act >= prev) "más" else "menos"} que el mes pasado."
                    }
                }
                Text(textoEvolucion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                    GraficoEvolucion(evolucion, Modifier.fillMaxWidth().padding(16.dp))
                }
            }

            // Comparativa entre negocios (solo si hay más de uno, como en el escritorio).
            if (comparativa.size > 1) {
                TituloSeccion("Comparativa entre negocios")
                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val maximo = comparativa.maxOf {
                            maxOf(it.ingresosCentavos, it.gastosCentavos)
                        }.coerceAtLeast(1L)
                        comparativa.forEach { n ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        n.nombre,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        Dinero.formatear(n.balanceCentavos),
                                        fontWeight = FontWeight.Bold,
                                        color = if (n.balanceCentavos < 0) Gasto else Ingreso,
                                    )
                                }
                                BarraComparativa(n.ingresosCentavos, maximo, Ingreso)
                                BarraComparativa(n.gastosCentavos, maximo, Gasto)
                            }
                        }
                    }
                }
            }

            // Presupuestos del mes.
            val conPresupuesto = categorias.filter { it.presupuestoCentavos > 0 }
            TituloSeccion("Presupuestos del mes")
            if (conPresupuesto.isEmpty()) {
                TextoMuted("No definiste presupuestos. Cargalos en Configuración → Categorías.")
            } else {
                val gastadoPorCat = mensual.gastosPorCategoria.associate { it.categoria to it.montoCentavos }
                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        conPresupuesto.forEach { cat ->
                            BarraPresupuesto(cat, gastadoPorCat[cat.nombre] ?: 0L)
                        }
                    }
                }
            }

            // Exportar.
            TituloSeccion("Exportar $etiquetaMes")
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

@Composable
private fun BarraPresupuesto(cat: com.tallerapp.domain.model.Categoria, gastadoCentavos: Long) {
    val frac = (gastadoCentavos.toFloat() / cat.presupuestoCentavos).coerceIn(0f, 1f)
    val ratio = gastadoCentavos.toDouble() / cat.presupuestoCentavos
    val color = when {
        ratio > 1.0 -> androidx.compose.ui.graphics.Color(0xFFE14B4B)
        ratio >= 0.8 -> androidx.compose.ui.graphics.Color(0xFFE09B22)
        else -> androidx.compose.ui.graphics.Color(0xFF27AE60)
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("${cat.icono}  ${cat.nombre}", modifier = Modifier.weight(1f))
            Text(
                "${Dinero.formatear(gastadoCentavos)} / ${Dinero.formatear(cat.presupuestoCentavos)}",
                color = color, fontWeight = FontWeight.SemiBold,
            )
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { frac },
            color = color,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
        if (ratio > 1.0) {
            Text(
                "Te pasaste ${Dinero.formatear(gastadoCentavos - cat.presupuestoCentavos)}",
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun GraficoEvolucion(datos: List<com.tallerapp.domain.usecase.MesEvolucion>, modifier: Modifier = Modifier) {
    val max = (datos.maxOfOrNull { maxOf(it.ingresosCentavos, it.gastosCentavos) } ?: 1L).coerceAtLeast(1L)
    val alturaMax = 120
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height((alturaMax + 4).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            datos.forEach { m ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        Box(
                            modifier = Modifier.width(7.dp)
                                .height((alturaMax * m.ingresosCentavos / max).toInt().coerceAtLeast(2).dp)
                                .background(Ingreso),
                        )
                        Box(
                            modifier = Modifier.width(7.dp)
                                .height((alturaMax * m.gastosCentavos / max).toInt().coerceAtLeast(2).dp)
                                .background(Gasto),
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            datos.forEach { m ->
                Text(
                    m.mes.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es"))
                        .take(3).replaceFirstChar { it.titlecase(java.util.Locale("es")) },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(Ingreso))
            Text("  Ingresos    ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(modifier = Modifier.size(10.dp).background(Gasto))
            Text("  Gastos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Barra proporcional usada en la comparativa entre negocios. */
@Composable
private fun BarraComparativa(valorCentavos: Long, maximoCentavos: Long, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((valorCentavos.toFloat() / maximoCentavos).coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color),
            )
        }
        Text(
            Dinero.formatear(valorCentavos),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
