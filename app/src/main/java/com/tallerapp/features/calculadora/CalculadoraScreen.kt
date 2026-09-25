package com.tallerapp.features.calculadora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Calculadora simple estilo teléfono (para no tener que salir de la app al anotar montos).
 * Ejecución inmediata: valor acumulado + operador pendiente + operando en pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraScreen(onBack: () -> Unit) {
    val estado = remember { CalculadoraEstado() }
    var pantalla by remember { mutableStateOf("0") }
    var formula by remember { mutableStateOf("") }

    fun tocar(accion: () -> Unit) {
        accion()
        pantalla = estado.pantalla
        formula = estado.formula
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        formula.ifEmpty { " " },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        pantalla,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val filas = listOf(
                listOf("C" to Tipo.FUNCION, "±" to Tipo.FUNCION, "%" to Tipo.FUNCION, "÷" to Tipo.OPERADOR),
                listOf("7" to Tipo.NUMERO, "8" to Tipo.NUMERO, "9" to Tipo.NUMERO, "×" to Tipo.OPERADOR),
                listOf("4" to Tipo.NUMERO, "5" to Tipo.NUMERO, "6" to Tipo.NUMERO, "−" to Tipo.OPERADOR),
                listOf("1" to Tipo.NUMERO, "2" to Tipo.NUMERO, "3" to Tipo.NUMERO, "+" to Tipo.OPERADOR),
                listOf("0" to Tipo.NUMERO, "," to Tipo.NUMERO, "⌫" to Tipo.FUNCION, "=" to Tipo.IGUAL),
            )

            filas.forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    fila.forEach { (etiqueta, tipo) ->
                        TeclaCalc(
                            etiqueta = etiqueta,
                            tipo = tipo,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            onClick = { tocar { estado.pulsar(etiqueta) } },
                        )
                    }
                }
            }
        }
    }
}

private enum class Tipo { NUMERO, OPERADOR, FUNCION, IGUAL }

@Composable
private fun TeclaCalc(etiqueta: String, tipo: Tipo, modifier: Modifier, onClick: () -> Unit) {
    val colores = when (tipo) {
        Tipo.IGUAL -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
        Tipo.OPERADOR -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Tipo.FUNCION -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Tipo.NUMERO -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            colors = colores,
            modifier = Modifier.fillMaxSize().aspectRatio(1f, matchHeightConstraintsFirst = true),
        ) {
            Text(etiqueta, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
