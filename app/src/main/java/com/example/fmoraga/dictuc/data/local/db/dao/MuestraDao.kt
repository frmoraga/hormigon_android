package com.example.fmoraga.dictuc.data.local.db.dao

import androidx.room.*
import com.example.fmoraga.dictuc.data.local.db.entity.MuestraEntity
import com.example.fmoraga.dictuc.data.local.db.entity.MuestraPlanificacionEntity
import com.example.fmoraga.dictuc.data.local.db.entity.MuestraTomadaEntity
import kotlinx.coroutines.flow.Flow
import com.example.fmoraga.dictuc.data.local.db.dao.MuestraPlanificacionConMuestra

@Dao
interface MuestraDao {

    // ─── Muestra (catálogo) ──────────────────────────────────────────────

    @Query("SELECT * FROM muestra WHERE id_muestra = :idMuestra LIMIT 1")
    suspend fun getMuestra(idMuestra: Long): MuestraEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuestras(list: List<MuestraEntity>)

    @Query("DELETE FROM muestra")
    suspend fun deleteAllMuestras()

    // ─── Planificación ───────────────────────────────────────────────────

    @Query("""
        SELECT mp.id, mp.id_muestra, mp.id_muestreador,
               CASE WHEN mt.id_muestra IS NOT NULL THEN MAX(mp.id_estado, 1) ELSE mp.id_estado END AS id_estado,
               COALESCE(mt.enviado, 0) AS enviado,
               COALESCE(m.card_fname, '') AS card_fname,
               COALESCE(m.address, '') AS address,
               COALESCE(m.county, '') AS county,
               COALESCE(m.prod_desc, '') AS prod_desc,
               COALESCE(m.nombre_material, '') AS nombre_material,
               COALESCE(m.nombre_molde, '') AS nombre_molde,
               COALESCE(m.edad_probeta, '') AS edad_probeta,
               COALESCE(m.total_probeta, 0) AS total_probeta
        FROM muestra_planificacion mp
        LEFT JOIN muestra m ON mp.id_muestra = m.id_muestra
        LEFT JOIN muestra_tomada mt ON mp.id_muestra = mt.id_muestra
        WHERE mp.id_muestreador = :idMuestreador
    """)
    fun getPlanificacion(idMuestreador: Long): Flow<List<MuestraPlanificacionConMuestra>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanificacion(list: List<MuestraPlanificacionEntity>)

    @Query("DELETE FROM muestra_planificacion")
    suspend fun deleteAllPlanificacion()

    @Query("SELECT COUNT(*) FROM muestra_planificacion")
    suspend fun countAllPlanificacion(): Int

    @Query("UPDATE muestra_planificacion SET id_estado = :estado WHERE id_muestra = :idMuestra")
    suspend fun updateEstadoPlanificacion(idMuestra: Long, estado: Int)

    // ─── Muestra tomada ──────────────────────────────────────────────────

    @Query("SELECT * FROM muestra_tomada WHERE id_muestra = :idMuestra LIMIT 1")
    suspend fun getMuestraTomada(idMuestra: Long): MuestraTomadaEntity?

    @Query("SELECT * FROM muestra_tomada WHERE enviado = 0")
    suspend fun getMuestrasSinEnviar(): List<MuestraTomadaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuestraTomada(mt: MuestraTomadaEntity): Long

    @Query("UPDATE muestra_tomada SET enviado = 1 WHERE id_muestra = :idMuestra")
    suspend fun markEnviada(idMuestra: Long)

    @Query("UPDATE muestra_tomada SET run_muestreador = :run WHERE run_muestreador = 0 AND enviado = 0")
    suspend fun fixRunMuestreador(run: Long)

    @Query("DELETE FROM muestra_tomada WHERE id_muestra = :idMuestra")
    suspend fun deleteMuestraTomada(idMuestra: Long)
}
