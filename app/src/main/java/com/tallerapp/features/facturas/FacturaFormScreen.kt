package com.tallerapp.features.facturas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.export.Compartir
import com.tallerapp.core.export.FacturaPdf
import com.tallerapp.core.scan.recordarEscaner
import com.tallerapp.core.ui.components.CampoMonto
import com.tallerapp.core.ui.components.FechaPicker
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.components.SelectorOpciones
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloBarra
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.Precios

/**
 * Carga y emisión de una factura.
 *
 * El camino rápido es el escáner: cada lectura agrega el producto del catálogo con su
 * precio y su descuento, y una segunda lectura del mismo artículo suma cantidad. Las líneas
 * que no son productos (un flete, una mano de obra) se agregan a mano.
 *
 * Al emitir, el usuario decide con dos casillas si la factura además descuenta stock y entra
 * a la caja como ingreso. Desmarcando las dos, la factura es solo un documento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturaFormScreen(
    viewModel: FacturaFormViewModel,
    negocioNombre: String,
    onBack: () -> Unit,
    onVerPlanes: () -> Unit = {},
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    val busqueda by viewModel.busquedaProducto.collectAsStateWithLifecycle()
    val cuentas by viewModel.cuentas.collectAsStateWithLifecycle()
    val numero by viewModel.numero.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val emitida by viewModel.emitida.collectAsStateWithLifecycle()

    var cliente by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(System.currentTimeMillis()) }
    var descuentoGeneral by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var cuenta by remember { mutableStateOf("") }
    var descontarStock by remember { mutableStateOf(true) }
    var registrarIngreso by remember { mutableStateOf(true) }
    var agregandoManual by remember { mutableStateOf(false) }

    val descuentoPct = (descuentoGeneral.replace(",", ".").toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    val subtotal = items.sumOf { it.subtotalCentavos }
    val total = Precios.conDescuento(subtotal, descuentoPct)

    val escanear = recordarEscaner(onCodigo = { viewModel.escanear(it) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Nueva factura") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
                actions = { TextButton(onClick = escanear) { Text("Escanear") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .anchoContenido()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TituloPantalla("Facturar")
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
                        value = documento,
                        onValueChange = { documento = it },
                        label = { Text("CUIT / DNI (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = numero,
                        onValueChange = viewModel::cambiarNumero,
                        label = { Text("N° de factura") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FechaPicker(fechaMillis = fecha, onFechaChange = { fecha = it })
                }
            }

            // Buscador del catálogo: tocar un producto lo agrega como línea.
            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                TituloSeccion("Agregar del catálogo")
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = viewModel::buscarProducto,
                    label = { Text("Buscar producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                if (productos.isEmpty()) {
                    TextoMuted(
                        if (busqueda.isBlank()) {
                            "Cargá productos en Productos y aparecen acá para facturarlos."
                        } else {
                            "Ningún producto coincide."
                        },
                    )
                } else {
                    Column(modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                        productos.take(30).forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.agregar(p) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.nombre, style = MaterialTheme.typography.bodyMedium)
                                    if (p.tieneDescuento) {
                                        TextoMuted("Descuento ${cantidad(p.descuentoPct)}%")
                                    }
                                }
                                Text(
                                    Dinero.formatear(p.precioFinalCentavos),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
                TextButton(onClick = { agregandoManual = true }) { Text("+ Línea a mano") }
            }

            TituloSeccion("Ítems")
            if (items.isEmpty()) {
                TextoMuted("Todavía no agregaste nada. Escaneá un código o buscá en el catálogo.")
            }
            items.forEachIndexed { indice, item ->
                TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.descripcion,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = { viewModel.quitar(indice) }) {
                            Text("Quitar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = cantidad(item.cantidad),
                            onValueChange = {
                                viewModel.cambiarCantidad(indice, it.replace(",", ".").toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Cant.") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = cantidad(item.descuentoPct),
                            onValueChange = {
                                viewModel.cambiarDescuentoItem(indice, it.replace(",", ".").toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Dto. %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            TextoMuted("${Dinero.formatear(item.precioUnitCentavos)} c/u")
                            Text(
                                Dinero.formatear(item.subtotalCentavos),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = descuentoGeneral,
                        onValueChange = { descuentoGeneral = it },
                        label = { Text("Descuento general %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas (opcional)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SelectorOpciones(
                        etiqueta = "Cuenta donde entra el cobro",
                        seleccionado = cuentas.firstOrNull { it.nombre == cuenta },
                        opciones = cuentas,
                        textoOpcion = { it.display },
                        onSeleccion = { cuenta = it.nombre },
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = registrarIngreso, onCheckedChange = { registrarIngreso = it })
                        Text("Registrar el cobro como ingreso", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = descontarStock, onCheckedChange = { descontarStock = it })
                        Text("Descontar del stock", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            TarjetaApp(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                conBorde = false,
            ) {
                if (descuentoPct > 0) {
                    Row {
                        Text("Subtotal", modifier = Modifier.weight(1f))
                        Text(Dinero.formatear(subtotal))
                    }
                    Row {
                        Text("Descuento ${cantidad(descuentoPct)}%", modifier = Modifier.weight(1f))
                        Text("- ${Dinero.formatear(subtotal - total)}")
                    }
                }
                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(
                        Dinero.formatear(total),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }

            PrimaryButton(
                text = "Emitir factura",
                onClick = {
                    viewModel.emitir(
                        cliente = cliente,
                        documento = documento,
                        fecha = fecha,
                        descuentoPct = descuentoPct,
                        notas = notas,
                        cuenta = cuenta,
                        descontarStock = descontarStock,
                        registrarIngreso = registrarIngreso,
                    )
                },
                enabled = items.isNotEmpty() && cliente.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            TextoMuted("Comprobante interno. No reemplaza una factura fiscal de ARCA.")
        }
    }

    if (agregandoManual) {
        LineaManualDialog(
            onCerrar = { agregandoManual = false },
            onAgregar = { desc, cant, precio, dto ->
                viewModel.agregarManual(desc, cant, precio, dto)
                agregandoManual = false
            },
        )
    }

    // Emitida: se ofrece el PDF y se vuelve al listado.
    emitida?.let { factura ->
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Factura emitida") },
            text = {
                Column {
                    Text("N° ${factura.numero} · ${Dinero.formatear(factura.totalCentavos)}")
                    if (factura.registradaEnCaja) {
                        TextoMuted("El cobro quedó registrado como ingreso.")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = FacturaPdf.generar(context, factura, negocioNombre)
                        Compartir.archivo(context, uri, "application/pdf")
                        onBack()
                    },
                ) { Text("Compartir PDF") }
            },
            dismissButton = { TextButton(onClick = onBack) { Text("Listo") } },
        )
    }

    when (val a = aviso) {
        AvisoFactura.RequierePro -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Facturar es una función Pro") },
            text = {
                Text(
                    "Con Pro podés cargar tu catálogo, escanear códigos de barras, facturar y " +
                        "llevar el stock, además de los negocios ilimitados y los remitos.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.descartarAviso(); onVerPlanes() }) { Text("Ver Pro") }
            },
            dismissButton = { TextButton(onClick = viewModel::descartarAviso) { Text("Ahora no") } },
        )

        is AvisoFactura.CodigoDesconocido -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Código no encontrado") },
            text = {
                Text(
                    "El código ${a.codigo} no está en el catálogo de este negocio. Cargalo en " +
                        "Productos, o agregá la línea a mano.",
                )
            },
            confirmButton = { TextButton(onClick = viewModel::descartarAviso) { Text("Entendido") } },
        )

        is AvisoFactura.Error -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Falta algo") },
            text = { Text(a.mensaje) },
            confirmButton = { TextButton(onClick = viewModel::descartarAviso) { Text("Entendido") } },
        )

        null -> Unit
    }
}

/** Línea que no viene del catálogo: un servicio, un envío, una mano de obra. */
@Composable
private fun LineaManualDialog(
    onCerrar: () -> Unit,
    onAgregar: (String, Double, Long, Double) -> Unit,
) {
    var descripcion by remember { mutableStateOf("") }
    var cant by remember { mutableStateOf("1") }
    var precio by remember { mutableStateOf("") }
    var dto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Línea a mano") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = cant,
                    onValueChange = { cant = it },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                CampoMonto(valor = precio, onChange = { precio = it }, etiqueta = "Precio unitario")
                OutlinedTextField(
                    value = dto,
                    onValueChange = { dto = it },
                    label = { Text("Descuento %") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = descripcion.isNotBlank(),
                onClick = {
                    onAgregar(
                        descripcion,
                        cant.replace(",", ".").toDoubleOrNull() ?: 1.0,
                        Dinero.parsearACentavos(precio) ?: 0L,
                        dto.replace(",", ".").toDoubleOrNull() ?: 0.0,
                    )
                },
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } },
    )
}

/** "2" en vez de "2.0" en los campos de cantidad y porcentaje. */
internal fun cantidad(valor: Double): String =
    if (valor == Math.floor(valor)) valor.toLong().toString() else valor.toString()
