package com.tallerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de la aplicación (Frozen Spec 6, persistencia local).
 * v2 agrega Ingresos y Egresos (Fase 3). v3 agrega fechaEntrega al trabajo (Fase 5).
 *
 * Las actualizaciones preservan los datos mediante migraciones reales
 * (ver Migraciones.kt). Solo se recurre a un borrado en caso de downgrade.
 */
@Database(
    entities = [TrabajoEntity::class, IngresoEntity::class, EgresoEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class TallerDatabase : RoomDatabase() {

    abstract fun trabajoDao(): TrabajoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun egresoDao(): EgresoDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instancia = it }
            }
    }
}
