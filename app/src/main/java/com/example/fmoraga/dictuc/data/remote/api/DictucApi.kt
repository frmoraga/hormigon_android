package com.example.fmoraga.dictuc.data.remote.api

import com.example.fmoraga.dictuc.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface DictucApi {

    // ─── GET: sincronización de catálogos ────────────────────────────────

    @GET("sendData/index.phtml")
    suspend fun getMuestras(
        @Query("action") action: String = "getListaMuestra"
    ): List<MuestraDto>

    @GET("sendData/index.phtml")
    suspend fun getMuestreadores(
        @Query("action") action: String = "getListaMuestreador"
    ): List<MuestreadorDto>

    @GET("sendData/index.phtml")
    suspend fun getPlanificacion(
        @Query("action") action: String = "getListaMuestraMuestreador"
    ): List<MuestraPlanificacionDto>

    @GET("sendData/index.phtml")
    suspend fun getRetiros(
        @Query("action") action: String = "getRetiro",
        @Query("id_muestreador") idMuestreador: Long
    ): List<RetiroMuestraDto>

    @GET("sendData/index.phtml")
    suspend fun getNotification(
        @Query("action") action: String = "getNotification",
        @Query("id_muestreador") idMuestreador: Long
    ): Boolean

    // ─── POST: envío de datos al servidor ────────────────────────────────

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendMuestra(@FieldMap fields: Map<String, String>): ResponseBody

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendMuestraRetirada(@FieldMap fields: Map<String, String>): ResponseBody

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendMuestraSuspendida(@FieldMap fields: Map<String, String>): ResponseBody

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendJornada(@FieldMap fields: Map<String, String>): ResponseBody

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendProbetaExtra(@FieldMap fields: Map<String, String>): ResponseBody

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendErrorReport(@FieldMap fields: Map<String, String>): ResponseBody

    @FormUrlEncoded
    @POST("getData/index.phtml")
    suspend fun sendDailyReport(@FieldMap fields: Map<String, String>): ResponseBody
}
