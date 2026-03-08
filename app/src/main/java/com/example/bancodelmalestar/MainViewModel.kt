package com.example.bancodelmalestar

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private val apiService: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://192.168.100.25:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    var token by mutableStateOf("")
    var user by mutableStateOf<User?>(null)
    var accounts by mutableStateOf<List<Account>>(emptyList())
    var movements by mutableStateOf<List<Movement>>(emptyList())
    var servicesAvailable by mutableStateOf<List<ServiceAvailable>>(emptyList())
    var myQrData by mutableStateOf<MyQrResponse?>(null)
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = apiService.login(mapOf("email" to email, "password" to password))
                if (response.isSuccessful) {
                    token = "Bearer ${response.body()?.accessToken}"
                    fetchInitialData()
                    onSuccess()
                } else {
                    errorMessage = "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(nombre: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = apiService.register(mapOf("nombre" to nombre, "email" to email, "password" to password))
                if (response.isSuccessful) {
                    token = "Bearer ${response.body()?.accessToken}"
                    NotificationHelper.showNotification(context, "Registro Exitoso", "Bienvenido a BDMLT, $nombre")
                    fetchInitialData()
                    onSuccess()
                } else {
                    errorMessage = "Error al registrarse"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userDef = async { apiService.getMe(token) }
                val accDef = async { apiService.getAccounts(token) }
                val movDef = async { apiService.getMovements(token) }
                val servDef = async { apiService.getAvailableServices() }
                val qrDef = async { apiService.getMyQr(token) }

                val uRes = userDef.await()
                val aRes = accDef.await()
                val mRes = movDef.await()
                val sRes = servDef.await()
                val qRes = qrDef.await()

                if (uRes.isSuccessful) user = uRes.body()
                if (aRes.isSuccessful) accounts = aRes.body() ?: emptyList()
                if (mRes.isSuccessful) movements = mRes.body() ?: emptyList()
                if (sRes.isSuccessful) servicesAvailable = sRes.body() ?: emptyList()
                if (qRes.isSuccessful) myQrData = qRes.body()
                
            } catch (e: Exception) {
                errorMessage = "Error al sincronizar datos"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAccounts() {
        viewModelScope.launch {
            try {
                val response = apiService.getAccounts(token)
                if (response.isSuccessful) accounts = response.body() ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    fun fetchMovements() {
        viewModelScope.launch {
            try {
                val response = apiService.getMovements(token)
                if (response.isSuccessful) movements = response.body() ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    fun fetchMyQr() {
        viewModelScope.launch {
            try {
                val response = apiService.getMyQr(token)
                if (response.isSuccessful) myQrData = response.body()
            } catch (e: Exception) { }
        }
    }

    fun transfer(dest: String, amount: Double, desc: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.transfer(token, TransferRequest(dest, amount, desc))
                if (response.isSuccessful) {
                    NotificationHelper.showNotification(context, "Transferencia Exitosa", "Se han enviado $$amount a la cuenta $dest")
                    fetchAccounts()
                    fetchMovements()
                    onSuccess()
                } else {
                    errorMessage = "Transferencia fallida"
                    NotificationHelper.showNotification(context, "Transferencia Fallida", "No se pudo realizar el envío de $$amount")
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
                NotificationHelper.showNotification(context, "Error de Red", "Fallo en la conexión durante la transferencia")
            } finally {
                isLoading = false
            }
        }
    }

    fun payService(service: String, ref: String, amount: Double, usarCredito: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.payService(token, ServicePaymentRequest(service, ref, amount, usarCredito))
                if (response.isSuccessful) {
                    NotificationHelper.showNotification(context, "Pago de Servicio Exitoso", "Pago de $service por $$amount realizado")
                    fetchAccounts()
                    fetchMovements()
                    onSuccess()
                } else {
                    errorMessage = "Pago fallido"
                    NotificationHelper.showNotification(context, "Pago Fallido", "No se pudo procesar el pago de $service")
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
                NotificationHelper.showNotification(context, "Error de Red", "Fallo en la conexión durante el pago")
            } finally {
                isLoading = false
            }
        }
    }

    fun payCredit(amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.payCredit(token, CreditPaymentRequest(amount))
                if (response.isSuccessful) {
                    NotificationHelper.showNotification(context, "Abono Exitoso", "Has pagado $$amount a tu tarjeta de crédito")
                    fetchAccounts()
                    fetchMovements()
                    onSuccess()
                } else {
                    errorMessage = "Abono fallido"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        token = ""
        user = null
        accounts = emptyList()
        movements = emptyList()
        myQrData = null
    }
}
