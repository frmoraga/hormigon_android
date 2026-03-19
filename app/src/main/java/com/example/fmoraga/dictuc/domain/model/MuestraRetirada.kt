package com.example.fmoraga.dictuc.domain.model

data class MuestraRetirada(
    val id: Long = 0,
    val idMuestra: Long,
    val runMuestreador: Long,
    val fechaRetiro: String,
    val pathFoto: String = "",
    val observacion: String = "",
    val enviado: Boolean = false
)
