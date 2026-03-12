package com.example.bancodelmalestar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bancodelmalestar.ui.components.CardAccount
import com.example.bancodelmalestar.ui.components.MovementItem
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.ExportUtils
import com.example.bancodelmalestar.util.getAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onPayCreditClick: () -> Unit, onSupportClick: () -> Unit) {
    val s = getAppStrings(viewModel)
    val debit = remember(viewModel.accounts) { viewModel.accounts.find { it.tipo == "debito" } }
    val credit = remember(viewModel.accounts) { viewModel.accounts.find { it.tipo == "credito" } }
    val context = LocalContext.current

    var showFilters by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedOrder by remember { mutableStateOf("desc") }
    var limit by remember { mutableStateOf("20") }

    var showLimitDialog by remember { mutableStateOf(false) }
    var limitType by remember { mutableStateOf("debito") }
    var limitValue by remember { mutableStateOf("") }


    PullToRefreshBox(
        isRefreshing = viewModel.isLoading,
        onRefresh = { viewModel.fetchInitialData() },
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    "${s.hello}, ${viewModel.user?.nombre ?: ""}", 
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // AI Support Assistant Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onSupportClick() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = s.aiAssistant,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = s.supportDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                debit?.let {
                    val limitInfo = viewModel.spendingLimits.find { l -> l.tipo == "debito" }
                    CardAccount(
                        title = s.debitAccount, 
                        number = it.numero, 
                        amount = it.saldo,
                        spendingLimit = limitInfo,
                        onSetLimit = { 
                            limitType = "debito"
                            limitValue = limitInfo?.limiteGastoMensual?.toString() ?: "0"
                            showLimitDialog = true 
                        },
                        viewModel = viewModel
                    )
                }
            }

            item {
                credit?.let {
                    val limitInfo = viewModel.spendingLimits.find { l -> l.tipo == "credito" }
                    CardAccount(
                        title = s.creditCard,
                        number = it.numero,
                        amount = it.deuda,
                        isCredit = true,
                        limit = it.limiteCredito,
                        onPayCredit = onPayCreditClick,
                        spendingLimit = limitInfo,
                        onSetLimit = {
                            limitType = "credito"
                            limitValue = limitInfo?.limiteGastoMensual?.toString() ?: "0"
                            showLimitDialog = true
                        },
                        viewModel = viewModel
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFilters = !showFilters }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = AppColors.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            s.movementFilters,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Red
                        )
                    }
                    Icon(
                        if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppColors.Red
                    )
                }
                
                AnimatedVisibility(visible = showFilters) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(s.movementType, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Todas" to null, "Transferencia" to "transferencia", "Servicio" to "pago_servicio", "Crédito" to "pago_credito", "Depósito" to "deposito").forEach { (label, value) ->
                                    FilterChip(
                                        selected = selectedType == value,
                                        onClick = { 
                                            selectedType = value
                                            viewModel.fetchMovements(limit.toIntOrNull(), selectedOrder, selectedType)
                                        },
                                        label = { Text(label, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AppColors.Red,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.order, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = selectedOrder == "desc",
                                            onClick = { 
                                                selectedOrder = "desc"
                                                viewModel.fetchMovements(limit.toIntOrNull(), selectedOrder, selectedType)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Red)
                                        )
                                        Text(s.recent, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        RadioButton(
                                            selected = selectedOrder == "asc",
                                            onClick = { 
                                                selectedOrder = "asc"
                                                viewModel.fetchMovements(limit.toIntOrNull(), selectedOrder, selectedType)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Red)
                                        )
                                        Text(s.old, fontSize = 12.sp)
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = limit,
                                    onValueChange = { 
                                        limit = it
                                        if (it.isNotEmpty()) {
                                            viewModel.fetchMovements(it.toIntOrNull(), selectedOrder, selectedType)
                                        }
                                    },
                                    label = { Text(s.limit, fontSize = 10.sp) },
                                    modifier = Modifier.width(80.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = appTextFieldColors(),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.user?.let { u ->
                                            ExportUtils.generateMovementsPdf(context, u, viewModel.accounts, viewModel.movements)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(s.exportPdf, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.user?.let { u ->
                                            ExportUtils.generateMovementsCsv(context, u, viewModel.movements)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gray)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(s.exportCsv, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    s.recentMovements, 
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(viewModel.movements, key = { it.id }) { movement ->
                MovementItem(movement = movement, viewModel = viewModel)
            }
        }
    }

    if (showLimitDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showLimitDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.settings, fontWeight = FontWeight.Bold, color = AppColors.Red)
                    Text("${limitType}", fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = limitValue,
                        onValueChange = { limitValue = it },
                        label = { Text(s.limit) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    val parsedLimit = limitValue.toDoubleOrNull() ?: 0.0
                    val isInvalid = limitType == "credito" && parsedLimit > 5000.0
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.updateSpendingLimit(parsedLimit, limitType) {
                                showLimitDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
                        enabled = !isInvalid
                    ) {
                        Text(s.save, color = Color.White)
                    }
                }
            }
        }
    }
}
