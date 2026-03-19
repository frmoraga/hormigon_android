package com.example.fmoraga.dictuc

import com.example.fmoraga.dictuc.data.local.db.dao.MuestraPlanificacionConMuestra
import org.junit.Assert.*
import org.junit.Test

/**
 * Verifica que el campo `enviado` (Int en el DTO de Room) se mapea correctamente
 * al Boolean del modelo de dominio en MuestraPlanificacion.
 *
 * Cómo ejecutar: ./gradlew testDebugUnitTest
 */
class MuestraPlanificacionConMuestraTest {

    private fun row(enviado: Int) = MuestraPlanificacionConMuestra(
        id = 1, idMuestra = 10, idMuestreador = 5, idEstado = 1,
        enviado = enviado,
        cardFName = "", address = "", county = "", prodDesc = "",
        nombreMaterial = "", nombreMolde = "", edadProbeta = "", totalProbeta = 0
    )

    @Test
    fun `enviado 0 se mapea a false en dominio`() {
        assertFalse(row(0).toDomain().enviado)
    }

    @Test
    fun `enviado 1 se mapea a true en dominio`() {
        assertTrue(row(1).toDomain().enviado)
    }

    @Test
    fun `estado 1 (tomada) se preserva en dominio`() {
        assertEquals(1, row(1).toDomain().estado)
    }
}
