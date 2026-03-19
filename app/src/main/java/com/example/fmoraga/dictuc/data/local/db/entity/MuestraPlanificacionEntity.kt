package com.example.fmoraga.dictuc.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muestra_planificacion")
data class MuestraPlanificacionEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "id_muestreador") val idMuestreador: Long,
    @ColumnInfo(name = "id_estado") val idEstado: Int = 0,
    val direccion: String = "",
    @ColumnInfo(name = "comuna_nombre") val comunaNombre: String = ""
)
