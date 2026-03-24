package com.example.fmoraga.dictuc.domain.model

data class MuestraSuspendida(
    val id: Long = 0,
    val idMuestra: Long,
    val runMuestreador: Long,
    val fechaSuspension: String,
    val motivo: String = "",
    val idEstado: Int = 2,          // 2=suspensión normal, 3=viaje perdido
    val numBoleta: String = "",
    val horaLlegada: String = "",
    val horaSalidaObra: String = "",
    val numPatente: String = "",
    val enviado: Boolean = false
)
