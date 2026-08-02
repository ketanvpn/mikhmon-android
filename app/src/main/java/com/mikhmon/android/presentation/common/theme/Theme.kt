package com.mikhmon.android.presentation.common.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Mikhmon Color Palette
private val MikhmonBlue = Color(0xFF2196F3)
private val MikhmonBlueDark = Color(0xFF1976D2)
private val MikhmonGreen = Color(0xFF4CAF50)
private val MikhmonOrange = Color(0xFFFF9800)
private val MikhmonRed = Color(0xFFF44336)
private val MikhmonPurple = Color(0xFF9C27B0)

// Light Theme Colors
private val LightPrimary = MikhmonBlue
private val LightOnPrimary = Color.White
private val LightPrimaryContainer = Color(0xFFD1E4FF)
private val LightOnPrimaryContainer = Color(0xFF001D36)
private val LightSecondary = MikhmonBlueDark
private val LightOnSecondary = Color.White
private val LightSecondaryContainer = Color(0xFFD1E4FF)
private val LightOnSecondaryContainer = Color(0xFF001D36)
private val LightTertiary = MikhmonPurple
private val LightOnTertiary = Color.White
private val LightBackground = Color(0xFFFAFAFA)
private val LightOnBackground = Color(0xFF1C1B1F)
private val LightSurface = Color.White
private val LightOnSurface = Color(0xFF1C1B1F)
private val LightError = MikhmonRed
private val LightOnError = Color.White

// Dark Theme Colors
private val DarkPrimary = MikhmonBlue
private val DarkOnPrimary = Color.White
private val DarkPrimaryContainer = Color(0xFF00497D)
private val DarkOnPrimaryContainer = Color(0xFFD1E4FF)
private val DarkSecondary = MikhmonBlueDark
private val DarkOnSecondary = Color.White
private val DarkSecondaryContainer = Color(0xFF00497D)
private val DarkOnSecondaryContainer = Color(0xFFD1E4FF)
private val DarkTertiary = MikhmonPurple
private val DarkOnTertiary = Color.White
private val DarkBackground = Color(0xFF1C1B1F)
private val DarkOnBackground = Color(0xFFE6E1E5)
private val DarkSurface = Color(0xFF2B2930)
private val DarkOnSurface = Color(0xFFE6E1E5)
private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = LightError,
    onError = LightOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError
)

@Composable
fun MikhmonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MikhmonTypography,
        content = content
    )
}
