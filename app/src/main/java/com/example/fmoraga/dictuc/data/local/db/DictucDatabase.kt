package com.example.fmoraga.dictuc.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 2,
    exportSchema = false
)
abstract class DictucDatabase : RoomDatabase() {
    abstract fun muestreadorDao(): MuestreadorDao
    abstract fun muestraDao(): MuestraDao
    abstract fun syncDao(): SyncDao
}
