package com.example.bancodelmalestar

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class User(
    val id: Int,
    val nombre: String,
    val email: String,
    @SerializedName("foto_perfil") val fotoPerfil: String?,
    @SerializedName("creado_en") val creadoEn: String
)

data class Account(
    val id: Int,
    val numero: String,
    val tipo: String, // "debito" or "credito"
    val saldo: Double,
    val deuda: Double,
    @SerializedName("limite_credito") val limiteCredito: Double,
    @SerializedName("creada_en") val creadaEn: String
)

data class Movement(
    val id: Int,
    val tipo: String, // "transferencia", "pago_servicio", "pago_credito", "deposito"
    val monto: Double,
    val descripcion: String,
    val estado: String, // "completada", "pendiente", "fallida"
    val servicio: String?,
    @SerializedName("referencia_servicio") val referenciaServicio: String?,
    @SerializedName("cuenta_origen_id") val cuentaOrigenId: Int?,
    @SerializedName("cuenta_destino_id") val cuentaDestinoId: Int?,
    @SerializedName("creada_en") val creadaEn: String
)

data class ServiceAvailable(
    val servicio: String
)

data class TransferRequest(
    @SerializedName("numero_cuenta_destino") val numeroCuentaDestino: String,
    val monto: Double,
    val descripcion: String? = ""
)

data class ServicePaymentRequest(
    val servicio: String,
    val referencia: String,
    val monto: Double,
    @SerializedName("usar_credito") val usarCredito: Boolean = false
)

data class CreditPaymentRequest(
    val monto: Double
)

data class MyQrResponse(
    @SerializedName("numero_cuenta") val numeroCuenta: String,
    val nombre: String,
    val fecha: String
)

data class QrTransferData(
    val numero_cuenta: String,
    val nombre: String,
    val monto: Double,
    val concepto: String,
    val fecha: String
)

data class SpendingLimit(
    val tipo: String,
    @SerializedName("limite_gasto_mensual") val limiteGastoMensual: Double,
    @SerializedName("gasto_mes_actual") val gastoMesActual: Double
)

data class UpdateSpendingLimitRequest(
    val limite: Double,
    val tipo: String? = null
)
