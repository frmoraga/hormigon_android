package com.example.fmoraga.dictuc.domain.model

data class RetiroMuestra(
    val id: Long = 0,
    val idMuestra: Long,
    val runMuestreador: Long,
    val fechaProgramada: String,
    val cardFName: String,
    val address: String,
    val county: String,
    val nombreMaterial: String
)
