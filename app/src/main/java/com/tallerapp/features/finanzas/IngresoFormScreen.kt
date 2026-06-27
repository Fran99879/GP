package com.tallerapp.features.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.CampoTexto
import com.tallerapp.core.ui.components.FechaPicker
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.components.SelectorOpciones
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.features.finanzas.ingreso.IngresoFormViewModel

/** Formulario de alta/edición de ingreso (Frozen Spec 9.5, validaciones V-2/V-6). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresoFormScreen(
    viewModel: IngresoFormViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.guardadoOk) {
        if (state.guardadoOk) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.titulo) },
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
            state.errores.general?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTexto(
                valor = state.monto,
                onChange = viewModel::onMontoChange,
                etiqueta = "Monto *",
                tipoTeclado = KeyboardType.Decimal,
                error = state.errores.monto,
                habilitado = state.editable,
            )
            CampoTexto(
                valor = state.concepto,
                onChange = viewModel::onConceptoChange,
                etiqueta = "Concepto *",
                error = state.errores.concepto,
                habilitado = state.editable,
            )
            SelectorOpciones(
                etiqueta = "Método de pago *",
                seleccionado = state.metodo,
                opciones = MetodoPago.entries,
                textoOpcion = { it.etiqueta },
                onSeleccion = viewModel::onMetodoChange,
                error = state.errores.metodo,
            )

            if (state.esMixto) {
                Text("Reparto del pago mixto", style = MaterialTheme.typography.titleSmall)
                state.errores.reparto?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                CampoTexto(state.efectivo, viewModel::onEfectivoChange, "Efectivo", tipoTeclado = KeyboardType.Decimal, habilitado = state.editable)
                CampoTexto(state.transferencia, viewModel::onTransferenciaChange, "Transferencia", tipoTeclado = KeyboardType.Decimal, habilitado = state.editable)
                CampoTexto(state.tarjeta, viewModel::onTarjetaChange, "Tarjeta", tipoTeclado = KeyboardType.Decimal, habilitado = state.editable)
                CampoTexto(state.mercadoPago, viewModel::onMercadoPagoChange, "Mercado Pago", tipoTeclado = KeyboardType.Decimal, habilitado = state.editable)
            }

            FechaPicker(fechaMillis = state.fecha, onFechaChange = viewModel::onFechaChange)

            PrimaryButton("Guardar", viewModel::guardar)
        }
    }
}
