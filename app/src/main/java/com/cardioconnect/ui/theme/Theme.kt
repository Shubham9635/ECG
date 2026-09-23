package com.cardioconnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cardioconnect.domain.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PhosphorGreen,
    onPrimary = Color.Black,
    primaryContainer = CardDark,
    onPrimaryContainer = PhosphorGreen,
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = CardDark,
    onSecondaryContainer = ElectricCyan,
    tertiary = HospitalBlue,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderDark,
    error = EmergencyRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00A34D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF003914),
    secondary = Color(0xFF00838F),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    onSurface = Color(0xFF1A1F2C),
    outline = Color(0xFFE0E5EC)
)

@Composable
fun CardioConnectTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
