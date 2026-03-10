package com.example.bancodelmalestar.data.remote

import com.example.bancodelmalestar.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/registro")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<User>

    @Multipart
    @PATCH("auth/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Part("nombre") nombre: RequestBody? = null,
        @Part("telefono") telefono: RequestBody? = null,
        @Part("calle_numero") calleNumero: RequestBody? = null,
        @Part("colonia") colonia: RequestBody? = null,
        @Part("ciudad") ciudad: RequestBody? = null,
        @Part("codigo_postal") codigoPostal: RequestBody? = null,
        @Part("password_actual") passwordActual: RequestBody? = null,
        @Part("password_nueva") passwordNueva: RequestBody? = null,
        @Part foto: MultipartBody.Part? = null
    ): Response<User>

    @POST("auth/desactivar")
    suspend fun deactivate(@Header("Authorization") token: String, @Body body: DeactivateRequest): Response<Map<String, String>>

    @GET("cuentas/")
    suspend fun getAccounts(@Header("Authorization") token: String): Response<List<Account>>

    @GET("cuentas/movimientos")
    suspend fun getMovements(
        @Header("Authorization") token: String,
        @Query("limite") limit: Int? = null,
        @Query("orden_fecha") order: String? = null,
        @Query("tipo") type: String? = null
    ): Response<List<Movement>>

    @GET("cuentas/limite-gasto")
    suspend fun getSpendingLimits(@Header("Authorization") token: String): Response<List<SpendingLimit>>

    @PUT("cuentas/limite-gasto")
    suspend fun updateSpendingLimit(
        @Header("Authorization") token: String,
        @Body body: UpdateSpendingLimitRequest
    ): Response<List<SpendingLimit>>

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
