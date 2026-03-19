package com.example.fmoraga.dictuc.data.remote.dto

import com.example.fmoraga.dictuc.data.local.db.entity.*
import com.google.gson.annotations.SerializedName

// ─── Muestra ─────────────────────────────────────────────────────────────

data class MuestraDto(
    @SerializedName("id") val id: Long,
    @SerializedName("tipo_muestra") val tipoMuestra: Long,
    @SerializedName("id_tipo_cliente") val idTipoCliente: Long,
    @SerializedName("CardFName") val cardFName: String,
    @SerializedName("LicTradNum") val licTradNum: String,
    @SerializedName("Address") val address: String,
    @SerializedName("County") val county: String,
    @SerializedName("prod_desc") val prodDesc: String,
    @SerializedName("nombre_material") val nombreMaterial: String,
    @SerializedName("nombre_molde") val nombreMolde: String,
    @SerializedName("id_probeta") val idProbeta: Long,
    @SerializedName("edad_probeta") val edadProbeta: String,
    @SerializedName("total_probeta") val totalProbeta: Long
) {
    fun toEntity() = MuestraEntity(
        idMuestra = id, tipoMuestra = tipoMuestra, idTipoCliente = idTipoCliente,
        cardFName = cardFName, licTradNum = licTradNum, address = address,
        county = county, prodDesc = prodDesc, nombreMaterial = nombreMaterial,
        nombreMolde = nombreMolde, idProbeta = idProbeta, edadProbeta = edadProbeta,
        totalProbeta = totalProbeta
    )
}

// ─── Muestreador ─────────────────────────────────────────────────────────

data class MuestreadorDto(
    @SerializedName("id") val id: Long,
    @SerializedName("run") val run: Long,
    @SerializedName("dv") val dv: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("ap_pat") val apPat: String,
    @SerializedName("calle") val calle: String,
    @SerializedName("numeracion") val numeracion: String,
    @SerializedName("activo") val activo: Long,
    @SerializedName("password") val password: String,
    @SerializedName("id_perfil") val idPerfil: Long
) {
    fun toEntity() = MuestreadorEntity(
        idMuestreador = id, run = run, dv = dv, nombre = nombre, apPat = apPat,
        calle = calle, numeracion = numeracion, activo = activo,
        password = password, idPerfil = idPerfil
    )
}

// ─── MuestraPlanificacion ────────────────────────────────────────────────

data class MuestraPlanificacionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("id_muestra") val idMuestra: Long,
    @SerializedName("id_muestreador") val idMuestreador: Long,
    @SerializedName("id_estado") val idEstado: Int,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("comuna_nombre") val comunaNombre: String
) {
    fun toEntity() = MuestraPlanificacionEntity(
        id = id, idMuestra = idMuestra, idMuestreador = idMuestreador,
        idEstado = idEstado, direccion = direccion, comunaNombre = comunaNombre
    )
}

// ─── RetiroMuestra ───────────────────────────────────────────────────────

data class RetiroMuestraDto(
    @SerializedName("id") val id: Long,
    @SerializedName("id_muestra") val idMuestra: Long,
    @SerializedName("run_muestreador") val runMuestreador: Long,
    @SerializedName("fecha_programada") val fechaProgramada: String,
    @SerializedName("CardFName") val cardFName: String,
    @SerializedName("Address") val address: String,
    @SerializedName("County") val county: String,
    @SerializedName("nombre_material") val nombreMaterial: String
) {
    fun toEntity() = RetiroMuestraEntity(
        id = id, idMuestra = idMuestra, runMuestreador = runMuestreador,
        fechaProgramada = fechaProgramada, cardFName = cardFName,
        address = address, county = county, nombreMaterial = nombreMaterial
    )
}
