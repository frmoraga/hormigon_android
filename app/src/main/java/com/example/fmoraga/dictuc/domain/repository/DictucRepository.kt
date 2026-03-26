package com.example.fmoraga.dictuc.domain.repository

import com.example.fmoraga.dictuc.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DictucRepository {

    // --- Auth ---
    suspend fun getMuestreador(run: Long, password: String): Muestreador?

    // --- Muestras planificadas (para el muestreador activo) ---
    fun getMuestrasPlanificadas(idMuestreador: Long): Flow<List<MuestraPlanificacion>>
    suspend fun countAllPlanificacion(): Int
    suspend fun getMuestra(idMuestra: Long): Muestra?

    // --- Toma de muestra ---
    suspend fun saveMuestraTomada(mt: MuestraTomada): Long
    suspend fun getMuestraTomada(idMuestra: Long): MuestraTomada?
    suspend fun getMuestrasSinEnviar(): List<MuestraTomada>
    fun countMuestrasSinEnviar(): Flow<Int>           // Fix 4: conteo reactivo para UI
    suspend fun getMuestrasSinSubirFotos(): List<MuestraTomada>  // Fix 1: retry SFTP
    suspend fun markFotosEnviadas(idMuestra: Long)    // Fix 1
    suspend fun incrementarIntentosEnvio(idMuestra: Long) // Fix 6
    suspend fun purgeOldSentRecords()                 // Fix 5
    suspend fun fixRunMuestreador(run: Long)

    // --- Retiro ---
    fun getRetirosAsignados(runMuestreador: Long): Flow<List<RetiroMuestra>>
    suspend fun saveMuestraRetirada(mr: MuestraRetirada): Long
    suspend fun getMuestraRetirada(idMuestra: Long): MuestraRetirada?
    suspend fun getMuestrasPendientesEnvioRetiro(): List<MuestraRetirada>
    suspend fun markRetiroEnviado(id: Long)
    suspend fun deleteMuestraRetirada(id: Long)

    // --- Suspensión ---
    suspend fun saveMuestraSuspendida(ms: MuestraSuspendida): Long
    suspend fun getMuestrasSuspendidas(): List<MuestraSuspendida>
    suspend fun deleteMuestraSuspendida(id: Long)

    // --- Probetas extra ---
    suspend fun saveProbetaExtra(pe: ProbetaExtra): Long
    suspend fun getProbetasExtra(): List<ProbetaExtra>
    suspend fun deleteProbetaExtra(id: Long)

    // --- Jornada laboral ---
    suspend fun iniciarJornada(run: Long, fecha: String)
    suspend fun terminarJornada(run: Long, fecha: String)
    suspend fun getJornadasPendientes(): List<JornadaLaboral>
    suspend fun deleteJornada(id: Long)

    // --- Sincronización remota (llamadas al servidor) ---
    suspend fun syncMuestrasFromServer(): Result<Unit>
    suspend fun syncMuestreadoresFromServer(): Result<Unit>
    suspend fun syncPlanificacionFromServer(runMuestreador: Long): Result<Unit>
    suspend fun syncRetirosFromServer(runMuestreador: Long): Result<Unit>
    suspend fun sendMuestraTomada(mt: MuestraTomada): Result<Unit>
    suspend fun sendMuestraRetirada(mr: MuestraRetirada): Result<Unit>
    suspend fun sendMuestraSuspendida(ms: MuestraSuspendida): Result<Unit>
    suspend fun sendJornada(jl: JornadaLaboral): Result<Unit>
    suspend fun sendProbetaExtra(pe: ProbetaExtra): Result<Unit>
    suspend fun uploadFile(localPath: String): Result<Unit>
    suspend fun checkNotification(idMuestreador: Long): Boolean

    // --- Reporte diario ---
    suspend fun getDailyReport(idMuestreador: Long): List<com.example.fmoraga.dictuc.data.local.db.dao.ResumenMuestraDia>
    suspend fun sendDailyReport(
        runMuestreador: Long,
        idMuestreador: Long,
        fecha: String,
        cuerpoHtml: String,
        total: Int,
        tomadas: Int,
        pendientes: Int,
        suspendidas: Int,
        viajePerdido: Int
    ): Result<Unit>

    // --- Reporte de errores ---
    suspend fun sendErrorReport(
        runMuestreador: Long,
        idMuestreador: Long,
        tipo: String,
        tag: String,
        mensaje: String,
        stacktrace: String,
        timestamp: String
    ): Result<Unit>
}
