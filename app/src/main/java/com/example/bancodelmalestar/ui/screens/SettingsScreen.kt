package com.example.bancodelmalestar.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bancodelmalestar.ui.theme.AppColors
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel
import com.example.bancodelmalestar.util.getAppStrings

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
