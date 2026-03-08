package com.example.bancodelmalestar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.Locale

object AppColors {
    val Red = Color(0xFFD32F2F)
    val Green = Color(0xFF388E3C)
    val Gray = Color(0xFF757575)
    val LightGray = Color(0xFFF5F5F5)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF212121)
    val HeaderBg = Color(0xFFEEEEEE)
}

@Composable
fun appTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Red,
        unfocusedBorderColor = AppColors.Gray,
        focusedLabelColor = AppColors.Red,
        unfocusedLabelColor = AppColors.Gray,
        cursorColor = AppColors.Red,
        focusedTextColor = AppColors.Black,
        unfocusedTextColor = AppColors.Black
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_banco),
                    contentDescription = "Logo BDMLT",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "BDMLT",
                    color = AppColors.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.HeaderBg
        )
    )
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    NavigationBar(
        containerColor = AppColors.White,
        tonalElevation = 8.dp
    ) {
        val items = remember {
            listOf(
                Triple("home", Icons.Default.Home, "Inicio"),
                Triple("transfers", Icons.AutoMirrored.Filled.Send, "Transf."),
                Triple("services", Icons.Default.Receipt, "Servicios"),
                Triple("profile", Icons.Default.Person, "Perfil"),
                Triple("branches", Icons.Default.LocationOn, "Sucurs.")
            )
        }

        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Red,
                    unselectedIconColor = AppColors.Gray,
                    selectedTextColor = AppColors.Red,
                    indicatorColor = AppColors.LightGray
                )
            )
        }
        
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir") },
            label = { Text("Salir", fontSize = 10.sp) },
            selected = false,
            onClick = onLogout,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = AppColors.Gray
            )
        )
    }
}

@Composable
fun CardAccount(
    title: String,
    number: String,
    amount: Double,
    isCredit: Boolean = false,
    limit: Double = 0.0,
    onPayCredit: (() -> Unit)? = null,
    spendingLimit: SpendingLimit? = null,
    onSetLimit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCredit) AppColors.Red else AppColors.Green
                )
                IconButton(onClick = onSetLimit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurar Límite", tint = AppColors.Gray)
                }
            }
            Text(
                "No. $number",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val amountText = remember(amount) { String.format(Locale.US, "$%.2f", amount) }
            Text(
                amountText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Black
            )

            if (spendingLimit != null && spendingLimit.limiteGastoMensual > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (spendingLimit.gastoMesActual / spendingLimit.limiteGastoMensual).toFloat()
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (progress > 0.9f) AppColors.Red else AppColors.Green,
                    trackColor = AppColors.LightGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Gastado: $${String.format(Locale.US, "%.2f", spendingLimit.gastoMesActual)}",
                        fontSize = 10.sp,
                        color = AppColors.Gray
                    )
                    Text(
                        "Límite: $${String.format(Locale.US, "%.2f", spendingLimit.limiteGastoMensual)}",
                        fontSize = 10.sp,
                        color = AppColors.Gray
                    )
                }
            }

            if (isCredit) {
                val limitText = remember(limit) { String.format(Locale.US, "Límite Crédito: $%.2f", limit) }
                Text(
                    limitText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (amount > 0) {
                    Button(
                        onClick = { onPayCredit?.invoke() },
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                    ) {
                        Text("Pagar Deuda", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MovementItem(movement: Movement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movement.descripcion,
                    color = AppColors.Black,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    movement.creadaEn.split("T")[0],
                    color = AppColors.Gray,
                    fontSize = 12.sp
                )
                Text(
                    movement.tipo.replace("_", " ").replaceFirstChar { it.uppercase() },
                    color = AppColors.Gray,
                    fontSize = 10.sp
                )
            }
            val color = when (movement.tipo) {
                "deposito" -> AppColors.Green
                else -> AppColors.Red
            }
            val prefix = if (movement.tipo == "deposito") "+" else "-"
            Text(
                "$prefix$${String.format(Locale.US, "%.2f", movement.monto)}",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun UserProfileImage(photoUrl: String?, baseUrl: String, size: Int = 100) {
    val fullUrl = if (photoUrl != null) "$baseUrl$photoUrl" else null
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(fullUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.foto_usuario),
        error = painterResource(R.drawable.foto_usuario),
        contentDescription = "Foto de perfil",
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
