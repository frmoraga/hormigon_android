package com.example.fmoraga.dictuc.domain.model

data class Muestra(
    val id: Long = 0,
    val idMuestra: Long,
    val tipoMuestra: Long,
    val idTipoCliente: Long,
    val cardFName: String,
    val licTradNum: String,
    val address: String,
    val county: String,
    val prodDesc: String,
    val nombreMaterial: String,
    val nombreMolde: String,
    val idProbeta: Long,
    val edadProbeta: String,
    val totalProbeta: Long
)
