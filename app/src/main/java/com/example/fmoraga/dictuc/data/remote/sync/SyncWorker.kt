package com.example.fmoraga.dictuc.data.remote.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.fmoraga.dictuc.R
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import com.example.fmoraga.dictuc.util.AppLogger
import com.example.fmoraga.dictuc.util.ErrorReporter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences,
    private val errorReporter: ErrorReporter,
    private val appLogger: AppLogger
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cicloInicio = System.currentTimeMillis()

        val idMuestreador = userPreferences.idMuestreador.first()
        val run           = userPreferences.run.first()
        val idPerfil      = userPreferences.idPerfil.first()

        // Detectar ejecución concurrente: si el ciclo anterior terminó hace menos de 60s,
        // hay dos Workers corriendo en paralelo — posible causa de triplicación de datos.
        val tiempoDesdeUltimoCiclo = cicloInicio - lastCycleStart
        if (lastCycleStart > 0L && tiempoDesdeUltimoCiclo < 60_000L) {
            appLogger.warn(TAG, "Posible ejecucion concurrente: ultimo ciclo hace ${tiempoDesdeUltimoCiclo}ms run=$run")
        }
        lastCycleStart = cicloInicio

        appLogger.info(TAG, "Ciclo iniciado run=$run id=$idMuestreador")
        Log.d(TAG, "doWork iniciado")

        var muestrasSent  = 0
        var muestrasFail  = 0
        var retirosSent   = 0
        var retirosFail   = 0
        var jornadasSent  = 0
        var sftpFail      = 0

        // ── Sincronizar catálogos desde el servidor ──────────────────────────
        repository.syncMuestrasFromServer()
            .onFailure { errorReporter.report("SyncWorker:syncMuestras", it) }
        repository.syncMuestreadoresFromServer()
            .onFailure { errorReporter.report("SyncWorker:syncMuestreadores", it) }
        repository.syncPlanificacionFromServer(run)
            .onFailure { errorReporter.report("SyncWorker:syncPlanificacion", it) }
        repository.syncRetirosFromServer(0L)
            .onFailure { errorReporter.report("SyncWorker:syncRetiros", it) }

        // ── Enviar jornadas pendientes ────────────────────────────────────────
        for (jl in repository.getJornadasPendientes()) {
            repository.sendJornada(jl)
                .onSuccess { repository.deleteJornada(jl.id); jornadasSent++ }
                .onFailure {
                    Log.e(TAG, "Error enviando jornada ${jl.id}", it)
                    errorReporter.report("SyncWorker:sendJornada[${jl.id}]", it)
                }
        }

        // ── Enviar suspensiones pendientes ───────────────────────────────────
        for (ms in repository.getMuestrasSuspendidas()) {
            repository.sendMuestraSuspendida(ms)
                .onSuccess { repository.deleteMuestraSuspendida(ms.id) }
                .onFailure {
                    Log.e(TAG, "Error enviando suspensión ${ms.id}", it)
                    errorReporter.report("SyncWorker:sendMuestraSuspendida[${ms.id}]", it)
                }
        }

        // ── Enviar probetas extra pendientes ─────────────────────────────────
        for (pe in repository.getProbetasExtra()) {
            repository.sendProbetaExtra(pe)
                .onSuccess { repository.deleteProbetaExtra(pe.id) }
                .onFailure {
                    Log.e(TAG, "Error enviando probeta extra ${pe.id}", it)
                    errorReporter.report("SyncWorker:sendProbetaExtra[${pe.id}]", it)
                }
        }

        repository.fixRunMuestreador(idMuestreador)

        // ── Enviar muestras tomadas ───────────────────────────────────────────
        val sinEnviar = repository.getMuestrasSinEnviar()
        Log.d(TAG, "Muestras pendientes de envío: ${sinEnviar.size}")
        if (sinEnviar.isNotEmpty()) {
            appLogger.info(TAG, "Enviando ${sinEnviar.size} muestras pendientes run=$run")
        }

        for (mt in sinEnviar) {
            Log.d(TAG, "Enviando muestra id=${mt.idMuestra} run=${mt.runMuestreador}")
            repository.sendMuestraTomada(mt)
                .onSuccess {
                    Log.d(TAG, "Muestra ${mt.idMuestra} enviada OK")
                    muestrasSent++
                    val allUploaded = uploadMuestraFiles(
                        mt.idMuestra,
                        mt.pathFirma, mt.pathFirma2,
                        mt.pathFoto, mt.pathFoto2, mt.pathFoto3, mt.pathFoto4
                    )
                    if (allUploaded) repository.markFotosEnviadas(mt.idMuestra)
                    else sftpFail++
                }
                .onFailure {
                    Log.e(TAG, "Error enviando muestra ${mt.idMuestra}", it)
                    muestrasFail++
                    repository.incrementarIntentosEnvio(mt.idMuestra)
                    errorReporter.report("SyncWorker:sendMuestraTomada[${mt.idMuestra}]", it)
                }
        }

        // ── Reintentar SFTP para muestras ya enviadas al API ─────────────────
        val sinFotos = repository.getMuestrasSinSubirFotos()
        Log.d(TAG, "Muestras con fotos pendientes de subir: ${sinFotos.size}")
        for (mt in sinFotos) {
            val allUploaded = uploadMuestraFiles(
                mt.idMuestra,
                mt.pathFirma, mt.pathFirma2,
                mt.pathFoto, mt.pathFoto2, mt.pathFoto3, mt.pathFoto4
            )
            if (allUploaded) repository.markFotosEnviadas(mt.idMuestra)
            else sftpFail++
        }

        // ── Enviar retiros pendientes ─────────────────────────────────────────
        for (mr in repository.getMuestrasPendientesEnvioRetiro()) {
            repository.sendMuestraRetirada(mr)
                .onSuccess {
                    retirosSent++
                    repository.markRetiroEnviado(mr.id)
                    if (mr.pathFoto.isNotBlank()) {
                        repository.uploadFile(mr.pathFoto).onFailure {
                            Log.e(TAG, "SFTP falló para retiro ${mr.idMuestra}: ${mr.pathFoto}", it)
                            sftpFail++
                            errorReporter.report("SyncWorker:uploadRetiroFile[${mr.idMuestra}]", it)
                        }
                    }
                }
                .onFailure {
                    Log.e(TAG, "Error enviando retiro ${mr.idMuestra}", it)
                    retirosFail++
                    errorReporter.report("SyncWorker:sendMuestraRetirada[${mr.idMuestra}]", it)
                }
        }

        // ── Purgar registros antiguos ya enviados ─────────────────────────────
        repository.purgeOldSentRecords()

        // ── Resumen diagnóstico del ciclo ─────────────────────────────────────
        val duracion = System.currentTimeMillis() - cicloInicio
        val resumen = buildString {
            append("Ciclo OK ${duracion}ms")
            if (muestrasSent > 0 || muestrasFail > 0)
                append(" | muestras +$muestrasSent f$muestrasFail")
            if (retirosSent > 0 || retirosFail > 0)
                append(" | retiros +$retirosSent f$retirosFail")
            if (jornadasSent > 0)
                append(" | jornadas +$jornadasSent")
            if (sftpFail > 0)
                append(" | sftp_fail=$sftpFail")
        }
        appLogger.info(TAG, resumen)

        // ── Notificación push si hay cambios en planificación ─────────────────
        val hasNotification = repository.checkNotification(idMuestreador)
        if (hasNotification) showNotification()

        return Result.success()
    }

    private suspend fun uploadMuestraFiles(idMuestra: Long, vararg paths: String): Boolean {
        var allOk = true
        for (path in paths) {
            if (path.isBlank()) continue
            repository.uploadFile(path).onFailure {
                Log.e(TAG, "SFTP falló para muestra $idMuestra: $path", it)
                errorReporter.report("SyncWorker:uploadFile[$idMuestra]", it)
                allOk = false
            }
        }
        return allOk
    }

    private fun showNotification() {
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sync_channel"

        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Sincronización", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Actualización DICTUC")
            .setContentText("Se ha modificado la planificación")
            .setAutoCancel(true)
            .build()

        manager.notify(1, notification)
    }

    companion object {
        const val WORK_NAME = "DictucSyncWorker"
        private const val TAG = "SyncWorker"

        // Timestamp del último inicio de ciclo — detecta Workers concurrentes.
        // WorkManager con KEEP debería impedirlo, pero esto lo confirma en logs.
        @Volatile private var lastCycleStart = 0L

        fun buildPeriodicRequest() = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
    }
}
