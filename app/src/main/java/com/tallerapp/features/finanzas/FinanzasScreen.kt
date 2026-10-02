package com.tallerapp.features.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.core.ui.components.BloqueResumen3
import com.tallerapp.core.ui.components.BotonGhost
import com.tallerapp.core.ui.components.CampoTexto
import com.tallerapp.core.ui.components.MetricaResumen
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.ui.components.FechaPicker
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.features.finanzas.components.MovimientoRow
import com.tallerapp.features.negocios.SelectorNegocioTopBar

private enum class TipoMov { INGRESO, EGRESO }

private data class PendienteEliminar(val tipo: TipoMov, val id: Long)

/**
 * Hub de Finanzas: caja del día, ingresos y gastos de hoy con edición/anulación (V-7),
 * y accesos para registrar nuevos movimientos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen(
    onOpenMenu: () -> Unit = {},
    viewModel: FinanzasViewModel,
    onBack: () -> Unit,
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
    onEditarIngreso: (Long) -> Unit,
    onEditarEgreso: (Long) -> Unit,
    onVerCuentas: () -> Unit = {},
    onVerCategorias: () -> Unit = {},
    onVerRecurrentes: () -> Unit = {},
    onVerMetas: () -> Unit = {},
    onVerRemito: () -> Unit = {},
    onVerFacturas: () -> Unit = {},
    onVerPlanes: () -> Unit = {},
) {
    val ingresos by viewModel.ingresos.collectAsStateWithLifecycle()
    val egresos by viewModel.egresos.collectAsStateWithLifecycle()
    val iconosEgreso by viewModel.iconosEgreso.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val sumIng = ingresos.sumOf { it.montoCentavos }
    val sumGas = egresos.sumOf { it.montoCentavos }
    var pendiente by remember { mutableStateOf<PendienteEliminar?>(null) }
    val plan by EstadoPlan.plan.collectAsStateWithLifecycle()
    val esPro = plan.esPro

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimientos") },
                navigationIcon = { TextButton(onClick = onOpenMenu) { Text("☰") } },
                actions = { SelectorNegocioTopBar() },
            )
        },
    ) { padding ->
        // Lista lazy y no un Column con scroll: el filtro por defecto es el mes, así que acá
        // entran cientos de movimientos y antes se componían todos de una (ROADMAP sección 13,
        // paso 1). La cabecera —filtros, resumen y accesos— va como primer item de la propia
        // lista: un LazyColumn dentro de un Column con scroll no tiene alto definido.
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .anchoContenido(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val (iniMes, finMes) = Fechas.rangoDelMesActual()
                    val esMesActual = filtro.desde == iniMes && filtro.hasta == finMes - 86_400_000L
                    val periodo = when {
                        esMesActual -> Fechas.etiquetaMes(Fechas.mesActual())
                        filtro.desde == filtro.hasta -> Fechas.formatear(filtro.desde)
                        else -> "${Fechas.formatear(filtro.desde)} → ${Fechas.formatear(filtro.hasta)}"
                    }

                    TituloPantalla("Movimientos")
                    TextoMuted(periodo)

                    // Búsqueda y filtros (tarjeta de filtros del escritorio).
                    TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoTexto(valor = filtro.texto, onChange = viewModel::setTexto, etiqueta = "Buscar (concepto, categoría, cuenta)")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FechaPicker(fechaMillis = filtro.desde, onFechaChange = viewModel::setDesde)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    FechaPicker(fechaMillis = filtro.hasta, onFechaChange = viewModel::setHasta)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = viewModel::filtrarHoy) { Text("Hoy") }
                                TextButton(onClick = viewModel::filtrarEsteMes) { Text("Este mes") }
                            }
                            // Ver una sola lista. Tocar la que ya está activa vuelve a mostrar las dos.
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = filtro.vista == VistaMov.INGRESOS,
                                    onClick = { viewModel.setVista(VistaMov.INGRESOS) },
                                    label = { Text("Ingresos") },
                                )
                                FilterChip(
                                    selected = filtro.vista == VistaMov.GASTOS,
                                    onClick = { viewModel.setVista(VistaMov.GASTOS) },
                                    label = { Text("Gastos") },
                                )
                            }
                        }
                    }

                    // Resumen del período (bloque de 3 métricas, igual al escritorio).
                    BloqueResumen3 {
                        MetricaResumen("Ingresos", Dinero.formatear(sumIng), Ingreso)
                        MetricaResumen("Gastos", Dinero.formatear(sumGas), Gasto)
                        MetricaResumen("Balance", Dinero.formatear(sumIng - sumGas), MaterialTheme.colorScheme.onSurface)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton("+ Ingreso", onNuevoIngreso, modifier = Modifier.weight(1f))
                        PrimaryButton("− Gasto", onNuevoGasto, modifier = Modifier.weight(1f))
                    }

                    // Herramientas (barra de accesos del escritorio), desplazable en horizontal.
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BotonGhost("🏦 Cuentas", onVerCuentas)
                        BotonGhost("🏷️ Categorías", onVerCategorias)
                        BotonGhost("🔁 Recurrentes", onVerRecurrentes)
                        BotonGhost("🎯 Metas", onVerMetas)
                        // Con candado en vez de oculto: el usuario tiene que saber que existe.
                        BotonGhost(if (esPro) "📄 Remito" else "🔒 Remito", if (esPro) onVerRemito else onVerPlanes)
                        BotonGhost(if (esPro) "🧾 Facturas" else "🔒 Facturas", if (esPro) onVerFacturas else onVerPlanes)
                    }
                }
            }

            if (filtro.vista != VistaMov.GASTOS) {
                item { TituloSeccion("Ingresos") }
                if (ingresos.isEmpty()) {
                    item { Text("Sin ingresos en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    // La clave lleva prefijo: ingresos y egresos comparten lista y los id se repiten.
                    items(ingresos, key = { "ingreso-${it.id}" }) { ingreso ->
                        val esHoy = Fechas.esHoy(ingreso.fechaRegistro)
                        MovimientoRow(
                            titulo = ingreso.concepto,
                            subtitulo = listOf(ingreso.cuenta)
                                .plus(Fechas.formatear(ingreso.fecha)).joinToString(" · "),
                            monto = Dinero.formatear(ingreso.montoCentavos),
                            montoColor = Ingreso,
                            permiteEditar = esHoy,
                            permiteAnular = esHoy,
                            onEditar = { onEditarIngreso(ingreso.id) },
                            onEliminar = { pendiente = PendienteEliminar(TipoMov.INGRESO, ingreso.id) },
                        )
                    }
                }
            }

            if (filtro.vista != VistaMov.INGRESOS) {
                item { TituloSeccion("Gastos") }
                if (egresos.isEmpty()) {
                    item { Text("Sin gastos en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(egresos, key = { "egreso-${it.id}" }) { egreso ->
                        val esHoy = Fechas.esHoy(egreso.fechaRegistro)
                        MovimientoRow(
                            titulo = "${iconosEgreso[egreso.categoria] ?: "📦"}  ${egreso.concepto}",
                            subtitulo = "${egreso.categoria} · ${egreso.cuenta} · ${Fechas.formatear(egreso.fecha)}",
                            monto = Dinero.formatear(egreso.montoCentavos),
                            montoColor = Gasto,
                            permiteEditar = esHoy,
                            permiteAnular = esHoy,
                            onEditar = { onEditarEgreso(egreso.id) },
                            onEliminar = { pendiente = PendienteEliminar(TipoMov.EGRESO, egreso.id) },
                        )
                    }
                }
            }
        }
    }

    pendiente?.let { p ->
        AlertDialog(
            onDismissRequest = { pendiente = null },
            title = { Text("Anular movimiento") },
            text = { Text("¿Anular este movimiento? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    when (p.tipo) {
                        TipoMov.INGRESO -> viewModel.eliminarIngreso(p.id)
                        TipoMov.EGRESO -> viewModel.eliminarEgreso(p.id)
                    }
                    pendiente = null
                }) { Text("Anular") }
            },
            dismissButton = {
                TextButton(onClick = { pendiente = null }) { Text("Cancelar") }
            },
        )
    }
}
