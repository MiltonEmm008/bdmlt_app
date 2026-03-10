package com.example.bancodelmalestar.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bancodelmalestar.ui.components.UserProfileImage
import com.example.bancodelmalestar.ui.components.appTextFieldColors
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings
import java.io.File
import java.io.FileOutputStream

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
