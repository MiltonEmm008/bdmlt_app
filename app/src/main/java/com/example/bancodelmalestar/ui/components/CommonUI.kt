package com.example.bancodelmalestar.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.bancodelmalestar.R
import com.example.bancodelmalestar.data.model.Account
import com.example.bancodelmalestar.data.model.Movement
import com.example.bancodelmalestar.data.model.SpendingLimit
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.theme.isAppDarkTheme
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings
import java.util.Locale

@Composable
fun appTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Red,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = AppColors.Red,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = AppColors.Red,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(onLogout: () -> Unit, viewModel: MainViewModel) {
    val isDark = isAppDarkTheme(viewModel)
    val strings = getAppStrings(viewModel)
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
                    color = if (isDark) Color.White else AppColors.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = strings.logout,
                    tint = AppColors.Red
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isDark) AppColors.HeaderBgDark else AppColors.HeaderBgLight
        )
    )
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel
) {
    val strings = getAppStrings(viewModel)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = remember(strings) {
            listOf(
                Triple("home", Icons.Default.Home, strings.hello),
                Triple("transfers", Icons.AutoMirrored.Filled.Send, strings.transfers),
                Triple("services", Icons.Default.Receipt, strings.services),
                Triple("branches", Icons.Default.LocationOn, strings.branches),
                Triple("settings", Icons.Default.Settings, strings.settings)
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
                    indicatorColor = AppColors.Red.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun PhysicalCard(number: String, isCredit: Boolean) {
    val bgColor = if (isCredit) AppColors.Red else AppColors.Green
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp)
                    .padding(top = 25.dp)
                    .background(Color.White.copy(alpha = 0.8f))
            )

            Text(
                text = number.chunked(4).joinToString(" "),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Image(
                painter = painterResource(id = R.drawable.logo_banco),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Fit
            )
        }
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
    onSetLimit: () -> Unit,
    viewModel: MainViewModel
) {
    val strings = getAppStrings(viewModel)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PhysicalCard(number = number, isCredit = isCredit)
            
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
                    Icon(Icons.Default.Settings, contentDescription = strings.settings, tint = AppColors.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val amountText = remember(amount) { String.format(Locale.US, "$%.2f", amount) }
            Text(
                amountText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (spendingLimit != null && spendingLimit.limiteGastoMensual > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (spendingLimit.gastoMesActual / spendingLimit.limiteGastoMensual).toFloat()
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (progress > 0.9f) AppColors.Red else AppColors.Green,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${strings.limit}: $${String.format(Locale.US, "%.2f", spendingLimit.gastoMesActual)}",
                        fontSize = 10.sp,
                        color = AppColors.Gray
                    )
                    Text(
                        "${strings.limit}: $${String.format(Locale.US, "%.2f", spendingLimit.limiteGastoMensual)}",
                        fontSize = 10.sp,
                        color = AppColors.Gray
                    )
                }
            }

            if (isCredit) {
                val limitText = remember(limit) { String.format(Locale.US, "${strings.limit}: $%.2f", limit) }
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
                        Text(strings.payDebt, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MovementItem(movement: Movement, viewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onSurface,
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
