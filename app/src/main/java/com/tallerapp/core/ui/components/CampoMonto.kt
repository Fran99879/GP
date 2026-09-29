package com.tallerapp.core.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.fillMaxWidth
import com.tallerapp.core.util.Dinero

/**
 * Campo de importe: muestra el signo de la moneda y va separando los miles mientras se
 * escribe, así "2500000" se lee "$ 2.500.000" y no hay que contar ceros.
 *
 * El formato es **solo visual** ([VisualTransformation]): el valor que sale por [onChange]
 * sigue siendo crudo ("2500000.5"), que es lo que esperan los ViewModels y
 * [Dinero.parsearACentavos]. Guardar el texto ya formateado obligaría a limpiarlo en cada
 * pantalla y a lidiar con el cursor saltando al final en cada tecla.
 */
@Composable
fun CampoMonto(
    valor: String,
    onChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onChange(filtrarMonto(it)) },
        label = { Text(etiqueta) },
        isError = error != null,
        enabled = habilitado,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        visualTransformation = MilesVisualTransformation(Dinero.simbolo),
        supportingText = error?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Deja solo lo que puede ser un importe: dígitos, **un** separador decimal y hasta dos
 * decimales. El separador se normaliza a punto, que es lo que parsea [Dinero].
 *
 * Con esto el usuario no puede escribir "1.2.3" ni pegar texto raro, y los puntos de miles
 * que ve en pantalla nunca entran al valor.
 */
internal fun filtrarMonto(texto: String): String {
    val salida = StringBuilder()
    var haySeparador = false
    var decimales = 0
    for (c in texto) {
        when {
            c.isDigit() -> {
                if (!haySeparador) {
                    salida.append(c)
                } else if (decimales < 2) {
                    salida.append(c)
                    decimales++
                }
            }
            // Un separador solo, y nunca como primer carácter ("," suelto no es un número).
            (c == ',' || c == '.') && !haySeparador && salida.isNotEmpty() -> {
                haySeparador = true
                salida.append('.')
            }
        }
    }
    return salida.toString()
}

/**
 * Pinta el importe con separador de miles y el signo de la moneda, sin tocar el valor real.
 *
 * El mapa de posiciones se arma mientras se formatea: cada carácter del texto crudo anota
 * dónde quedó en el visible. Sin ese mapa el cursor se descoloca apenas aparece un punto.
 */
internal class MilesVisualTransformation(private val simbolo: String) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val crudo = text.text
        // Con el campo vacío no se muestra el signo solo: se vería como un valor escrito.
        if (crudo.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)

        val separador = crudo.indexOf('.')
        val entera = if (separador >= 0) crudo.substring(0, separador) else crudo
        val decimales = if (separador >= 0) crudo.substring(separador + 1) else ""

        val visible = StringBuilder("$simbolo ")
        // posiciones[i] = dónde cae el carácter crudo i dentro del texto visible.
        val posiciones = IntArray(crudo.length + 1)

        entera.forEachIndexed { i, digito ->
            posiciones[i] = visible.length
            visible.append(digito)
            val restantes = entera.length - 1 - i
            if (restantes > 0 && restantes % 3 == 0) visible.append('.')
        }
        if (separador >= 0) {
            posiciones[separador] = visible.length
            visible.append(',')
            decimales.forEachIndexed { i, digito ->
                posiciones[separador + 1 + i] = visible.length
                visible.append(digito)
            }
        }
        posiciones[crudo.length] = visible.length

        val mapeo = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                posiciones[offset.coerceIn(0, crudo.length)]

            override fun transformedToOriginal(offset: Int): Int {
                // El último carácter crudo que empieza antes del cursor visible.
                var resultado = 0
                for (i in 0..crudo.length) {
                    if (posiciones[i] <= offset) resultado = i else break
                }
                return resultado
            }
        }

        return TransformedText(AnnotatedString(visible.toString()), mapeo)
    }
}
