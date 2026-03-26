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
import com.example.fmoraga.dictuc.util.ErrorReporter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Reemplaza SyncInfo.java (Service + Timer).
 * Se ejecuta en segundo plano cada 60 segundos con WorkManager,
 * garantizando ejecución aunque la app esté en background.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences,
    private val errorReporter: ErrorReporter
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork iniciado")
        val idMuestreador = userPreferences.idMuestreador.first()
        val run           = userPreferences.run.first()
        val idPerfil      = userPreferences.idPerfil.first()

        // Sincronizar catálogos desde el servidor
        repository.syncMuestrasFromServer()
        repository.syncMuestreadoresFromServer()
        repository.syncPlanificacionFromServer(run)

        repository.syncRetirosFromServer(idMuestreador)

        // Enviar datos pendientes al servidor
        for (jl in repository.getJornadasPendientes()) {
            repository.sendJornada(jl)
                .onSuccess { repository.deleteJornada(jl.id) }
                .onFailure {
                    Log.e(TAG, "Error enviando jornada ${jl.id}", it)
                    errorReporter.report("SyncWorker:sendJornada[${jl.id}]", it)
                }
        }
        for (ms in repository.getMuestrasSuspendidas()) {
            repository.sendMuestraSuspendida(ms)
                .onSuccess { repository.deleteMuestraSuspendida(ms.id) }
                .onFailure {
                    Log.e(TAG, "Error enviando suspensión ${ms.id}", it)
                    errorReporter.report("SyncWorker:sendMuestraSuspendida[${ms.id}]", it)
                }
        }
        for (pe in repository.getProbetasExtra()) {
            repository.sendProbetaExtra(pe)
                .onSuccess { repository.deleteProbetaExtra(pe.id) }
                .onFailure {
                    Log.e(TAG, "Error enviando probeta extra ${pe.id}", it)
                    errorReporter.report("SyncWorker:sendProbetaExtra[${pe.id}]", it)
                }
        }
        repository.fixRunMuestreador(idMuestreador)
        val sinEnviar = repository.getMuestrasSinEnviar()
        Log.d(TAG, "Muestras pendientes de envío: ${sinEnviar.size}")
        for (mt in sinEnviar) {
            Log.d(TAG, "Enviando muestra id=${mt.idMuestra} run=${mt.runMuestreador}")
            repository.sendMuestraTomada(mt)
                .onSuccess {
                    Log.d(TAG, "Muestra ${mt.idMuestra} enviada OK")
                    // Fix 1: subir fotos y marcarlas solo si SFTP tuvo éxito total
                    val allUploaded = uploadMuestraFiles(mt.idMuestra,
                        mt.pathFirma, mt.pathFirma2,
                        mt.pathFoto, mt.pathFoto2, mt.pathFoto3, mt.pathFoto4)
                    if (allUploaded) repository.markFotosEnviadas(mt.idMuestra)
                }
                .onFailure {
                    Log.e(TAG, "Error enviando muestra ${mt.idMuestra}", it)
                    // Fix 6: incrementar contador de intentos para no reintentar indefinidamente
                    repository.incrementarIntentosEnvio(mt.idMuestra)
                    errorReporter.report("SyncWorker:sendMuestraTomada[${mt.idMuestra}]", it)
                }
        }

        // Fix 1: reintentar subida SFTP de fotos para muestras ya enviadas al API
        val sinFotos = repository.getMuestrasSinSubirFotos()
        Log.d(TAG, "Muestras con fotos pendientes de subir: ${sinFotos.size}")
        for (mt in sinFotos) {
            val allUploaded = uploadMuestraFiles(mt.idMuestra,
                mt.pathFirma, mt.pathFirma2,
                mt.pathFoto, mt.pathFoto2, mt.pathFoto3, mt.pathFoto4)
            if (allUploaded) repository.markFotosEnviadas(mt.idMuestra)
        }

        for (mr in repository.getMuestrasPendientesEnvioRetiro()) {
            repository.sendMuestraRetirada(mr)
                .onSuccess {
                    repository.markRetiroEnviado(mr.id)
                    if (mr.pathFoto.isNotBlank()) {
                        repository.uploadFile(mr.pathFoto).onFailure {
                            Log.e(TAG, "SFTP falló para retiro ${mr.idMuestra}: ${mr.pathFoto}", it)
                            errorReporter.report("SyncWorker:uploadRetiroFile[${mr.idMuestra}]", it)
                        }
                    }
                }
                .onFailure {
                    Log.e(TAG, "Error enviando retiro ${mr.idMuestra}", it)
                    errorReporter.report("SyncWorker:sendMuestraRetirada[${mr.idMuestra}]", it)
                }
        }

        // Fix 5: purgar registros completamente enviados con más de 30 días
        repository.purgeOldSentRecords()

        // Notificar si hay actualizaciones pendientes
        val hasNotification = repository.checkNotification(idMuestreador)
        if (hasNotification) showNotification()

        return Result.success()
    }

    /** Sube todos los archivos de una muestra vía SFTP. Retorna true solo si todos tuvieron éxito. */
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

        /** Programa sincronización periódica cada 15 minutos (mínimo de WorkManager). */
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
