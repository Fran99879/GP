package com.tallerapp.features.licencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.licensemanager.sdk.core.LicenseStatus
import com.tallerapp.core.license.Licencia
import com.tallerapp.core.ui.components.PrimaryButton

/**
 * Pantalla de activación por licencia. Muestra el Device ID (para generar la licencia),
 * permite pegar la licencia y la valida. Al quedar VÁLIDA, avisa con [onActivada].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivacionScreen(onActivada: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sdk = remember { Licencia.sdk(context) }
    val deviceId = remember { Licencia.deviceId(context) }

    var licencia by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Activar Mis Finanzas") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Esta app requiere una licencia para usarse.",
                style = MaterialTheme.typography.bodyLarge,
            )

            // Device ID.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Pasá este código de dispositivo a tu proveedor:", fontWeight = FontWeight.SemiBold)
                    Text(deviceId, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(deviceId)) }) {
                        Text("Copiar código")
                    }
                }
            }

            // Pegar licencia.
            Text("2. Pegá la licencia que te entregaron:", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = licencia,
                onValueChange = { licencia = it },
                label = { Text("Licencia") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton("Activar", onClick = {
                // Limpia la licencia pegada: saca espacios/saltos y normaliza base64 url-safe.
                val limpia = licencia
                    .filterNot { it.isWhitespace() }
                    .replace('-', '+')
                    .replace('_', '/')
                val importada = sdk.importLicense(limpia)
                if (importada.isFailure) {
                    esError = true
                    mensaje = "No se pudo leer la licencia. Revisá que esté completa."
                    return@PrimaryButton
                }
                when (sdk.validate()) {
                    LicenseStatus.VALID -> onActivada()
                    LicenseStatus.EXPIRED -> { esError = true; mensaje = "La licencia está vencida." }
                    LicenseStatus.WRONG_DEVICE -> { esError = true; mensaje = "La licencia es de otro dispositivo." }
                    LicenseStatus.WRONG_PRODUCT -> { esError = true; mensaje = "La licencia es de otra aplicación." }
                    LicenseStatus.CLOCK_TAMPERED -> { esError = true; mensaje = "La fecha del dispositivo parece incorrecta." }
                    LicenseStatus.INVALID_SIGNATURE -> { esError = true; mensaje = "La licencia no es válida." }
                }
            })

            mensaje?.let {
                Text(
                    it,
                    color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }

            if (!Licencia.configurada) {
                Text(
                    "Nota: todavía no está configurada la clave pública del sistema de licencias. " +
                        "Ninguna licencia validará hasta cargarla (ver LICENSING-SETUP.md).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
