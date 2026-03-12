package com.example.bancodelmalestar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bancodelmalestar.R
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel, onBackToLogin: () -> Unit) {
    val s = getAppStrings(viewModel)
    var email by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var emailSent by remember { mutableStateOf(false) }

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
                text = s.resetPassword,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.Red,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (!emailSent) {
                Text(
                    text = s.enterEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; localError = null },
                    label = { Text(s.email) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                val displayError = localError ?: viewModel.errorMessage
                displayError?.let {
                    Text(it, color = AppColors.Red, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            localError = "Email is required"
                        } else {
                            viewModel.forgotPassword(email) {
                                emailSent = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(s.send, color = Color.White)
                    }
                }
            } else {
                Text(
                    text = s.emailSent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text(s.login, color = Color.White)
                }
            }

            TextButton(onClick = onBackToLogin) {
                Text(s.cancel, color = AppColors.Gray)
            }
        }
    }
}
