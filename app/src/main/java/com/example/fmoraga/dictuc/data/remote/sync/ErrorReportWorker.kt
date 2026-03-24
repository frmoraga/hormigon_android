package com.example.fmoraga.dictuc.data.remote.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import com.example.fmoraga.dictuc.util.CrashHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Envía el reporte de crash guardado por CrashHandler en el arranque siguiente.
 * Requiere conexión de red; WorkManager reintentará si no hay red.
 */
@HiltWorker
class ErrorReportWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = appContext.getSharedPreferences(CrashHandler.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(CrashHandler.KEY_PENDING, false)) return Result.success()

        val run           = userPreferences.run.first()
        val idMuestreador = userPreferences.idMuestreador.first()

        repository.sendErrorReport(
            runMuestreador = run,
            idMuestreador  = idMuestreador,
            tipo           = "crash",
            tag            = prefs.getString(CrashHandler.KEY_TYPE,       "Unknown")   ?: "Unknown",
            mensaje        = prefs.getString(CrashHandler.KEY_MESSAGE,    "")          ?: "",
            stacktrace     = prefs.getString(CrashHandler.KEY_STACKTRACE, "")          ?: "",
            timestamp      = prefs.getString(CrashHandler.KEY_TIMESTAMP,  "")          ?: ""
        ).onSuccess {
            prefs.edit().putBoolean(CrashHandler.KEY_PENDING, false).apply()
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "ErrorReportWorker"

        fun buildRequest() = OneTimeWorkRequestBuilder<ErrorReportWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
    }
}
