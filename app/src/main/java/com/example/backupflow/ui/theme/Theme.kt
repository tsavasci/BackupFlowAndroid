package com.example.backupflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BackupBluePrimary,
    onPrimary = Color.White,
    secondary = BackupBlueDark,
    background = DarkBackground,
    surface = DarkSurfaceCard,
    error = BackupErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = BackupBluePrimary,
    onPrimary = Color.White,
    secondary = BackupBlueDark,
    background = LightBackground,
    surface = LightSurfaceCard,
    error = BackupErrorRed
)

@Composable
fun BackupFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
