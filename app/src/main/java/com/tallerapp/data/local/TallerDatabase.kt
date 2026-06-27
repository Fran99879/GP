package com.tallerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de la aplicación (Frozen Spec 6, persistencia local).
 * En la Fase 2 contiene solo la tabla de trabajos; crecerá en fases siguientes.
 */
@Database(entities = [TrabajoEntity::class], version = 1, exportSchema = false)
abstract class TallerDatabase : RoomDatabase() {

    abstract fun trabajoDao(): TrabajoDao

    companion object {
        @Volatile
        private var instancia: TallerDatabase? = null

        fun obtener(context: Context): TallerDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    TallerDatabase::class.java,
                    "tallerapp.db",
                ).build().also { instancia = it }
            }
    }
}
