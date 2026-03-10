package com.example.bancodelmalestar

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    var BASE_URL by mutableStateOf(prefs.getString("base_url", "http://10.223.4.143:3000/") ?: "http://10.223.4.143:3000/")
        private set

    // Theme state: "system", "light", "dark"
    var themeConfig by mutableStateOf(prefs.getString("theme_config", "system") ?: "system")
        private set

    // Language state: "es", "en", "pt"
    var languageConfig by mutableStateOf(prefs.getString("language_config", "es") ?: "es")
        private set

    private var _apiService: ApiService? = null
    val apiService: ApiService
        get() {
            if (_apiService == null) {
                _apiService = createApiService(BASE_URL)
            }
            return _apiService!!
        }

    private fun createApiService(url: String): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        BASE_URL = formattedUrl
        prefs.edit().putString("base_url", formattedUrl).apply()
        _apiService = null
    }

    fun updateThemeConfig(config: String) {
        themeConfig = config
        prefs.edit().putString("theme_config", config).apply()
    }

    fun updateLanguageConfig(config: String) {
        languageConfig = config
        prefs.edit().putString("language_config", config).apply()
    }

    var token by mutableStateOf("")
    var user by mutableStateOf<User?>(null)
    var accounts by mutableStateOf<List<Account>>(emptyList())
    var movements by mutableStateOf<List<Movement>>(emptyList())
    var spendingLimits by mutableStateOf<List<SpendingLimit>>(emptyList())
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
                val movDef = async { apiService.getMovements(token, limit = 20) }
                val limDef = async { apiService.getSpendingLimits(token) }
                val servDef = async { apiService.getAvailableServices() }
                val qrDef = async { apiService.getMyQr(token) }

                val uRes = userDef.await()
                val aRes = accDef.await()
                val mRes = movDef.await()
                val lRes = limDef.await()
                val sRes = servDef.await()
                val qRes = qrDef.await()

                if (uRes.isSuccessful) user = uRes.body()
                if (aRes.isSuccessful) accounts = aRes.body() ?: emptyList()
                if (mRes.isSuccessful) movements = mRes.body() ?: emptyList()
                if (lRes.isSuccessful) spendingLimits = lRes.body() ?: emptyList()
                if (sRes.isSuccessful) servicesAvailable = sRes.body() ?: emptyList()
                if (qRes.isSuccessful) myQrData = qRes.body()
                
            } catch (e: Exception) {
                errorMessage = "Error al sincronizar datos"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(
        nombre: String?,
        passActual: String?,
        passNueva: String?,
        fotoFile: File?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val namePart = nombre?.toRequestBody("text/plain".toMediaTypeOrNull())
                val curPassPart = passActual?.toRequestBody("text/plain".toMediaTypeOrNull())
                val newPassPart = passNueva?.toRequestBody("text/plain".toMediaTypeOrNull())
                
                var fotoPart: MultipartBody.Part? = null
                fotoFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    fotoPart = MultipartBody.Part.createFormData("foto", it.name, requestFile)
                }

                val response = apiService.updateMe(token, namePart, curPassPart, newPassPart, fotoPart)
                if (response.isSuccessful) {
                    user = response.body()
                    NotificationHelper.showNotification(context, "Perfil Actualizado", "Tus datos se han guardado correctamente")
                    onSuccess()
                } else {
                    errorMessage = "Error al actualizar perfil: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchSpendingLimits() {
        viewModelScope.launch {
            try {
                val response = apiService.getSpendingLimits(token)
                if (response.isSuccessful) spendingLimits = response.body() ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    fun updateSpendingLimit(limit: Double, type: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.updateSpendingLimit(token, UpdateSpendingLimitRequest(limit, type))
                if (response.isSuccessful) {
                    spendingLimits = response.body() ?: emptyList()
                    NotificationHelper.showNotification(context, "Límite Actualizado", "Nuevo límite establecido correctamente")
                    onSuccess()
                } else {
                    errorMessage = "Error al actualizar límite"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red"
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

    fun fetchMovements(limit: Int? = null, order: String? = null, type: String? = null) {
        viewModelScope.launch {
            try {
                val response = apiService.getMovements(token, limit, order, type)
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
                    fetchSpendingLimits()
                    onSuccess()
                } else {
                    errorMessage = "Transferencia fallida (Verifica tu límite de gasto)"
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
                    fetchSpendingLimits()
                    onSuccess()
                } else {
                    errorMessage = "Pago fallido (Verifica tu límite de gasto)"
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
                    fetchSpendingLimits()
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
        spendingLimits = emptyList()
        myQrData = null
    }
}
