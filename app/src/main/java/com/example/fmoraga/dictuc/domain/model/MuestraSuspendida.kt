package com.example.fmoraga.dictuc.domain.model

data class MuestraSuspendida(
    val id: Long = 0,
    val idMuestra: Long,
    val runMuestreador: Long,
    val fechaSuspension: String,
    val motivo: String = "",
    val enviado: Boolean = false
)
