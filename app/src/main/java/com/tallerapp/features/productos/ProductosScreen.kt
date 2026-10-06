package com.tallerapp.features.productos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.scan.recordarEscaner
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoFila
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloBarra
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.ImagenProducto
import com.tallerapp.domain.model.Producto

/**
 * Catálogo de productos del negocio activo: buscar, escanear, crear, editar y eliminar.
 *
 * El botón de escanear de esta pantalla es un **atajo de búsqueda**: si el código ya está
 * cargado abre ese producto, y si no, arranca un producto nuevo con el código puesto. Es el
 * camino rápido para cargar mercadería con el lector en la mano.
 *
 * El candado de Pro se avisa al guardar, no esconde la pantalla: nadie compra lo que no
 * sabe que existe (mismo criterio que Negocios).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    viewModel: ProductosViewModel,
    onBack: () -> Unit,
    onVerPlanes: () -> Unit = {},
) {
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val abrir by viewModel.abrirProducto.collectAsStateWithLifecycle()

    var creando by remember { mutableStateOf(false) }
    var codigoNuevo by remember { mutableStateOf("") }
    var borrando by remember { mutableStateOf<Producto?>(null) }

    val escanear = recordarEscaner(
        onCodigo = { viewModel.escaneoEnListado(it) },
        onError = { /* El aviso ya lo da el formulario; acá no se interrumpe la lista. */ },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Productos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
                actions = { TextButton(onClick = escanear) { Text("Escanear") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { codigoNuevo = ""; creando = true },
                text = { Text("Nuevo") },
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
            TituloPantalla("Catálogo")
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::buscar,
                label = { Text("Buscar por nombre o código") },
                singleLine = true,
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        TextButton(onClick = { viewModel.buscar("") }) { Text("Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (productos.isEmpty()) {
                TextoMuted(
                    if (busqueda.isBlank()) {
                        "Todavía no cargaste productos. Tocá «Escanear» para cargar uno con el " +
                            "código de barras, o «Nuevo» para hacerlo a mano."
                    } else {
                        "Ningún producto coincide con «$busqueda»."
                    },
                )
                if (busqueda.isNotBlank()) {
                    TextButton(onClick = { codigoNuevo = busqueda; creando = true }) {
                        Text("Crear producto con este código")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(productos, key = { it.id }) { p ->
                        FilaProducto(producto = p, onClick = { viewModel.abrir(p) })
                    }
                }
            }
        }
    }

    if (creando) {
        ProductoFormDialog(
            inicial = null,
            codigoPrecargado = codigoNuevo,
            onCerrar = { creando = false },
            onGuardar = { p -> viewModel.guardar(p) { creando = false } },
        )
    }

    abrir?.let { p ->
        ProductoFormDialog(
            inicial = p,
            onCerrar = { viewModel.abrir(null) },
            onGuardar = { editado -> viewModel.guardar(editado) { viewModel.abrir(null) } },
            onEliminar = { viewModel.abrir(null); borrando = p },
        )
    }

    borrando?.let { p ->
        AlertDialog(
            onDismissRequest = { borrando = null },
            title = { Text("Eliminar «${p.nombre}»") },
            text = {
                Text(
                    "Se saca del catálogo. Las facturas ya emitidas no cambian: guardan la " +
                        "descripción y el precio con los que se cobraron.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // La foto es un archivo propio de la app: si no se borra acá, queda huérfana.
                        ImagenProducto.borrar(p.imagen)
                        viewModel.eliminar(p)
                        borrando = null
                    },
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { borrando = null }) { Text("Cancelar") } },
        )
    }

    when (val a = aviso) {
        AvisoProducto.RequierePro -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("El catálogo es una función Pro") },
            text = {
                Text(
                    "Cargar productos, escanear códigos de barras y facturar son parte de Pro, " +
                        "el plan para usar la app en tu negocio.\n\n" +
                        "El plan gratuito mantiene todas las funciones de finanzas personales.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.descartarAviso(); onVerPlanes() }) { Text("Ver Pro") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::descartarAviso) { Text("Ahora no") }
            },
        )

        is AvisoProducto.CodigoRepetido -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Código repetido") },
            text = { Text("Ese código de barras ya lo tiene «${a.nombre}». Cambialo o editá ese producto.") },
            confirmButton = { TextButton(onClick = viewModel::descartarAviso) { Text("Entendido") } },
        )

        is AvisoProducto.YaExiste -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Ya está cargado") },
            text = { Text("«${a.producto.nombre}» tiene ese código. ¿Lo abrís para editarlo?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.descartarAviso(); viewModel.abrir(a.producto) },
                ) { Text("Abrir") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::descartarAviso) { Text("Cerrar") }
            },
        )

        is AvisoProducto.Error -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("No se pudo guardar") },
            text = { Text(a.mensaje) },
            confirmButton = { TextButton(onClick = viewModel::descartarAviso) { Text("Entendido") } },
        )

        null -> Unit
    }
}

/** Fila del catálogo: foto, nombre, código, precio (tachado si hay descuento) y stock. */
@Composable
private fun FilaProducto(producto: Producto, onClick: () -> Unit) {
    TarjetaApp(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FotoProducto(producto.imagen)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Con tope de renglones: en una pantalla angosta el nombre se corta con "…" en vez
                // de partirse letra por letra (ver FilaLista).
                TextoFila(producto.nombre, fontWeight = FontWeight.SemiBold)
                if (producto.codigoBarras.isNotBlank()) {
                    TextoFila(
                        producto.codigoBarras,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                // El stock y el aviso, en un solo texto: así no compiten por el ancho entre sí.
                val stock = buildString {
                    append("Stock: ${textoCantidad(producto.stock)}")
                    if (producto.stockBajo) append("  · stock bajo")
                }
                TextoFila(
                    stock,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.stockBajo) Gasto else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (producto.stockBajo) FontWeight.SemiBold else null,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (producto.tieneDescuento) {
                    Text(
                        Dinero.formatear(producto.precioCentavos),
                        style = MaterialTheme.typography.labelSmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                Text(
                    Dinero.formatear(producto.precioFinalCentavos),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    softWrap = false,
                )
                if (producto.tieneDescuento) {
                    Text(
                        "-${textoCantidad(producto.descuentoPct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** "3" en vez de "3.0", pero conserva los decimales cuando los hay. */
internal fun textoCantidad(valor: Double): String =
    if (valor == Math.floor(valor)) valor.toLong().toString() else valor.toString()
