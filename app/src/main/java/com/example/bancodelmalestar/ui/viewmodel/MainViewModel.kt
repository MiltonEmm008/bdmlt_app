package com.example.bancodelmalestar.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bancodelmalestar.NotificationHelper
import com.example.bancodelmalestar.data.model.*
import com.example.bancodelmalestar.data.remote.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    
    var BASE_URL by mutableStateOf(prefs.getString("base_url", "http://10.0.2.2:8000/api/") ?: "http://10.0.2.2:8000/api/")
        private set

    private var retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var apiService = retrofit.create(ApiService::class.java)

    var token by mutableStateOf(prefs.getString("token", null))
    var user by mutableStateOf<User?>(null)
    var accounts by mutableStateOf<List<Account>>(emptyList())
    var movements by mutableStateOf<List<Movement>>(emptyList())
    var servicesAvailable by mutableStateOf<List<ServiceAvailable>>(emptyList())
    var spendingLimits by mutableStateOf<List<SpendingLimit>>(emptyList())
    var myQrData by mutableStateOf<MyQrResponse?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var themeConfig by mutableStateOf(prefs.getString("theme", "system") ?: "system")
    var languageConfig by mutableStateOf(prefs.getString("lang", "es") ?: "es")

    init {
        if (token != null) {
            fetchInitialData()
        }
        fetchServices()
    }

    fun updateBaseUrl(newUrl: String) {
        val url = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        BASE_URL = url
        prefs.edit().putString("base_url", url).apply()
        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    fun updateThemeConfig(config: String) {
        themeConfig = config
        prefs.edit().putString("theme", config).apply()
    }

    fun updateLanguageConfig(config: String) {
        languageConfig = config
        prefs.edit().putString("lang", config).apply()
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = apiService.login(mapOf("email" to email, "password" to password))
                if (response.isSuccessful) {
                    token = "Bearer ${response.body()?.accessToken}"
                    prefs.edit().putString("token", token).apply()
                    fetchInitialData()
                    onSuccess()
                } else {
                    errorMessage = "Error al iniciar sesión"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
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
                    prefs.edit().putString("token", token).apply()
                    fetchInitialData()
                    onSuccess()
                } else {
                    errorMessage = "Error en el registro"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        token = null
        user = null
        accounts = emptyList()
        movements = emptyList()
        prefs.edit().remove("token").apply()
    }

    fun fetchInitialData() {
        val currentToken = token ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val userRes = apiService.getMe(currentToken)
                if (userRes.isSuccessful) user = userRes.body()

                val accRes = apiService.getAccounts(currentToken)
                if (accRes.isSuccessful) accounts = accRes.body() ?: emptyList()

                fetchMovements()
                fetchSpendingLimits()
            } catch (e: Exception) {
                errorMessage = "Error al cargar datos"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchMovements(limit: Int? = 20, order: String = "desc", type: String? = null) {
        val currentToken = token ?: return
        viewModelScope.launch {
            try {
                val movRes = apiService.getMovements(currentToken, limit, order, type)
                if (movRes.isSuccessful) movements = movRes.body() ?: emptyList()
            } catch (e: Exception) {}
        }
    }

    private fun fetchServices() {
        viewModelScope.launch {
            try {
                val res = apiService.getAvailableServices()
                if (res.isSuccessful) servicesAvailable = res.body() ?: emptyList()
            } catch (e: Exception) {}
        }
    }

    fun fetchSpendingLimits() {
        val currentToken = token ?: return
        viewModelScope.launch {
            try {
                val res = apiService.getSpendingLimits(currentToken)
                if (res.isSuccessful) spendingLimits = res.body() ?: emptyList()
            } catch (e: Exception) {}
        }
    }

    fun updateSpendingLimit(limit: Double, tipo: String, onSuccess: () -> Unit) {
        val currentToken = token ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val res = apiService.updateSpendingLimit(currentToken, UpdateSpendingLimitRequest(limit, tipo))
                if (res.isSuccessful) {
                    spendingLimits = res.body() ?: emptyList()
                    onSuccess()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun transfer(destAccount: String, amount: Double, description: String, onSuccess: () -> Unit) {
        val currentToken = token ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val res = apiService.transfer(currentToken, TransferRequest(destAccount, amount, description))
                if (res.isSuccessful) {
                    fetchInitialData()
                    NotificationHelper.showNotification(getApplication(), "Transferencia Exitosa", "Se han enviado $amount a la cuenta $destAccount")
                    onSuccess()
                } else {
                    errorMessage = "Error en transferencia"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun payService(service: String, reference: String, amount: Double, usarCredito: Boolean, onSuccess: () -> Unit) {
        val currentToken = token ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val res = apiService.payService(currentToken, ServicePaymentRequest(service, reference, amount, usarCredito))
                if (res.isSuccessful) {
                    fetchInitialData()
                    NotificationHelper.showNotification(getApplication(), "Pago de Servicio", "Pago de $service por $amount realizado")
                    onSuccess()
                } else {
                    errorMessage = "Error en pago de servicio"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun payCredit(amount: Double, onSuccess: () -> Unit) {
        val currentToken = token ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val res = apiService.payCredit(currentToken, CreditPaymentRequest(amount))
                if (res.isSuccessful) {
                    fetchInitialData()
                    onSuccess()
                } else {
                    errorMessage = "Saldo insuficiente o error en el pago"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(nombre: String?, passActual: String?, passNueva: String?, imageFile: File?, onSuccess: () -> Unit) {
        val currentToken = token ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val nameBody = nombre?.toRequestBody("text/plain".toMediaTypeOrNull())
                val paBody = passActual?.toRequestBody("text/plain".toMediaTypeOrNull())
                val pnBody = passNueva?.toRequestBody("text/plain".toMediaTypeOrNull())
                val photoPart = imageFile?.let {
                    val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("foto", it.name, requestFile)
                }

                val res = apiService.updateMe(currentToken, nameBody, paBody, pnBody, photoPart)
                if (res.isSuccessful) {
                    user = res.body()
                    onSuccess()
                } else {
                    errorMessage = "Error al actualizar perfil"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchMyQr() {
        val currentToken = token ?: return
        viewModelScope.launch {
            try {
                val res = apiService.getMyQr(currentToken)
                if (res.isSuccessful) myQrData = res.body()
            } catch (e: Exception) {}
        }
    }
}
