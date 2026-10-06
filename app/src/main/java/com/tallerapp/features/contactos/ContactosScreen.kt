package com.tallerapp.features.contactos

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloBarra
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.domain.model.Contacto
import com.tallerapp.domain.model.TipoContacto

/**
 * Listado de clientes o de proveedores, según el tipo del ViewModel: buscar, crear, editar
 * y eliminar. Es una sola pantalla porque el contenido es el mismo; lo único que cambia es
 * a quién lista y los textos.
 *
 * El candado de Pro salta al guardar, no esconde la pantalla (mismo criterio que Negocios
 * y Productos).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    viewModel: ContactosViewModel,
    onBack: () -> Unit,
    onVerPlanes: () -> Unit = {},
) {
    val contactos by viewModel.contactos.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val abierto by viewModel.abierto.collectAsStateWithLifecycle()

    var creando by remember { mutableStateOf(false) }
    var borrando by remember { mutableStateOf<Contacto?>(null) }

    val esProveedor = viewModel.tipo == TipoContacto.PROVEEDOR
    val titulo = if (esProveedor) "Proveedores" else "Clientes"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra(titulo) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creando = true },
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
            TituloPantalla(titulo)
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::buscar,
                label = { Text("Buscar por nombre, documento o teléfono") },
                singleLine = true,
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        TextButton(onClick = { viewModel.buscar("") }) { Text("Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (contactos.isEmpty()) {
                TextoMuted(
                    if (busqueda.isNotBlank()) {
                        "Ninguno coincide con «$busqueda»."
                    } else if (esProveedor) {
                        "Todavía no cargaste proveedores. Tocá «Nuevo» para agregar el primero."
                    } else {
                        "Todavía no cargaste clientes. También se cargan solos cuando facturás " +
                            "o anotás una deuda a nombre de alguien."
                    },
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contactos, key = { it.id }) { c ->
                        FilaContacto(contacto = c, onClick = { viewModel.abrir(c) })
                    }
                }
            }
        }
    }

    if (creando) {
        ContactoFormDialog(
            inicial = null,
            tipoPorDefecto = viewModel.tipo,
            onCerrar = { creando = false },
            onGuardar = { c -> viewModel.guardar(c) { creando = false } },
        )
    }

    abierto?.let { c ->
        ContactoFormDialog(
            inicial = c,
            tipoPorDefecto = viewModel.tipo,
            onCerrar = { viewModel.abrir(null) },
            onGuardar = { editado -> viewModel.guardar(editado) { viewModel.abrir(null) } },
            onEliminar = { viewModel.abrir(null); borrando = c },
        )
    }

    borrando?.let { c ->
        AlertDialog(
            onDismissRequest = { borrando = null },
            title = { Text("Eliminar «${c.nombre}»") },
            text = {
                Text(
                    "Se borra la ficha. Las facturas, deudas y gastos que lo nombran no " +
                        "cambian: guardan el nombre con el que se registraron.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(c); borrando = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { borrando = null }) { Text("Cancelar") } },
        )
    }

    when (val a = aviso) {
        AvisoContacto.RequierePro -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Las fichas son una función Pro") },
            text = {
                Text(
                    "Clientes y proveedores, con sus datos y su historial, son parte de Pro, " +
                        "el plan para usar la app en tu negocio.\n\n" +
                        "El plan gratuito sigue anotando a quién le fiaste en «Me deben».",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.descartarAviso(); onVerPlanes() }) { Text("Ver Pro") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::descartarAviso) { Text("Ahora no") }
            },
        )

        is AvisoContacto.NombreRepetido -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("Ese nombre ya está") },
            text = { Text("«${a.existente.nombre}» ya figura en este negocio. ¿Lo abrís?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.descartarAviso(); viewModel.abrir(a.existente) },
                ) { Text("Abrir") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::descartarAviso) { Text("Cerrar") }
            },
        )

        is AvisoContacto.Error -> AlertDialog(
            onDismissRequest = viewModel::descartarAviso,
            title = { Text("No se pudo guardar") },
            text = { Text(a.mensaje) },
            confirmButton = { TextButton(onClick = viewModel::descartarAviso) { Text("Entendido") } },
        )

        null -> Unit
    }
}

/** Fila del listado: ícono según el tipo, nombre y los datos que haya cargados. */
@Composable
private fun FilaContacto(contacto: Contacto, onClick: () -> Unit) {
    TarjetaApp(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(TipoContacto.icono(contacto.tipo), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contacto.nombre, fontWeight = FontWeight.SemiBold)
                if (contacto.detalle.isNotBlank()) TextoMuted(contacto.detalle)
                if (contacto.tipo == TipoContacto.AMBOS) {
                    TextoMuted(TipoContacto.etiqueta(contacto.tipo))
                }
            }
        }
    }
}
