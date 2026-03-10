package com.example.bancodelmalestar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bancodelmalestar.R
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings

@Composable
fun LoginScreen(viewModel: MainViewModel, onLoginSuccess: () -> Unit) {
    val s = getAppStrings(viewModel)
    var isRegister by remember { mutableStateOf(false) }
    
    // Form fields
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var calleNumero by remember { mutableStateOf("") }
    var colonia by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    
    var localError by remember { mutableStateOf<String?>(null) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf(viewModel.BASE_URL) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_banco),
                contentDescription = "Logo BDMLT",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isRegister) s.register else s.welcome,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.Red,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; localError = null },
                    label = { Text(s.name) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; localError = null },
                label = { Text(s.email) },
                modifier = Modifier.fillMaxWidth(),
                colors = appTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; localError = null },
                label = { Text(s.password) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = appTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (isRegister) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; localError = null },
                    label = { Text(s.confirmPassword) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Información Adicional (Opcional)", color = AppColors.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = calleNumero,
                    onValueChange = { calleNumero = it },
                    label = { Text("Calle y Número") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = colonia,
                    onValueChange = { colonia = it },
                    label = { Text("Colonia") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ciudad,
                    onValueChange = { ciudad = it },
                    label = { Text("Ciudad") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = codigoPostal,
                    onValueChange = { codigoPostal = it },
                    label = { Text("Código Postal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            val displayError = localError ?: viewModel.errorMessage
            displayError?.let {
                Text(it, color = AppColors.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isRegister) {
                        if (password != confirmPassword) {
                            localError = "Contraseñas no coinciden"
                        } else if (password.length < 6) {
                            localError = "Contraseña muy corta"
                        } else if (nombre.isBlank() || email.isBlank()) {
                            localError = "Campos obligatorios vacíos"
                        } else {
                            viewModel.register(
                                nombre, email, password,
                                telefono.ifBlank { null },
                                calleNumero.ifBlank { null },
                                colonia.ifBlank { null },
                                ciudad.ifBlank { null },
                                codigoPostal.ifBlank { null },
                                onLoginSuccess
                            )
                        }
                    } else {
                        viewModel.login(email, password, onLoginSuccess)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isRegister) s.doRegister else s.enter, color = Color.White)
                }
            }

            TextButton(onClick = { 
                isRegister = !isRegister
                localError = null
                confirmPassword = ""
            }) {
                Text(
                    if (isRegister) s.haveAccount else s.noAccount,
                    color = AppColors.Gray
                )
            }
        }

        IconButton(
            onClick = { showUrlDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = s.settings, tint = AppColors.Gray)
        }
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("URL Base") },
            text = {
                Column {
                    Text("${viewModel.BASE_URL}", fontSize = 12.sp, color = AppColors.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("http://ip:port") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateBaseUrl(tempUrl)
                    showUrlDialog = false
                }) {
                    Text(s.save, color = AppColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text(s.cancel, color = AppColors.Gray)
                }
            }
        )
    }
}
