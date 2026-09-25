package com.tallerapp.domain.model

/** Tipos de entrada de agenda (mismos que el escritorio). */
object TipoAgenda {
    const val TAREA = "tarea"
    const val TURNO = "turno"
    const val PRODUCTO = "producto"

    val todos = listOf(TAREA, TURNO, PRODUCTO)

    fun etiqueta(tipo: String): String = when (tipo) {
        TURNO -> "Turno"
        PRODUCTO -> "Producto"
        else -> "Tarea"
    }

    fun icono(tipo: String): String = when (tipo) {
        TURNO -> "🕒"
        PRODUCTO -> "📦"
        else -> "✔️"
    }
}

/** Entrada de agenda: tarea, turno o producto para vender, en una fecha (y hora opcional). */
data class AgendaItem(
    val id: Long = 0,
    val tipo: String = TipoAgenda.TAREA,
    val titulo: String,
    val descripcion: String = "",
    val fecha: Long,
    val hora: String = "",
    val hecho: Boolean = false,
    val createdAt: Long = 0,
)
