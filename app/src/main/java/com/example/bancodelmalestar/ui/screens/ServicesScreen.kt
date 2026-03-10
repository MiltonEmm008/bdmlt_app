package com.example.bancodelmalestar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(viewModel: MainViewModel, onAuthRequired: (() -> Unit) -> Unit) {
    val s = getAppStrings(viewModel)
    var selectedService by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var usarCredito by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            s.payService, 
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedService,
                onValueChange = {},
                readOnly = true,
                label = { Text(s.services) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                colors = appTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                viewModel.servicesAvailable.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service.servicio, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedService = service.servicio
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = reference,
            onValueChange = { reference = it },
            label = { Text("Ref") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(s.amount) },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = usarCredito,
                onCheckedChange = { usarCredito = it },
                colors = CheckboxDefaults.colors(checkedColor = AppColors.Red)
            )
            Text(s.creditCard, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onAuthRequired {
                    viewModel.payService(selectedService, reference, amount.toDoubleOrNull() ?: 0.0, usarCredito) {
                        reference = ""; amount = ""; selectedService = "" ; usarCredito = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
            enabled = !viewModel.isLoading && selectedService.isNotEmpty()
        ) {
            Text(s.enter, color = Color.White)
        }
    }
}
