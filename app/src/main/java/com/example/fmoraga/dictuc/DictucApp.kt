package com.example.fmoraga.dictuc

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.fmoraga.dictuc.data.remote.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DictucApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleSync()
    }

    private fun scheduleSync() {
        val wm = WorkManager.getInstance(this)
        wm.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            SyncWorker.buildPeriodicRequest()
        )
        // Sync inmediato al abrir la app (único — si ya hay uno corriendo, no iniciar otro)
        wm.enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SyncWorker>().build()
        )
    }
}
