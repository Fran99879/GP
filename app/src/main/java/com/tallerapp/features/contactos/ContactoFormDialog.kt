package com.tallerapp.features.contactos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tallerapp.core.ui.components.BotonGhost
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.components.SelectorOpciones
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.domain.model.Contacto
import com.tallerapp.domain.model.TipoContacto

/**
 * Ficha de un cliente o proveedor, en un diálogo a pantalla completa (mismo patrón que el
 * formulario de productos, insets incluidos: sin ellos el teclado tapa el botón Guardar).
 *
 * Solo el nombre es obligatorio. El resto se completa cuando hace falta: en el mostrador se
 * carga un nombre y se sigue vendiendo.
 */
@Composable
fun ContactoFormDialog(
    inicial: Contacto?,
    tipoPorDefecto: String,
    onCerrar: () -> Unit,
    onGuardar: (Contacto) -> Unit,
    onEliminar: (() -> Unit)? = null,
) {
    val editando = inicial != null

    var nombre by remember { mutableStateOf(inicial?.nombre ?: "") }
    var tipo by remember { mutableStateOf(inicial?.tipo ?: tipoPorDefecto) }
    var documento by remember { mutableStateOf(inicial?.documento ?: "") }
    var telefono by remember { mutableStateOf(inicial?.telefono ?: "") }
    var email by remember { mutableStateOf(inicial?.email ?: "") }
    var direccion by remember { mutableStateOf(inicial?.direccion ?: "") }
    var nota by remember { mutableStateOf(inicial?.nota ?: "") }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TituloPantalla(
                    if (editando) "Editar contacto"
                    else "Nuevo ${TipoContacto.etiqueta(tipoPorDefecto).lowercase()}",
                )

                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SelectorOpciones(
                            etiqueta = "Tipo",
                            seleccionado = tipo,
                            opciones = TipoContacto.todos,
                            textoOpcion = { "${TipoContacto.icono(it)}  ${TipoContacto.etiqueta(it)}" },
                            onSeleccion = { tipo = it },
                        )
                        TextoMuted("«Cliente y proveedor» aparece en las dos listas.")
                    }
                }

                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = documento,
                            onValueChange = { documento = it },
                            label = { Text("CUIT / DNI") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = nota,
                            onValueChange = { nota = it },
                            label = { Text("Nota") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                PrimaryButton(
                    text = "Guardar",
                    onClick = {
                        onGuardar(
                            (inicial ?: Contacto(nombre = "")).copy(
                                nombre = nombre,
                                tipo = tipo,
                                documento = documento,
                                telefono = telefono,
                                email = email,
                                direccion = direccion,
                                nota = nota,
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
