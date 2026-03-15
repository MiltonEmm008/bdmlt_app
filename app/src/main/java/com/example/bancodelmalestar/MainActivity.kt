package com.example.bancodelmalestar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bancodelmalestar.ui.components.AppHeader
import com.example.bancodelmalestar.ui.components.BottomNavigationBar
import com.example.bancodelmalestar.ui.screens.*
import com.example.bancodelmalestar.ui.theme.BancoDelMalestarTheme
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        
        // Inicializar MapLibre antes de setContent con el valor correcto del Enum
        MapLibre.getInstance(this, null, WellKnownTileServer.MapLibre)
        
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "login"

            BancoDelMalestarTheme(viewModel = viewModel) {
                Scaffold(
                    topBar = { 
                        if (currentRoute != "login" && currentRoute != "support" && currentRoute != "forgot_password") {
                            AppHeader(onLogout = {
                                viewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }, viewModel = viewModel)
                        } 
                    },
                    bottomBar = {
                        if (currentRoute != "login" && currentRoute != "support" && currentRoute != "forgot_password") {
                            BottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigate = { navController.navigate(it) },
                                viewModel = viewModel
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = if (currentRoute == "support" || currentRoute == "login" || currentRoute == "forgot_password") Modifier else Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onForgotPassword = {
                                    navController.navigate("forgot_password")
                                }
                            )
                        }
                        composable("forgot_password") {
                            ForgotPasswordScreen(viewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onPayCreditClick = { navController.navigate("pay_credit") },
                                onSupportClick = { navController.navigate("support") }
                            )
                        }
                        composable("transfers") {
                            TransfersScreen(viewModel) { onAuth ->
                                showBiometricPrompt(onAuth)
                            }
                        }
                        composable("services") {
                            ServicesScreen(viewModel) { onAuth ->
                                showBiometricPrompt(onAuth)
                            }
                        }
                        composable("profile") {
                            ProfileScreen(viewModel) {
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                        composable("branches") {
                            BranchesScreen()
                        }
                        composable("settings") {
                            SettingsScreen(viewModel, onNavigateToProfile = {
                                navController.navigate("profile")
                            })
                        }
                        composable("pay_credit") {
                            PayCreditScreen(viewModel, { onAuth ->
                                showBiometricPrompt(onAuth)
                            }) {
                                navController.popBackStack()
                            }
                        }
                        composable("support") {
                            SupportScreen(viewModel, onBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(this)

        val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // En caso de error (cancelado por usuario, etc), no llamamos a onSuccess
                    Toast.makeText(applicationContext, "Autenticación requerida para continuar", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Requerida")
            .setSubtitle("Confirma tu identidad para realizar el movimiento")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
