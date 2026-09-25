package com.tallerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Entrada de agenda: tarea, turno o producto para vender, en una fecha (y hora opcional). */
@Entity(
    tableName = "agenda",
    // El nombre debe coincidir con el índice que crea MIGRATION_11_12: Room valida
    // los índices al abrir la base y un nombre distinto aborta el arranque.
    indices = [Index(value = ["fecha"], name = "ix_agenda_fecha")],
)
data class AgendaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,            // "tarea" | "turno" | "producto"
    val titulo: String,
    val descripcion: String,
    val fecha: Long,             // día (epoch millis, inicio del día)
    val hora: String,            // "HH:mm" o "" si no aplica
    val hecho: Boolean,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "1") val negocioId: Long = 1,
)
