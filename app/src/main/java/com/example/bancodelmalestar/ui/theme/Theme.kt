package com.example.bancodelmalestar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.example.bancodelmalestar.ui.viewmodel.MainViewModel

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Red,
    secondary = AppColors.Gray,
    background = AppColors.SurfaceDark,
    surface = AppColors.CardDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Red,
    secondary = AppColors.Gray,
    background = AppColors.LightGray,
    surface = AppColors.White,
    onPrimary = Color.White,
    onBackground = AppColors.Black,
    onSurface = AppColors.Black
)

@Composable
@ReadOnlyComposable
fun isAppDarkTheme(viewModel: MainViewModel): Boolean {
    return when (viewModel.themeConfig) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun BancoDelMalestarTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val darkTheme = isAppDarkTheme(viewModel)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
