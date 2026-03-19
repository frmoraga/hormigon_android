package com.example.fmoraga.dictuc

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fmoraga.dictuc.data.local.db.DictucDatabase
import com.example.fmoraga.dictuc.data.local.db.dao.MuestraDao
import com.example.fmoraga.dictuc.data.local.db.entity.MuestraPlanificacionEntity
import com.example.fmoraga.dictuc.data.local.db.entity.MuestraTomadaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas del DAO con base de datos Room en memoria (sin teléfono).
 * Robolectric provee un entorno Android sobre la JVM.
 *
 * Cómo ejecutar: ./gradlew testDebugUnitTest
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MuestraDaoTest {

    private lateinit var db: DictucDatabase
    private lateinit var dao: MuestraDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DictucDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.muestraDao()
    }

    @After
    fun tearDown() = db.close()

    // ─── Helpers ─────────────────────────────────────────────────────────

    private val planificacion = MuestraPlanificacionEntity(
        id = 1, idMuestra = 10, idMuestreador = 5
    )

    private fun tomada(idMuestra: Long = 10L, enviado: Boolean) = MuestraTomadaEntity(
        idMuestra = idMuestra,
        runMuestreador = 5L,
        fechaElaboracion = "2026-03-18",
        tipoHormigon = 0L,
        idMaterial = 0L,
        idPreparadoObra = 0L,
        idEmpleadoEn = 0L,
        idEjesEmpleadoEn = 0L,
        idCompactacion = 0L,
        idColocacion = 0L,
        idCompactacionProbeta = 0L,
        idAspecto = 0L,
        idTextura = 0L,
        tipoCurado = 0L,
        tipoProbeta = 0L,
        enviado = enviado
    )

    // ─── Tests: COALESCE(mt.enviado, 0) en getPlanificacion ──────────────

    @Test
    fun `sin muestra tomada - enviado es 0 por COALESCE`() = runTest {
        dao.insertPlanificacion(listOf(planificacion))

        val row = dao.getPlanificacion(5L).first().first()

        assertEquals(0, row.enviado)
    }

    @Test
    fun `muestra tomada con enviado false - enviado es 0`() = runTest {
        dao.insertPlanificacion(listOf(planificacion))
        dao.insertMuestraTomada(tomada(enviado = false))

        val row = dao.getPlanificacion(5L).first().first()

        assertEquals(0, row.enviado)
    }

    @Test
    fun `muestra tomada con enviado true - enviado es 1`() = runTest {
        dao.insertPlanificacion(listOf(planificacion))
        dao.insertMuestraTomada(tomada(enviado = true))

        val row = dao.getPlanificacion(5L).first().first()

        assertEquals(1, row.enviado)
    }

    @Test
    fun `estado calculado es 2 cuando existe muestra tomada`() = runTest {
        dao.insertPlanificacion(listOf(planificacion))
        dao.insertMuestraTomada(tomada(enviado = false))

        val row = dao.getPlanificacion(5L).first().first()

        assertEquals(2, row.idEstado)  // 2=tomada en escala del servidor
    }

    // ─── Tests: getMuestrasSinEnviar ──────────────────────────────────────

    @Test
    fun `getMuestrasSinEnviar retorna solo las no enviadas`() = runTest {
        dao.insertMuestraTomada(tomada(idMuestra = 10, enviado = false))
        dao.insertMuestraTomada(tomada(idMuestra = 20, enviado = false))
        dao.insertMuestraTomada(tomada(idMuestra = 30, enviado = true))

        val sinEnviar = dao.getMuestrasSinEnviar()

        assertEquals(2, sinEnviar.size)
        assertTrue("No debe incluir enviadas", sinEnviar.none { it.enviado })
    }

    // ─── Tests: markEnviada ───────────────────────────────────────────────

    @Test
    fun `markEnviada cambia enviado a true`() = runTest {
        dao.insertMuestraTomada(tomada(enviado = false))

        dao.markEnviada(10L)

        val updated = dao.getMuestraTomada(10L)
        assertNotNull(updated)
        assertTrue("Debe estar marcada como enviada", updated!!.enviado)
    }

    @Test
    fun `markEnviada no afecta otras muestras`() = runTest {
        dao.insertMuestraTomada(tomada(idMuestra = 10, enviado = false))
        dao.insertMuestraTomada(tomada(idMuestra = 20, enviado = false))

        dao.markEnviada(10L)

        val otra = dao.getMuestraTomada(20L)
        assertFalse("La otra muestra no debe cambiar", otra!!.enviado)
    }
}
