package com.example.fmoraga.dictuc

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.fmoraga.dictuc.data.remote.sync.DailyReportWorker
import com.example.fmoraga.dictuc.data.remote.sync.ErrorReportWorker
import com.example.fmoraga.dictuc.data.remote.sync.SyncWorker
import com.example.fmoraga.dictuc.util.CrashHandler
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
        installCrashHandler()
        scheduleSync()
        sendPendingCrashIfAny()
        registerNetworkCallback()
    }

    private fun installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(applicationContext, Thread.getDefaultUncaughtExceptionHandler())
        )
    }

    private fun sendPendingCrashIfAny() {
        val prefs = getSharedPreferences(CrashHandler.PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(CrashHandler.KEY_PENDING, false)) {
            WorkManager.getInstance(this).enqueueUniqueWork(
                ErrorReportWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                ErrorReportWorker.buildRequest()
            )
        }
    }

    /**
     * Fix 2: dispara sync inmediato cuando el dispositivo recupera conectividad,
     * sin esperar al próximo ciclo de 15 minutos del WorkManager.
     */
    private fun registerNetworkCallback() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "${SyncWorker.WORK_NAME}_on_network",
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<SyncWorker>().build()
                )
            }
        })
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
        // Reporte diario a las 21:00
        wm.enqueueUniquePeriodicWork(
            DailyReportWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            DailyReportWorker.buildPeriodicRequest()
        )
    }
}
