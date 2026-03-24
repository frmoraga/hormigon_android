package com.example.fmoraga.dictuc.data.remote.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.fmoraga.dictuc.data.local.db.dao.ResumenMuestraDia
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DictucRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val run           = userPreferences.run.first()
        val idMuestreador = userPreferences.idMuestreador.first()

        // No enviar si no hay sesión activa
        if (idMuestreador < 0) return Result.success()

        val muestras = repository.getDailyReport(idMuestreador)
        val fecha    = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val tomadas      = muestras.count { it.idEstado == 2 }
        val pendientes   = muestras.count { it.idEstado <= 1 }
        val viajePerdido = muestras.count { it.idEstado == 3 && it.idEstadoSuspension == 3 }
        val suspendidas  = muestras.count { it.idEstado == 3 && it.idEstadoSuspension != 3 }
        val total        = muestras.size

        val cuerpoHtml = buildHtml(fecha, idMuestreador, muestras, total, tomadas, pendientes, suspendidas, viajePerdido)

        repository.sendDailyReport(
            runMuestreador = run,
            idMuestreador  = idMuestreador,
            fecha          = fecha,
            cuerpoHtml     = cuerpoHtml,
            total          = total,
            tomadas        = tomadas,
            pendientes     = pendientes,
            suspendidas    = suspendidas,
            viajePerdido   = viajePerdido
        )

        return Result.success()
    }

    private fun buildHtml(
        fecha: String,
        idMuestreador: Long,
        muestras: List<ResumenMuestraDia>,
        total: Int,
        tomadas: Int,
        pendientes: Int,
        suspendidas: Int,
        viajePerdido: Int
    ): String {
        val sb = StringBuilder()
        sb.append("<h2>Resumen diario — DICTUC Android</h2>")
        sb.append("<p><b>Fecha:</b> $fecha &nbsp;&nbsp; <b>ID Muestreador:</b> $idMuestreador</p>")
        sb.append("<hr/>")

        // Resumen numérico
        sb.append("<h3>Resumen</h3>")
        sb.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse'>")
        sb.append("<tr><th>Total</th><th>Tomadas</th><th>Pendientes</th><th>Suspendidas</th><th>Viaje perdido</th></tr>")
        sb.append("<tr style='text-align:center'>")
        sb.append("<td>$total</td><td>$tomadas</td><td>$pendientes</td><td>$suspendidas</td><td>$viajePerdido</td>")
        sb.append("</tr></table>")

        // Detalle por muestra
        sb.append("<h3>Detalle</h3>")
        sb.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%'>")
        sb.append("<tr><th>#ID</th><th>Obra</th><th>Dirección</th><th>Boleta</th><th>Hora muestreo</th><th>Estado</th></tr>")

        for (m in muestras.sortedBy { it.idEstado }) {
            val (estadoLabel, color) = when {
                m.idEstado <= 1                              -> Pair("Pendiente",     "#1565C0")
                m.idEstado == 2                              -> Pair("Tomada",        "#2E7D32")
                m.idEstadoSuspension == 3                    -> Pair("Viaje perdido", "#E65100")
                else                                         -> Pair("Suspendida",    "#F57F17")
            }
            sb.append("<tr>")
            sb.append("<td>${m.idMuestra}</td>")
            sb.append("<td>${m.cardFName}</td>")
            sb.append("<td>${m.address}</td>")
            sb.append("<td>${m.numBoleta.ifEmpty { "—" }}</td>")
            sb.append("<td>${m.horaMuestreo.ifEmpty { "—" }}</td>")
            sb.append("<td style='color:$color;font-weight:bold'>$estadoLabel</td>")
            sb.append("</tr>")
        }

        sb.append("</table>")
        return sb.toString()
    }

    companion object {
        const val WORK_NAME = "DailyReportWorker"

        fun buildPeriodicRequest(): PeriodicWorkRequest {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_MONTH, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            return PeriodicWorkRequestBuilder<DailyReportWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }
    }
}
