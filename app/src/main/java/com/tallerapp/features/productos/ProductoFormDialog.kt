package com.tallerapp.features.productos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tallerapp.core.scan.recordarEscaner
import com.tallerapp.core.ui.components.BotonGhost
import com.tallerapp.core.ui.components.CampoMonto
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.ImagenProducto
import com.tallerapp.domain.model.Precios
import com.tallerapp.domain.model.Producto

/**
 * Alta y edición de un producto, en un diálogo a pantalla completa.
 *
 * Tres cosas que no son obvias:
 * - El **precio final** se muestra en vivo mientras se escribe el descuento, con la misma
 *   cuenta que usa la factura ([Precios]), así el usuario ve lo que va a cobrar.
 * - La foto se **copia** al almacenamiento de la app apenas se elige (el Uri del selector
 *   caduca), y si se reemplaza, la copia anterior se borra.
 * - El escáner escribe en el campo del código, que queda editable para los productos que
 *   no tienen etiqueta.
 */
@Composable
fun ProductoFormDialog(
    inicial: Producto?,
    onCerrar: () -> Unit,
    onGuardar: (Producto) -> Unit,
    onEliminar: (() -> Unit)? = null,
    codigoPrecargado: String = "",
) {
    val context = LocalContext.current
    val editando = inicial != null

    var nombre by remember { mutableStateOf(inicial?.nombre ?: "") }
    var codigo by remember { mutableStateOf(inicial?.codigoBarras ?: codigoPrecargado) }
    var descripcion by remember { mutableStateOf(inicial?.descripcion ?: "") }
    var precio by remember {
        mutableStateOf(inicial?.precioCentavos?.takeIf { it > 0 }?.let(Dinero::centavosAEntrada) ?: "")
    }
    var descuento by remember {
        mutableStateOf(inicial?.descuentoPct?.takeIf { it > 0 }?.let(::textoNumero) ?: "")
    }
    var stock by remember { mutableStateOf(inicial?.stock?.let(::textoNumero) ?: "") }
    var stockMinimo by remember {
        mutableStateOf(inicial?.stockMinimo?.takeIf { it > 0 }?.let(::textoNumero) ?: "")
    }
    var imagen by remember { mutableStateOf(inicial?.imagen ?: "") }
    var errorEscaner by remember { mutableStateOf<String?>(null) }

    val precioCentavos = Dinero.parsearACentavos(precio) ?: 0L
    val descuentoPct = (descuento.replace(",", ".").toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    val finalCentavos = Precios.conDescuento(precioCentavos, descuentoPct)

    val elegirFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val nueva = ImagenProducto.guardar(context, uri)
            if (nueva != null) {
                // La copia vieja ya no se referencia: borrarla evita que filesDir crezca solo.
                if (imagen.isNotBlank() && imagen != inicial?.imagen) ImagenProducto.borrar(imagen)
                imagen = nueva
            }
        }
    }

    val escanear = recordarEscaner(
        onCodigo = { leido ->
            codigo = leido
            errorEscaner = null
        },
        onError = { errorEscaner = it },
    )

    Dialog(
        onDismissRequest = onCerrar,
        // decorFitsSystemWindows = false para que el diálogo reciba los insets: sin esto el
        // teclado tapa el botón Guardar y no hay forma de llegar (Lecciones, punto 3).
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // safeDrawing incluye barra de estado, barra de navegación y teclado.
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TituloPantalla(if (editando) "Editar producto" else "Nuevo producto")

                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FotoProducto(imagen, lado = 72.dp)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            TextButton(
                                onClick = {
                                    elegirFoto.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                },
                            ) { Text(if (imagen.isBlank()) "Elegir foto" else "Cambiar foto") }
                            if (imagen.isNotBlank()) {
                                TextButton(
                                    onClick = {
                                        if (imagen != inicial?.imagen) ImagenProducto.borrar(imagen)
                                        imagen = ""
                                    },
                                ) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }

                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = codigo,
                            onValueChange = { codigo = it },
                            label = { Text("Código de barras") },
                            singleLine = true,
                            trailingIcon = { TextButton(onClick = escanear) { Text("Escanear") } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        errorEscaner?.let { TextoMuted(it) }
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (opcional)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CampoMonto(
                            valor = precio,
                            onChange = { precio = it },
                            etiqueta = "Precio de lista",
                        )
                        OutlinedTextField(
                            value = descuento,
                            onValueChange = { descuento = it },
                            label = { Text("Descuento %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Precio final",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                Dinero.formatear(finalCentavos),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (descuentoPct > 0 && precioCentavos > 0) {
                            TextoMuted(
                                "Descuento de ${Dinero.formatear(precioCentavos - finalCentavos)} " +
                                    "sobre ${Dinero.formatear(precioCentavos)}",
                            )
                        }
                    }
                }

                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = stockMinimo,
                            onValueChange = { stockMinimo = it },
                            label = { Text("Avisar cuando baje de") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextoMuted("Dejalo vacío si no querés aviso de stock bajo.")
                    }
                }

                PrimaryButton(
                    text = "Guardar",
                    onClick = {
                        onGuardar(
                            (inicial ?: Producto(nombre = "")).copy(
                                nombre = nombre,
                                codigoBarras = codigo,
                                descripcion = descripcion,
                                precioCentavos = precioCentavos,
                                descuentoPct = descuentoPct,
                                stock = stock.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                stockMinimo = stockMinimo.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                imagen = imagen,
                            ),
                        )
                    },
                    enabled = nombre.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonGhost("Cancelar", onCerrar, modifier = Modifier.weight(1f))
                    if (onEliminar != null) {
                        BotonGhost("Eliminar", onEliminar, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Muestra "3" en vez de "3.0" en los campos numéricos del formulario. */
private fun textoNumero(valor: Double): String =
    if (valor == Math.floor(valor)) valor.toLong().toString() else valor.toString()
