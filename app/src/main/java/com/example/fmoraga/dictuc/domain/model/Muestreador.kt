package com.example.fmoraga.dictuc.domain.model

data class Muestreador(
    val id: Long = 0,
    val run: Long,
    val dv: String,
    val nombre: String,
    val apPat: String,
    val calle: String,
    val numeracion: String,
    val activo: Long,
    val password: String,
    val idPerfil: Long
)
