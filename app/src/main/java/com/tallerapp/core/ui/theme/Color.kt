package com.tallerapp.core.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de marca provista por el usuario: azul marino, azul, marrón y tostado.
val Navy = Color(0xFF02223A)   // azul marino (más oscuro)
val Azul = Color(0xFF023A5D)   // azul
val Marron = Color(0xFF98643A) // marrón / ocre
val Tostado = Color(0xFFC99A6D) // tostado / camel

// Tintes derivados para fondos y contenedores.
val TostadoClaro = Color(0xFFEDDFCD) // contenedor cálido (tema claro)
val FondoClaro = Color(0xFFF5F1EA)   // fondo cálido claro
val NavyProfundo = Color(0xFF011526) // fondo del tema oscuro
val SuperficieOscura = Color(0xFF032B47)
val GrisCalido700 = Color(0xFF5C5348)
val GrisCalido200 = Color(0xFFDCD3C5)
val BlancoCalido = Color(0xFFEDE6DB)
val White = Color(0xFFFFFFFF)

// Colores semánticos de finanzas: verde ingresos, rojo gastos (pedido del usuario), ámbar deudas.
val Ingreso = Color(0xFF1E9E5B)
val Gasto = Color(0xFFD1493F)
val Deuda = Color(0xFFB8791F)

// Paleta del gráfico de gastos por categoría: derivada de la marca, tonos distinguibles.
val CategoriaColores = listOf(
    Azul,               // azul
    Marron,             // marrón
    Tostado,            // tostado
    Color(0xFF0A6E8C),  // teal azulado
    Color(0xFF6B4E2E),  // marrón oscuro
    Color(0xFF4A7C59),  // verde apagado
    Color(0xFF8A8D91),  // gris (Otros)
)

/** Verde de "meta cumplida" (mismo que el escritorio: #27AE60). */
val MetaCompleta = Color(0xFF27AE60)
