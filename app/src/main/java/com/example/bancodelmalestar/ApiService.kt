package com.example.bancodelmalestar

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/registro")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<User>

    @GET("cuentas/")
    suspend fun getAccounts(@Header("Authorization") token: String): Response<List<Account>>

    @GET("cuentas/movimientos")
    suspend fun getMovements(
        @Header("Authorization") token: String,
        @Query("limite") limit: Int? = null,
        @Query("orden_fecha") order: String? = null,
        @Query("tipo") type: String? = null
    ): Response<List<Movement>>

    @GET("cuentas/mi-qr")
    suspend fun getMyQr(@Header("Authorization") token: String): Response<MyQrResponse>

    @POST("operaciones/transferencia")
    suspend fun transfer(
        @Header("Authorization") token: String,
        @Body body: TransferRequest
    ): Response<Movement>

    @POST("operaciones/pago-servicio")
    suspend fun payService(
        @Header("Authorization") token: String,
        @Body body: ServicePaymentRequest
    ): Response<Movement>

    @POST("operaciones/pago-credito")
    suspend fun payCredit(
        @Header("Authorization") token: String,
        @Body body: CreditPaymentRequest
    ): Response<Movement>

    @GET("operaciones/servicios-disponibles")
    suspend fun getAvailableServices(): Response<List<ServiceAvailable>>
}
