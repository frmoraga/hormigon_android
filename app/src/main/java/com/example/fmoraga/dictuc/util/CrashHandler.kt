package com.example.fmoraga.dictuc.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Captura excepciones no manejadas (crashes), guarda los detalles en SharedPreferences
 * de forma sincrónica y luego pasa el control al handler original de Android.
 *
 * No intenta enviar por red aquí porque el hilo está muriendo.
 * ErrorReportWorker leerá y enviará el reporte en el próximo arranque.
 */
class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TIMESTAMP,  timestamp)
                .putString(KEY_TYPE,       throwable.javaClass.name)
                .putString(KEY_MESSAGE,    throwable.message ?: "Sin mensaje")
                .putString(KEY_STACKTRACE, throwable.stackTraceToString().take(MAX_TRACE_LEN))
                .putBoolean(KEY_PENDING,   true)
                .commit()   // commit() sincrónico — imprescindible antes del crash
        } catch (_: Exception) {
            // Nunca fallar dentro del handler de crashes
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        const val PREFS_NAME      = "dictuc_crash_report"
        const val KEY_PENDING     = "has_pending_crash"
        const val KEY_TIMESTAMP   = "crash_timestamp"
        const val KEY_TYPE        = "crash_type"
        const val KEY_MESSAGE     = "crash_message"
        const val KEY_STACKTRACE  = "crash_stacktrace"
        private const val MAX_TRACE_LEN = 4000
    }
}
