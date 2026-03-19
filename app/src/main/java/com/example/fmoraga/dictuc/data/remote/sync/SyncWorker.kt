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
    private val userPreferences: UserPreferences
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

        val runMuestreador = if (idPerfil == 2L) 0L else idMuestreador
        repository.syncRetirosFromServer(runMuestreador)

        // Enviar datos pendientes al servidor
        for (jl in repository.getJornadasPendientes()) {
            repository.sendJornada(jl)
                .onSuccess { repository.deleteJornada(jl.id) }
                .onFailure { Log.e(TAG, "Error enviando jornada ${jl.id}", it) }
        }
        for (ms in repository.getMuestrasSuspendidas()) {
            repository.sendMuestraSuspendida(ms)
                .onSuccess { repository.deleteMuestraSuspendida(ms.id) }
                .onFailure { Log.e(TAG, "Error enviando suspensión ${ms.id}", it) }
        }
        for (pe in repository.getProbetasExtra()) {
            repository.sendProbetaExtra(pe)
                .onSuccess { repository.deleteProbetaExtra(pe.id) }
                .onFailure { Log.e(TAG, "Error enviando probeta extra ${pe.id}", it) }
        }
        repository.fixRunMuestreador(idMuestreador)
        val sinEnviar = repository.getMuestrasSinEnviar()
        Log.d(TAG, "Muestras pendientes de envío: ${sinEnviar.size}")
        for (mt in sinEnviar) {
            Log.d(TAG, "Enviando muestra id=${mt.idMuestra} run=${mt.runMuestreador}")
            repository.sendMuestraTomada(mt)
                .onSuccess {
                    Log.d(TAG, "Muestra ${mt.idMuestra} enviada OK")
                    repository.uploadFile(mt.pathFirma)
                    repository.uploadFile(mt.pathFirma2)
                    repository.uploadFile(mt.pathFoto)
                    repository.uploadFile(mt.pathFoto2)
                    repository.uploadFile(mt.pathFoto3)
                    repository.uploadFile(mt.pathFoto4)
                }
                .onFailure { Log.e(TAG, "Error enviando muestra ${mt.idMuestra}", it) }
        }
        for (mr in repository.getMuestrasPendientesEnvioRetiro()) {
            repository.sendMuestraRetirada(mr)
                .onSuccess { repository.uploadFile(mr.pathFoto) }
                .onFailure { Log.e(TAG, "Error enviando retiro ${mr.idMuestra}", it) }
        }

        // Notificar si hay actualizaciones pendientes
        val hasNotification = repository.checkNotification(idMuestreador)
        if (hasNotification) showNotification()

        return Result.success()
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
