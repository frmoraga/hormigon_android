package com.example.fmoraga.dictuc.domain.model

data class RetiroMuestra(
    val id: Long = 0,
    val idMuestra: Long,
    val runMuestreador: Long,
    val fechaProgramada: String,
    val cardFName: String,
    val address: String,
    val county: String,
    val nombreMaterial: String,
    val numBoleta: String = "",
    val idEstado: Int = 2,  // 2=pendiente retiro, 3=retirada (desde servidor)
    val retiradaLocalmente: Boolean = false,  // hay registro en muestra_retirada
    val retiroEnviado: Boolean = false         // muestra_retirada.enviado = true
)
