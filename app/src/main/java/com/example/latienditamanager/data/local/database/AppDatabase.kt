package com.example.latienditamanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.latienditamanager.data.local.dao.MovimientoDao
import com.example.latienditamanager.data.local.dao.ProductoDao
import com.example.latienditamanager.data.local.dao.ProductoProveedorDao
import com.example.latienditamanager.data.local.dao.ProveedorDao
import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.ProductoEntity
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity

@Database(
    entities = [
        ProductoEntity::class,
        ProveedorEntity::class,
        ProductoProveedorEntity::class,
        MovimientoEntity::class
    ],
    version = 2,                // ← era 1
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun productoProveedorDao(): ProductoProveedorDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {

        // Migración de versión 1 a 2 — añade columna impuesto
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE productos ADD COLUMN impuesto REAL NOT NULL DEFAULT 0.0"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "latienditamanager.db"
                )
                    .addMigrations(MIGRATION_1_2)   // ← registrar migración
                    .build().also { INSTANCE = it }
            }
        }
    }
}