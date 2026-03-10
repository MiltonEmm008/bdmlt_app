package com.example.bancodelmalestar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings

@Composable
fun PayCreditScreen(
    viewModel: MainViewModel,
    onAuthRequired: (() -> Unit) -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val s = getAppStrings(viewModel)
    var amount by remember { mutableStateOf("") }
    val creditAccount = remember(viewModel.accounts) { viewModel.accounts.find { it.tipo == "credito" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            s.payDebt, 
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        creditAccount?.let {
            Text("$${it.deuda}", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(s.amount) },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                onAuthRequired {
                    viewModel.payCredit(amount.toDoubleOrNull() ?: 0.0) {
                        amount = ""
                        onPaymentSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
            enabled = !viewModel.isLoading
        ) {
            Text(s.payDebt, color = Color.White)
        }
        
        viewModel.errorMessage?.let {
            Text(it, color = AppColors.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
