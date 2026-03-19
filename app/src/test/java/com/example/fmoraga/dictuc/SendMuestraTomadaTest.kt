package com.example.fmoraga.dictuc

import com.example.fmoraga.dictuc.data.local.db.dao.MuestraDao
import com.example.fmoraga.dictuc.data.remote.api.DictucApi
import com.example.fmoraga.dictuc.data.repository.DictucRepositoryImpl
import com.example.fmoraga.dictuc.domain.model.MuestraTomada
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Verifica que `sendMuestraTomada` solo llama `markEnviada` cuando el servidor
 * confirma la inserción retornando "1". Cualquier otro valor o fallo de red
 * debe resultar en Result.failure sin marcar la muestra como enviada.
 *
 * Usa MockWebServer (sin red real) y MockK (sin código Android real).
 * Cómo ejecutar: ./gradlew testDebugUnitTest
 */
class SendMuestraTomadaTest {

    private val mockWebServer = MockWebServer()
    private lateinit var api: DictucApi
    private lateinit var muestraDao: MuestraDao
    private lateinit var repository: DictucRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer.start()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictucApi::class.java)

        // relaxed = true: métodos sin stub retornan Unit/null en vez de fallar
        muestraDao = mockk(relaxed = true)
        repository = DictucRepositoryImpl(
            api,
            mockk(relaxed = true),  // muestreadorDao
            muestraDao,
            mockk(relaxed = true)   // syncDao
        )
    }

    @After
    fun tearDown() = mockWebServer.shutdown()

    /** Muestra mínima válida para el test (campos obligatorios sin default). */
    private fun muestra(idMuestra: Long = 10L) = MuestraTomada(
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
        tipoProbeta = 0L
    )

    @Test
    fun `servidor retorna 1 - Result success y markEnviada llamada`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("1"))

        val result = repository.sendMuestraTomada(muestra())

        assertTrue("Debe ser success", result.isSuccess)
        coVerify(exactly = 1) { muestraDao.markEnviada(10L) }
    }

    @Test
    fun `servidor retorna 0 - Result failure y markEnviada NO llamada`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("0"))

        val result = repository.sendMuestraTomada(muestra())

        assertTrue("Debe ser failure", result.isFailure)
        coVerify(exactly = 0) { muestraDao.markEnviada(any()) }
    }

    @Test
    fun `servidor retorna body vacio - Result failure y markEnviada NO llamada`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(""))

        val result = repository.sendMuestraTomada(muestra())

        assertTrue("Debe ser failure", result.isFailure)
        coVerify(exactly = 0) { muestraDao.markEnviada(any()) }
    }

    @Test
    fun `servidor retorna HTTP 500 - Result failure y markEnviada NO llamada`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Error interno"))

        val result = repository.sendMuestraTomada(muestra())

        assertTrue("Debe ser failure", result.isFailure)
        coVerify(exactly = 0) { muestraDao.markEnviada(any()) }
    }

    @Test
    fun `falla de red - Result failure y markEnviada NO llamada`() = runTest {
        // Servidor cierra la conexión sin responder
        mockWebServer.enqueue(MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START))

        val result = repository.sendMuestraTomada(muestra())

        assertTrue("Debe ser failure", result.isFailure)
        coVerify(exactly = 0) { muestraDao.markEnviada(any()) }
    }
}
