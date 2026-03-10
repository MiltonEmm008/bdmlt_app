package com.example.bancodelmalestar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoginScreen(viewModel: MainViewModel, onLoginSuccess: () -> Unit) {
    val s = getAppStrings(viewModel)
    var isRegister by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    
    var showUrlDialog by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf(viewModel.BASE_URL) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
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
                            localError = "Error"
                        } else if (password.length < 6) {
                            localError = "Error"
                        } else if (nombre.isBlank() || email.isBlank()) {
                            localError = "Error"
                        } else {
                            viewModel.register(nombre, email, password, onLoginSuccess)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onPayCreditClick: () -> Unit) {
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
        Dialog(onDismissRequest = { showLimitDialog = false }) {
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

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val s = getAppStrings(viewModel)
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(viewModel.user?.nombre ?: "") }
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var capturedImageFile by remember { mutableStateOf<File?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            val file = File(context.cacheDir, "profile_temp.jpg")
            FileOutputStream(file).use { 
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) 
            }
            capturedImageFile = file
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            s.myProfile,
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            if (capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Nueva foto",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                UserProfileImage(viewModel.user?.fotoPerfil, viewModel.BASE_URL, size = 120)
            }
            
            FloatingActionButton(
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch()
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = AppColors.Red,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tomar Foto", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            viewModel.user?.email ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(s.name) },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(s.changePassword, fontWeight = FontWeight.Bold, color = AppColors.Gray, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = passwordActual,
            onValueChange = { passwordActual = it },
            label = { Text(s.currentPassword) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = passwordNueva,
            onValueChange = { passwordNueva = it },
            label = { Text(s.newPassword) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val n = if (nombre != viewModel.user?.nombre) nombre else null
                val pa = if (passwordActual.isNotEmpty()) passwordActual else null
                val pn = if (passwordNueva.isNotEmpty()) passwordNueva else null
                
                viewModel.updateProfile(n, pa, pn, capturedImageFile) {
                    passwordActual = ""
                    passwordNueva = ""
                    capturedImageFile = null
                    capturedBitmap = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(s.saveChanges, color = Color.White)
            }
        }
        
        viewModel.errorMessage?.let {
            Text(it, color = AppColors.Red, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "${s.memberSince}: ${viewModel.user?.creadoEn?.split("T")?.get(0) ?: ""}",
            fontSize = 12.sp,
            color = AppColors.Gray
        )
    }
}

@Composable
fun BranchesScreen() {
    val tepicCenter = GeoPoint(21.5042, -104.8947)
    val points = remember {
        listOf(
            Triple("Sucursal La Cantera", 21.4879679, -104.8318394),
            Triple("Sucursal Av. México", 21.474311, -104.8586215),
            Triple("Sucursal Principal", 21.471942, -104.853678),
            Triple("Sucursal Las Brisas", 21.5148355, -104.9229643),
            Triple("Sucursal Cecy", 21.4784741, -104.8541761)
        )
    }
    val tepicBbox = remember {
        BoundingBox(21.55, -104.78, 21.42, -104.95)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(14.5)
                controller.setCenter(tepicCenter)
                setMultiTouchControls(true)
                minZoomLevel = 13.0
                maxZoomLevel = 18.0
                setScrollableAreaLimitDouble(tepicBbox)
                points.forEach { (name, lat, lon) ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(lat, lon)
                    marker.title = name
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val iconDrawable = ContextCompat.getDrawable(ctx, R.drawable.banco_mapa)
                    if (iconDrawable != null) {
                        marker.icon = iconDrawable
                    }
                    overlays.add(marker)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { mapView ->
            mapView.onDetach()
        }
    )
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigateToProfile: () -> Unit) {
    val s = getAppStrings(viewModel)
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            s.settings,
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        SettingsItem(
            icon = Icons.Default.Person,
            title = s.myProfile,
            subtitle = s.saveChanges,
            onClick = onNavigateToProfile
        )

        SettingsItem(
            icon = Icons.Default.Brightness6,
            title = s.theme,
            subtitle = when(viewModel.themeConfig) {
                "light" -> s.light
                "dark" -> s.dark
                else -> s.system
            },
            onClick = { showThemeDialog = true }
        )

        SettingsItem(
            icon = Icons.Default.Language,
            title = s.language,
            subtitle = when(viewModel.languageConfig) {
                "en" -> s.english
                "pt" -> s.portuguese
                else -> s.spanish
            },
            onClick = { showLangDialog = true }
        )

        SettingsItem(
            icon = Icons.Default.DeleteForever,
            title = s.deleteAccount,
            subtitle = s.deleteAccountWarning,
            iconColor = AppColors.Red,
            onClick = {
                Toast.makeText(context, s.comingSoon, Toast.LENGTH_SHORT).show()
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        SettingsItem(
            icon = Icons.Default.Description,
            title = s.terms,
            subtitle = "",
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                context.startActivity(intent)
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(s.selectTheme) },
            text = {
                Column {
                    ThemeOption(s.system, viewModel.themeConfig == "system") {
                        viewModel.updateThemeConfig("system")
                        showThemeDialog = false
                    }
                    ThemeOption(s.light, viewModel.themeConfig == "light") {
                        viewModel.updateThemeConfig("light")
                        showThemeDialog = false
                    }
                    ThemeOption(s.dark, viewModel.themeConfig == "dark") {
                        viewModel.updateThemeConfig("dark")
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text(s.selectLanguage) },
            text = {
                Column {
                    ThemeOption(s.spanish, viewModel.languageConfig == "es") {
                        viewModel.updateLanguageConfig("es")
                        showLangDialog = false
                    }
                    ThemeOption(s.english, viewModel.languageConfig == "en") {
                        viewModel.updateLanguageConfig("en")
                        showLangDialog = false
                    }
                    ThemeOption(s.portuguese, viewModel.languageConfig == "pt") {
                        viewModel.updateLanguageConfig("pt")
                        showLangDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = AppColors.Red))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = AppColors.Gray)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AppColors.Gray, modifier = Modifier.size(16.dp))
    }
}
