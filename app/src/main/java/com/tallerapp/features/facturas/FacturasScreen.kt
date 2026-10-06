package com.tallerapp.features.facturas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.export.Compartir
import com.tallerapp.core.export.FacturaPdf
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoFila
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloBarra
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Factura

/**
 * Facturas emitidas del negocio activo. Tocar una abre el detalle, desde donde se vuelve a
 * generar el PDF (el archivo no se guarda: se regenera de los datos, que son la fuente).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(
    viewModel: FacturasViewModel,
    negocioNombre: String,
    onBack: () -> Unit,
    onNuevaFactura: () -> Unit,
) {
    val facturas by viewModel.facturas.collectAsStateWithLifecycle()
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    var borrando by remember { mutableStateOf<Factura?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Facturas") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaFactura,
                text = { Text("Nueva") },
                icon = { Text("＋") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .anchoContenido()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TituloPantalla("Emitidas")
            TextoMuted(negocioNombre)

            if (facturas.isEmpty()) {
                TextoMuted(
                    "Todavía no emitiste facturas. Tocá «Nueva» y cargá los productos con el " +
                        "escáner o desde el catálogo.",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(facturas, key = { it.id }) { f ->
                        TarjetaApp(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.abrir(f.id) },
                            padding = 12.dp,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    TextoFila(
                                        if (f.numero.isBlank()) f.cliente else "N° ${f.numero} · ${f.cliente}",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    TextoFila(
                                        Fechas.formatear(f.fecha),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                    if (!f.registradaEnCaja) {
                                        TextoFila(
                                            "Sin registrar en caja",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    Dinero.formatear(f.totalCentavos),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    detalle?.let { f ->
        DetalleFacturaDialog(
            factura = f,
            negocioNombre = negocioNombre,
            onCerrar = viewModel::cerrarDetalle,
            onEliminar = { viewModel.cerrarDetalle(); borrando = f },
        )
    }

    borrando?.let { f ->
        AlertDialog(
            onDismissRequest = { borrando = null },
            title = { Text("Eliminar factura ${f.numero}") },
            text = {
                Text(
                    "Se borra la factura con sus líneas. El ingreso que se registró y el stock " +
                        "que se descontó no se revierten: si hace falta, corregilos en " +
                        "Movimientos y en Productos.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(f.id); borrando = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { borrando = null }) { Text("Cancelar") } },
        )
    }
}

/** Detalle con las líneas y el botón de PDF. */
@Composable
private fun DetalleFacturaDialog(
    factura: Factura,
    negocioNombre: String,
    onCerrar: () -> Unit,
    onEliminar: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (factura.numero.isBlank()) "Factura" else "Factura N° ${factura.numero}") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(factura.cliente, fontWeight = FontWeight.SemiBold)
                if (factura.documento.isNotBlank()) TextoMuted(factura.documento)
                TextoMuted(Fechas.formatear(factura.fecha))

                factura.items.forEach { item ->
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            TextoFila(item.descripcion, style = MaterialTheme.typography.bodyMedium)
                            TextoFila(
                                "${cantidad(item.cantidad)} × ${Dinero.formatear(item.precioUnitCentavos)}" +
                                    if (item.descuentoPct > 0) "  ·  -${cantidad(item.descuentoPct)}%" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            Dinero.formatear(item.subtotalCentavos),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                if (factura.descuentoPct > 0) {
                    Row(modifier = Modifier.padding(top = 6.dp)) {
                        Text("Descuento ${cantidad(factura.descuentoPct)}%", modifier = Modifier.weight(1f))
                        Text("- ${Dinero.formatear(factura.descuentoCentavos)}")
                    }
                }
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    Text("TOTAL", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(Dinero.formatear(factura.totalCentavos), fontWeight = FontWeight.Bold)
                }
                if (factura.registradaEnCaja) {
                    TextoMuted("Registrada en caja${if (factura.cuenta.isNotBlank()) " · ${factura.cuenta}" else ""}")
                }
                if (factura.notas.isNotBlank()) TextoMuted(factura.notas)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val uri = FacturaPdf.generar(context, factura, negocioNombre)
                    Compartir.archivo(context, uri, "application/pdf")
                },
            ) { Text("Compartir PDF") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onCerrar) { Text("Cerrar") }
            }
        },
    )
}
