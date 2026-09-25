package com.tallerapp.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Íconos de línea (Tabler, MIT) portados desde el escritorio (Themes/Colors.xaml).
 * Son vectores de trazo 2px sobre viewport 24×24; el color lo aplica `Icon(tint = ...)`.
 * Mantener los mismos trazados que la app de escritorio asegura identidad visual.
 */
private fun iconoLinea(nombre: String, path: String): ImageVector =
    ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(path),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

object IconosApp {
    val Inicio = iconoLinea(
        "IcHome",
        "M5 12l-2 0l9 -9l9 9l-2 0 M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7 " +
            "M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6",
    )
    val Movimientos = iconoLinea(
        "IcCoin",
        "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0 M14.8 9a2 2 0 0 0 -1.8 -1h-2a2 2 0 1 0 0 4h2a2 2 0 1 1 0 4h-2a2 2 0 0 1 -1.8 -1 M12 7v10",
    )
    val Deudas = iconoLinea(
        "IcUsers",
        "M5 7a4 4 0 1 0 8 0a4 4 0 1 0 -8 0 M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2 " +
            "M16 3.13a4 4 0 0 1 0 7.75 M21 21v-2a4 4 0 0 0 -3 -3.85",
    )
    val Agenda = iconoLinea(
        "IcCalendar",
        "M4 7a2 2 0 0 1 2 -2h12a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-12 " +
            "M16 3v4 M8 3v4 M4 11h16 M11 15h1 M12 15v3",
    )
    val Reportes = iconoLinea(
        "IcChart",
        "M3 13a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v6a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -6 " +
            "M15 9a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v10a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -10 " +
            "M9 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -14 M4 20h14",
    )
    val Negocios = iconoLinea(
        "IcStore",
        "M3 21l18 0 M3 7v1a3 3 0 0 0 6 0v-1m0 1a3 3 0 0 0 6 0v-1m0 1a3 3 0 0 0 6 0v-1h-18l2 -4h14l2 4 " +
            "M5 21l0 -10.15 M19 21l0 -10.15 M9 21v-4a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v4",
    )
    val Configuracion = iconoLinea(
        "IcSettings",
        "M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37" +
            "a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573" +
            "c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0" +
            "a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572" +
            "c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37" +
            "c1 .608 2.296 .07 2.572 -1.065 M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0",
    )
    val Calculadora = iconoLinea(
        "IcCalc",
        "M4 3a1 1 0 0 1 1 -1h14a1 1 0 0 1 1 1v18a1 1 0 0 1 -1 1h-14a1 1 0 0 1 -1 -1v-18z " +
            "M8 6h8 M8 10h0 M12 10h0 M16 10h0 M8 14h0 M12 14h0 M16 14h4 M8 18h0 M12 18h0",
    )
    val Editar = iconoLinea(
        "IcPencil",
        "M4 20h4l10.5 -10.5a2.828 2.828 0 1 0 -4 -4l-10.5 10.5v4 M13.5 6.5l4 4",
    )
    val Eliminar = iconoLinea(
        "IcTrash",
        "M4 7l16 0 M10 11l0 6 M14 11l0 6 M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12 " +
            "M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3",
    )
}
