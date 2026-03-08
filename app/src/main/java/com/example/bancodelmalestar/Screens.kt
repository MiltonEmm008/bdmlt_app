package com.example.bancodelmalestar

import android.Manifest
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
    var isRegister by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White)
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
            text = if (isRegister) "Registro BDMLT" else "Bienvenido a BDMLT",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (isRegister) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; localError = null },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                colors = appTextFieldColors(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; localError = null },
            label = { Text("Contraseña") },
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
                label = { Text("Confirmar Contraseña") },
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
                        localError = "Las contraseñas no coinciden"
                    } else if (password.length < 6) {
                        localError = "La contraseña debe tener al menos 6 caracteres"
                    } else if (nombre.isBlank() || email.isBlank()) {
                        localError = "Todos los campos son obligatorios"
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
                Text(if (isRegister) "Registrar" else "Entrar", color = Color.White)
            }
        }

        TextButton(onClick = { 
            isRegister = !isRegister
            localError = null
            confirmPassword = ""
        }) {
            Text(
                if (isRegister) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate",
                color = AppColors.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onPayCreditClick: () -> Unit) {
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

    val types = listOf(
        "Todas" to null,
        "Transferencia" to "transferencia",
        "Servicio" to "pago_servicio",
        "Crédito" to "pago_credito",
        "Depósito" to "deposito"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.LightGray)
            .padding(16.dp)
    ) {
        item {
            Text(
                "Hola, ${viewModel.user?.nombre ?: ""}", 
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            debit?.let {
                val limitInfo = viewModel.spendingLimits.find { l -> l.tipo == "debito" }
                CardAccount(
                    title = "Cuenta de Débito", 
                    number = it.numero, 
                    amount = it.saldo,
                    spendingLimit = limitInfo,
                    onSetLimit = { 
                        limitType = "debito"
                        limitValue = limitInfo?.limiteGastoMensual?.toString() ?: "0"
                        showLimitDialog = true 
                    }
                )
            }
        }

        item {
            credit?.let {
                val limitInfo = viewModel.spendingLimits.find { l -> l.tipo == "credito" }
                CardAccount(
                    title = "Tarjeta de Crédito",
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
                    }
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
                        "Filtros de Movimientos",
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
                    colors = CardDefaults.cardColors(containerColor = AppColors.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tipo de movimiento:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            types.forEach { (label, value) ->
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
                                Text("Orden:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedOrder == "desc",
                                        onClick = { 
                                            selectedOrder = "desc"
                                            viewModel.fetchMovements(limit.toIntOrNull(), selectedOrder, selectedType)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Red)
                                    )
                                    Text("Recientes", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    RadioButton(
                                        selected = selectedOrder == "asc",
                                        onClick = { 
                                            selectedOrder = "asc"
                                            viewModel.fetchMovements(limit.toIntOrNull(), selectedOrder, selectedType)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Red)
                                    )
                                    Text("Antiguos", fontSize = 12.sp)
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
                                label = { Text("Límite", fontSize = 10.sp) },
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
                                Text("Exportar PDF", fontSize = 12.sp)
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
                                Text("Exportar CSV", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Movimientos Recientes", 
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(viewModel.movements, key = { it.id }) { movement ->
            MovementItem(movement = movement)
        }
    }

    if (showLimitDialog) {
        Dialog(onDismissRequest = { showLimitDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configurar Límite de Gasto", fontWeight = FontWeight.Bold, color = AppColors.Red)
                    Text("Cuenta: ${limitType.replaceFirstChar { it.uppercase() }}", fontSize = 12.sp)
                    if (limitType == "credito") {
                        Text("(Máximo permitido: $5,000)", fontSize = 10.sp, color = AppColors.Gray)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = limitValue,
                        onValueChange = { limitValue = it },
                        label = { Text("Límite Mensual (0 para desactivar)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    val parsedLimit = limitValue.toDoubleOrNull() ?: 0.0
                    val isInvalid = limitType == "credito" && parsedLimit > 5000.0
                    
                    if (isInvalid) {
                        Text("El límite no puede exceder los $5,000", color = AppColors.Red, fontSize = 10.sp)
                    }

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
                        Text("Guardar Límite", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TransfersScreen(viewModel: MainViewModel, onAuthRequired: (() -> Unit) -> Unit) {
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
            .background(AppColors.White)
            .padding(16.dp)
    ) {
        Text(
            "Transferir a otros usuarios", 
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
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.LightGray),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("Generar QR", color = AppColors.Black)
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
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.LightGray),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("Escanear", color = AppColors.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = destAccount,
            onValueChange = { destAccount = it },
            label = { Text("Número de cuenta destino") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Concepto (Opcional)") },
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
            Text("Transferir", color = Color.White)
        }
    }

    if (showQrInputDialog) {
        Dialog(onDismissRequest = { showQrInputDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datos para recibir transferencia", fontWeight = FontWeight.Bold, color = AppColors.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = qrAmountInput,
                        onValueChange = { qrAmountInput = it },
                        label = { Text("Monto a cobrar") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = qrConceptInput,
                        onValueChange = { qrConceptInput = it },
                        label = { Text("Concepto (Opcional)") },
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
                        Text("Generar QR", color = Color.White)
                    }
                }
            }
        }
    }

    if (showGeneratedQr) {
        Dialog(onDismissRequest = { showGeneratedQr = false; qrAmountInput = ""; qrConceptInput = "" }) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Código QR de Cobro", fontWeight = FontWeight.Bold, color = AppColors.Red)
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
                                contentDescription = "QR Generado",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(data.nombre, color = AppColors.Black)
                        Text("$${qrAmountInput}", fontWeight = FontWeight.Bold, color = AppColors.Red)
                        if (qrConceptInput.isNotEmpty()) Text(qrConceptInput, color = AppColors.Gray)
                    } ?: CircularProgressIndicator(color = AppColors.Red)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showGeneratedQr = false; qrAmountInput = ""; qrConceptInput = "" }) {
                        Text("Cerrar", color = AppColors.Red)
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
                            
                            val diffMinutes = (now.time - (qrDate?.time ?: 0)) / (60 * 1000)
                            
                            if (diffMinutes <= 10) {
                                destAccount = decodedData.numero_cuenta
                                amount = decodedData.monto.toString()
                                description = decodedData.concepto
                            } else {
                                Toast.makeText(context, "El código QR ha expirado (más de 10 min)", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al procesar la fecha del QR", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Si no es un JSON de transferencia, tal vez es solo el número de cuenta
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
    var selectedService by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var usarCredito by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White)
            .padding(16.dp)
    ) {
        Text(
            "Pago de Servicios", 
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
                label = { Text("Seleccionar Servicio") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                colors = appTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppColors.White)
            ) {
                viewModel.servicesAvailable.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service.servicio, color = AppColors.Black) },
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
            label = { Text("Referencia / Contrato") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto a pagar") },
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
            Text("Pagar con Tarjeta de Crédito", color = AppColors.Black)
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
            Text("Realizar Pago", color = Color.White)
        }
    }
}

@Composable
fun PayCreditScreen(
    viewModel: MainViewModel,
    onAuthRequired: (() -> Unit) -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val creditAccount = remember(viewModel.accounts) { viewModel.accounts.find { it.tipo == "credito" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White)
            .padding(16.dp)
    ) {
        Text(
            "Pagar Tarjeta de Crédito", 
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Red,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        creditAccount?.let {
            Text("Deuda actual: $${it.deuda}", color = AppColors.Black)
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto a abonar") },
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
            Text("Pagar Deuda", color = Color.White)
        }
        
        viewModel.errorMessage?.let {
            Text(it, color = AppColors.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
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
            .background(AppColors.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Mi Perfil",
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
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cambiar Contraseña", fontWeight = FontWeight.Bold, color = AppColors.Gray, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = passwordActual,
            onValueChange = { passwordActual = it },
            label = { Text("Contraseña Actual") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = passwordNueva,
            onValueChange = { passwordNueva = it },
            label = { Text("Nueva Contraseña") },
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
                Text("Guardar Cambios", color = Color.White)
            }
        }
        
        viewModel.errorMessage?.let {
            Text(it, color = AppColors.Red, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Miembro desde: ${viewModel.user?.creadoEn?.split("T")?.get(0) ?: ""}",
            fontSize = 12.sp,
            color = AppColors.Gray
        )
    }
}

@Composable
fun BranchesScreen() {
    val tepicCenter = GeoPoint(21.5042, -104.8947)
    
    // Fixed points provided
    val points = remember {
        listOf(
            Triple("Sucursal La Cantera", 21.4879679, -104.8318394),
            Triple("Sucursal Av. México", 21.474311, -104.8586215),
            Triple("Sucursal Principal", 21.471942, -104.853678),
            Triple("Sucursal Las Brisas", 21.5148355, -104.9229643),
            Triple("Sucursal Cecy", 21.4784741, -104.8541761)
        )
    }

    // Tepic Bounding Box (roughly)
    val tepicBbox = remember {
        BoundingBox(21.55, -104.78, 21.42, -104.95)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(14.5)
                controller.setCenter(tepicCenter)
                setMultiTouchControls(true)
                
                // Limits
                minZoomLevel = 13.0
                maxZoomLevel = 18.0
                setScrollableAreaLimitDouble(tepicBbox)
                
                points.forEach { (name, lat, lon) ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(lat, lon)
                    marker.title = name
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    // Use custom icon banco_mapa.png
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
        val observer = LifecycleEventObserver { _, event -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
