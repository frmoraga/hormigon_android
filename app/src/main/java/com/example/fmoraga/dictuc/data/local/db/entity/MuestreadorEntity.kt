package com.example.fmoraga.dictuc.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fmoraga.dictuc.domain.model.Muestreador

@Entity(tableName = "muestreador")
data class MuestreadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_muestreador") val idMuestreador: Long,
    val run: Long,
    val dv: String,
    val nombre: String,
    @ColumnInfo(name = "ap_pat") val apPat: String,
    val calle: String,
    val numeracion: String,
    val activo: Long,
    val password: String,
    @ColumnInfo(name = "id_perfil") val idPerfil: Long
) {
    fun toDomain() = Muestreador(
        id = idMuestreador,
        run = run,
        dv = dv,
        nombre = nombre,
        apPat = apPat,
        calle = calle,
        numeracion = numeracion,
        activo = activo,
        password = password,
        idPerfil = idPerfil
    )

    companion object {
        fun fromDomain(m: Muestreador) = MuestreadorEntity(
            id = m.id,
            idMuestreador = m.id,
            run = m.run,
            dv = m.dv,
            nombre = m.nombre,
            apPat = m.apPat,
            calle = m.calle,
            numeracion = m.numeracion,
            activo = m.activo,
            password = m.password,
            idPerfil = m.idPerfil
        )
    }
}
