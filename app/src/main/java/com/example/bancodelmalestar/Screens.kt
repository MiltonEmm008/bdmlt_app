package com.example.bancodelmalestar

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
        Text(
            text = if (isRegister) "Registro" else "Iniciar Sesión",
            style = MaterialTheme.typography.headlineLarge,
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

@Composable
fun HomeScreen(viewModel: MainViewModel, onPayCreditClick: () -> Unit) {
    val debit = remember(viewModel.accounts) { viewModel.accounts.find { it.tipo == "debito" } }
    val credit = remember(viewModel.accounts) { viewModel.accounts.find { it.tipo == "credito" } }

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
                CardAccount(title = "Cuenta de Débito", number = it.numero, amount = it.saldo)
            }
        }

        item {
            credit?.let {
                CardAccount(
                    title = "Tarjeta de Crédito",
                    number = it.numero,
                    amount = it.deuda,
                    isCredit = true,
                    limit = it.limiteCredito,
                    onPayCredit = onPayCreditClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Movimientos Recientes", 
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(viewModel.movements, key = { it.id }) { movement ->
            ListItem(
                headlineContent = { 
                    Text(movement.descripcion, color = AppColors.Black, fontWeight = FontWeight.Medium) 
                },
                supportingContent = { 
                    Text(movement.creadaEn.split("T")[0], color = AppColors.Gray) 
                },
                trailingContent = {
                    val color = when(movement.tipo) {
                        "deposito" -> AppColors.Green
                        else -> AppColors.Red
                    }
                    Text("$${movement.monto}", color = color, fontWeight = FontWeight.Bold)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider(color = AppColors.HeaderBg)
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
                onClick = { showQrInputDialog = true },
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
