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
    DestinoNav("Planes", Destination.PLANES, IconosApp.Planes),
    DestinoNav("Configuración", Destination.AJUSTES, IconosApp.Configuracion),
)

/**
 * Herramientas de uso comercial: van juntas en su propio bloque del menú, con candado
 * mientras el plan sea Gratis. El bloque se muestra siempre — nadie compra lo que no sabe
 * que existe — y al tocar una con candado se abre Planes.
 */
val DESTINOS_PRO = listOf(
    DestinoNav("Productos", Destination.PRODUCTOS, IconosApp.Productos),
    DestinoNav("Clientes", Destination.CLIENTES, IconosApp.Clientes),
    DestinoNav("Proveedores", Destination.PROVEEDORES, IconosApp.Proveedores),
    DestinoNav("Facturas", Destination.FACTURAS, IconosApp.Facturas),
)
