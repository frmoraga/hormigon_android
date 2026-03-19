package com.example.fmoraga.dictuc.domain.model

data class JornadaLaboral(
    val id: Long = 0,
    val run: Long,
    val fecha: String,
    val tipo: Int   // 1 = inicio, 2 = fin
)
