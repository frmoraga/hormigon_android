package com.example.fmoraga.dictuc.util

import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reporte de errores silenciosos (sync, guardado, etc.) que ocurren
 * en contextos con corrutinas activas (SyncWorker, ViewModels).
 *
 * Nunca lanza excepciones — si el envío falla, simplemente se ignora
 * para no interferir con el flujo principal.
 */
@Singleton
class ErrorReporter @Inject constructor(
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences
) {
    suspend fun report(tag: String, error: Throwable) {
        runCatching {
            val run           = userPreferences.run.first()
            val idMuestreador = userPreferences.idMuestreador.first()
            val timestamp     = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            repository.sendErrorReport(
                runMuestreador = run,
                idMuestreador  = idMuestreador,
                tipo           = "error_silencioso",
                tag            = tag,
                mensaje        = error.message ?: "Sin mensaje",
                stacktrace     = error.stackTraceToString().take(4000),
                timestamp      = timestamp
            )
        }
    }
}
