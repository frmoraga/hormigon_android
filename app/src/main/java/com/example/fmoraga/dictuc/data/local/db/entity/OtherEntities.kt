package com.example.fmoraga.dictuc.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fmoraga.dictuc.domain.model.*

// ─── MuestraRetirada ───────────────────────────────────────────────────────

@Entity(tableName = "muestra_retirada")
data class MuestraRetiradaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "run_muestreador") val runMuestreador: Long,
    @ColumnInfo(name = "fecha_retiro") val fechaRetiro: String,
    @ColumnInfo(name = "path_foto") val pathFoto: String = "",
    val observacion: String = "",
    val enviado: Boolean = false
) {
    fun toDomain() = MuestraRetirada(
        id = id, idMuestra = idMuestra, runMuestreador = runMuestreador,
        fechaRetiro = fechaRetiro, pathFoto = pathFoto,
        observacion = observacion, enviado = enviado
    )

    companion object {
        fun fromDomain(mr: MuestraRetirada) = MuestraRetiradaEntity(
            id = mr.id, idMuestra = mr.idMuestra, runMuestreador = mr.runMuestreador,
            fechaRetiro = mr.fechaRetiro, pathFoto = mr.pathFoto,
            observacion = mr.observacion, enviado = mr.enviado
        )
    }
}

// ─── MuestraSuspendida ────────────────────────────────────────────────────

@Entity(tableName = "muestra_suspendida")
data class MuestraSuspendidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "run_muestreador") val runMuestreador: Long,
    @ColumnInfo(name = "fecha_suspension") val fechaSuspension: String,
    val motivo: String = "",
    @ColumnInfo(name = "id_estado") val idEstado: Int = 2,
    @ColumnInfo(name = "num_boleta") val numBoleta: String = "",
    @ColumnInfo(name = "hora_llegada") val horaLlegada: String = "",
    @ColumnInfo(name = "hora_salida_obra") val horaSalidaObra: String = "",
    @ColumnInfo(name = "num_patente") val numPatente: String = "",
    val enviado: Boolean = false
) {
    fun toDomain() = MuestraSuspendida(
        id = id, idMuestra = idMuestra, runMuestreador = runMuestreador,
        fechaSuspension = fechaSuspension, motivo = motivo, idEstado = idEstado,
        numBoleta = numBoleta, horaLlegada = horaLlegada,
        horaSalidaObra = horaSalidaObra, numPatente = numPatente, enviado = enviado
    )

    companion object {
        fun fromDomain(ms: MuestraSuspendida) = MuestraSuspendidaEntity(
            id = ms.id, idMuestra = ms.idMuestra, runMuestreador = ms.runMuestreador,
            fechaSuspension = ms.fechaSuspension, motivo = ms.motivo, idEstado = ms.idEstado,
            numBoleta = ms.numBoleta, horaLlegada = ms.horaLlegada,
            horaSalidaObra = ms.horaSalidaObra, numPatente = ms.numPatente, enviado = ms.enviado
        )
    }
}

// ─── JornadaLaboral ──────────────────────────────────────────────────────

@Entity(tableName = "jornada_laboral")
data class JornadaLaboralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val run: Long,
    val fecha: String,
    val tipo: Int
) {
    fun toDomain() = JornadaLaboral(id = id, run = run, fecha = fecha, tipo = tipo)

    companion object {
        fun fromDomain(jl: JornadaLaboral) =
            JornadaLaboralEntity(id = jl.id, run = jl.run, fecha = jl.fecha, tipo = jl.tipo)
    }
}

// ─── ProbetaExtra ────────────────────────────────────────────────────────

@Entity(tableName = "probeta_extra")
data class ProbetaExtraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "run_muestreador") val runMuestreador: Long,
    val cantidad: Int,
    val observacion: String = ""
) {
    fun toDomain() = ProbetaExtra(
        id = id, idMuestra = idMuestra, runMuestreador = runMuestreador,
        cantidad = cantidad, observacion = observacion
    )

    companion object {
        fun fromDomain(pe: ProbetaExtra) = ProbetaExtraEntity(
            id = pe.id, idMuestra = pe.idMuestra, runMuestreador = pe.runMuestreador,
            cantidad = pe.cantidad, observacion = pe.observacion
        )
    }
}

// ─── RetiroMuestra ───────────────────────────────────────────────────────

@Entity(tableName = "retiro_muestra")
data class RetiroMuestraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_muestra") val idMuestra: Long,
    @ColumnInfo(name = "run_muestreador") val runMuestreador: Long,
    @ColumnInfo(name = "fecha_programada") val fechaProgramada: String,
    @ColumnInfo(name = "card_fname") val cardFName: String,
    val address: String,
    val county: String,
    @ColumnInfo(name = "nombre_material") val nombreMaterial: String,
    @ColumnInfo(name = "num_boleta") val numBoleta: String = "",
    @ColumnInfo(name = "id_estado") val idEstado: Int = 2  // 2=pendiente retiro, 3=retirada
) {
    fun toDomain() = RetiroMuestra(
        id = id, idMuestra = idMuestra, runMuestreador = runMuestreador,
        fechaProgramada = fechaProgramada, cardFName = cardFName,
        address = address, county = county, nombreMaterial = nombreMaterial,
        numBoleta = numBoleta, idEstado = idEstado
    )

    companion object {
        fun fromDomain(rm: RetiroMuestra) = RetiroMuestraEntity(
            id = rm.id, idMuestra = rm.idMuestra, runMuestreador = rm.runMuestreador,
            fechaProgramada = rm.fechaProgramada, cardFName = rm.cardFName,
            address = rm.address, county = rm.county, nombreMaterial = rm.nombreMaterial,
            numBoleta = rm.numBoleta, idEstado = rm.idEstado
        )
    }
}

/** Resultado de la query JOIN retiro_muestra + muestra_retirada. */
data class RetiroMuestraConEstado(
    @Embedded val retiro: RetiroMuestraEntity,
    @ColumnInfo(name = "retirada_localmente") val retiradaLocalmente: Boolean = false,
    @ColumnInfo(name = "retiro_enviado") val retiroEnviado: Boolean = false
) {
    fun toDomain() = retiro.toDomain().copy(
        retiradaLocalmente = retiradaLocalmente,
        retiroEnviado = retiroEnviado
    )
}
