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

    @Query("SELECT * FROM retiro_muestra WHERE run_muestreador = :runMuestreador AND id_estado != 3")
    fun getRetiros(runMuestreador: Long): Flow<List<RetiroMuestraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetiros(list: List<RetiroMuestraEntity>)

    @Query("DELETE FROM retiro_muestra")
    suspend fun deleteAllRetiros()
}
