package com.example.fmoraga.dictuc.data.local.db.dao

import androidx.room.ColumnInfo
import com.example.fmoraga.dictuc.domain.model.MuestraPlanificacion

data class MuestraPlanificacionConMuestra(
    val id: Long,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "id_muestreador") val idMuestreador: Long,
    @ColumnInfo(name = "id_estado") val idEstado: Int,
    val enviado: Int,
    @ColumnInfo(name = "card_fname") val cardFName: String,
    val address: String,
    val county: String,
    @ColumnInfo(name = "prod_desc") val prodDesc: String,
    @ColumnInfo(name = "nombre_material") val nombreMaterial: String,
    @ColumnInfo(name = "nombre_molde") val nombreMolde: String,
    @ColumnInfo(name = "edad_probeta") val edadProbeta: String,
    @ColumnInfo(name = "total_probeta") val totalProbeta: Long
) {
    fun toDomain() = MuestraPlanificacion(
        id = id, idMuestra = idMuestra, idMuestreador = idMuestreador,
        cardFName = cardFName, address = address, county = county,
        prodDesc = prodDesc, nombreMaterial = nombreMaterial, nombreMolde = nombreMolde,
        edadProbeta = edadProbeta, totalProbeta = totalProbeta,
        estado = idEstado, enviado = enviado == 1
    )
}
