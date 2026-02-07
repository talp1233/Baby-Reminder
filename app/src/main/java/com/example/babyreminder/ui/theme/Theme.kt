package com.example.babyreminder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BabyOrange,
    onPrimary = Color.White,
    primaryContainer = BabyOrangeLight,
    onPrimaryContainer = BabyOrangeDark,
    secondary = BabyRed,
    onSecondary = Color.White,
    secondaryContainer = BabyRedLight,
    onSecondaryContainer = BabyRed,
    background = WarmBackground,
    onBackground = DarkText,
    surface = WarmSurface,
    onSurface = DarkText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = SubtleText,
    outline = BabyOrangeLight,
    outlineVariant = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = BabyOrange,
    onPrimary = Color.Black,
    primaryContainer = BabyOrangeDark,
    onPrimaryContainer = BabyOrangeLight,
    secondary = BabyRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF93000A),
    onSecondaryContainer = BabyRedLight,
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF2D2D2D),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF3D3D3D),
    onSurfaceVariant = Color(0xFFBDAFA4),
    outline = Color(0xFF5C5C5C),
    outlineVariant = Color(0xFF3D3D3D)
)

@Composable
fun BabyReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
