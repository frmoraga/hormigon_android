package com.example.fmoraga.dictuc.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fmoraga.dictuc.data.local.db.dao.MuestraDao
import com.example.fmoraga.dictuc.data.local.db.dao.MuestreadorDao
import com.example.fmoraga.dictuc.data.local.db.dao.SyncDao
import com.example.fmoraga.dictuc.data.local.db.entity.*

@Database(
    entities = [
        MuestreadorEntity::class,
        MuestraEntity::class,
        MuestraPlanificacionEntity::class,
        MuestraTomadaEntity::class,
        MuestraRetiradaEntity::class,
        MuestraSuspendidaEntity::class,
        JornadaLaboralEntity::class,
        ProbetaExtraEntity::class,
        RetiroMuestraEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class DictucDatabase : RoomDatabase() {
    abstract fun muestreadorDao(): MuestreadorDao
    abstract fun muestraDao(): MuestraDao
    abstract fun syncDao(): SyncDao

    companion object {
        /** Agrega fotos_enviadas e intentos_envio a muestra_tomada (Fix 1 y Fix 6). */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE muestra_tomada ADD COLUMN fotos_enviadas INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE muestra_tomada ADD COLUMN intentos_envio INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
