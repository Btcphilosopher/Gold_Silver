package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AurumGold,
    secondary = SlateGraphite,
    tertiary = SterlingSilver,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = ObsidianCharcoal,
    onSecondary = PlatinumWhite,
    onTertiary = ObsidianCharcoal,
    onBackground = PlatinumWhite,
    onSurface = PlatinumWhite,
    surfaceVariant = SteelCardBorder,
    onSurfaceVariant = SterlingSilver
)

private val LightColorScheme = lightColorScheme(
    primary = AurumDarkGold,
    secondary = LightSurface,
    tertiary = SlateGraphite,
    background = PlatinumWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = ObsidianCharcoal,
    onTertiary = Color.White,
    onBackground = ObsidianCharcoal,
    onSurface = ObsidianCharcoal,
    surfaceVariant = LightSurface,
    onSurfaceVariant = SlateGraphite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode by default for that stellar private-bank feeling!
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our brand wealth-management design
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
