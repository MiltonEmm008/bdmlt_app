package com.example.bancodelmalestar.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.bancodelmalestar.data.model.QrTransferData
import com.example.bancodelmalestar.ui.components.QrScannerView
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.QrUtils
import com.example.bancodelmalestar.util.getAppStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransfersScreen(viewModel: MainViewModel, onAuthRequired: (() -> Unit) -> Unit) {
    val s = getAppStrings(viewModel)
    var destAccount by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var showQrInputDialog by remember { mutableStateOf(false) }
    var qrAmountInput by remember { mutableStateOf("") }
    var qrConceptInput by remember { mutableStateOf("") }
    
    var showGeneratedQr by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            s.transfers, 
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    onAuthRequired {
                        showQrInputDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text(s.generateQr, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        showScanner = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text(s.scan, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = destAccount,
            onValueChange = { destAccount = it },
            label = { Text(s.destAccount) },
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
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(s.concept) },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                onAuthRequired {
                    viewModel.transfer(destAccount, amount.toDoubleOrNull() ?: 0.0, description) {
                        destAccount = ""; amount = ""; description = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
            enabled = !viewModel.isLoading
        ) {
            Text(s.transfer, color = Color.White)
        }
    }

    if (showQrInputDialog) {
        Dialog(onDismissRequest = { showQrInputDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.generateQr, fontWeight = FontWeight.Bold, color = AppColors.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = qrAmountInput,
                        onValueChange = { qrAmountInput = it },
                        label = { Text(s.amount) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = qrConceptInput,
                        onValueChange = { qrConceptInput = it },
                        label = { Text(s.concept) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.fetchMyQr()
                            showQrInputDialog = false
                            showGeneratedQr = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
                        enabled = qrAmountInput.toDoubleOrNull() != null && qrAmountInput.toDouble() > 0
                    ) {
                        Text(s.generateQr, color = Color.White)
                    }
                }
            }
        }
    }

    if (showGeneratedQr) {
        Dialog(onDismissRequest = { showGeneratedQr = false; qrAmountInput = ""; qrConceptInput = "" }) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.generateQr, fontWeight = FontWeight.Bold, color = AppColors.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    viewModel.myQrData?.let { data ->
                        val qrTransferData = QrTransferData(
                            numero_cuenta = data.numeroCuenta,
                            nombre = data.nombre,
                            monto = qrAmountInput.toDoubleOrNull() ?: 0.0,
                            concepto = qrConceptInput,
                            fecha = data.fecha
                        )
                        val qrString = QrUtils.encodeQrData(qrTransferData)
                        val qrBitmap = remember(qrString) {
                            QrUtils.generateQrCode(qrString)
                        }
                        qrBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "QR",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(data.nombre, color = MaterialTheme.colorScheme.onSurface)
                        Text("$${qrAmountInput}", fontWeight = FontWeight.Bold, color = AppColors.Red)
                        if (qrConceptInput.isNotEmpty()) Text(qrConceptInput, color = AppColors.Gray)
                    } ?: CircularProgressIndicator(color = AppColors.Red)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showGeneratedQr = false; qrAmountInput = ""; qrConceptInput = "" }) {
                        Text(s.cancel, color = AppColors.Red)
                    }
                }
            }
        }
    }

    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }) {
            Box(modifier = Modifier.size(300.dp)) {
                QrScannerView { scannedValue ->
                    val decodedData = QrUtils.decodeQrData(scannedValue)
                    if (decodedData != null) {
                        try {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
                            val qrDate = sdf.parse(decodedData.fecha)
                            val now = Date()
                            
                            destAccount = decodedData.numero_cuenta
                            amount = decodedData.monto.toString()
                            description = decodedData.concepto
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        destAccount = scannedValue
                    }
                    showScanner = false
                }
            }
        }
    }
}
