package com.tallerapp.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.tallerapp.core.ui.icons.IconosApp

/** Un destino de navegación con su ícono de línea (mismo trazo que el escritorio). */
data class DestinoNav(val etiqueta: String, val ruta: String, val icono: ImageVector)

/** Destinos raíz: los que aparecen en la barra inferior (como la barra lateral del escritorio). */
val DESTINOS_RAIZ = listOf(
    DestinoNav("Inicio", Destination.DASHBOARD, IconosApp.Inicio),
    DestinoNav("Movimientos", Destination.FINANZAS, IconosApp.Movimientos),
    DestinoNav("Me deben", Destination.DEUDAS, IconosApp.Deudas),
    DestinoNav("Reportes", Destination.REPORTES, IconosApp.Reportes),
)

/** Destinos secundarios: solo en el menú lateral. */
val DESTINOS_SECUNDARIOS = listOf(
    DestinoNav("Agenda", Destination.AGENDA, IconosApp.Agenda),
    DestinoNav("Negocios", Destination.NEGOCIOS, IconosApp.Negocios),
    DestinoNav("Calculadora", Destination.CALCULADORA, IconosApp.Calculadora),
    DestinoNav("Configuración", Destination.AJUSTES, IconosApp.Configuracion),
)
