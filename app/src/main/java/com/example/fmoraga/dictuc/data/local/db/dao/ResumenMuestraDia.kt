package com.example.fmoraga.dictuc.data.local.db.dao

import androidx.room.ColumnInfo

/** Resultado de la query para el reporte diario. */
data class ResumenMuestraDia(
    @ColumnInfo(name = "id_muestra")           val idMuestra: Long,
    @ColumnInfo(name = "id_estado")            val idEstado: Int,       // 0/1=pendiente, 2=tomada, 3=suspendida
    @ColumnInfo(name = "card_fname")           val cardFName: String,
    val address: String,
    @ColumnInfo(name = "num_boleta")           val numBoleta: String,
    @ColumnInfo(name = "hora_muestreo")        val horaMuestreo: String,
    @ColumnInfo(name = "id_estado_suspension") val idEstadoSuspension: Int  // 2=suspendida, 3=viaje perdido, 0=n/a
)
