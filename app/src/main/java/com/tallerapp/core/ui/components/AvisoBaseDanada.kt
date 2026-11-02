package com.tallerapp.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.backup.EstadoBase

/**
 * Aviso de base de datos dañada. No se ve nunca, salvo que algo haya salido mal de verdad.
 *
 * Aparece cuando el control de integridad del arranque falla. Dice qué pasa en castellano y deja
 * el camino a mano: restaurar una copia desde Configuración. No ofrece "reparar" porque no hay
 * tal cosa, ni borra nada por su cuenta: esa decisión es del usuario.
 */
@Composable
fun AvisoBaseDanada(onVerAjustes: () -> Unit) {
    val danada by EstadoBase.danada.collectAsStateWithLifecycle()
    if (!danada) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⚠ Hay un problema con tus datos", fontWeight = FontWeight.Bold)
            Text(
                "La base de datos está dañada, así que puede faltar información o la app puede " +
                    "cerrarse sola. Si tenés una copia de seguridad, restaurala.",
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = onVerAjustes) { Text("Ir a Configuración") }
        }
    }
}
