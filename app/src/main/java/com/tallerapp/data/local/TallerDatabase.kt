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
    entities = [IngresoEntity::class, EgresoEntity::class, DeudaEntity::class, CategoriaEntity::class, CuentaEntity::class, MetaEntity::class, RecurrenteEntity::class, ContactoEntity::class, NegocioEntity::class, AgendaEntity::class],
    version = 12,
    exportSchema = false,
)
abstract class TallerDatabase : RoomDatabase() {

    abstract fun ingresoDao(): IngresoDao
    abstract fun egresoDao(): EgresoDao
    abstract fun deudaDao(): DeudaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun metaDao(): MetaDao
    abstract fun recurrenteDao(): RecurrenteDao
    abstract fun contactoDao(): ContactoDao
    abstract fun negocioDao(): NegocioDao
    abstract fun agendaDao(): AgendaDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instancia = it }
            }
    }
}
