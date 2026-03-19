package com.example.fmoraga.dictuc.domain.model

data class MuestraPlanificacion(
    val id: Long = 0,
    val idMuestra: Long,
    val idMuestreador: Long,
    val cardFName: String,
    val address: String,
    val county: String,
    val prodDesc: String,
    val nombreMaterial: String,
    val nombreMolde: String,
    val edadProbeta: String,
    val totalProbeta: Long,
    val estado: Int = 0,      // 0=pendiente, 1=tomada, 2=suspendida, 3=retirada
    val enviado: Boolean = false  // true = sincronizada con el servidor
)
