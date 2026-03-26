package com.example.fmoraga.dictuc.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.fmoraga.dictuc.BuildConfig
import com.example.fmoraga.dictuc.data.local.db.DictucDatabase
import com.example.fmoraga.dictuc.data.local.db.dao.MuestraDao
import com.example.fmoraga.dictuc.data.local.db.dao.MuestreadorDao
import com.example.fmoraga.dictuc.data.local.db.dao.SyncDao
import com.example.fmoraga.dictuc.data.local.db.entity.*
import com.example.fmoraga.dictuc.data.remote.api.DictucApi
import com.example.fmoraga.dictuc.domain.model.*
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictucRepositoryImpl @Inject constructor(
    private val api: DictucApi,
    private val db: DictucDatabase,
    private val muestreadorDao: MuestreadorDao,
    private val muestraDao: MuestraDao,
    private val syncDao: SyncDao
) : DictucRepository {

    // ─── Auth ─────────────────────────────────────────────────────────────

    override suspend fun getMuestreador(run: Long, password: String): Muestreador? =
        muestreadorDao.getByCredentials(run, password)?.toDomain()

    // ─── Muestras planificadas ────────────────────────────────────────────

    override fun getMuestrasPlanificadas(idMuestreador: Long): Flow<List<MuestraPlanificacion>> =
        muestraDao.getPlanificacion(idMuestreador).map { list -> list.map { it.toDomain() } }



    override suspend fun countAllPlanificacion(): Int =
        muestraDao.countAllPlanificacion()

    override suspend fun getMuestra(idMuestra: Long): Muestra? =
        muestraDao.getMuestra(idMuestra)?.toDomain()

    // ─── Muestra tomada ───────────────────────────────────────────────────

    override suspend fun saveMuestraTomada(mt: MuestraTomada): Long {
        muestraDao.deleteMuestraTomada(mt.idMuestra)  // evita duplicados (PK es autoincremental)
        val id = muestraDao.insertMuestraTomada(MuestraTomadaEntity.fromDomain(mt))
        muestraDao.updateEstadoPlanificacion(mt.idMuestra, 2)  // 2=tomada en escala del servidor
        return id
    }

    override suspend fun getMuestraTomada(idMuestra: Long): MuestraTomada? =
        muestraDao.getMuestraTomada(idMuestra)?.toDomain()

    override suspend fun getMuestrasSinEnviar(): List<MuestraTomada> =
        muestraDao.getMuestrasSinEnviar().map { it.toDomain() }

    override fun countMuestrasSinEnviar(): Flow<Int> =
        muestraDao.countMuestrasSinEnviar()

    override suspend fun getMuestrasSinSubirFotos(): List<MuestraTomada> =
        muestraDao.getMuestrasSinSubirFotos().map { it.toDomain() }

    override suspend fun markFotosEnviadas(idMuestra: Long) =
        muestraDao.markFotosEnviadas(idMuestra)

    override suspend fun incrementarIntentosEnvio(idMuestra: Long) =
        muestraDao.incrementarIntentosEnvio(idMuestra)

    override suspend fun purgeOldSentRecords() {
        val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
        val fechaLimite = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cutoff.time)
        muestraDao.deleteSentMuestrasAntes(fechaLimite)
    }

    override suspend fun fixRunMuestreador(run: Long) =
        muestraDao.fixRunMuestreador(run)

    // ─── Retiro ───────────────────────────────────────────────────────────

    override fun getRetirosAsignados(runMuestreador: Long): Flow<List<RetiroMuestra>> =
        syncDao.getRetiros(runMuestreador).map { list -> list.map { it.toDomain() } }

    override suspend fun saveMuestraRetirada(mr: MuestraRetirada): Long {
        val id = syncDao.insertMuestraRetirada(MuestraRetiradaEntity.fromDomain(mr))
        return id
    }

    override suspend fun getMuestraRetirada(idMuestra: Long): MuestraRetirada? =
        syncDao.getMuestraRetirada(idMuestra)?.toDomain()

    override suspend fun getMuestrasPendientesEnvioRetiro(): List<MuestraRetirada> =
        syncDao.getMuestrasPendientesRetiro().map { it.toDomain() }

    override suspend fun deleteMuestraRetirada(id: Long) = syncDao.deleteMuestraRetirada(id)

    // ─── Suspensión ───────────────────────────────────────────────────────

    override suspend fun saveMuestraSuspendida(ms: MuestraSuspendida): Long {
        val id = syncDao.insertMuestraSuspendida(MuestraSuspendidaEntity.fromDomain(ms))
        muestraDao.updateEstadoPlanificacion(ms.idMuestra, 3)  // 3=suspendida/viaje perdido en lista
        return id
    }

    override suspend fun getMuestrasSuspendidas(): List<MuestraSuspendida> =
        syncDao.getMuestrasSuspendidas().map { it.toDomain() }

    override suspend fun deleteMuestraSuspendida(id: Long) = syncDao.deleteMuestraSuspendida(id)

    // ─── Probeta extra ────────────────────────────────────────────────────

    override suspend fun saveProbetaExtra(pe: ProbetaExtra): Long =
        syncDao.insertProbetaExtra(ProbetaExtraEntity.fromDomain(pe))

    override suspend fun getProbetasExtra(): List<ProbetaExtra> =
        syncDao.getProbetasExtra().map { it.toDomain() }

    override suspend fun deleteProbetaExtra(id: Long) = syncDao.deleteProbetaExtra(id)

    // ─── Jornada laboral ──────────────────────────────────────────────────

    override suspend fun iniciarJornada(run: Long, fecha: String) {
        syncDao.insertJornada(JornadaLaboralEntity(run = run, fecha = fecha, tipo = 1))
    }

    override suspend fun terminarJornada(run: Long, fecha: String) {
        syncDao.insertJornada(JornadaLaboralEntity(run = run, fecha = fecha, tipo = 2))
    }

    override suspend fun getJornadasPendientes(): List<JornadaLaboral> =
        syncDao.getJornadasPendientes().map { it.toDomain() }

    override suspend fun deleteJornada(id: Long) = syncDao.deleteJornada(id)

    // ─── Sincronización remota ────────────────────────────────────────────

    override suspend fun syncMuestrasFromServer(): Result<Unit> = runCatching {
        val dtos = api.getMuestras()
        val entities = dtos.map { it.toEntity() }
        val serverIds = entities.map { it.idMuestra }
        // Borrar antes de insertar: MuestraEntity usa PK autoincremental,
        // por lo que REPLACE no evita duplicados por id_muestra
        if (serverIds.isNotEmpty()) muestraDao.deleteMuestrasByIdMuestra(serverIds)
        muestraDao.insertMuestras(entities)
        if (serverIds.isNotEmpty()) {
            muestraDao.deleteMuestrasNotInPlanificacion(serverIds)
        }
    }

    override suspend fun syncMuestreadoresFromServer(): Result<Unit> = runCatching {
        val dtos = api.getMuestreadores()
        muestreadorDao.deleteAll()
        muestreadorDao.insertAll(dtos.map { it.toEntity() })
    }

    override suspend fun syncPlanificacionFromServer(runMuestreador: Long): Result<Unit> = runCatching {
        val dtos = api.getPlanificacion()
        val entities = dtos.map { it.toEntity() }
        val serverPkIds = entities.map { it.id }
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        // Transacción atómica: evita duplicados por Workers concurrentes
        db.withTransaction {
            muestraDao.dedupMuestra()
            muestraDao.dedupMuestraTomada()
            muestraDao.insertPlanificacion(entities)
            muestraDao.dedupAllPlanificacion()
            if (serverPkIds.isNotEmpty()) {
                muestraDao.deletePlanificacionNotInServer(serverPkIds)
            }
            muestraDao.deletePlanificacionTakenBefore(today)
        }
    }

    override suspend fun syncRetirosFromServer(runMuestreador: Long): Result<Unit> = runCatching {
        val dtos = api.getRetiros(idMuestreador = runMuestreador)
        syncDao.deleteAllRetiros()
        syncDao.insertRetiros(dtos.map { it.toEntity() })
    }

    override suspend fun sendMuestraTomada(mt: MuestraTomada): Result<Unit> = runCatching {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val fields = mapOf(
            "action" to "sendMuestra",
            "hora_toma" to now,
            "id_muestra" to mt.idMuestra.toString(),
            "run_muestreador" to mt.runMuestreador.toString(),
            "fecha_elaboracion" to mt.fechaElaboracion,
            "tipo_hormigon" to mt.tipoHormigon.toString(),
            "otra_opcion" to mt.otraOpcion,
            "id_material" to mt.idMaterial.toString(),
            "marca_premezclado" to mt.marcaPremezclado,
            "num_camion" to mt.numCamion.toString(),
            "num_guia" to mt.numGuia.toString(),
            "id_preparado_obra" to mt.idPreparadoObra.toString(),
            "planta" to mt.planta,
            "dosificacion_kg_m" to mt.dosificacionKgM.toString(),
            "marca_p_obra" to mt.marcaPObra,
            "id_tipo_p_obra" to mt.tipoPObra,
            "procedencia_arena" to mt.procedenciaArena,
            "procedencia_gravilla" to mt.procedenciaGravilla,
            "procedencia_grava" to mt.procedenciaGrava,
            "aditivo_obra" to mt.aditivoObra.toString(),
            "marca_aditivo_obra" to mt.marcaAditivoObra,
            "tipo_aditivo_obra" to mt.tipoAditivoObra,
            "cantidad_aditivo_obra" to mt.cantidadAditivoObra.toString(),
            "id_empleado_en" to mt.idEmpleadoEn.toString(),
            "id_ejes_empleado_en" to mt.idEjesEmpleadoEn.toString(),
            "eje_x" to mt.ejeX, "eje_y" to mt.ejeY, "eje_z" to mt.ejeZ,
            "eje_a" to mt.ejeA, "eje_b" to mt.ejeB,
            "id_compactacion" to mt.idCompactacion.toString(),
            "id_colocacion" to mt.idColocacion.toString(),
            "id_compactacion_probeta" to mt.idCompactacionProbeta.toString(),
            "num_vibrador" to mt.numVibrador.toString(),
            "id_ensayo" to mt.idEnsayo,
            "num_cono" to mt.numCono.toString(),
            "nombre_equipo_cono" to mt.nombreEquipoCono.toString(),
            "id_aspecto" to mt.idAspecto.toString(),
            "id_textura" to mt.idTextura.toString(),
            "c_ambiente" to mt.cAmbiente.toString(),
            "nombre_equipo_t" to mt.nombreEquipoT.toString(),
            "hormigon_c" to mt.hormigonC.toString(),
            "densidad_kg_m" to mt.densidadKgM.toString(),
            "vol_recipiente_l" to mt.volRecipienteL.toString(),
            "peso_recipiente_material_kg" to mt.pesoRecipienteMaterialKg.toString(),
            "num_equipo_densidad" to mt.numEquipoDensidad.toString(),
            "peso_recipiente_kg" to mt.pesoRecipienteKg.toString(),
            "peso_material_kg" to mt.pesoMaterialKg.toString(),
            "contenido_aire" to mt.contenidoAire.toString(),
            "nombre_equipo_cont_aire" to mt.nombreEquipoContAire,
            "path_foto" to mt.pathFoto, "path_foto2" to mt.pathFoto2,
            "path_foto3" to mt.pathFoto3, "path_foto4" to mt.pathFoto4,
            "path_firma" to mt.pathFirma, "nombre_mixero" to mt.nombreMixero,
            "mail_mixero" to mt.mailMixero, "path_firma2" to mt.pathFirma2,
            "nombre_mixero2" to mt.nombreMixero2, "mail_mixero2" to mt.mailMixero2,
            "observacion" to mt.observacion, "otra_opcionEje" to mt.otraOpcionEje,
            "num_boleta" to mt.numBoleta, "hora_llegada" to mt.horaLlegada,
            "hora_muestreo" to mt.horaMuestreo, "hora_salida" to mt.horaSalida,
            "hora_salida_obra" to mt.horaSalidaObra,
            "metros_cubicos" to mt.metrosCubicos.toString(),
            "num_patente" to mt.numPatente,
            "tipo_curado" to mt.tipoCurado.toString(),
            "designacion" to mt.designacion,
            "tipo_probeta" to mt.tipoProbeta.toString()
        )
        val serverResponse = api.sendMuestra(fields).string().trim()
        if (serverResponse != "1") throw Exception("Servidor no confirmó la muestra: '$serverResponse'")
        muestraDao.markEnviada(mt.idMuestra)
    }

    override suspend fun sendMuestraRetirada(mr: MuestraRetirada): Result<Unit> = runCatching {
        val fields = mapOf(
            "action" to "sendMuestraRetirada",
            "id_muestra" to mr.idMuestra.toString(),
            "run_muestreador" to mr.runMuestreador.toString(),
            "fecha_retiro" to mr.fechaRetiro,
            "path_foto" to mr.pathFoto,
            "observacion" to mr.observacion
        )
        api.sendMuestraRetirada(fields).close()
    }

    override suspend fun sendMuestraSuspendida(ms: MuestraSuspendida): Result<Unit> = runCatching {
        val fields = buildMap<String, String> {
            put("action", "sendMuestraSuspendida")
            put("id_muestra", ms.idMuestra.toString())
            put("run_muestreador", ms.runMuestreador.toString())
            put("fecha_suspension", ms.fechaSuspension)
            put("id_estado", ms.idEstado.toString())
            if (ms.idEstado == 3) {  // Viaje perdido
                put("otra_opcionEje", ms.motivo)
                put("num_boleta", ms.numBoleta)
                put("hora_llegada", ms.horaLlegada)
                put("hora_salida_obra", ms.horaSalidaObra)
                put("num_patente", ms.numPatente)
            } else {
                put("observacion", ms.motivo)
            }
        }
        api.sendMuestraSuspendida(fields).close()
    }

    override suspend fun sendJornada(jl: JornadaLaboral): Result<Unit> = runCatching {
        val fields = mapOf(
            "action" to "sendJornada",
            "run" to jl.run.toString(),
            "fecha" to jl.fecha,
            "tipo" to jl.tipo.toString()
        )
        api.sendJornada(fields).close()
    }

    override suspend fun sendProbetaExtra(pe: ProbetaExtra): Result<Unit> = runCatching {
        val fields = mapOf(
            "action" to "sendProbetaExtra",
            "id_muestra" to pe.idMuestra.toString(),
            "run_muestreador" to pe.runMuestreador.toString(),
            "cantidad" to pe.cantidad.toString(),
            "observacion" to pe.observacion
        )
        api.sendProbetaExtra(fields).close()
    }

    override suspend fun uploadFile(localPath: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val jsch = JSch()
            val session = jsch.getSession(BuildConfig.SFTP_USER, BuildConfig.SFTP_HOST, 22)
            session.setPassword(BuildConfig.SFTP_PASS)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect(30_000)
            try {
                val channel = session.openChannel("sftp") as ChannelSftp
                channel.connect()
                channel.put(localPath, BuildConfig.SFTP_REMOTE_PATH)
                channel.disconnect()
            } finally {
                session.disconnect()
            }
        }
    }

    override suspend fun checkNotification(idMuestreador: Long): Boolean = runCatching {
        api.getNotification(idMuestreador = idMuestreador)
    }.getOrDefault(false)

    override suspend fun getDailyReport(idMuestreador: Long) =
        muestraDao.getDailyReport(idMuestreador)

    override suspend fun sendDailyReport(
        runMuestreador: Long,
        idMuestreador: Long,
        fecha: String,
        cuerpoHtml: String,
        total: Int,
        tomadas: Int,
        pendientes: Int,
        suspendidas: Int,
        viajePerdido: Int
    ): Result<Unit> = runCatching {
        val fields = mapOf(
            "action"          to "sendDailyReport",
            "run_muestreador" to runMuestreador.toString(),
            "id_muestreador"  to idMuestreador.toString(),
            "fecha"           to fecha,
            "cuerpo_html"     to cuerpoHtml,
            "total"           to total.toString(),
            "tomadas"         to tomadas.toString(),
            "pendientes"      to pendientes.toString(),
            "suspendidas"     to suspendidas.toString(),
            "viaje_perdido"   to viajePerdido.toString()
        )
        api.sendDailyReport(fields).close()
    }

    override suspend fun sendErrorReport(
        runMuestreador: Long,
        idMuestreador: Long,
        tipo: String,
        tag: String,
        mensaje: String,
        stacktrace: String,
        timestamp: String
    ): Result<Unit> = runCatching {
        val fields = mapOf(
            "action"          to "sendErrorReport",
            "run_muestreador" to runMuestreador.toString(),
            "id_muestreador"  to idMuestreador.toString(),
            "tipo"            to tipo,
            "tag"             to tag,
            "mensaje"         to mensaje,
            "stacktrace"      to stacktrace,
            "timestamp"       to timestamp
        )
        api.sendErrorReport(fields).close()
    }
}
