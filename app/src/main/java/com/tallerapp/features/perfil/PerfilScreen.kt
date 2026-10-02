package com.tallerapp.features.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding as paddingLayout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.tallerapp.core.ui.components.AvatarPerfil
import com.tallerapp.core.ui.components.AvataresApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.ui.theme.TemaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(TemaApp.nombre) }
    var guardado by remember { mutableStateOf(false) }
    // Se lee del estado global: al tocar otro avatar, la barra superior también cambia.
    val avatarActual = TemaApp.avatar

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().anchoContenido().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AvatarPerfil(id = avatarActual, lado = 88.dp)

            TextoMuted("Elegí tu foto")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // El primero saca la foto y vuelve al marcador genérico.
                OpcionAvatar("", avatarActual) { TemaApp.cambiarAvatar(context, "") }
                AvataresApp.todos.forEach { (id, _) ->
                    OpcionAvatar(id, avatarActual) { TemaApp.cambiarAvatar(context, id) }
                }
            }

            if (nombre.isNotBlank()) {
                Text("Hola, $nombre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; guardado = false },
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { TemaApp.cambiarNombre(context, nombre.trim()); guardado = true },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (guardado) "Guardado ✓" else "Guardar") }

            Text(
                "Tu nombre y tu foto se guardan solo en este teléfono.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Un avatar del selector, con anillo cuando es el elegido. */
@Composable
private fun OpcionAvatar(id: String, elegido: String, onClick: () -> Unit) {
    val seleccionado = id == elegido
    val borde = if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent
    AvatarPerfil(
        id = id,
        lado = 52.dp,
        modifier = Modifier
            .border(width = if (seleccionado) 3.dp else 0.dp, color = borde, shape = CircleShape)
            .clickable(onClick = onClick),
    )
}
