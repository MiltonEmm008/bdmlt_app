package com.example.bancodelmalestar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    private val apiService: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE // Reducir logs para producción/velocidad
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
                // Lanzar todas las peticiones en paralelo
                val userDef = async { apiService.getMe(token) }
                val accDef = async { apiService.getAccounts(token) }
                val movDef = async { apiService.getMovements(token) }
                val servDef = async { apiService.getAvailableServices() }

                // Esperar resultados
                val uRes = userDef.await()
                val aRes = accDef.await()
                val mRes = movDef.await()
                val sRes = servDef.await()

                // Actualizar estados una sola vez (o de forma agrupada por Compose)
                if (uRes.isSuccessful) user = uRes.body()
                if (aRes.isSuccessful) accounts = aRes.body() ?: emptyList()
                if (mRes.isSuccessful) movements = mRes.body() ?: emptyList()
                if (sRes.isSuccessful) servicesAvailable = sRes.body() ?: emptyList()
                
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

    fun transfer(dest: String, amount: Double, desc: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.transfer(token, TransferRequest(dest, amount, desc))
                if (response.isSuccessful) {
                    fetchAccounts()
                    fetchMovements()
                    onSuccess()
                } else {
                    errorMessage = "Transferencia fallida"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
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
                    fetchAccounts()
                    fetchMovements()
                    onSuccess()
                } else {
                    errorMessage = "Pago fallido"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
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
    }
}
