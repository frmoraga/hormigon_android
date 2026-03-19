package com.example.fmoraga.dictuc.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fmoraga.dictuc.domain.model.Muestra

@Entity(tableName = "muestra")
data class MuestraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "tipo_muestra") val tipoMuestra: Long,
    @ColumnInfo(name = "id_tipo_cliente") val idTipoCliente: Long,
    @ColumnInfo(name = "card_fname") val cardFName: String,
    @ColumnInfo(name = "lic_trad_num") val licTradNum: String,
    val address: String,
    val county: String,
    @ColumnInfo(name = "prod_desc") val prodDesc: String,
    @ColumnInfo(name = "nombre_material") val nombreMaterial: String,
    @ColumnInfo(name = "nombre_molde") val nombreMolde: String,
    @ColumnInfo(name = "id_probeta") val idProbeta: Long,
    @ColumnInfo(name = "edad_probeta") val edadProbeta: String,
    @ColumnInfo(name = "total_probeta") val totalProbeta: Long
) {
    fun toDomain() = Muestra(
        id = id,
        idMuestra = idMuestra,
        tipoMuestra = tipoMuestra,
        idTipoCliente = idTipoCliente,
        cardFName = cardFName,
        licTradNum = licTradNum,
        address = address,
        county = county,
        prodDesc = prodDesc,
        nombreMaterial = nombreMaterial,
        nombreMolde = nombreMolde,
        idProbeta = idProbeta,
        edadProbeta = edadProbeta,
        totalProbeta = totalProbeta
    )

    companion object {
        fun fromDomain(m: Muestra) = MuestraEntity(
            id = m.id,
            idMuestra = m.idMuestra,
            tipoMuestra = m.tipoMuestra,
            idTipoCliente = m.idTipoCliente,
            cardFName = m.cardFName,
            licTradNum = m.licTradNum,
            address = m.address,
            county = m.county,
            prodDesc = m.prodDesc,
            nombreMaterial = m.nombreMaterial,
            nombreMolde = m.nombreMolde,
            idProbeta = m.idProbeta,
            edadProbeta = m.edadProbeta,
            totalProbeta = m.totalProbeta
        )
    }
}
