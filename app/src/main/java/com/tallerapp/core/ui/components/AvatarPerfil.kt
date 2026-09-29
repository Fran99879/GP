package com.tallerapp.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tallerapp.R
import com.tallerapp.core.ui.theme.TemaApp

/**
 * Avatares que trae la app.
 *
 * Son dibujos de 256×256 en WebP con fondo transparente (unos 15 KB cada uno): alcanzan
 * para el tamaño más grande en que se muestran (80dp) y no hacen falta versiones por
 * densidad. Vienen recortados al círculo, así que no necesitan máscara en la UI.
 */
object AvataresApp {

    /** (id guardado en preferencias, recurso). El id se guarda, el recurso puede cambiar. */
    val todos = listOf(
        "hombre_1" to R.drawable.avatar_hombre_1,
        "hombre_2" to R.drawable.avatar_hombre_2,
        "mujer_1" to R.drawable.avatar_mujer_1,
        "mujer_2" to R.drawable.avatar_mujer_2,
    )

    /** Recurso del id guardado, o null si no eligió ninguno (o el id ya no existe). */
    fun recurso(id: String): Int? = todos.firstOrNull { it.first == id }?.second
}

/**
 * Foto de perfil: el avatar elegido, o un marcador genérico si todavía no eligió.
 * Lee [TemaApp.avatar], así que cambia solo en toda la app al elegir otro.
 */
@Composable
fun AvatarPerfil(modifier: Modifier = Modifier, lado: Dp = 34.dp) {
    AvatarPerfil(id = TemaApp.avatar, modifier = modifier, lado = lado)
}

/** Igual que [AvatarPerfil] pero con un id fijo: lo usa el selector del perfil. */
@Composable
fun AvatarPerfil(id: String, modifier: Modifier = Modifier, lado: Dp = 34.dp) {
    val recurso = AvataresApp.recurso(id)
    if (recurso != null) {
        Image(
            painter = painterResource(recurso),
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(lado).clip(CircleShape),
        )
    } else {
        Box(
            modifier = modifier
                .size(lado)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // El emoji se escala con el círculo: el mismo componente sirve para 34dp y 80dp.
            Text("👤", style = TextStyle(fontSize = MaterialTheme.typography.titleMedium.fontSize * (lado / 34.dp)))
        }
    }
}
