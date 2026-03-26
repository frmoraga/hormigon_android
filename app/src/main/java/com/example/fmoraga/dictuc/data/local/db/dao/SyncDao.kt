package com.example.fmoraga.dictuc.data.local.db.dao

import androidx.room.*
import com.example.fmoraga.dictuc.data.local.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {

    // ─── Muestra retirada ────────────────────────────────────────────────

    @Query("SELECT * FROM muestra_retirada WHERE enviado = 0")
    suspend fun getMuestrasPendientesRetiro(): List<MuestraRetiradaEntity>

    @Query("SELECT * FROM muestra_retirada WHERE id_muestra = :idMuestra LIMIT 1")
    suspend fun getMuestraRetirada(idMuestra: Long): MuestraRetiradaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuestraRetirada(mr: MuestraRetiradaEntity): Long

    @Query("DELETE FROM muestra_retirada WHERE id = :id")
    suspend fun deleteMuestraRetirada(id: Long)

    // ─── Muestra suspendida ──────────────────────────────────────────────

    @Query("SELECT * FROM muestra_suspendida WHERE enviado = 0")
    suspend fun getMuestrasSuspendidas(): List<MuestraSuspendidaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuestraSuspendida(ms: MuestraSuspendidaEntity): Long

    @Query("DELETE FROM muestra_suspendida WHERE id = :id")
    suspend fun deleteMuestraSuspendida(id: Long)

    // ─── Jornada laboral ─────────────────────────────────────────────────

    @Query("SELECT * FROM jornada_laboral")
    suspend fun getJornadasPendientes(): List<JornadaLaboralEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJornada(jl: JornadaLaboralEntity): Long

    @Query("DELETE FROM jornada_laboral WHERE id = :id")
    suspend fun deleteJornada(id: Long)

    // ─── Probeta extra ───────────────────────────────────────────────────

    @Query("SELECT * FROM probeta_extra")
    suspend fun getProbetasExtra(): List<ProbetaExtraEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProbetaExtra(pe: ProbetaExtraEntity): Long

    @Query("DELETE FROM probeta_extra WHERE id = :id")
    suspend fun deleteProbetaExtra(id: Long)

    // ─── Retiro muestra (asignaciones del servidor) ──────────────────────

    @Query("""
        SELECT rm.*,
               CASE WHEN mr.id IS NOT NULL THEN 1 ELSE 0 END AS retirada_localmente,
               COALESCE(mr.enviado, 0) AS retiro_enviado
        FROM retiro_muestra rm
        LEFT JOIN muestra_retirada mr ON rm.id_muestra = mr.id_muestra
        WHERE rm.run_muestreador = :runMuestreador
        ORDER BY CASE WHEN mr.id IS NOT NULL THEN 1 ELSE 0 END ASC, rm.id_estado ASC
    """)
    fun getRetirosConEstado(runMuestreador: Long): Flow<List<RetiroMuestraConEstado>>

    @Query("UPDATE muestra_retirada SET enviado = 1 WHERE id = :id")
    suspend fun markRetiroEnviado(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetiros(list: List<RetiroMuestraEntity>)

    @Query("DELETE FROM retiro_muestra")
    suspend fun deleteAllRetiros()
}
