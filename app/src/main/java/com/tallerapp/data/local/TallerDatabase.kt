package com.tallerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de la aplicación (persistencia local).
 * v2 agrega Ingresos y Egresos. v3 agregó datos del taller (ya retirado).
 * v4 retira la tabla de trabajos del taller y agrega Deudas ("quién te debe").
 *
 * Las actualizaciones preservan los datos mediante migraciones reales
 * (ver Migraciones.kt). Solo se recurre a un borrado en caso de downgrade.
 */
@Database(
    entities = [IngresoEntity::class, EgresoEntity::class, DeudaEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class TallerDatabase : RoomDatabase() {

    abstract fun ingresoDao(): IngresoDao
    abstract fun egresoDao(): EgresoDao
    abstract fun deudaDao(): DeudaDao

    companion object {
        @Volatile
        private var instancia: TallerDatabase? = null

        fun obtener(context: Context): TallerDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    TallerDatabase::class.java,
                    "tallerapp.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instancia = it }
            }
    }
}
