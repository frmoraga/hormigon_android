package com.example.fmoraga.dictuc.util

import android.util.Log
import com.example.fmoraga.dictuc.data.local.preferences.UserPreferences
import com.example.fmoraga.dictuc.data.remote.api.DictucApi
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Log diagnóstico remoto hacia sop_hormigon_app_log.
 * No lanza excepciones y no envía correos — solo inserta en BD.
 * Úsalo para eventos que no son errores pero revelan anomalías de datos
 * o métricas de sincronización (conteos, duraciones, flujos de usuario).
 */
@Singleton
class AppLogger @Inject constructor(
    private val api: DictucApi,
    private val userPreferences: UserPreferences
) {
    suspend fun info(tag: String, mensaje: String)  = send("INFO",  tag, mensaje)
    suspend fun warn(tag: String, mensaje: String)  = send("WARN",  tag, mensaje)
    suspend fun error(tag: String, mensaje: String) = send("ERROR", tag, mensaje)

    private suspend fun send(nivel: String, tag: String, mensaje: String) {
        Log.println(
            when (nivel) { "WARN" -> Log.WARN; "ERROR" -> Log.ERROR; else -> Log.DEBUG },
            tag, mensaje
        )
        runCatching {
            val idMuestreador = userPreferences.idMuestreador.first()
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            api.sendAppLog(
                mapOf(
                    "action"            to "sendAppLog",
                    "id_muestreador"    to idMuestreador.toString(),
                    "nivel"             to nivel,
                    "tag"               to tag,
                    "mensaje"           to mensaje,
                    "fecha_dispositivo" to ts
                )
            ).close()
        }
    }
}
