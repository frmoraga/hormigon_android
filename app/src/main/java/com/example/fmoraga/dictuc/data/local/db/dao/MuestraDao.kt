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

    @Query("DELETE FROM muestra WHERE id_muestra IN (:ids)")
    suspend fun deleteMuestrasByIdMuestra(ids: List<Long>)

    @Query("DELETE FROM muestra")
    suspend fun deleteAllMuestras()

    /**
     * Elimina filas duplicadas en muestra por id_muestra (PK es autoincremental).
     * Conserva solo la más reciente (MAX id) por id_muestra.
     */
    @Query("""
        DELETE FROM muestra
        WHERE id NOT IN (
            SELECT MAX(id) FROM muestra GROUP BY id_muestra
        )
    """)
    suspend fun dedupMuestra()

    /**
     * Borra muestras que el servidor ya no envía Y que no están referenciadas
     * en muestra_planificacion (pendientes o tomadas hoy).
     */
    @Query("""
        DELETE FROM muestra
        WHERE id_muestra NOT IN (:serverIds)
        AND id_muestra NOT IN (SELECT id_muestra FROM muestra_planificacion)
    """)
    suspend fun deleteMuestrasNotInPlanificacion(serverIds: List<Long>)

    // ─── Planificación ───────────────────────────────────────────────────

    @Query("""
        SELECT mp.id, mp.id_muestra, mp.id_muestreador,
               CASE WHEN mt.id_muestra IS NOT NULL THEN MAX(mp.id_estado, 2) ELSE mp.id_estado END AS id_estado,
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

    /**
     * Borra entradas de planificacion cuya PK (id) no está en la lista del servidor
     * y cuya id_muestra tampoco fue tomada localmente hoy.
     * Elimina tanto entradas obsoletas como duplicados con distinta PK.
     */
    @Query("""
        DELETE FROM muestra_planificacion
        WHERE id NOT IN (:serverPkIds)
        AND id_muestra NOT IN (SELECT id_muestra FROM muestra_tomada)
    """)
    suspend fun deletePlanificacionNotInServer(serverPkIds: List<Long>)

    /**
     * Elimina filas duplicadas: para cada id_muestra conserva solo la fila
     * con el mayor id (el más reciente). Aplica tanto a muestras pendientes
     * como tomadas. Se debe llamar ANTES de deletePlanificacionNotInServer.
     */
    @Query("""
        DELETE FROM muestra_planificacion
        WHERE id NOT IN (
            SELECT MAX(id) FROM muestra_planificacion GROUP BY id_muestra
        )
    """)
    suspend fun dedupAllPlanificacion()

    /**
     * Borra entradas de planificacion cuya muestra fue tomada en un día distinto
     * al de hoy (limpieza al inicio del nuevo día). El formato de fecha es dd-MM-yyyy.
     */
    @Query("""
        DELETE FROM muestra_planificacion
        WHERE id_muestra IN (
            SELECT id_muestra FROM muestra_tomada WHERE fecha_elaboracion != :today
        )
    """)
    suspend fun deletePlanificacionTakenBefore(today: String)

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

    /**
     * Elimina filas duplicadas en muestra_tomada para el mismo id_muestra,
     * conservando solo la más reciente (MAX id autoincremental).
     */
    @Query("""
        DELETE FROM muestra_tomada
        WHERE id NOT IN (
            SELECT MAX(id) FROM muestra_tomada GROUP BY id_muestra
        )
    """)
    suspend fun dedupMuestraTomada()
}

