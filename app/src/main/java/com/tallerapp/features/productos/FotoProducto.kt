package com.tallerapp.features.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.tallerapp.core.util.ImagenProducto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Foto del producto, o un cajón de emoji cuando no tiene.
 *
 * El archivo se decodifica fuera del hilo principal: son unos 100 KB de JPEG y hacerlo en
 * la composición trababa el scroll de la lista.
 */
@Composable
fun FotoProducto(ruta: String, lado: Dp = 52.dp, modifier: Modifier = Modifier) {
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, ruta) {
        value = if (ruta.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { ImagenProducto.cargar(ruta)?.asImageBitmap() }
        }
    }

    val forma = RoundedCornerShape(10.dp)
    val imagen = bitmap
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(lado).clip(forma),
        )
    } else {
        Box(
            modifier = modifier
                .size(lado)
                .clip(forma)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text("📦", style = MaterialTheme.typography.titleMedium)
        }
    }
}
