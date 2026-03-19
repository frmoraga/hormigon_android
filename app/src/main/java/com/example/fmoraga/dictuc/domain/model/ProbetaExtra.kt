package com.example.fmoraga.dictuc.domain.model

data class ProbetaExtra(
    val id: Long = 0,
    val idMuestra: Long,
    val runMuestreador: Long,
    val cantidad: Int,
    val observacion: String = ""
)
